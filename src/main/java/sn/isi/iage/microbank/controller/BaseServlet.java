package sn.isi.iage.microbank.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import sn.isi.iage.microbank.dto.PageResult;
import sn.isi.iage.microbank.model.User;
import sn.isi.iage.microbank.util.CsrfTokenManager;
import sn.isi.iage.microbank.util.SessionAttributes;
import sn.isi.iage.microbank.util.ValueParser;

import java.io.IOException;

/**
 * Comportements communs a toutes les servlets de l'application :
 * acces a l'utilisateur en session, affichage d'une JSP, redirection et messages.
 * <p>
 * Les servlets restent ainsi limitees a leur role de controleur : lire la requete,
 * appeler un service, choisir la vue.
 */
public abstract class BaseServlet extends HttpServlet {

    protected static final String DOSSIER_DES_VUES = "/WEB-INF/views/";

    /**
     * Affiche une JSP. Le nom est relatif au dossier des vues, sans extension.
     * Le jeton anti-CSRF est expose a la vue afin que chaque formulaire puisse l'inclure.
     */
    protected void afficher(HttpServletRequest request, HttpServletResponse response, String vue)
            throws ServletException, IOException {
        request.setAttribute(CsrfTokenManager.NOM_DU_CHAMP,
                CsrfTokenManager.obtenirOuCreerJeton(request.getSession()));
        request.getRequestDispatcher(DOSSIER_DES_VUES + vue + ".jsp").forward(request, response);
    }

    /** Redirige vers un chemin de l'application (motif POST puis redirection). */
    protected void rediriger(HttpServletRequest request, HttpServletResponse response,
                             String chemin) throws IOException {
        response.sendRedirect(request.getContextPath() + chemin);
    }

    protected User utilisateurConnecte(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null
                : (User) session.getAttribute(SessionAttributes.UTILISATEUR_CONNECTE);
    }

    protected void definirMessageSucces(HttpServletRequest request, String message) {
        request.getSession().setAttribute(SessionAttributes.MESSAGE_SUCCES, message);
    }

    protected void definirMessageErreur(HttpServletRequest request, String message) {
        request.getSession().setAttribute(SessionAttributes.MESSAGE_ERREUR, message);
    }

    protected String parametre(HttpServletRequest request, String nom) {
        return ValueParser.nettoyer(request.getParameter(nom));
    }

    protected Long parametreIdentifiant(HttpServletRequest request, String nom) {
        return ValueParser.versIdentifiant(request.getParameter(nom)).orElse(null);
    }

    protected int numeroDePageDemande(HttpServletRequest request) {
        return PageResult.normaliserNumeroPage(
                ValueParser.versEntier(request.getParameter("page")).orElse(null));
    }

    protected int tailleDePageDemandee(HttpServletRequest request) {
        return PageResult.normaliserTaillePage(
                ValueParser.versEntier(request.getParameter("size")).orElse(null));
    }

    /** Chemin restant apres l'URL de la servlet : "/create", "/details"... */
    protected String action(HttpServletRequest request) {
        String cheminSupplementaire = request.getPathInfo();
        return cheminSupplementaire == null || cheminSupplementaire.isBlank()
                ? "/" : cheminSupplementaire;
    }
}
