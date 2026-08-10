package sn.isi.iage.microbank.enums;

/**
 * Role d'un utilisateur de l'application.
 * ADMIN possede tous les droits de l'AGENT, plus la gestion des utilisateurs.
 */
public enum Role {
    AGENT("Agent"),
    ADMIN("Administrateur");

    private final String libelle;

    Role(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
