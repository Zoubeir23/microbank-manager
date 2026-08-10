package sn.isi.iage.microbank.enums;

/** Nature d'une operation bancaire enregistree dans l'historique. */
public enum TypeOperation {
    DEPOT("Depot"),
    RETRAIT("Retrait"),
    VIREMENT("Virement");

    private final String libelle;

    TypeOperation(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
