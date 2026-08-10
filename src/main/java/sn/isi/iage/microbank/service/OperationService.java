package sn.isi.iage.microbank.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import sn.isi.iage.microbank.dao.AccountDAO;
import sn.isi.iage.microbank.dao.OperationDAO;
import sn.isi.iage.microbank.dao.TransactionExecutor;
import sn.isi.iage.microbank.dto.OperationSearchCriteria;
import sn.isi.iage.microbank.dto.PageResult;
import sn.isi.iage.microbank.entity.Account;
import sn.isi.iage.microbank.entity.Operation;
import sn.isi.iage.microbank.entity.User;
import sn.isi.iage.microbank.enums.SensOperation;
import sn.isi.iage.microbank.enums.TypeOperation;
import sn.isi.iage.microbank.exception.BusinessRuleException;
import sn.isi.iage.microbank.exception.ResourceNotFoundException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Operations bancaires : depot, retrait et virement.
 * <p>
 * Chaque operation s'execute dans une transaction unique ouverte par
 * {@link TransactionExecutor}. Pour un virement, le debit du compte source, le credit
 * du compte destination et les deux lignes d'historique sont valides ensemble ; si une
 * regle metier echoue en cours de route, l'exception remonte et tout est annule
 * (rollback), aucun compte n'est modifie.
 */
public class OperationService {

    private final TransactionExecutor transactionExecutor;
    private final AccountDAO accountDAO = new AccountDAO();
    private final OperationDAO operationDAO = new OperationDAO();

    public OperationService() {
        this.transactionExecutor = new TransactionExecutor();
    }

    public OperationService(EntityManagerFactory entityManagerFactory) {
        this.transactionExecutor = new TransactionExecutor(entityManagerFactory);
    }

    // ===================== ECRITURES =====================

    /** Depot : montant strictement positif, compte existant et actif. */
    public Operation deposer(Long compteId, BigDecimal montant, String description, User agent) {
        return transactionExecutor.executeInTransaction(entityManager -> {
            exigerMontantStrictementPositif(montant);
            Account compte = chargerComptePourMiseAJour(entityManager, compteId, "Le compte");
            exigerCompteActif(compte, "Le compte " + compte.getNumeroCompte());

            LocalDateTime maintenant = LocalDateTime.now();
            Operation depot = OperationFactory.appliquerMouvement(
                    compte, referenceAgent(entityManager, agent),
                    TypeOperation.DEPOT, SensOperation.CREDIT, montant,
                    descriptionOuDefaut(description, "Depot en especes"), null,
                    genererReference(entityManager, maintenant), maintenant);

            operationDAO.save(entityManager, depot);
            accountDAO.save(entityManager, compte);
            return depot;
        });
    }

    /** Retrait : montant strictement positif, compte actif et solde suffisant. */
    public Operation retirer(Long compteId, BigDecimal montant, String description, User agent) {
        return transactionExecutor.executeInTransaction(entityManager -> {
            exigerMontantStrictementPositif(montant);
            Account compte = chargerComptePourMiseAJour(entityManager, compteId, "Le compte");
            exigerCompteActif(compte, "Le compte " + compte.getNumeroCompte());
            exigerSoldeSuffisant(compte, montant);

            LocalDateTime maintenant = LocalDateTime.now();
            Operation retrait = OperationFactory.appliquerMouvement(
                    compte, referenceAgent(entityManager, agent),
                    TypeOperation.RETRAIT, SensOperation.DEBIT, montant,
                    descriptionOuDefaut(description, "Retrait en especes"), null,
                    genererReference(entityManager, maintenant), maintenant);

            operationDAO.save(entityManager, retrait);
            accountDAO.save(entityManager, compte);
            return retrait;
        });
    }

    /**
     * Virement : debit de la source, credit de la destination et journalisation,
     * le tout dans une seule transaction.
     *
     * @return la ligne de debit, cote compte source
     */
    public Operation virer(Long compteSourceId, Long compteDestinationId, BigDecimal montant,
                           String description, User agent) {
        return transactionExecutor.executeInTransaction(entityManager -> {
            exigerMontantStrictementPositif(montant);
            if (compteSourceId == null || compteSourceId.equals(compteDestinationId)) {
                throw new BusinessRuleException(
                        "Le compte source et le compte destination doivent etre differents.");
            }

            Account compteSource =
                    chargerComptePourMiseAJour(entityManager, compteSourceId, "Le compte source");
            Account compteDestination = chargerComptePourMiseAJour(
                    entityManager, compteDestinationId, "Le compte destination");

            exigerCompteActif(compteSource, "Le compte source " + compteSource.getNumeroCompte());
            exigerCompteActif(compteDestination,
                    "Le compte destination " + compteDestination.getNumeroCompte());
            exigerSoldeSuffisant(compteSource, montant);

            LocalDateTime maintenant = LocalDateTime.now();
            String reference = genererReference(entityManager, maintenant);
            User auteur = referenceAgent(entityManager, agent);
            String libelle = descriptionOuDefaut(description, "Virement");

            Operation debit = OperationFactory.appliquerMouvement(
                    compteSource, auteur, TypeOperation.VIREMENT, SensOperation.DEBIT, montant,
                    libelle + " vers " + compteDestination.getNumeroCompte(),
                    compteDestination.getNumeroCompte(), reference, maintenant);

            Operation credit = OperationFactory.appliquerMouvement(
                    compteDestination, auteur, TypeOperation.VIREMENT, SensOperation.CREDIT,
                    montant, libelle + " recu de " + compteSource.getNumeroCompte(),
                    compteSource.getNumeroCompte(), reference, maintenant);

            operationDAO.save(entityManager, debit);
            operationDAO.save(entityManager, credit);
            accountDAO.save(entityManager, compteSource);
            accountDAO.save(entityManager, compteDestination);
            return debit;
        });
    }

    // ===================== LECTURES =====================

    public PageResult<Operation> rechercher(OperationSearchCriteria criteres,
                                            int numeroPage, int taillePage) {
        return transactionExecutor.executeQuery(entityManager ->
                operationDAO.search(entityManager, criteres, numeroPage, taillePage));
    }

    public List<Operation> listerPourExport(OperationSearchCriteria criteres) {
        return transactionExecutor.executeQuery(entityManager ->
                operationDAO.findAll(entityManager, criteres));
    }

    public BigDecimal totalDesDepots(OperationSearchCriteria criteres) {
        return transactionExecutor.executeQuery(entityManager ->
                operationDAO.totalParSens(entityManager, criteres, SensOperation.CREDIT));
    }

    public BigDecimal totalDesRetraits(OperationSearchCriteria criteres) {
        return transactionExecutor.executeQuery(entityManager ->
                operationDAO.totalParSens(entityManager, criteres, SensOperation.DEBIT));
    }

    // ===================== REGLES METIER =====================

    private void exigerMontantStrictementPositif(BigDecimal montant) {
        if (montant == null || montant.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("Le montant doit etre strictement positif.");
        }
    }

    private void exigerCompteActif(Account compte, String libelleCompte) {
        if (!compte.estActif()) {
            throw new BusinessRuleException(
                    libelleCompte + " n'est pas actif (" + compte.getStatut().getLibelle() + ").");
        }
    }

    private void exigerSoldeSuffisant(Account compte, BigDecimal montant) {
        if (!compte.soldeSuffisantPour(montant)) {
            throw new BusinessRuleException("Solde insuffisant sur le compte "
                    + compte.getNumeroCompte() + " : solde actuel " + compte.getSolde() + ".");
        }
    }

    private Account chargerComptePourMiseAJour(EntityManager entityManager, Long compteId,
                                               String libelleCompte) {
        if (compteId == null) {
            throw new BusinessRuleException(libelleCompte + " est obligatoire.");
        }
        return accountDAO.findByIdPourMiseAJour(entityManager, compteId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        libelleCompte + " est introuvable (identifiant " + compteId + ")."));
    }

    /** L'agent provient de la session HTTP : on le rattache au contexte de persistance. */
    private User referenceAgent(EntityManager entityManager, User agent) {
        if (agent == null || agent.getId() == null) {
            throw new BusinessRuleException("Operation impossible : agent non identifie.");
        }
        return entityManager.getReference(User.class, agent.getId());
    }

    private String genererReference(EntityManager entityManager, LocalDateTime dateOperation) {
        return OperationFactory.genererReference(
                dateOperation, operationDAO.prochainNumeroReference(entityManager));
    }

    private String descriptionOuDefaut(String description, String valeurParDefaut) {
        return description == null || description.isBlank() ? valeurParDefaut : description.trim();
    }
}
