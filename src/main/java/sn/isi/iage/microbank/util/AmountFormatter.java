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

    /**
     * Separateur de milliers : espace insecable, pour qu'un montant ne soit jamais
     * coupe en deux en fin de ligne dans un tableau ou dans le releve PDF.
     */
    public static final char ESPACE_INSECABLE = ' ';

    private static final DateTimeFormatter FORMAT_DATE =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMAT_DATE_HEURE =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private AmountFormatter() {
    }

    /** 125000.50 devient "125 000,50" : espace insecable comme separateur de milliers. */
    public static String formaterMontant(BigDecimal montant) {
        return construireFormat("#,##0.00").format(montant == null ? BigDecimal.ZERO : montant);
    }

    /**
     * Format francais : espace insecable pour les milliers, virgule pour les decimales.
     * Le formatage est fait ici, en Java, plutot qu'avec {@code fmt:formatNumber} :
     * l'implementation JSTL utilisee ignore l'attribut {@code pattern}, ce qui produisait
     * un affichage different de celui attendu.
     */
    private static DecimalFormat construireFormat(String motif) {
        DecimalFormatSymbols symboles = new DecimalFormatSymbols(Locale.FRANCE);
        symboles.setGroupingSeparator(ESPACE_INSECABLE);
        symboles.setDecimalSeparator(',');
        return new DecimalFormat(motif, symboles);
    }

    public static String formaterMontantAvecDevise(BigDecimal montant) {
        return formaterMontant(montant) + " " + DEVISE;
    }

    /** Montant sans decimale, pour les compteurs du tableau de bord : "125 000". */
    public static String formaterMontantArrondi(BigDecimal montant) {
        return construireFormat("#,##0").format(montant == null ? BigDecimal.ZERO : montant);
    }

    /** Nombre entier avec separateur de milliers : 1250 devient "1 250". */
    public static String formaterNombre(long valeur) {
        return construireFormat("#,##0").format(valeur);
    }

    /** Numero d'affichage d'un client, au format C001 (§8 du cahier des charges). */
    public static String formaterNumeroClient(Long identifiant) {
        return identifiant == null ? "" : "C" + new DecimalFormat("000").format(identifiant);
    }

    public static String formaterDate(LocalDate date) {
        return date == null ? "" : FORMAT_DATE.format(date);
    }

    public static String formaterDateHeure(LocalDateTime dateHeure) {
        return dateHeure == null ? "" : FORMAT_DATE_HEURE.format(dateHeure);
    }
}
