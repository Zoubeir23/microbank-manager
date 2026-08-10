package sn.isi.iage.microbank.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import sn.isi.iage.microbank.dao.AccountDAO;
import sn.isi.iage.microbank.dao.AgencyDAO;
import sn.isi.iage.microbank.dao.ClientDAO;
import sn.isi.iage.microbank.dao.OperationDAO;
import sn.isi.iage.microbank.dao.TransactionExecutor;
import sn.isi.iage.microbank.dto.AccountForm;
import sn.isi.iage.microbank.dto.PageResult;
import sn.isi.iage.microbank.model.Account;
import sn.isi.iage.microbank.model.Agency;
import sn.isi.iage.microbank.model.Client;
import sn.isi.iage.microbank.model.Operation;
import sn.isi.iage.microbank.model.User;
import sn.isi.iage.microbank.enums.SensOperation;
import sn.isi.iage.microbank.enums.Statut;
import sn.isi.iage.microbank.enums.StatutCompte;
import sn.isi.iage.microbank.enums.TypeCompte;
import sn.isi.iage.microbank.enums.TypeOperation;
import sn.isi.iage.microbank.exception.BusinessRuleException;
import sn.isi.iage.microbank.exception.ResourceNotFoundException;
import sn.isi.iage.microbank.util.FormValidator;
import sn.isi.iage.microbank.util.ValueParser;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Regles de gestion des comptes : ouverture, numerotation unique, changement de statut. */
public class AccountService {

    /** Premier numero attribue si la base ne contient encore aucun compte. */
    private static final long PREMIER_NUMERO_COMPTE = 100_001L;

    private final TransactionExecutor transactionExecutor;
    private final AccountDAO accountDAO = new AccountDAO();
    private final ClientDAO clientDAO = new ClientDAO();
    private final AgencyDAO agencyDAO = new AgencyDAO();
    private final OperationDAO operationDAO = new OperationDAO();

    public AccountService() {
        this.transactionExecutor = new TransactionExecutor();
    }

    public AccountService(EntityManagerFactory entityManagerFactory) {
        this.transactionExecutor = new TransactionExecutor(entityManagerFactory);
    }

    public PageResult<Account> rechercher(String recherche, int numeroPage, int taillePage) {
        return transactionExecutor.executeQuery(entityManager ->
                accountDAO.search(entityManager, recherche, numeroPage, taillePage));
    }

    public Account consulter(Long id) {
        return transactionExecutor.executeQuery(entityManager ->
                accountDAO.findByIdAvecClient(entityManager, id)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Compte introuvable (identifiant " + id + ")")));
    }

    public List<Account> listerParClient(Long clientId) {
        return transactionExecutor.executeQuery(entityManager ->
                accountDAO.findByClientId(entityManager, clientId));
    }

    public List<Account> listerComptesActifs() {
        return transactionExecutor.executeQuery(accountDAO::findActifsAvecClient);
    }

    public List<Agency> listerAgences() {
        return transactionExecutor.executeQuery(agencyDAO::findAllOrdonnees);
    }

    public List<Operation> listerDernieresOperations(Long compteId, int limite) {
        return transactionExecutor.executeQuery(entityManager ->
                operationDAO.findDernieresOperations(entityManager, compteId, limite));
    }

    /**
     * Ouvre un compte pour un client. Si un depot initial est saisi, le compte et
     * l'operation de depot sont ecrits dans la meme transaction : jamais de compte
     * credite sans trace dans l'historique.
     */
    public Account ouvrirCompte(AccountForm formulaire, User agent) {
        return transactionExecutor.executeInTransaction(entityManager -> {
            Long clientId = ValueParser.versIdentifiant(formulaire.clientId()).orElse(null);
            TypeCompte type = ValueParser.versEnumeration(TypeCompte.class, formulaire.type())
                    .orElse(null);
            BigDecimal depotInitial = ValueParser.versMontant(formulaire.depotInitial())
                    .orElse(BigDecimal.ZERO);

            FormValidator validateur = new FormValidator()
                    .exiger("clientId", clientId != null, "Le client est obligatoire.")
                    .exiger("type", type != null, "Le type de compte est obligatoire.")
                    .exigerMontantPositifOuNul("depotInitial", depotInitial,
                            "Le depot initial ne peut pas etre negatif.");
            validateur.lancerSiErreurs();

            Client client = clientDAO.findById(entityManager, clientId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Client introuvable (identifiant " + clientId + ")"));
            if (client.getStatut() != Statut.ACTIF) {
                throw new BusinessRuleException(
                        "Impossible d'ouvrir un compte pour un client inactif.");
            }

            Account compte = new Account();
            compte.setNumeroCompte(genererNumeroCompte(entityManager));
            compte.setType(type);
            compte.setSolde(BigDecimal.ZERO);
            compte.setDateOuverture(LocalDate.now());
            compte.setStatut(StatutCompte.ACTIF);
            compte.setClient(client);
            ValueParser.versIdentifiant(formulaire.agenceId())
                    .flatMap(agenceId -> agencyDAO.findById(entityManager, agenceId))
                    .ifPresent(compte::setAgence);

            accountDAO.save(entityManager, compte);

            if (depotInitial.compareTo(BigDecimal.ZERO) > 0) {
                enregistrerDepotInitial(entityManager, compte, agent, depotInitial);
            }
            return compte;
        });
    }

    /** Change le statut d'un compte (ACTIF, BLOQUE, CLOTURE). */
    public Account changerStatut(Long compteId, String nouveauStatut) {
        return transactionExecutor.executeInTransaction(entityManager -> {
            StatutCompte statut = ValueParser
                    .versEnumeration(StatutCompte.class, nouveauStatut)
                    .orElseThrow(() -> new BusinessRuleException("Statut de compte inconnu."));

            Account compte = accountDAO.findById(entityManager, compteId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Compte introuvable (identifiant " + compteId + ")"));

            if (statut == StatutCompte.CLOTURE
                    && compte.getSolde().compareTo(BigDecimal.ZERO) != 0) {
                throw new BusinessRuleException(
                        "Un compte ne peut etre cloture que si son solde est nul.");
            }

            compte.setStatut(statut);
            return accountDAO.save(entityManager, compte);
        });
    }

    /**
     * Numero de compte unique, sequentiel et lisible.
     * Genere dans la transaction d'ouverture ; la contrainte d'unicite en base reste
     * le dernier rempart en cas d'ouvertures simultanees.
     */
    private String genererNumeroCompte(EntityManager entityManager) {
        Optional<String> dernierNumero = accountDAO.findDernierNumeroCompte(entityManager);
        long prochainNumero = dernierNumero
                .map(numero -> {
                    try {
                        return Long.parseLong(numero) + 1;
                    } catch (NumberFormatException numeroNonNumerique) {
                        return PREMIER_NUMERO_COMPTE;
                    }
                })
                .orElse(PREMIER_NUMERO_COMPTE);
        return String.valueOf(prochainNumero);
    }

    private void enregistrerDepotInitial(EntityManager entityManager, Account compte,
                                         User agent, BigDecimal montant) {
        LocalDateTime maintenant = LocalDateTime.now();
        String reference = OperationFactory.genererReference(
                maintenant, operationDAO.prochainNumeroReference(entityManager));
        // L'agent vient de la session HTTP : il est detache. On le rattache au contexte
        // de persistance courant avant de le referencer depuis l'operation.
        User auteur = entityManager.getReference(User.class, agent.getId());

        Operation depot = OperationFactory.appliquerMouvement(
                compte, auteur, TypeOperation.DEPOT, SensOperation.CREDIT, montant,
                "Depot initial a l'ouverture du compte", null, reference, maintenant);

        operationDAO.save(entityManager, depot);
        accountDAO.save(entityManager, compte);
    }
}
