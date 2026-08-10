package sn.isi.iage.microbank.util;

import sn.isi.iage.microbank.exception.ValidationException;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Accumulateur d'erreurs de formulaire.
 * On collecte toutes les erreurs avant de lever l'exception : l'agent voit d'un coup
 * tous les champs a corriger au lieu de les decouvrir un par un.
 */
public class FormValidator {

    private static final Pattern MOTIF_EMAIL =
            Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[\\w.-]{2,}$");
    private static final Pattern MOTIF_TELEPHONE =
            Pattern.compile("^[0-9 +().-]{6,30}$");

    private final Map<String, String> erreursParChamp = new LinkedHashMap<>();

    public FormValidator exigerObligatoire(String champ, String valeur, String message) {
        if (valeur == null || valeur.isBlank()) {
            ajouter(champ, message);
        }
        return this;
    }

    public FormValidator exigerLongueurMaximale(String champ, String valeur,
                                                int longueurMaximale, String message) {
        if (valeur != null && valeur.length() > longueurMaximale) {
            ajouter(champ, message);
        }
        return this;
    }

    /** L'email est facultatif ; s'il est renseigne, il doit etre valide. */
    public FormValidator exigerEmailValideSiRenseigne(String champ, String valeur, String message) {
        if (valeur != null && !valeur.isBlank() && !MOTIF_EMAIL.matcher(valeur.trim()).matches()) {
            ajouter(champ, message);
        }
        return this;
    }

    public FormValidator exigerTelephoneValide(String champ, String valeur, String message) {
        if (valeur != null && !valeur.isBlank()
                && !MOTIF_TELEPHONE.matcher(valeur.trim()).matches()) {
            ajouter(champ, message);
        }
        return this;
    }

    public FormValidator exigerMontantStrictementPositif(String champ, BigDecimal valeur,
                                                         String message) {
        if (valeur == null || valeur.compareTo(BigDecimal.ZERO) <= 0) {
            ajouter(champ, message);
        }
        return this;
    }

    public FormValidator exigerMontantPositifOuNul(String champ, BigDecimal valeur,
                                                   String message) {
        if (valeur == null || valeur.compareTo(BigDecimal.ZERO) < 0) {
            ajouter(champ, message);
        }
        return this;
    }

    public FormValidator exiger(String champ, boolean conditionAttendue, String message) {
        if (!conditionAttendue) {
            ajouter(champ, message);
        }
        return this;
    }

    public boolean aDesErreurs() {
        return !erreursParChamp.isEmpty();
    }

    public Map<String, String> getErreursParChamp() {
        return Map.copyOf(erreursParChamp);
    }

    /** Leve une {@link ValidationException} si au moins une erreur a ete collectee. */
    public void lancerSiErreurs() {
        if (aDesErreurs()) {
            throw new ValidationException(erreursParChamp);
        }
    }

    private void ajouter(String champ, String message) {
        erreursParChamp.putIfAbsent(champ, message);
    }
}
