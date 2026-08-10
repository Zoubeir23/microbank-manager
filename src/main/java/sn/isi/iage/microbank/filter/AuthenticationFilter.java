package sn.isi.iage.microbank.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import sn.isi.iage.microbank.util.CsrfTokenManager;
import sn.isi.iage.microbank.util.SessionAttributes;

import java.io.IOException;
import java.util.Set;

/**
 * Filtre de securite applique a toutes les requetes (§6).
 * <p>
 * Une page protegee n'est servie que si la session contient un utilisateur ; sinon
 * la requete est redirigee vers /login. Le filtre verifie egalement le jeton anti-CSRF
 * de toutes les soumissions POST.
 */
@WebFilter(filterName = "authenticationFilter", urlPatterns = "/*")
public class AuthenticationFilter implements Filter {

    /** Ressources accessibles sans etre connecte. */
    private static final Set<String> CHEMINS_PUBLICS = Set.of("/login", "/logout");
    private static final String PREFIXE_ASSETS = "/assets/";

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String chemin = request.getRequestURI().substring(request.getContextPath().length());

        if (estPublic(chemin)) {
            chain.doFilter(request, response);
            return;
        }

        if (!estConnecte(request)) {
            // Les pages protegees ne doivent jamais etre servies sans session.
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        if ("POST".equalsIgnoreCase(request.getMethod())
                && !CsrfTokenManager.jetonValide(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Jeton de securite invalide ou expire. Rechargez la page et reessayez.");
            return;
        }

        // Empeche le navigateur de servir une page protegee depuis son cache
        // apres une deconnexion.
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.setHeader("Pragma", "no-cache");

        chain.doFilter(request, response);
    }

    private boolean estPublic(String chemin) {
        return chemin.startsWith(PREFIXE_ASSETS)
                || CHEMINS_PUBLICS.contains(chemin)
                || "/favicon.ico".equals(chemin);
    }

    private boolean estConnecte(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null
                && session.getAttribute(SessionAttributes.UTILISATEUR_CONNECTE) != null;
    }
}
