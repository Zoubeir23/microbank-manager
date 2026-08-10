package sn.isi.iage.microbank.exception;

/**
 * Regle metier violee : solde insuffisant, compte bloque, comptes identiques...
 * Le message est destine a etre affiche tel quel a l'agent.
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
