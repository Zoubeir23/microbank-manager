package sn.isi.iage.microbank.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sn.isi.iage.microbank.dto.OperationSearchCriteria;
import sn.isi.iage.microbank.entity.Account;
import sn.isi.iage.microbank.entity.Operation;
import sn.isi.iage.microbank.exception.ResourceNotFoundException;
import sn.isi.iage.microbank.service.AccountService;
import sn.isi.iage.microbank.service.OperationService;
import sn.isi.iage.microbank.util.StatementPdfWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * Telechargement du releve de compte au format PDF (§20).
 * Les filtres affiches a l'ecran (periode, type, montants) sont repris tels quels.
 */
@WebServlet(name = "statementPdfServlet", urlPatterns = "/operations/statement.pdf")
public class StatementPdfServlet extends BaseServlet {

    private final transient AccountService accountService = new AccountService();
    private final transient OperationService operationService = new OperationService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        OperationSearchCriteria criteres = OperationCriteriaReader.lire(request);
        if (criteres.compteId() == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Le releve PDF exige un compte : parametre accountId manquant.");
            return;
        }

        try {
            Account compte = accountService.consulter(criteres.compteId());
            List<Operation> operations = operationService.listerPourExport(criteres);

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"releve-" + compte.getNumeroCompte() + ".pdf\"");

            try (OutputStream fluxDeSortie = response.getOutputStream()) {
                StatementPdfWriter.ecrire(fluxDeSortie, compte, operations,
                        criteres.dateDebut(), criteres.dateFin(),
                        operationService.totalDesDepots(criteres),
                        operationService.totalDesRetraits(criteres));
            }
        } catch (ResourceNotFoundException compteIntrouvable) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, compteIntrouvable.getMessage());
        }
    }
}
