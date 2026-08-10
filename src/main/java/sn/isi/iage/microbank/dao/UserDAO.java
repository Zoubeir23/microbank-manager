package sn.isi.iage.microbank.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import sn.isi.iage.microbank.dto.PageResult;
import sn.isi.iage.microbank.entity.User;

import java.util.List;
import java.util.Optional;

/**
 * Acces aux donnees des utilisateurs.
 * Aucune methode n'ouvre de transaction : l'EntityManager est fourni par la couche
 * service, qui decide du perimetre transactionnel.
 */
public class UserDAO {

    private static final String CLAUSE_RECHERCHE =
            " WHERE LOWER(u.nom) LIKE :filtre"
                    + " OR LOWER(u.prenom) LIKE :filtre"
                    + " OR LOWER(u.login) LIKE :filtre";

    public User save(EntityManager entityManager, User user) {
        if (user.getId() == null) {
            entityManager.persist(user);
            return user;
        }
        return entityManager.merge(user);
    }

    public Optional<User> findById(EntityManager entityManager, Long id) {
        return Optional.ofNullable(entityManager.find(User.class, id));
    }

    public Optional<User> findByLogin(EntityManager entityManager, String login) {
        return entityManager.createQuery(
                        "SELECT u FROM User u WHERE LOWER(u.login) = LOWER(:login)", User.class)
                .setParameter("login", login)
                .getResultStream()
                .findFirst();
    }

    public boolean loginDejaUtilise(EntityManager entityManager, String login, Long idAExclure) {
        StringBuilder requete = new StringBuilder(
                "SELECT COUNT(u) FROM User u WHERE LOWER(u.login) = LOWER(:login)");
        if (idAExclure != null) {
            requete.append(" AND u.id <> :idAExclure");
        }
        TypedQuery<Long> query = entityManager.createQuery(requete.toString(), Long.class)
                .setParameter("login", login);
        if (idAExclure != null) {
            query.setParameter("idAExclure", idAExclure);
        }
        return query.getSingleResult() > 0;
    }

    /**
     * Liste paginee, filtrable sur le nom, le prenom ou le login.
     * La pagination est faite par la base : setFirstResult + setMaxResults.
     */
    public PageResult<User> search(EntityManager entityManager, String recherche,
                                   int numeroPage, int taillePage) {
        boolean avecFiltre = recherche != null && !recherche.isBlank();
        String clause = avecFiltre ? CLAUSE_RECHERCHE : "";
        String filtre = avecFiltre ? "%" + recherche.trim().toLowerCase() + "%" : null;

        TypedQuery<Long> requeteComptage = entityManager.createQuery(
                "SELECT COUNT(u) FROM User u" + clause, Long.class);
        TypedQuery<User> requetePage = entityManager.createQuery(
                "SELECT u FROM User u" + clause + " ORDER BY u.nom ASC, u.prenom ASC", User.class);
        if (avecFiltre) {
            requeteComptage.setParameter("filtre", filtre);
            requetePage.setParameter("filtre", filtre);
        }

        long total = requeteComptage.getSingleResult();
        List<User> utilisateurs = requetePage
                .setFirstResult(numeroPage * taillePage)
                .setMaxResults(taillePage)
                .getResultList();

        return new PageResult<>(utilisateurs, numeroPage, taillePage, total);
    }

    public long count(EntityManager entityManager) {
        return entityManager.createQuery("SELECT COUNT(u) FROM User u", Long.class)
                .getSingleResult();
    }

    public void delete(EntityManager entityManager, User user) {
        entityManager.remove(entityManager.contains(user) ? user : entityManager.merge(user));
    }
}
