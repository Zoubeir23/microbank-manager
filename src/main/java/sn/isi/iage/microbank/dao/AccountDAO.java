package sn.isi.iage.microbank.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.TypedQuery;
import sn.isi.iage.microbank.dto.PageResult;
import sn.isi.iage.microbank.model.Account;
import sn.isi.iage.microbank.enums.StatutCompte;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/** Acces aux donnees des comptes bancaires. */
public class AccountDAO {

    private static final String CLAUSE_RECHERCHE =
            " WHERE LOWER(a.numeroCompte) LIKE :filtre"
                    + " OR LOWER(titulaire.nom) LIKE :filtre"
                    + " OR LOWER(titulaire.prenom) LIKE :filtre";

    public Account save(EntityManager entityManager, Account compte) {
        if (compte.getId() == null) {
            entityManager.persist(compte);
            return compte;
        }
        return entityManager.merge(compte);
    }

    public Optional<Account> findById(EntityManager entityManager, Long id) {
        return Optional.ofNullable(entityManager.find(Account.class, id));
    }

    /**
     * Charge un compte en vue de le modifier, avec un verrou optimiste.
     * Deux operations simultanees sur le meme compte ne peuvent pas se marcher dessus :
     * la seconde transaction echoue et est annulee plutot que d'ecraser le solde.
     */
    public Optional<Account> findByIdPourMiseAJour(EntityManager entityManager, Long id) {
        return Optional.ofNullable(
                entityManager.find(Account.class, id, LockModeType.OPTIMISTIC_FORCE_INCREMENT));
    }

    public Optional<Account> findByNumeroCompte(EntityManager entityManager, String numeroCompte) {
        return entityManager.createQuery(
                        "SELECT a FROM Account a WHERE a.numeroCompte = :numeroCompte",
                        Account.class)
                .setParameter("numeroCompte", numeroCompte)
                .getResultStream()
                .findFirst();
    }

    /** Compte avec son client deja charge : evite toute lecture paresseuse dans la JSP. */
    public Optional<Account> findByIdAvecClient(EntityManager entityManager, Long id) {
        return entityManager.createQuery(
                        "SELECT a FROM Account a JOIN FETCH a.client LEFT JOIN FETCH a.agence "
                                + "WHERE a.id = :id", Account.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst();
    }

    public PageResult<Account> search(EntityManager entityManager, String recherche,
                                      int numeroPage, int taillePage) {
        boolean avecFiltre = recherche != null && !recherche.isBlank();
        String clause = avecFiltre ? CLAUSE_RECHERCHE : "";
        String filtre = avecFiltre ? "%" + recherche.trim().toLowerCase() + "%" : null;

        TypedQuery<Long> requeteComptage = entityManager.createQuery(
                "SELECT COUNT(a) FROM Account a JOIN a.client titulaire" + clause, Long.class);
        TypedQuery<Account> requetePage = entityManager.createQuery(
                "SELECT a FROM Account a JOIN FETCH a.client titulaire" + clause
                        + " ORDER BY a.numeroCompte ASC", Account.class);
        if (avecFiltre) {
            requeteComptage.setParameter("filtre", filtre);
            requetePage.setParameter("filtre", filtre);
        }

        long total = requeteComptage.getSingleResult();
        List<Account> comptes = requetePage
                .setFirstResult(numeroPage * taillePage)
                .setMaxResults(taillePage)
                .getResultList();

        return new PageResult<>(comptes, numeroPage, taillePage, total);
    }

    public List<Account> findByClientId(EntityManager entityManager, Long clientId) {
        return entityManager.createQuery(
                        "SELECT a FROM Account a WHERE a.client.id = :clientId "
                                + "ORDER BY a.numeroCompte ASC", Account.class)
                .setParameter("clientId", clientId)
                .getResultList();
    }

    /** Comptes ouverts et utilisables, pour les listes deroulantes des operations. */
    public List<Account> findActifsAvecClient(EntityManager entityManager) {
        return entityManager.createQuery(
                        "SELECT a FROM Account a JOIN FETCH a.client "
                                + "WHERE a.statut = :statut ORDER BY a.numeroCompte ASC",
                        Account.class)
                .setParameter("statut", StatutCompte.ACTIF)
                .getResultList();
    }

    /** Dernier numero de compte attribue, utilise pour generer le suivant. */
    public Optional<String> findDernierNumeroCompte(EntityManager entityManager) {
        return entityManager.createQuery(
                        "SELECT a.numeroCompte FROM Account a ORDER BY a.numeroCompte DESC",
                        String.class)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }

    public long count(EntityManager entityManager) {
        return entityManager.createQuery("SELECT COUNT(a) FROM Account a", Long.class)
                .getSingleResult();
    }

    public BigDecimal sommeDesSoldes(EntityManager entityManager) {
        BigDecimal total = entityManager.createQuery(
                        "SELECT COALESCE(SUM(a.solde), 0) FROM Account a", BigDecimal.class)
                .getSingleResult();
        return total == null ? BigDecimal.ZERO : total;
    }

    public void delete(EntityManager entityManager, Account compte) {
        entityManager.remove(entityManager.contains(compte) ? compte : entityManager.merge(compte));
    }
}
