package sn.isi.iage.microbank.dto;

import java.math.BigDecimal;

/**
 * Chiffres affiches sur le tableau de bord (§22 et bonus 3).
 * Record immuable : une photographie de l'etat de l'institution a l'instant du calcul.
 */
public record DashboardStatistics(
        long nombreDeClients,
        long nombreDeComptes,
        BigDecimal soldeTotal,
        long nombreOperationsDuJour,
        BigDecimal totalDepotsDuJour,
        BigDecimal totalRetraitsDuJour,
        long nombreDUtilisateurs,
        long nombreDAgences) {

    // Accesseurs au format JavaBean : le langage d'expression des JSP (EL) reconnait
    // getXxx(), pas les accesseurs de record xxx().

    public long getNombreDeClients() {
        return nombreDeClients;
    }

    public long getNombreDeComptes() {
        return nombreDeComptes;
    }

    public BigDecimal getSoldeTotal() {
        return soldeTotal;
    }

    public long getNombreOperationsDuJour() {
        return nombreOperationsDuJour;
    }

    public BigDecimal getTotalDepotsDuJour() {
        return totalDepotsDuJour;
    }

    public BigDecimal getTotalRetraitsDuJour() {
        return totalRetraitsDuJour;
    }

    public long getNombreDUtilisateurs() {
        return nombreDUtilisateurs;
    }

    public long getNombreDAgences() {
        return nombreDAgences;
    }
}
