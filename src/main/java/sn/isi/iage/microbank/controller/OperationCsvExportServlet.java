package sn.isi.iage.microbank.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sn.isi.iage.microbank.dto.OperationSearchCriteria;
import sn.isi.iage.microbank.model.Operation;
import sn.isi.iage.microbank.service.OperationService;
import sn.isi.iage.microbank.util.OperationCsvWriter;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;

/**
 * Export CSV de l'historique (§21).
 * Les filtres appliques a l'ecran sont conserves : l'export correspond exactement
 * a ce que l'agent a sous les yeux.
 */
@WebServlet(name = "operationCsvExportServlet", urlPatterns = "/operations/export.csv")
public class OperationCsvExportServlet extends BaseServlet {

    private final transient OperationService operationService = new OperationService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        OperationSearchCriteria criteres = OperationCriteriaReader.lire(request);
        List<Operation> operations = operationService.listerPourExport(criteres);

        response.setContentType("text/csv");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"operations-" + LocalDate.now() + ".csv\"");

        try (PrintWriter redacteur = response.getWriter()) {
            OperationCsvWriter.ecrire(redacteur, operations);
        }
    }
}
