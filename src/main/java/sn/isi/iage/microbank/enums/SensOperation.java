package sn.isi.iage.microbank.enums;

/**
 * Sens d'une operation vis-a-vis du compte concerne.
 * Un virement genere deux lignes : un DEBIT sur le compte source et un CREDIT
 * sur le compte destination.
 */
public enum SensOperation {
    CREDIT("+"),
    DEBIT("-");

    private final String signe;

    SensOperation(String signe) {
        this.signe = signe;
    }

    public String getSigne() {
        return signe;
    }
}
