package sn.isi.iage.microbank.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Optional;

/**
 * Conversion defensive des valeurs recues du navigateur.
 * Toute donnee venant d'un formulaire est une chaine potentiellement absente,
 * vide ou mal formee : ces methodes ne levent jamais d'exception, elles rendent
 * un {@link Optional} vide.
 */
public final class ValueParser {

    private ValueParser() {
    }

    public static String nettoyer(String valeur) {
        return valeur == null || valeur.isBlank() ? null : valeur.trim();
    }

    public static Optional<Long> versIdentifiant(String valeur) {
        String valeurNettoyee = nettoyer(valeur);
        if (valeurNettoyee == null) {
            return Optional.empty();
        }
        try {
            long identifiant = Long.parseLong(valeurNettoyee);
            return identifiant > 0 ? Optional.of(identifiant) : Optional.empty();
        } catch (NumberFormatException erreur) {
            return Optional.empty();
        }
    }

    public static Optional<Integer> versEntier(String valeur) {
        String valeurNettoyee = nettoyer(valeur);
        if (valeurNettoyee == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(Integer.parseInt(valeurNettoyee));
        } catch (NumberFormatException erreur) {
            return Optional.empty();
        }
    }

    /** Accepte "100000", "100 000" et "100000,50". */
    public static Optional<BigDecimal> versMontant(String valeur) {
        String valeurNettoyee = nettoyer(valeur);
        if (valeurNettoyee == null) {
            return Optional.empty();
        }
        String valeurNormalisee = valeurNettoyee
                .replace(" ", "")
                .replace(" ", "")
                .replace(",", ".");
        try {
            return Optional.of(new BigDecimal(valeurNormalisee));
        } catch (NumberFormatException erreur) {
            return Optional.empty();
        }
    }

    /** Attend le format ISO produit par un champ HTML {@code <input type="date">}. */
    public static Optional<LocalDate> versDate(String valeur) {
        String valeurNettoyee = nettoyer(valeur);
        if (valeurNettoyee == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(LocalDate.parse(valeurNettoyee));
        } catch (DateTimeParseException erreur) {
            return Optional.empty();
        }
    }

    public static <E extends Enum<E>> Optional<E> versEnumeration(Class<E> type, String valeur) {
        String valeurNettoyee = nettoyer(valeur);
        if (valeurNettoyee == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(Enum.valueOf(type, valeurNettoyee.toUpperCase()));
        } catch (IllegalArgumentException erreur) {
            return Optional.empty();
        }
    }
}
