package sn.isi.iage.microbank.enums;

/** Statut d'un compte bancaire. Seul un compte ACTIF accepte des operations. */
public enum StatutCompte {
    ACTIF("Actif"),
    BLOQUE("Bloque"),
    CLOTURE("Cloture");

    private final String libelle;

    StatutCompte(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }

    public boolean accepteOperation() {
        return this == ACTIF;
    }
}
