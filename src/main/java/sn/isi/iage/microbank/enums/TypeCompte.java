package sn.isi.iage.microbank.enums;

/** Type d'un compte bancaire. */
public enum TypeCompte {
    COURANT("Compte courant"),
    EPARGNE("Compte epargne");

    private final String libelle;

    TypeCompte(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
