package sn.isi.iage.microbank.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordHasherTest {

    @Test
    @DisplayName("un mot de passe correct est reconnu")
    void motDePasseCorrectEstReconnu() {
        String empreinte = PasswordHasher.hacher("agent123");

        assertTrue(PasswordHasher.correspond("agent123", empreinte));
    }

    @Test
    @DisplayName("un mot de passe errone est rejete")
    void motDePasseErroneEstRejete() {
        String empreinte = PasswordHasher.hacher("agent123");

        assertFalse(PasswordHasher.correspond("agent124", empreinte));
        assertFalse(PasswordHasher.correspond("", empreinte));
        assertFalse(PasswordHasher.correspond(null, empreinte));
    }

    @Test
    @DisplayName("le meme mot de passe produit deux empreintes differentes grace au sel")
    void selRendLesEmpreintesUniques() {
        String premiere = PasswordHasher.hacher("motdepasse");
        String seconde = PasswordHasher.hacher("motdepasse");

        assertNotEquals(premiere, seconde);
        assertTrue(PasswordHasher.correspond("motdepasse", premiere));
        assertTrue(PasswordHasher.correspond("motdepasse", seconde));
    }

    @Test
    @DisplayName("l'empreinte contient l'algorithme, les iterations, le sel et le hash")
    void formatDeLEmpreinte() {
        String[] parties = PasswordHasher.hacher("admin123").split("\\$");

        assertEquals(4, parties.length);
        assertEquals("pbkdf2_sha256", parties[0]);
        assertEquals(120_000, Integer.parseInt(parties[1]));
    }

    @Test
    @DisplayName("une empreinte corrompue ne laisse jamais passer")
    void empreinteCorrompueEstRejetee() {
        assertFalse(PasswordHasher.correspond("agent123", "n-importe-quoi"));
        assertFalse(PasswordHasher.correspond("agent123", "pbkdf2_sha256$abc$def"));
        assertFalse(PasswordHasher.correspond("agent123", null));
    }

    @Test
    @DisplayName("hacher un mot de passe vide est refuse")
    void motDePasseVideRefuse() {
        assertThrows(IllegalArgumentException.class, () -> PasswordHasher.hacher(""));
        assertThrows(IllegalArgumentException.class, () -> PasswordHasher.hacher(null));
    }
}
