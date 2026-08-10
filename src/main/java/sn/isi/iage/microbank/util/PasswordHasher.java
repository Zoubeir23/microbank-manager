package sn.isi.iage.microbank.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * Hachage des mots de passe avec PBKDF2-HMAC-SHA256 (algorithme fourni par le JDK,
 * aucune bibliotheque externe).
 * <p>
 * Format stocke en base : {@code pbkdf2_sha256$iterations$selBase64$empreinteBase64}.
 * Le sel est aleatoire et different pour chaque utilisateur : deux comptes ayant le
 * meme mot de passe n'ont pas la meme empreinte.
 */
public final class PasswordHasher {

    private static final String ALGORITHME = "PBKDF2WithHmacSHA256";
    private static final String PREFIXE = "pbkdf2_sha256";
    private static final String SEPARATEUR = "\\$";
    private static final int NOMBRE_ITERATIONS = 120_000;
    private static final int TAILLE_SEL_OCTETS = 16;
    private static final int TAILLE_EMPREINTE_BITS = 256;

    private static final SecureRandom GENERATEUR_ALEATOIRE = new SecureRandom();

    private PasswordHasher() {
    }

    /** Produit l'empreinte a stocker en base pour un mot de passe en clair. */
    public static String hacher(String motDePasseEnClair) {
        exigerMotDePasseNonVide(motDePasseEnClair);

        byte[] sel = new byte[TAILLE_SEL_OCTETS];
        GENERATEUR_ALEATOIRE.nextBytes(sel);
        byte[] empreinte = calculerEmpreinte(motDePasseEnClair, sel, NOMBRE_ITERATIONS);

        Base64.Encoder encodeur = Base64.getEncoder();
        return PREFIXE + "$" + NOMBRE_ITERATIONS
                + "$" + encodeur.encodeToString(sel)
                + "$" + encodeur.encodeToString(empreinte);
    }

    /**
     * Verifie un mot de passe saisi contre l'empreinte stockee.
     * Retourne false plutot que de lever une exception si l'empreinte est illisible :
     * un enregistrement corrompu ne doit pas permettre de se connecter.
     */
    public static boolean correspond(String motDePasseEnClair, String empreinteStockee) {
        if (motDePasseEnClair == null || empreinteStockee == null) {
            return false;
        }
        String[] parties = empreinteStockee.split(SEPARATEUR);
        if (parties.length != 4 || !PREFIXE.equals(parties[0])) {
            return false;
        }
        try {
            int iterations = Integer.parseInt(parties[1]);
            byte[] sel = Base64.getDecoder().decode(parties[2]);
            byte[] empreinteAttendue = Base64.getDecoder().decode(parties[3]);
            byte[] empreinteCalculee = calculerEmpreinte(motDePasseEnClair, sel, iterations);
            // Comparaison a temps constant : ne fuit pas le nombre d'octets corrects.
            return MessageDigest.isEqual(empreinteAttendue, empreinteCalculee);
        } catch (IllegalArgumentException erreurDeFormat) {
            return false;
        }
    }

    private static byte[] calculerEmpreinte(String motDePasse, byte[] sel, int iterations) {
        PBEKeySpec specification = new PBEKeySpec(
                motDePasse.toCharArray(), sel, iterations, TAILLE_EMPREINTE_BITS);
        try {
            return SecretKeyFactory.getInstance(ALGORITHME).generateSecret(specification).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException erreur) {
            throw new IllegalStateException("Hachage du mot de passe impossible", erreur);
        } finally {
            specification.clearPassword();
        }
    }

    private static void exigerMotDePasseNonVide(String motDePasse) {
        if (motDePasse == null || motDePasse.isBlank()) {
            throw new IllegalArgumentException("Le mot de passe ne peut pas etre vide");
        }
    }
}
