package sn.isi.iage.microbank.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import sn.isi.iage.microbank.dto.ClientForm;
import sn.isi.iage.microbank.model.Client;
import sn.isi.iage.microbank.exception.BusinessRuleException;
import sn.isi.iage.microbank.exception.ResourceNotFoundException;
import sn.isi.iage.microbank.exception.ValidationException;
import sn.isi.iage.microbank.service.AccountService;
import sn.isi.iage.microbank.service.ClientDocumentService;
import sn.isi.iage.microbank.service.ClientService;
import sn.isi.iage.microbank.util.SessionAttributes;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Gestion des clients (§7 a §10) et de leur piece d'identite jointe (bonus 1).
 * <p>
 * Une seule servlet dessert toutes les URL de la ressource client ; l'action est
 * lue dans le chemin : /clients, /clients/create, /clients/update, /clients/delete,
 * /clients/details. Le formulaire de creation et de modification est multipart :
 * il porte a la fois les champs texte du client et, en option, le fichier de sa
 * piece d'identite.
 */
@WebServlet(name = "clientServlet", urlPatterns = {"/clients", "/clients/*"})
@MultipartConfig(
        fileSizeThreshold = 512 * 1024,
        maxFileSize = ClientServlet.TAILLE_MAXIMALE_FICHIER,
        maxRequestSize = ClientServlet.TAILLE_MAXIMALE_REQUETE)
public class ClientServlet extends BaseServlet {

    static final long TAILLE_MAXIMALE_FICHIER = 2L * 1024 * 1024;
    static final long TAILLE_MAXIMALE_REQUETE = 3L * 1024 * 1024;

    private final transient ClientService clientService = new ClientService();
    private final transient AccountService accountService = new AccountService();
    private final transient ClientDocumentService clientDocumentService =
            new ClientDocumentService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            switch (action(request)) {
                case "/nouveau" -> afficherFormulaireDeCreation(request, response);
                case "/edit" -> afficherFormulaireDeModification(request, response);
                case "/details" -> afficherDetails(request, response);
                case "/delete" -> supprimer(request, response);
                default -> afficherListe(request, response);
            }
        } catch (ResourceNotFoundException clientIntrouvable) {
            definirMessageErreur(request, clientIntrouvable.getMessage());
            rediriger(request, response, "/clients");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ClientForm formulaire = lireFormulaire(request);

        try {
            Part fichier = lireFichierEnvoye(request);
            byte[] contenuDuFichier = null;

            if (fichier != null) {
                try (InputStream fluxDEntree = fichier.getInputStream()) {
                    contenuDuFichier = fluxDEntree.readAllBytes();
                }
                // La piece jointe est verifiee avant meme d'enregistrer le client :
                // un fichier invalide ne doit pas laisser un client a moitie enregistre.
                clientDocumentService.validerFichier(
                        nomDeFichierSecurise(fichier), fichier.getContentType(), contenuDuFichier);
            }

            Client client = clientService.enregistrer(formulaire);

            if (fichier != null) {
                clientDocumentService.enregistrer(client.getId(),
                        nomDeFichierSecurise(fichier), fichier.getContentType(), contenuDuFichier);
            }

            definirMessageSucces(request, formulaire.estCreation()
                    ? "Client cree avec succes."
                    : "Client mis a jour avec succes.");
            rediriger(request, response, "/clients/details?id=" + client.getId());

        } catch (ValidationException formulaireInvalide) {
            request.setAttribute(SessionAttributes.ERREURS_DE_VALIDATION,
                    formulaireInvalide.getErreursParChamp());
            request.setAttribute("formulaire", formulaire);
            afficher(request, response, "clients/form");
        } catch (BusinessRuleException documentInvalide) {
            request.setAttribute(SessionAttributes.ERREURS_DE_VALIDATION,
                    Map.of("document", documentInvalide.getMessage()));
            request.setAttribute("formulaire", formulaire);
            afficher(request, response, "clients/form");
        }
    }

    private void afficherListe(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String recherche = parametre(request, "search");
        int numeroPage = numeroDePageDemande(request);
        int taillePage = tailleDePageDemandee(request);

        request.setAttribute("page", clientService.rechercher(recherche, numeroPage, taillePage));
        request.setAttribute("recherche", recherche);
        afficher(request, response, "clients/list");
    }

    private void afficherFormulaireDeCreation(HttpServletRequest request,
                                              HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("formulaire",
                new ClientForm(null, null, null, null, null, null, null, null, "ACTIF"));
        afficher(request, response, "clients/form");
    }

    private void afficherFormulaireDeModification(HttpServletRequest request,
                                                  HttpServletResponse response)
            throws ServletException, IOException {
        Client client = clientService.consulter(parametreIdentifiant(request, "id"));
        request.setAttribute("formulaire", versFormulaire(client));
        clientDocumentService.consulterParClient(client.getId())
                .ifPresent(document -> request.setAttribute("documentActuel", document));
        afficher(request, response, "clients/form");
    }

    private void afficherDetails(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Long identifiant = parametreIdentifiant(request, "id");
        request.setAttribute("client", clientService.consulter(identifiant));
        request.setAttribute("comptes", accountService.listerParClient(identifiant));
        afficher(request, response, "clients/details");
    }

    private void supprimer(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            clientService.supprimer(parametreIdentifiant(request, "id"));
            definirMessageSucces(request, "Client supprime.");
        } catch (BusinessRuleException suppressionRefusee) {
            definirMessageErreur(request, suppressionRefusee.getMessage());
        }
        rediriger(request, response, "/clients");
    }

    private ClientForm lireFormulaire(HttpServletRequest request) {
        return new ClientForm(
                parametre(request, "id"),
                parametre(request, "nom"),
                parametre(request, "prenom"),
                parametre(request, "dateNaissance"),
                parametre(request, "telephone"),
                parametre(request, "email"),
                parametre(request, "adresse"),
                parametre(request, "numeroPiece"),
                parametre(request, "statut"));
    }

    private ClientForm versFormulaire(Client client) {
        return new ClientForm(
                String.valueOf(client.getId()),
                client.getNom(),
                client.getPrenom(),
                client.getDateNaissance() == null ? null : client.getDateNaissance().toString(),
                client.getTelephone(),
                client.getEmail(),
                client.getAdresse(),
                client.getNumeroPiece(),
                client.getStatut().name());
    }

    /**
     * La piece jointe est facultative : un champ vide, ou meme un formulaire soumis
     * sans encodage multipart, ne doivent pas etre traites comme une erreur.
     * {@code getPart} leve une exception si la requete n'est pas multipart ; dans ce
     * cas, il n'y a simplement pas de fichier a lire.
     */
    private Part lireFichierEnvoye(HttpServletRequest request) {
        try {
            Part fichier = request.getPart("document");
            return fichier == null || fichier.getSize() == 0 ? null : fichier;
        } catch (ServletException | IOException requeteNonMultipart) {
            return null;
        }
    }
}
