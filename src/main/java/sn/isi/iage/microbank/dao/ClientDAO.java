package sn.isi.iage.microbank.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import sn.isi.iage.microbank.dto.PageResult;
import sn.isi.iage.microbank.entity.Client;

import java.util.List;
import java.util.Optional;

/** Acces aux donnees des clients : recherche multi-critere et pagination JPA. */
public class ClientDAO {

    /** Recherche §9 du cahier des charges : nom, prenom, telephone ou numero de piece. */
    private static final String CLAUSE_RECHERCHE =
            " WHERE LOWER(c.nom) LIKE :filtre"
                    + " OR LOWER(c.prenom) LIKE :filtre"
                    + " OR LOWER(c.telephone) LIKE :filtre"
                    + " OR LOWER(c.numeroPiece) LIKE :filtre";

    public Client save(EntityManager entityManager, Client client) {
        if (client.getId() == null) {
            entityManager.persist(client);
            return client;
        }
        return entityManager.merge(client);
    }

    public Optional<Client> findById(EntityManager entityManager, Long id) {
        return Optional.ofNullable(entityManager.find(Client.class, id));
    }

    public Optional<Client> findByNumeroPiece(EntityManager entityManager, String numeroPiece) {
        return entityManager.createQuery(
                        "SELECT c FROM Client c WHERE LOWER(c.numeroPiece) = LOWER(:numeroPiece)",
                        Client.class)
                .setParameter("numeroPiece", numeroPiece)
                .getResultStream()
                .findFirst();
    }

    public boolean numeroPieceDejaUtilise(EntityManager entityManager,
                                          String numeroPiece, Long idAExclure) {
        StringBuilder requete = new StringBuilder(
                "SELECT COUNT(c) FROM Client c WHERE LOWER(c.numeroPiece) = LOWER(:numeroPiece)");
        if (idAExclure != null) {
            requete.append(" AND c.id <> :idAExclure");
        }
        TypedQuery<Long> query = entityManager.createQuery(requete.toString(), Long.class)
                .setParameter("numeroPiece", numeroPiece);
        if (idAExclure != null) {
            query.setParameter("idAExclure", idAExclure);
        }
        return query.getSingleResult() > 0;
    }

    /**
     * Page de clients correspondant a la recherche.
     * Le decoupage est delegue a la base via setFirstResult / setMaxResults :
     * on ne charge jamais toute la table pour paginer ensuite en Java.
     */
    public PageResult<Client> search(EntityManager entityManager, String recherche,
                                     int numeroPage, int taillePage) {
        boolean avecFiltre = recherche != null && !recherche.isBlank();
        String clause = avecFiltre ? CLAUSE_RECHERCHE : "";
        String filtre = avecFiltre ? "%" + recherche.trim().toLowerCase() + "%" : null;

        TypedQuery<Long> requeteComptage = entityManager.createQuery(
                "SELECT COUNT(c) FROM Client c" + clause, Long.class);
        TypedQuery<Client> requetePage = entityManager.createQuery(
                "SELECT c FROM Client c" + clause + " ORDER BY c.nom ASC, c.prenom ASC",
                Client.class);
        if (avecFiltre) {
            requeteComptage.setParameter("filtre", filtre);
            requetePage.setParameter("filtre", filtre);
        }

        long total = requeteComptage.getSingleResult();
        List<Client> clients = requetePage
                .setFirstResult(numeroPage * taillePage)
                .setMaxResults(taillePage)
                .getResultList();

        return new PageResult<>(clients, numeroPage, taillePage, total);
    }

    /** Liste complete, utilisee pour alimenter les listes deroulantes des formulaires. */
    public List<Client> findAllOrdonnes(EntityManager entityManager) {
        return entityManager.createQuery(
                        "SELECT c FROM Client c ORDER BY c.nom ASC, c.prenom ASC", Client.class)
                .getResultList();
    }

    public long count(EntityManager entityManager) {
        return entityManager.createQuery("SELECT COUNT(c) FROM Client c", Long.class)
                .getSingleResult();
    }

    public void delete(EntityManager entityManager, Client client) {
        entityManager.remove(entityManager.contains(client) ? client : entityManager.merge(client));
    }
}
