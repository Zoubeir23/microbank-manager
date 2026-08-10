package sn.isi.iage.microbank.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import sn.isi.iage.microbank.model.User;
import sn.isi.iage.microbank.exception.BusinessRuleException;
import sn.isi.iage.microbank.service.AuthenticationService;
import sn.isi.iage.microbank.util.SessionAttributes;

import java.io.IOException;
import java.util.Optional;

/**
 * Authentification par formulaire (§5).
 * GET affiche le formulaire, POST verifie les identifiants et ouvre la session.
 */
@WebServlet(name = "loginServlet", urlPatterns = "/login")
public class LoginServlet extends BaseServlet {

    private final transient AuthenticationService authenticationService =
            new AuthenticationService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (utilisateurConnecte(request) != null) {
            rediriger(request, response, "/dashboard");
            return;
        }
        afficher(request, response, "login");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String login = parametre(request, "login");
        String motDePasse = request.getParameter("motDePasse");

        try {
            Optional<User> utilisateur = authenticationService.authentifier(login, motDePasse);

            if (utilisateur.isEmpty()) {
                afficherEchec(request, response, login, "Login ou mot de passe incorrect.");
                return;
            }

            // Nouvelle session apres authentification : empeche la fixation de session.
            HttpSession ancienneSession = request.getSession(false);
            if (ancienneSession != null) {
                ancienneSession.invalidate();
            }
            HttpSession session = request.getSession(true);
            session.setAttribute(SessionAttributes.UTILISATEUR_CONNECTE, utilisateur.get());
            session.setMaxInactiveInterval(30 * 60);

            rediriger(request, response, "/dashboard");
        } catch (BusinessRuleException compteDesactive) {
            afficherEchec(request, response, login, compteDesactive.getMessage());
        }
    }

    private void afficherEchec(HttpServletRequest request, HttpServletResponse response,
                               String login, String message)
            throws ServletException, IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        request.setAttribute("erreurAuthentification", message);
        request.setAttribute("login", login);
        afficher(request, response, "login");
    }
}
