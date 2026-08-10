package sn.isi.iage.microbank.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sn.isi.iage.microbank.AbstractDatabaseTest;
import sn.isi.iage.microbank.dto.AccountForm;
import sn.isi.iage.microbank.dto.OperationSearchCriteria;
import sn.isi.iage.microbank.model.Account;
import sn.isi.iage.microbank.model.Client;
import sn.isi.iage.microbank.model.Operation;
import sn.isi.iage.microbank.model.User;
import sn.isi.iage.microbank.enums.StatutCompte;
import sn.isi.iage.microbank.enums.TypeCompte;
import sn.isi.iage.microbank.enums.TypeOperation;
import sn.isi.iage.microbank.exception.BusinessRuleException;
import sn.isi.iage.microbank.exception.ValidationException;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Verifie l'ouverture de compte (§12), la numerotation unique et le depot initial. */
class AccountServiceTest extends AbstractDatabaseTest {

    private AccountService accountService;
    private OperationService operationService;
    private User agent;
    private Client client;

    @BeforeEach
    void preparerLesServices() {
        accountService = new AccountService(entityManagerFactory);
        operationService = new OperationService(entityManagerFactory);
        agent = creerAgent("awa");
        client = creerClient("GAYE", "Abdoulaye", "PIECE-300");
    }

    private AccountForm formulaire(String depotInitial) {
        return new AccountForm(null, String.valueOf(client.getId()), null,
                TypeCompte.COURANT.name(), null, depotInitial);
    }

    @Test
    @DisplayName("l'ouverture cree un compte actif avec un numero unique")
    void ouvertureDUnCompte() {
        Account premierCompte = accountService.ouvrirCompte(formulaire("0"), agent);
        Account secondCompte = accountService.ouvrirCompte(formulaire("0"), agent);

        assertEquals("100001", premierCompte.getNumeroCompte());
        assertEquals("100002", secondCompte.getNumeroCompte());
        assertNotEquals(premierCompte.getNumeroCompte(), secondCompte.getNumeroCompte());
        assertEquals(StatutCompte.ACTIF, premierCompte.getStatut());
        assertEquals(0, BigDecimal.ZERO.compareTo(premierCompte.getSolde()));
    }

    @Test
    @DisplayName("le depot initial credite le compte et laisse une trace dans l'historique")
    void depotInitialJournalise() {
        Account compte = accountService.ouvrirCompte(formulaire("100000"), agent);

        assertEquals(0, new BigDecimal("100000").compareTo(soldeDuCompte(compte.getId())));

        List<Operation> operations = operationService.listerPourExport(
                OperationSearchCriteria.pourCompte(compte.getId()));
        assertEquals(1, operations.size());
        assertEquals(TypeOperation.DEPOT, operations.get(0).getType());
        assertTrue(operations.get(0).getDescription().contains("Depot initial"));
    }

    @Test
    @DisplayName("une ouverture sans client ni type est refusee")
    void ouvertureSansClientNiTypeRefusee() {
        AccountForm formulaireIncomplet = new AccountForm(null, null, null, null, null, "0");

        ValidationException erreur = assertThrows(ValidationException.class,
                () -> accountService.ouvrirCompte(formulaireIncomplet, agent));

        assertTrue(erreur.getErreursParChamp().containsKey("clientId"));
        assertTrue(erreur.getErreursParChamp().containsKey("type"));
    }

    @Test
    @DisplayName("un depot initial negatif est refuse")
    void depotInitialNegatifRefuse() {
        assertThrows(ValidationException.class,
                () -> accountService.ouvrirCompte(formulaire("-1000"), agent));
    }

    @Test
    @DisplayName("bloquer un compte empeche ensuite toute operation")
    void blocageDUnCompte() {
        Account compte = accountService.ouvrirCompte(formulaire("50000"), agent);

        accountService.changerStatut(compte.getId(), StatutCompte.BLOQUE.name());

        assertThrows(BusinessRuleException.class, () -> operationService.deposer(
                compte.getId(), new BigDecimal("1000"), null, agent));
    }

    @Test
    @DisplayName("cloturer un compte au solde non nul est refuse")
    void cloturerUnCompteNonVideRefuse() {
        Account compte = accountService.ouvrirCompte(formulaire("50000"), agent);

        assertThrows(BusinessRuleException.class,
                () -> accountService.changerStatut(compte.getId(), StatutCompte.CLOTURE.name()));
    }

    @Test
    @DisplayName("les comptes d'un client sont retrouves")
    void listerLesComptesDUnClient() {
        accountService.ouvrirCompte(formulaire("1000"), agent);
        accountService.ouvrirCompte(formulaire("2000"), agent);

        assertEquals(2, accountService.listerParClient(client.getId()).size());
    }

    @Test
    @DisplayName("la recherche de comptes porte sur le numero et le titulaire")
    void rechercheDeComptes() {
        accountService.ouvrirCompte(formulaire("1000"), agent);

        assertEquals(1, accountService.rechercher("100001", 0, 10).totalElements());
        assertEquals(1, accountService.rechercher("gaye", 0, 10).totalElements());
        assertEquals(0, accountService.rechercher("inconnu", 0, 10).totalElements());
    }
}
