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
}
