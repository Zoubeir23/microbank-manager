package sn.isi.iage.microbank.util;

/** Noms des attributs deposes en session ou en requete, regroupes pour eviter les fautes de frappe. */
public final class SessionAttributes {

    /** Utilisateur connecte, depose par LoginServlet : session.setAttribute("user", user). */
    public static final String UTILISATEUR_CONNECTE = "user";

    /** Jeton anti-CSRF associe a la session. */
    public static final String JETON_CSRF = "csrfToken";

    /** Message de succes affiche une seule fois apres une redirection. */
    public static final String MESSAGE_SUCCES = "messageSucces";

    /** Message d'erreur affiche une seule fois apres une redirection. */
    public static final String MESSAGE_ERREUR = "messageErreur";

    /** Erreurs de validation champ par champ, transmises a la JSP du formulaire. */
    public static final String ERREURS_DE_VALIDATION = "erreurs";

    private SessionAttributes() {
    }
}
