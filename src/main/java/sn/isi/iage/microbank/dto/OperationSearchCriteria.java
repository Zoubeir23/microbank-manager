package sn.isi.iage.microbank.dto;

import sn.isi.iage.microbank.enums.TypeOperation;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Criteres de filtrage de l'historique des operations (§18 et bonus 2).
 * Tous les champs sont facultatifs : un champ null signifie "pas de filtre".
 * Record : les criteres sont immuables, on en cree un nouveau plutot que de modifier
 * celui recu de la requete HTTP.
 */
public record OperationSearchCriteria(
        Long compteId,
        Long clientId,
        TypeOperation type,
        LocalDate dateDebut,
        LocalDate dateFin,
        BigDecimal montantMinimum,
        BigDecimal montantMaximum) {

    public static OperationSearchCriteria vide() {
        return new OperationSearchCriteria(null, null, null, null, null, null, null);
    }

    public static OperationSearchCriteria pourCompte(Long compteId) {
        return new OperationSearchCriteria(compteId, null, null, null, null, null, null);
    }

    public OperationSearchCriteria avecPeriode(LocalDate debut, LocalDate fin) {
        return new OperationSearchCriteria(compteId, clientId, type, debut, fin,
                montantMinimum, montantMaximum);
    }

    public boolean aUnePeriode() {
        return dateDebut != null || dateFin != null;
    }

    // Accesseurs au format JavaBean : le langage d'expression des JSP (EL) reconnait
    // getXxx(), pas les accesseurs de record xxx().

    public Long getCompteId() {
        return compteId;
    }

    public Long getClientId() {
        return clientId;
    }

    public TypeOperation getType() {
        return type;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public BigDecimal getMontantMinimum() {
        return montantMinimum;
    }

    public BigDecimal getMontantMaximum() {
        return montantMaximum;
    }
}
