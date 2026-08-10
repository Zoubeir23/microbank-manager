package sn.isi.iage.microbank.service;

import jakarta.persistence.EntityManagerFactory;
import sn.isi.iage.microbank.dao.AccountDAO;
import sn.isi.iage.microbank.dao.AgencyDAO;
import sn.isi.iage.microbank.dao.ClientDAO;
import sn.isi.iage.microbank.dao.OperationDAO;
import sn.isi.iage.microbank.dao.TransactionExecutor;
import sn.isi.iage.microbank.dao.UserDAO;
import sn.isi.iage.microbank.dto.DashboardStatistics;
import sn.isi.iage.microbank.dto.OperationSearchCriteria;
import sn.isi.iage.microbank.enums.SensOperation;

import java.time.LocalDate;

/**
 * Agregats du tableau de bord.
 * Tous les chiffres sont calcules par la base dans une seule lecture : aucune liste
 * complete n'est chargee en memoire pour etre comptee en Java.
 */
public class DashboardService {

    private final TransactionExecutor transactionExecutor;
    private final ClientDAO clientDAO = new ClientDAO();
    private final AccountDAO accountDAO = new AccountDAO();
    private final OperationDAO operationDAO = new OperationDAO();
    private final UserDAO userDAO = new UserDAO();
    private final AgencyDAO agencyDAO = new AgencyDAO();

    public DashboardService() {
        this.transactionExecutor = new TransactionExecutor();
    }

    public DashboardService(EntityManagerFactory entityManagerFactory) {
        this.transactionExecutor = new TransactionExecutor(entityManagerFactory);
    }

    public DashboardStatistics calculerStatistiques() {
        LocalDate aujourdHui = LocalDate.now();
        OperationSearchCriteria criteresDuJour = OperationSearchCriteria.vide()
                .avecPeriode(aujourdHui, aujourdHui);

        return transactionExecutor.executeQuery(entityManager -> new DashboardStatistics(
                clientDAO.count(entityManager),
                accountDAO.count(entityManager),
                accountDAO.sommeDesSoldes(entityManager),
                operationDAO.compterOperationsDuJour(entityManager, aujourdHui),
                operationDAO.totalParSens(entityManager, criteresDuJour, SensOperation.CREDIT),
                operationDAO.totalParSens(entityManager, criteresDuJour, SensOperation.DEBIT),
                userDAO.count(entityManager),
                agencyDAO.count(entityManager)));
    }
}
