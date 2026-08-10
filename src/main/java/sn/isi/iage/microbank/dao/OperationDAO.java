package sn.isi.iage.microbank.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import sn.isi.iage.microbank.dto.OperationSearchCriteria;
import sn.isi.iage.microbank.dto.PageResult;
import sn.isi.iage.microbank.model.Operation;
import sn.isi.iage.microbank.enums.SensOperation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Acces a l'historique des operations : filtres combinables, pagination JPA et totaux.
 * Les conditions sont assemblees dynamiquement afin qu'un critere non renseigne
 * n'apparaisse pas du tout dans la requete.
 */
public class OperationDAO {

    public Operation save(EntityManager entityManager, Operation operation) {
        entityManager.persist(operation);
        return operation;
    }

    /** Page d'operations correspondant aux criteres, la plus recente d'abord. */
    public PageResult<Operation> search(EntityManager entityManager,
                                        OperationSearchCriteria criteres,
                                        int numeroPage, int taillePage) {
        Map<String, Object> parametres = new LinkedHashMap<>();
        String clause = construireClauseWhere(criteres, parametres);

        TypedQuery<Long> requeteComptage = entityManager.createQuery(
                "SELECT COUNT(o) FROM Operation o" + clause, Long.class);
        TypedQuery<Operation> requetePage = entityManager.createQuery(
                "SELECT o FROM Operation o JOIN FETCH o.compte compte "
                        + "JOIN FETCH compte.client JOIN FETCH o.user" + clause
                        + " ORDER BY o.dateOperation DESC, o.id DESC", Operation.class);
        appliquerParametres(requeteComptage, parametres);
        appliquerParametres(requetePage, parametres);

        long total = requeteComptage.getSingleResult();
        List<Operation> operations = requetePage
                .setFirstResult(numeroPage * taillePage)
                .setMaxResults(taillePage)
                .getResultList();

        return new PageResult<>(operations, numeroPage, taillePage, total);
    }

    /**
     * Toutes les operations correspondant aux criteres, sans pagination.
     * Reservee aux exports (PDF, CSV) qui doivent restituer l'integralite du filtre.
     */
    public List<Operation> findAll(EntityManager entityManager,
                                   OperationSearchCriteria criteres) {
        Map<String, Object> parametres = new LinkedHashMap<>();
        String clause = construireClauseWhere(criteres, parametres);

        TypedQuery<Operation> requete = entityManager.createQuery(
                "SELECT o FROM Operation o JOIN FETCH o.compte compte "
                        + "JOIN FETCH compte.client JOIN FETCH o.user" + clause
                        + " ORDER BY o.dateOperation ASC, o.id ASC", Operation.class);
        appliquerParametres(requete, parametres);
        return requete.getResultList();
    }

    /** Dernieres operations d'un compte, pour la page de detail du compte. */
    public List<Operation> findDernieresOperations(EntityManager entityManager,
                                                   Long compteId, int limite) {
        return entityManager.createQuery(
                        "SELECT o FROM Operation o WHERE o.compte.id = :compteId "
                                + "ORDER BY o.dateOperation DESC, o.id DESC", Operation.class)
                .setParameter("compteId", compteId)
                .setMaxResults(limite)
                .getResultList();
    }

    /** Total des mouvements d'un sens donne (CREDIT ou DEBIT) pour les criteres fournis. */
    public BigDecimal totalParSens(EntityManager entityManager,
                                   OperationSearchCriteria criteres, SensOperation sens) {
        Map<String, Object> parametres = new LinkedHashMap<>();
        String clause = construireClauseWhere(criteres, parametres);
        String conditionSens = clause.isEmpty() ? " WHERE o.sens = :sens" : " AND o.sens = :sens";

        TypedQuery<BigDecimal> requete = entityManager.createQuery(
                "SELECT COALESCE(SUM(o.montant), 0) FROM Operation o" + clause + conditionSens,
                BigDecimal.class);
        appliquerParametres(requete, parametres);
        requete.setParameter("sens", sens);

        BigDecimal total = requete.getSingleResult();
        return total == null ? BigDecimal.ZERO : total;
    }

    public long compterOperationsDuJour(EntityManager entityManager, LocalDate jour) {
        return entityManager.createQuery(
                        "SELECT COUNT(o) FROM Operation o "
                                + "WHERE o.dateOperation >= :debut AND o.dateOperation < :fin",
                        Long.class)
                .setParameter("debut", jour.atStartOfDay())
                .setParameter("fin", jour.plusDays(1).atStartOfDay())
                .getSingleResult();
    }

    public long count(EntityManager entityManager) {
        return entityManager.createQuery("SELECT COUNT(o) FROM Operation o", Long.class)
                .getSingleResult();
    }

    /** Numero d'ordre de la prochaine operation, base sur le nombre deja enregistre. */
    public long prochainNumeroReference(EntityManager entityManager) {
        return count(entityManager) + 1;
    }

    private String construireClauseWhere(OperationSearchCriteria criteres,
                                         Map<String, Object> parametres) {
        if (criteres == null) {
            return "";
        }
        List<String> conditions = new ArrayList<>();

        if (criteres.compteId() != null) {
            conditions.add("o.compte.id = :compteId");
            parametres.put("compteId", criteres.compteId());
        }
        if (criteres.clientId() != null) {
            conditions.add("o.compte.client.id = :clientId");
            parametres.put("clientId", criteres.clientId());
        }
        if (criteres.type() != null) {
            conditions.add("o.type = :type");
            parametres.put("type", criteres.type());
        }
        if (criteres.dateDebut() != null) {
            conditions.add("o.dateOperation >= :dateDebut");
            parametres.put("dateDebut", criteres.dateDebut().atStartOfDay());
        }
        if (criteres.dateFin() != null) {
            // Borne haute incluse : on prend toute la journee de fin.
            LocalDateTime finDeJournee = LocalDateTime.of(criteres.dateFin(), LocalTime.MAX);
            conditions.add("o.dateOperation <= :dateFin");
            parametres.put("dateFin", finDeJournee);
        }
        if (criteres.montantMinimum() != null) {
            conditions.add("o.montant >= :montantMinimum");
            parametres.put("montantMinimum", criteres.montantMinimum());
        }
        if (criteres.montantMaximum() != null) {
            conditions.add("o.montant <= :montantMaximum");
            parametres.put("montantMaximum", criteres.montantMaximum());
        }

        return conditions.isEmpty() ? "" : " WHERE " + String.join(" AND ", conditions);
    }

    private void appliquerParametres(TypedQuery<?> requete, Map<String, Object> parametres) {
        parametres.forEach(requete::setParameter);
    }
}
