package sn.isi.iage.microbank.enums;

/** Statut d'activation, commun aux utilisateurs et aux clients. */
public enum Statut {
    ACTIF("Actif"),
    INACTIF("Inactif");

    private final String libelle;

    Statut(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
