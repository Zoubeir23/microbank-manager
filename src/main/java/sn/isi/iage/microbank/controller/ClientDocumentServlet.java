package sn.isi.iage.microbank.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sn.isi.iage.microbank.model.ClientDocument;
import sn.isi.iage.microbank.service.ClientDocumentService;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;

/**
 * Consultation de la piece d'identite d'un client (bonus 1).
 * Le depot du fichier se fait desormais depuis le formulaire client lui-meme
 * (creation ou modification) ; cette servlet ne fait que restituer le fichier stocke.
 */
@WebServlet(name = "clientDocumentServlet", urlPatterns = "/clients/document")
public class ClientDocumentServlet extends BaseServlet {

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
}
