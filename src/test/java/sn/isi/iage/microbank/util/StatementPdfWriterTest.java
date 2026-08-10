package sn.isi.iage.microbank.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sn.isi.iage.microbank.model.Account;
import sn.isi.iage.microbank.model.Client;
import sn.isi.iage.microbank.model.Operation;
import sn.isi.iage.microbank.enums.SensOperation;
import sn.isi.iage.microbank.enums.StatutCompte;
import sn.isi.iage.microbank.enums.TypeCompte;
import sn.isi.iage.microbank.enums.TypeOperation;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Verifie la generation du releve PDF (§20). */
class StatementPdfWriterTest {

    private Account compteDeTest() {
        Client client = new Client();
        client.setNom("GAYE");
        client.setPrenom("Abdoulaye");

        Account compte = new Account();
        compte.setNumeroCompte("100001");
        compte.setType(TypeCompte.COURANT);
        compte.setSolde(new BigDecimal("130000.00"));
        compte.setDateOuverture(LocalDate.of(2026, 8, 1));
        compte.setStatut(StatutCompte.ACTIF);
        compte.setClient(client);
        return compte;
    }

    private Operation operation(Account compte, TypeOperation type, SensOperation sens,
                                String montant, String soldeApres) {
        Operation operation = new Operation();
        operation.setReference("OP20260810-000001");
        operation.setType(type);
        operation.setSens(sens);
        operation.setMontant(new BigDecimal(montant));
        operation.setSoldeApres(new BigDecimal(soldeApres));
        operation.setDateOperation(LocalDateTime.of(2026, 8, 10, 9, 30));
        operation.setDescription("Operation de test");
        operation.setCompte(compte);
        return operation;
    }

    private byte[] genererReleve(List<Operation> operations) {
        Account compte = compteDeTest();
        ByteArrayOutputStream tampon = new ByteArrayOutputStream();
        StatementPdfWriter.ecrire(tampon, compte, operations,
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31),
                new BigDecimal("150000.00"), new BigDecimal("20000.00"));
        return tampon.toByteArray();
    }

    @Test
    @DisplayName("le flux produit est bien un fichier PDF")
    void fichierPdfValide() {
        Account compte = compteDeTest();
        byte[] pdf = genererReleve(List.of(
                operation(compte, TypeOperation.DEPOT, SensOperation.CREDIT,
                        "100000.00", "100000.00"),
                operation(compte, TypeOperation.RETRAIT, SensOperation.DEBIT,
                        "20000.00", "80000.00")));

        assertEquals("%PDF", new String(pdf, 0, 4, StandardCharsets.US_ASCII));
        assertTrue(pdf.length > 1000, "un releve renseigne doit peser plus de 1 Ko");
    }

    @Test
    @DisplayName("un compte sans operation produit quand meme un releve")
    void releveSansOperation() {
        byte[] pdf = genererReleve(List.of());

        assertEquals("%PDF", new String(pdf, 0, 4, StandardCharsets.US_ASCII));
        assertTrue(pdf.length > 500);
    }
}
