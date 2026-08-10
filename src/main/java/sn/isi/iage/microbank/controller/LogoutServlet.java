package sn.isi.iage.microbank.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Deconnexion : la session est invalidee, l'utilisateur ne peut plus acceder
 * aux pages protegees meme avec le bouton retour du navigateur.
 */
@WebServlet(name = "logoutServlet", urlPatterns = "/logout")
public class LogoutServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        rediriger(request, response, "/login");
    }
}
