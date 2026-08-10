package sn.isi.iage.microbank.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import sn.isi.iage.microbank.entity.ClientDocument;
import sn.isi.iage.microbank.exception.BusinessRuleException;
import sn.isi.iage.microbank.exception.ResourceNotFoundException;
import sn.isi.iage.microbank.service.ClientDocumentService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Depot et consultation de la piece d'identite d'un client (bonus 1).
 * GET rend le fichier stocke, POST enregistre celui envoye par le formulaire.
 */
@WebServlet(name = "clientDocumentServlet", urlPatterns = "/clients/document")
@MultipartConfig(
        fileSizeThreshold = 512 * 1024,
        maxFileSize = ClientDocumentServlet.TAILLE_MAXIMALE_FICHIER,
        maxRequestSize = ClientDocumentServlet.TAILLE_MAXIMALE_REQUETE)
public class ClientDocumentServlet extends BaseServlet {

    static final long TAILLE_MAXIMALE_FICHIER = 2L * 1024 * 1024;
    static final long TAILLE_MAXIMALE_REQUETE = 3L * 1024 * 1024;

    private final transient ClientDocumentService clientDocumentService =
            new ClientDocumentService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Long clientId = parametreIdentifiant(request, "clientId");
        Optional<ClientDocument> document = clientId == null
                ? Optional.empty()
                : clientDocumentService.consulterParClient(clientId);

        if (document.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "Aucune piece d'identite enregistree pour ce client.");
            return;
        }

        ClientDocument piece = document.get();
        response.setContentType(piece.getTypeContenu());
        response.setContentLengthLong(piece.getTailleOctets());
        response.setHeader("Content-Disposition",
                "inline; filename=\"" + piece.getNomFichier() + "\"");

        try (OutputStream fluxDeSortie = response.getOutputStream()) {
            fluxDeSortie.write(piece.getContenu());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Long clientId = parametreIdentifiant(request, "clientId");
        try {
            Part fichier = request.getPart("document");
            if (fichier == null || fichier.getSize() == 0) {
                throw new BusinessRuleException("Aucun fichier n'a ete selectionne.");
            }

            byte[] contenu;
            try (InputStream fluxDEntree = fichier.getInputStream()) {
                contenu = fluxDEntree.readAllBytes();
            }

            clientDocumentService.enregistrer(clientId, nomDeFichierSecurise(fichier),
                    fichier.getContentType(), contenu);
            definirMessageSucces(request, "Piece d'identite enregistree.");

        } catch (BusinessRuleException | ResourceNotFoundException envoiRefuse) {
            definirMessageErreur(request, envoiRefuse.getMessage());
        } catch (IllegalStateException fichierTropVolumineux) {
            definirMessageErreur(request, "Le fichier depasse la taille maximale de 2 Mo.");
        }
        rediriger(request, response, "/clients/details?id=" + clientId);
    }

    /**
     * Ne conserve que le nom du fichier, sans son chemin : un navigateur peut envoyer
     * "../../etc/passwd" comme nom de fichier.
     */
    private String nomDeFichierSecurise(Part fichier) {
        String nomSoumis = fichier.getSubmittedFileName();
        if (nomSoumis == null || nomSoumis.isBlank()) {
            return "piece-identite";
        }
        return Paths.get(nomSoumis.replace('\\', '/')).getFileName().toString();
    }
}
