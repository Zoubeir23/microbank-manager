package sn.isi.iage.microbank.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Protection anti-CSRF.
 * Un jeton aleatoire est cree avec la session, insere dans chaque formulaire et
 * verifie a la reception : une page hostile ne peut pas deviner ce jeton, donc
 * elle ne peut pas declencher un virement au nom de l'agent connecte.
 */
public final class CsrfTokenManager {

    public static final String NOM_DU_CHAMP = "csrfToken";

    private static final SecureRandom GENERATEUR_ALEATOIRE = new SecureRandom();
    private static final int TAILLE_JETON_OCTETS = 32;

    private CsrfTokenManager() {
    }

    /** Rend le jeton de la session, en le creant a la premiere demande. */
    public static String obtenirOuCreerJeton(HttpSession session) {
        String jeton = (String) session.getAttribute(SessionAttributes.JETON_CSRF);
        if (jeton == null) {
            byte[] octetsAleatoires = new byte[TAILLE_JETON_OCTETS];
            GENERATEUR_ALEATOIRE.nextBytes(octetsAleatoires);
            jeton = Base64.getUrlEncoder().withoutPadding().encodeToString(octetsAleatoires);
            session.setAttribute(SessionAttributes.JETON_CSRF, jeton);
        }
        return jeton;
    }

    /** Verifie le jeton porte par une requete POST. */
    public static boolean jetonValide(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }
        String jetonAttendu = (String) session.getAttribute(SessionAttributes.JETON_CSRF);
        String jetonRecu = request.getParameter(NOM_DU_CHAMP);
        if (jetonAttendu == null || jetonRecu == null) {
            return false;
        }
        return MessageDigest.isEqual(
                jetonAttendu.getBytes(StandardCharsets.UTF_8),
                jetonRecu.getBytes(StandardCharsets.UTF_8));
    }
}
