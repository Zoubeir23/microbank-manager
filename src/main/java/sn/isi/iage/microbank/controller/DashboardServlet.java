package sn.isi.iage.microbank.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sn.isi.iage.microbank.service.DashboardService;

import java.io.IOException;

/** Tableau de bord affiche apres la connexion (§22). */
@WebServlet(name = "dashboardServlet", urlPatterns = {"", "/dashboard"})
public class DashboardServlet extends BaseServlet {

    private final transient DashboardService dashboardService = new DashboardService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("statistiques", dashboardService.calculerStatistiques());
        afficher(request, response, "dashboard");
    }
}
