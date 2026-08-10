package sn.isi.iage.microbank.dao;

import jakarta.persistence.EntityManager;
import sn.isi.iage.microbank.model.Agency;

import java.util.List;
import java.util.Optional;

/** Acces aux donnees des agences (bonus 4). */
public class AgencyDAO {

    public Agency save(EntityManager entityManager, Agency agence) {
        if (agence.getId() == null) {
            entityManager.persist(agence);
            return agence;
        }
        return entityManager.merge(agence);
    }

    public Optional<Agency> findById(EntityManager entityManager, Long id) {
        return Optional.ofNullable(entityManager.find(Agency.class, id));
    }

    public List<Agency> findAllOrdonnees(EntityManager entityManager) {
        return entityManager.createQuery(
                        "SELECT a FROM Agency a ORDER BY a.code ASC", Agency.class)
                .getResultList();
    }

    public long count(EntityManager entityManager) {
        return entityManager.createQuery("SELECT COUNT(a) FROM Agency a", Long.class)
                .getSingleResult();
    }
}
