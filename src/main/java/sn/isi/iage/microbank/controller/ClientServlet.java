package sn.isi.iage.microbank.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sn.isi.iage.microbank.dto.ClientForm;
import sn.isi.iage.microbank.model.Client;
import sn.isi.iage.microbank.exception.BusinessRuleException;
import sn.isi.iage.microbank.exception.ResourceNotFoundException;
import sn.isi.iage.microbank.exception.ValidationException;
import sn.isi.iage.microbank.service.AccountService;
import sn.isi.iage.microbank.service.ClientService;
import sn.isi.iage.microbank.util.SessionAttributes;

import java.io.IOException;

/**
 * Gestion des clients (§7 a §10).
 * <p>
 * Une seule servlet dessert toutes les URL de la ressource client ; l'action est
 * lue dans le chemin : /clients, /clients/create, /clients/update, /clients/delete,
 * /clients/details.
 */
@WebServlet(name = "clientServlet", urlPatterns = {"/clients", "/clients/*"})
public class ClientServlet extends BaseServlet {

    private final transient ClientService clientService = new ClientService();
    private final transient AccountService accountService = new AccountService();

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
            Client client = clientService.enregistrer(formulaire);
            definirMessageSucces(request, formulaire.estCreation()
                    ? "Client cree avec succes."
                    : "Client mis a jour avec succes.");
            rediriger(request, response, "/clients/details?id=" + client.getId());
        } catch (ValidationException formulaireInvalide) {
            request.setAttribute(SessionAttributes.ERREURS_DE_VALIDATION,
                    formulaireInvalide.getErreursParChamp());
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
}
