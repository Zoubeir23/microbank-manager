package sn.isi.iage.microbank.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sn.isi.iage.microbank.enums.TypeOperation;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Verifie que les valeurs venant d'un formulaire ne peuvent jamais faire planter le serveur. */
class ValueParserTest {

    @Test
    @DisplayName("les chaines vides sont ramenees a null")
    void nettoyage() {
        assertNull(ValueParser.nettoyer(null));
        assertNull(ValueParser.nettoyer(""));
        assertNull(ValueParser.nettoyer("   "));
        assertEquals("Dakar", ValueParser.nettoyer("  Dakar  "));
    }

    @Test
    @DisplayName("un identifiant doit etre un entier strictement positif")
    void identifiants() {
        assertEquals(12L, ValueParser.versIdentifiant("12").orElse(null));
        assertTrue(ValueParser.versIdentifiant("0").isEmpty());
        assertTrue(ValueParser.versIdentifiant("-3").isEmpty());
        assertTrue(ValueParser.versIdentifiant("abc").isEmpty());
        assertTrue(ValueParser.versIdentifiant(null).isEmpty());
        assertTrue(ValueParser.versIdentifiant("1; DROP TABLE clients").isEmpty());
    }

    @Test
    @DisplayName("un montant accepte les espaces et la virgule decimale")
    void montants() {
        assertEquals(0, new BigDecimal("100000")
                .compareTo(ValueParser.versMontant("100000").orElseThrow()));
        assertEquals(0, new BigDecimal("100000")
                .compareTo(ValueParser.versMontant("100 000").orElseThrow()));
        assertEquals(0, new BigDecimal("1000.50")
                .compareTo(ValueParser.versMontant("1000,50").orElseThrow()));
        assertTrue(ValueParser.versMontant("beaucoup").isEmpty());
        assertTrue(ValueParser.versMontant("").isEmpty());
    }

    @Test
    @DisplayName("une date doit respecter le format ISO du champ HTML")
    void dates() {
        assertEquals(LocalDate.of(2026, 8, 25),
                ValueParser.versDate("2026-08-25").orElse(null));
        assertTrue(ValueParser.versDate("25/08/2026").isEmpty());
        assertTrue(ValueParser.versDate("2026-13-45").isEmpty());
        assertTrue(ValueParser.versDate(null).isEmpty());
    }

    @Test
    @DisplayName("une enumeration inconnue ne leve pas d'exception")
    void enumerations() {
        assertEquals(TypeOperation.DEPOT,
                ValueParser.versEnumeration(TypeOperation.class, "depot").orElse(null));
        assertEquals(TypeOperation.VIREMENT,
                ValueParser.versEnumeration(TypeOperation.class, "VIREMENT").orElse(null));
        assertTrue(ValueParser.versEnumeration(TypeOperation.class, "INCONNU").isEmpty());
        assertTrue(ValueParser.versEnumeration(TypeOperation.class, null).isEmpty());
    }

    @Test
    @DisplayName("un entier invalide est ignore")
    void entiers() {
        assertEquals(10, ValueParser.versEntier("10").orElse(null));
        assertEquals(-2, ValueParser.versEntier("-2").orElse(null));
        assertTrue(ValueParser.versEntier("dix").isEmpty());
    }
}
