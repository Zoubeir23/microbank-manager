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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Verifie le format de l'export CSV (§21) et la robustesse de l'echappement. */
class OperationCsvWriterTest {

    private Operation operation(String description) {
        Client client = new Client();
        client.setNom("GAYE");
        client.setPrenom("Abdoulaye");

        Account compte = new Account();
        compte.setNumeroCompte("100001");
        compte.setType(TypeCompte.COURANT);
        compte.setSolde(new BigDecimal("100000.00"));
        compte.setDateOuverture(LocalDate.now());
        compte.setStatut(StatutCompte.ACTIF);
        compte.setClient(client);

        Operation operation = new Operation();
        operation.setReference("OP20260810-000001");
        operation.setType(TypeOperation.DEPOT);
        operation.setSens(SensOperation.CREDIT);
        operation.setMontant(new BigDecimal("25000.00"));
        operation.setSoldeApres(new BigDecimal("125000.00"));
        operation.setDateOperation(LocalDateTime.of(2026, 8, 10, 9, 30));
        operation.setDescription(description);
        operation.setCompte(compte);
        return operation;
    }

    private String ecrire(List<Operation> operations) {
        StringWriter tampon = new StringWriter();
        OperationCsvWriter.ecrire(new PrintWriter(tampon), operations);
        return tampon.toString();
    }

    @Test
    @DisplayName("le fichier commence par l'entete demande")
    void enteteDuFichier() {
        String csv = ecrire(List.of());

        assertTrue(csv.contains("Date;Reference;Type;Sens;Montant;Solde;Compte;Client;Description"));
    }

    @Test
    @DisplayName("une operation produit une ligne exploitable")
    void ligneDOperation() {
        String[] lignes = ecrire(List.of(operation("Depot guichet"))).split("\\R");
        String ligne = lignes[1];

        assertEquals(9, ligne.split(";", -1).length);
        assertTrue(ligne.startsWith("10/08/2026 09:30"));
        assertTrue(ligne.contains("OP20260810-000001"));
        assertTrue(ligne.contains("DEPOT"));
        assertTrue(ligne.contains("25000.00"));
        assertTrue(ligne.contains("Abdoulaye GAYE"));
    }

    @Test
    @DisplayName("un point-virgule dans la description ne casse pas les colonnes")
    void echappementDuSeparateur() {
        String ligne = ecrire(List.of(operation("Depot ; especes"))).split("\\R")[1];

        assertTrue(ligne.contains("\"Depot ; especes\""));
    }

    @Test
    @DisplayName("les guillemets et les retours a la ligne sont neutralises")
    void echappementDesGuillemets() {
        String ligne = ecrire(List.of(operation("Depot \"urgent\"\nligne 2"))).split("\\R")[1];

        assertTrue(ligne.contains("\"\"urgent\"\""));
        assertFalse(ligne.endsWith("ligne 2\n"));
    }

    @Test
    @DisplayName("une description commencant par = est neutralisee (injection de formule)")
    void protectionContreLInjectionDeFormule() {
        String ligne = ecrire(List.of(operation("=1+1"))).split("\\R")[1];

        assertTrue(ligne.contains("'=1+1"));
    }

    @Test
    @DisplayName("une description absente laisse une colonne vide")
    void descriptionAbsente() {
        String ligne = ecrire(List.of(operation(null))).split("\\R")[1];

        assertTrue(ligne.endsWith(";"));
    }
}
