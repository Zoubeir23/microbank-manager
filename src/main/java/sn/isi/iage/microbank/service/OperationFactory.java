package sn.isi.iage.microbank.service;

import sn.isi.iage.microbank.entity.Account;
import sn.isi.iage.microbank.entity.Operation;
import sn.isi.iage.microbank.entity.User;
import sn.isi.iage.microbank.enums.SensOperation;
import sn.isi.iage.microbank.enums.TypeOperation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Fabrique des lignes d'historique coherentes.
 * Centralisee ici pour qu'un depot initial, un depot courant, un retrait ou une
 * jambe de virement soient tous construits de la meme facon.
 */
final class OperationFactory {

    private static final DateTimeFormatter FORMAT_JOUR = DateTimeFormatter.ofPattern("yyyyMMdd");

    private OperationFactory() {
    }

    /**
     * Applique un mouvement sur un compte et produit la ligne d'historique
     * correspondante. Le solde du compte est mis a jour ici afin que le solde
     * fige dans l'historique corresponde toujours au solde reel.
     */
    static Operation appliquerMouvement(Account compte, User auteur, TypeOperation type,
                                        SensOperation sens, BigDecimal montant,
                                        String description, String compteContrepartie,
                                        String reference, LocalDateTime dateOperation) {
        BigDecimal nouveauSolde = sens == SensOperation.CREDIT
                ? compte.getSolde().add(montant)
                : compte.getSolde().subtract(montant);
        compte.setSolde(nouveauSolde);

        Operation operation = new Operation();
        operation.setReference(reference);
        operation.setType(type);
        operation.setSens(sens);
        operation.setMontant(montant);
        operation.setSoldeApres(nouveauSolde);
        operation.setDateOperation(dateOperation);
        operation.setDescription(description);
        operation.setCompteContrepartie(compteContrepartie);
        operation.setCompte(compte);
        operation.setUser(auteur);
        return operation;
    }

    /** Reference lisible et unique dans la journee : OP20260810-000042. */
    static String genererReference(LocalDateTime dateOperation, long numeroDOrdre) {
        return "OP" + FORMAT_JOUR.format(dateOperation) + "-"
                + String.format("%06d", numeroDOrdre);
    }
}
