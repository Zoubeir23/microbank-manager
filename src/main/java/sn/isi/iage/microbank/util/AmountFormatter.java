package sn.isi.iage.microbank.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Mise en forme des montants et des dates pour les documents exportes (PDF, CSV). */
public final class AmountFormatter {

    public static final String DEVISE = "FCFA";

    private static final DateTimeFormatter FORMAT_DATE =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMAT_DATE_HEURE =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private AmountFormatter() {
    }

    /** 125000.50 devient "125 000,50" : espace insecable comme separateur de milliers. */
    public static String formaterMontant(BigDecimal montant) {
        if (montant == null) {
            return "0,00";
        }
        DecimalFormatSymbols symboles = new DecimalFormatSymbols(Locale.FRANCE);
        symboles.setGroupingSeparator(' ');
        symboles.setDecimalSeparator(',');
        return new DecimalFormat("#,##0.00", symboles).format(montant);
    }

    public static String formaterMontantAvecDevise(BigDecimal montant) {
        return formaterMontant(montant) + " " + DEVISE;
    }

    public static String formaterDate(LocalDate date) {
        return date == null ? "" : FORMAT_DATE.format(date);
    }

    public static String formaterDateHeure(LocalDateTime dateHeure) {
        return dateHeure == null ? "" : FORMAT_DATE_HEURE.format(dateHeure);
    }
}
