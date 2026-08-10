package sn.isi.iage.microbank.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Verifie la mise en forme des montants, des nombres et des dates.
 * Ces fonctions remplacent {@code fmt:formatNumber} dans les JSP : l'implementation
 * JSTL utilisee ignore l'attribut {@code pattern}, ce qui produisait un affichage
 * different de celui attendu (par exemple "C1" au lieu de "C001").
 */
class AmountFormatterTest {

    /** Le separateur de milliers est une espace insecable, pas une espace ordinaire. */
    private static final String ESPACE = "\u00A0";

    @Test
    @DisplayName("un montant est affiche avec deux decimales et un separateur de milliers")
    void formatageDUnMontant() {
        assertEquals("125" + ESPACE + "000,00",
                AmountFormatter.formaterMontant(new BigDecimal("125000")));
        assertEquals("1" + ESPACE + "250,50",
                AmountFormatter.formaterMontant(new BigDecimal("1250.5")));
        assertEquals("0,00", AmountFormatter.formaterMontant(BigDecimal.ZERO));
        assertEquals("0,00", AmountFormatter.formaterMontant(null));
    }

    @Test
    @DisplayName("un montant arrondi est affiche sans decimale")
    void formatageDUnMontantArrondi() {
        assertEquals("125" + ESPACE + "000",
                AmountFormatter.formaterMontantArrondi(new BigDecimal("125000.00")));
        assertEquals("0", AmountFormatter.formaterMontantArrondi(null));
    }

    @Test
    @DisplayName("un montant avec devise porte le suffixe FCFA")
    void formatageAvecDevise() {
        assertEquals("100" + ESPACE + "000,00 FCFA",
                AmountFormatter.formaterMontantAvecDevise(new BigDecimal("100000")));
    }

    @Test
    @DisplayName("un nombre entier est groupe par milliers")
    void formatageDUnNombre() {
        assertEquals("1" + ESPACE + "250", AmountFormatter.formaterNombre(1250));
        assertEquals("0", AmountFormatter.formaterNombre(0));
        assertEquals("52", AmountFormatter.formaterNombre(52));
    }

    @Test
    @DisplayName("le numero d'affichage du client est complete par des zeros")
    void formatageDuNumeroClient() {
        assertEquals("C001", AmountFormatter.formaterNumeroClient(1L));
        assertEquals("C012", AmountFormatter.formaterNumeroClient(12L));
        assertEquals("C1250", AmountFormatter.formaterNumeroClient(1250L));
        assertEquals("", AmountFormatter.formaterNumeroClient(null));
    }

    @Test
    @DisplayName("les dates suivent le format francais")
    void formatageDesDates() {
        assertEquals("25/08/2026", AmountFormatter.formaterDate(LocalDate.of(2026, 8, 25)));
        assertEquals("", AmountFormatter.formaterDate(null));
        assertEquals("10/08/2026 09:30",
                AmountFormatter.formaterDateHeure(LocalDateTime.of(2026, 8, 10, 9, 30)));
        assertEquals("", AmountFormatter.formaterDateHeure(null));
    }
}
