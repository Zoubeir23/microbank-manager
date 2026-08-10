package sn.isi.iage.microbank.exception;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Formulaire invalide. Porte le detail champ par champ pour que la JSP puisse
 * afficher chaque message a cote du champ fautif.
 */
public class ValidationException extends RuntimeException {

    private final Map<String, String> erreursParChamp;

    public ValidationException(Map<String, String> erreursParChamp) {
        super("Formulaire invalide : " + erreursParChamp.size() + " champ(s) en erreur");
        this.erreursParChamp = Collections.unmodifiableMap(new LinkedHashMap<>(erreursParChamp));
    }

    public Map<String, String> getErreursParChamp() {
        return erreursParChamp;
    }
}
