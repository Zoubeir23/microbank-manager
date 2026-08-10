package sn.isi.iage.microbank.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sn.isi.iage.microbank.AbstractDatabaseTest;
import sn.isi.iage.microbank.dto.OperationSearchCriteria;
import sn.isi.iage.microbank.entity.Account;
import sn.isi.iage.microbank.entity.Client;
import sn.isi.iage.microbank.entity.Operation;
import sn.isi.iage.microbank.entity.User;
import sn.isi.iage.microbank.enums.SensOperation;
import sn.isi.iage.microbank.enums.StatutCompte;
import sn.isi.iage.microbank.enums.TypeOperation;
import sn.isi.iage.microbank.exception.BusinessRuleException;
import sn.isi.iage.microbank.exception.ResourceNotFoundException;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Verifie les regles metier du §15 et le comportement transactionnel du §16. */
class OperationServiceTest extends AbstractDatabaseTest {

    private OperationService operationService;
    private User agent;
    private Account compteSource;
    private Account compteDestination;

    @BeforeEach
    void preparerLeJeuDeDonnees() {
        operationService = new OperationService(entityManagerFactory);
        agent = creerAgent("awa");
        Client premierClient = creerClient("GAYE", "Abdoulaye", "PIECE-001");
        Client secondClient = creerClient("DIOP", "Awa", "PIECE-002");
        compteSource = creerCompte(premierClient, "100001",
                new BigDecimal("100000.00"), StatutCompte.ACTIF);
        compteDestination = creerCompte(secondClient, "100002",
                new BigDecimal("50000.00"), StatutCompte.ACTIF);
    }

    // ===================== DEPOT =====================

    @Test
    @DisplayName("un depot credite le compte et enregistre l'operation")
    void depotCrediteLeCompte() {
        Operation depot = operationService.deposer(
                compteSource.getId(), new BigDecimal("25000"), "Depot guichet", agent);

        assertEquals(0, new BigDecimal("125000.00").compareTo(soldeDuCompte(compteSource.getId())));
        assertEquals(TypeOperation.DEPOT, depot.getType());
        assertEquals(SensOperation.CREDIT, depot.getSens());
        assertEquals(0, new BigDecimal("125000.00").compareTo(depot.getSoldeApres()));
        assertNotNull(depot.getReference());
    }

    @Test
    @DisplayName("un depot de montant nul ou negatif est refuse")
    void depotDeMontantNonPositifRefuse() {
        assertThrows(BusinessRuleException.class, () -> operationService.deposer(
                compteSource.getId(), BigDecimal.ZERO, null, agent));
        assertThrows(BusinessRuleException.class, () -> operationService.deposer(
                compteSource.getId(), new BigDecimal("-5000"), null, agent));

        assertEquals(0, new BigDecimal("100000.00").compareTo(soldeDuCompte(compteSource.getId())));
    }

    @Test
    @DisplayName("un depot sur un compte bloque est refuse")
    void depotSurCompteBloqueRefuse() {
        Client client = creerClient("SOW", "Fatou", "PIECE-003");
        Account compteBloque = creerCompte(client, "100003",
                new BigDecimal("10000.00"), StatutCompte.BLOQUE);

        assertThrows(BusinessRuleException.class, () -> operationService.deposer(
                compteBloque.getId(), new BigDecimal("1000"), null, agent));

        assertEquals(0, new BigDecimal("10000.00").compareTo(soldeDuCompte(compteBloque.getId())));
    }

    @Test
    @DisplayName("un depot sur un compte inexistant est refuse")
    void depotSurCompteInexistantRefuse() {
        assertThrows(ResourceNotFoundException.class, () -> operationService.deposer(
                999_999L, new BigDecimal("1000"), null, agent));
    }

    // ===================== RETRAIT =====================

    @Test
    @DisplayName("un retrait debite le compte")
    void retraitDebiteLeCompte() {
        Operation retrait = operationService.retirer(
                compteSource.getId(), new BigDecimal("20000"), null, agent);

        assertEquals(0, new BigDecimal("80000.00").compareTo(soldeDuCompte(compteSource.getId())));
        assertEquals(SensOperation.DEBIT, retrait.getSens());
        assertEquals(0, new BigDecimal("-20000").compareTo(retrait.getMontantSigne()));
    }

    @Test
    @DisplayName("un retrait superieur au solde est refuse et ne modifie rien")
    void retraitSuperieurAuSoldeRefuse() {
        BusinessRuleException erreur = assertThrows(BusinessRuleException.class,
                () -> operationService.retirer(
                        compteSource.getId(), new BigDecimal("150000"), null, agent));

        assertTrue(erreur.getMessage().contains("Solde insuffisant"));
        assertEquals(0, new BigDecimal("100000.00").compareTo(soldeDuCompte(compteSource.getId())));
        assertEquals(0, compterOperations());
    }

    @Test
    @DisplayName("un retrait egal au solde est accepte et laisse le compte a zero")
    void retraitEgalAuSoldeAccepte() {
        operationService.retirer(compteSource.getId(), new BigDecimal("100000.00"), null, agent);

        assertEquals(0, BigDecimal.ZERO.compareTo(soldeDuCompte(compteSource.getId())));
    }

    // ===================== VIREMENT =====================

    @Test
    @DisplayName("un virement debite la source, credite la destination et cree deux lignes")
    void virementDeplaceLesFondsEtJournaliseLesDeuxCotes() {
        Operation debit = operationService.virer(compteSource.getId(),
                compteDestination.getId(), new BigDecimal("30000"), "Loyer", agent);

        assertEquals(0, new BigDecimal("70000.00").compareTo(soldeDuCompte(compteSource.getId())));
        assertEquals(0, new BigDecimal("80000.00")
                .compareTo(soldeDuCompte(compteDestination.getId())));

        List<Operation> operations = operationService.listerPourExport(
                OperationSearchCriteria.vide());
        assertEquals(2, operations.size());
        assertTrue(operations.stream().allMatch(o -> o.getType() == TypeOperation.VIREMENT));
        // Les deux jambes partagent la meme reference : elles forment un seul mouvement.
        assertEquals(1, operations.stream().map(Operation::getReference).distinct().count());
        assertEquals(compteDestination.getNumeroCompte(), debit.getCompteContrepartie());
    }

    @Test
    @DisplayName("un virement vers le meme compte est refuse")
    void virementVersLeMemeCompteRefuse() {
        assertThrows(BusinessRuleException.class, () -> operationService.virer(
                compteSource.getId(), compteSource.getId(), new BigDecimal("1000"), null, agent));

        assertEquals(0, new BigDecimal("100000.00").compareTo(soldeDuCompte(compteSource.getId())));
    }

    @Test
    @DisplayName("un virement au solde insuffisant est annule integralement (rollback)")
    void virementAuSoldeInsuffisantEstAnnule() {
        assertThrows(BusinessRuleException.class, () -> operationService.virer(
                compteSource.getId(), compteDestination.getId(),
                new BigDecimal("500000"), null, agent));

        // Aucun des deux comptes n'a bouge, aucune ligne d'historique n'a ete ecrite.
        assertEquals(0, new BigDecimal("100000.00").compareTo(soldeDuCompte(compteSource.getId())));
        assertEquals(0, new BigDecimal("50000.00")
                .compareTo(soldeDuCompte(compteDestination.getId())));
        assertEquals(0, compterOperations());
    }

    @Test
    @DisplayName("un virement vers un compte bloque est annule integralement")
    void virementVersCompteBloqueEstAnnule() {
        Client client = creerClient("BA", "Moussa", "PIECE-004");
        Account compteBloque = creerCompte(client, "100004",
                new BigDecimal("1000.00"), StatutCompte.BLOQUE);

        assertThrows(BusinessRuleException.class, () -> operationService.virer(
                compteSource.getId(), compteBloque.getId(), new BigDecimal("5000"), null, agent));

        assertEquals(0, new BigDecimal("100000.00").compareTo(soldeDuCompte(compteSource.getId())));
        assertEquals(0, new BigDecimal("1000.00").compareTo(soldeDuCompte(compteBloque.getId())));
        assertEquals(0, compterOperations());
    }

    // ===================== TOTAUX =====================

    @Test
    @DisplayName("les totaux de depots et de retraits suivent le sens des operations")
    void totauxParSens() {
        operationService.deposer(compteSource.getId(), new BigDecimal("10000"), null, agent);
        operationService.deposer(compteSource.getId(), new BigDecimal("5000"), null, agent);
        operationService.retirer(compteSource.getId(), new BigDecimal("3000"), null, agent);

        OperationSearchCriteria criteres =
                OperationSearchCriteria.pourCompte(compteSource.getId());

        assertEquals(0, new BigDecimal("15000")
                .compareTo(operationService.totalDesDepots(criteres)));
        assertEquals(0, new BigDecimal("3000")
                .compareTo(operationService.totalDesRetraits(criteres)));
    }

    private long compterOperations() {
        return lire(entityManager -> entityManager
                .createQuery("SELECT COUNT(o) FROM Operation o", Long.class)
                .getSingleResult());
    }
}
