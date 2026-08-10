package sn.isi.iage.microbank.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sn.isi.iage.microbank.AbstractDatabaseTest;
import sn.isi.iage.microbank.dao.OperationDAO;
import sn.isi.iage.microbank.dto.OperationSearchCriteria;
import sn.isi.iage.microbank.dto.PageResult;
import sn.isi.iage.microbank.model.Account;
import sn.isi.iage.microbank.model.Client;
import sn.isi.iage.microbank.model.Operation;
import sn.isi.iage.microbank.model.User;
import sn.isi.iage.microbank.enums.StatutCompte;
import sn.isi.iage.microbank.enums.TypeOperation;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Verifie les filtres de l'historique (§18), leur combinaison (bonus 2) et la pagination (§19). */
class OperationFilterTest extends AbstractDatabaseTest {

    private OperationService operationService;
    private final OperationDAO operationDAO = new OperationDAO();
    private User agent;
    private Account compte;
    private Account autreCompte;

    @BeforeEach
    void preparerLHistorique() {
        operationService = new OperationService(entityManagerFactory);
        agent = creerAgent("awa");
        Client premierClient = creerClient("GAYE", "Abdoulaye", "PIECE-400");
        Client secondClient = creerClient("DIOP", "Awa", "PIECE-401");
        compte = creerCompte(premierClient, "100001",
                new BigDecimal("500000.00"), StatutCompte.ACTIF);
        autreCompte = creerCompte(secondClient, "100002",
                new BigDecimal("500000.00"), StatutCompte.ACTIF);

        operationService.deposer(compte.getId(), new BigDecimal("10000"), null, agent);
        operationService.deposer(compte.getId(), new BigDecimal("70000"), null, agent);
        operationService.retirer(compte.getId(), new BigDecimal("25000"), null, agent);
        operationService.deposer(autreCompte.getId(), new BigDecimal("40000"), null, agent);
    }

    @Test
    @DisplayName("le filtre par compte ne rend que les operations de ce compte")
    void filtreParCompte() {
        PageResult<Operation> page = operationService.rechercher(
                OperationSearchCriteria.pourCompte(compte.getId()), 0, 10);

        assertEquals(3, page.totalElements());
        assertTrue(page.contenu().stream()
                .allMatch(operation -> operation.getCompte().getId().equals(compte.getId())));
    }

    @Test
    @DisplayName("le filtre par type isole les depots et les retraits")
    void filtreParType() {
        OperationSearchCriteria depots = new OperationSearchCriteria(
                compte.getId(), null, TypeOperation.DEPOT, null, null, null, null);
        OperationSearchCriteria retraits = new OperationSearchCriteria(
                compte.getId(), null, TypeOperation.RETRAIT, null, null, null, null);

        assertEquals(2, operationService.rechercher(depots, 0, 10).totalElements());
        assertEquals(1, operationService.rechercher(retraits, 0, 10).totalElements());
    }

    @Test
    @DisplayName("le filtre par periode inclut la journee de fin en entier")
    void filtreParPeriode() {
        LocalDate aujourdHui = LocalDate.now();

        OperationSearchCriteria periodeCouvrante =
                OperationSearchCriteria.vide().avecPeriode(aujourdHui, aujourdHui);
        OperationSearchCriteria periodePassee = OperationSearchCriteria.vide()
                .avecPeriode(aujourdHui.minusDays(10), aujourdHui.minusDays(5));

        assertEquals(4, operationService.rechercher(periodeCouvrante, 0, 10).totalElements());
        assertEquals(0, operationService.rechercher(periodePassee, 0, 10).totalElements());
    }

    @Test
    @DisplayName("le filtre par montant minimum et maximum borne les resultats")
    void filtreParMontant() {
        OperationSearchCriteria entre20000Et80000 = new OperationSearchCriteria(
                compte.getId(), null, null, null, null,
                new BigDecimal("20000"), new BigDecimal("80000"));

        PageResult<Operation> page = operationService.rechercher(entre20000Et80000, 0, 10);

        assertEquals(2, page.totalElements());
        assertTrue(page.contenu().stream().allMatch(operation ->
                operation.getMontant().compareTo(new BigDecimal("20000")) >= 0
                        && operation.getMontant().compareTo(new BigDecimal("80000")) <= 0));
    }

    @Test
    @DisplayName("les criteres se combinent : client + type + periode + montant")
    void filtresCombinesBonusDeux() {
        Long clientId = lire(entityManager ->
                entityManager.find(Account.class, compte.getId()).getClient().getId());
        LocalDate aujourdHui = LocalDate.now();

        OperationSearchCriteria criteres = new OperationSearchCriteria(
                null, clientId, TypeOperation.DEPOT, aujourdHui, aujourdHui,
                new BigDecimal("50000"), null);

        PageResult<Operation> page = operationService.rechercher(criteres, 0, 10);

        assertEquals(1, page.totalElements());
        assertEquals(0, new BigDecimal("70000").compareTo(page.contenu().get(0).getMontant()));
    }

    @Test
    @DisplayName("l'historique est pagine par la base")
    void paginationDeLHistorique() {
        OperationSearchCriteria criteres = OperationSearchCriteria.pourCompte(compte.getId());

        PageResult<Operation> premierePage = operationService.rechercher(criteres, 0, 2);
        PageResult<Operation> secondePage = operationService.rechercher(criteres, 1, 2);

        assertEquals(2, premierePage.contenu().size());
        assertEquals(1, secondePage.contenu().size());
        assertEquals(2, premierePage.getTotalPages());
    }

    @Test
    @DisplayName("les totaux respectent les filtres appliques a l'ecran")
    void totauxFiltres() {
        OperationSearchCriteria criteres = OperationSearchCriteria.pourCompte(compte.getId());

        assertEquals(0, new BigDecimal("80000")
                .compareTo(operationService.totalDesDepots(criteres)));
        assertEquals(0, new BigDecimal("25000")
                .compareTo(operationService.totalDesRetraits(criteres)));
    }

    @Test
    @DisplayName("les dernieres operations d'un compte sont rendues les plus recentes d'abord")
    void dernieresOperations() {
        var dernieres = lire(entityManager ->
                operationDAO.findDernieresOperations(entityManager, compte.getId(), 2));

        assertEquals(2, dernieres.size());
        assertTrue(dernieres.get(0).getDateOperation()
                .compareTo(dernieres.get(1).getDateOperation()) >= 0);
    }

    @Test
    @DisplayName("le comptage des operations du jour alimente le tableau de bord")
    void operationsDuJour() {
        long operationsDuJour = lire(entityManager ->
                operationDAO.compterOperationsDuJour(entityManager, LocalDate.now()));

        assertEquals(4, operationsDuJour);
    }
}
