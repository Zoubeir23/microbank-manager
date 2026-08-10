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
import sn.isi.iage.microbank.entity.User;
import sn.isi.iage.microbank.util.SessionAttributes;

import java.io.IOException;

/**
 * Reserve la gestion des utilisateurs aux administrateurs (§3).
 * S'applique apres {@link AuthenticationFilter} : a ce stade, la session existe deja.
 */
@WebFilter(filterName = "adminAuthorizationFilter", urlPatterns = "/users/*")
public class AdminAuthorizationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        HttpSession session = request.getSession(false);
        User utilisateur = session == null ? null
                : (User) session.getAttribute(SessionAttributes.UTILISATEUR_CONNECTE);

        // L'ordre d'execution des filtres annotes n'etant pas garanti, ce filtre ne
        // suppose pas que AuthenticationFilter est deja passe.
        if (utilisateur == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        if (!utilisateur.estAdministrateur()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Cette page est reservee aux administrateurs.");
            return;
        }
        chain.doFilter(request, response);
    }
}
