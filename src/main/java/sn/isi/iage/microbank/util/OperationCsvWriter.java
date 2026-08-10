package sn.isi.iage.microbank.util;

import sn.isi.iage.microbank.entity.Operation;

import java.io.PrintWriter;
import java.util.List;

/**
 * Export CSV de l'historique des operations (§21).
 * Separateur point-virgule, comme attendu par Excel en configuration francaise.
 */
public final class OperationCsvWriter {

    public static final String SEPARATEUR = ";";
    private static final String ENTETE =
            "Date;Reference;Type;Sens;Montant;Solde;Compte;Client;Description";

    private OperationCsvWriter() {
    }

    public static void ecrire(PrintWriter redacteur, List<Operation> operations) {
        // BOM UTF-8 : sans lui, Excel affiche mal les accents du fichier.
        redacteur.print('﻿');
        redacteur.println(ENTETE);

        for (Operation operation : operations) {
            redacteur.println(String.join(SEPARATEUR,
                    AmountFormatter.formaterDateHeure(operation.getDateOperation()),
                    echapper(operation.getReference()),
                    operation.getType().name(),
                    operation.getSens().name(),
                    operation.getMontant().toPlainString(),
                    operation.getSoldeApres().toPlainString(),
                    echapper(operation.getCompte().getNumeroCompte()),
                    echapper(operation.getCompte().getClient().getNomComplet()),
                    echapper(operation.getDescription())));
        }
        redacteur.flush();
    }

    /**
     * Neutralise les caracteres qui casseraient le format : guillemets, separateur,
     * retours a la ligne. Le prefixe apostrophe protege aussi contre l'injection de
     * formules dans un tableur.
     */
    private static String echapper(String valeur) {
        if (valeur == null || valeur.isEmpty()) {
            return "";
        }
        String valeurProtegee = valeur;
        if ("=+-@".indexOf(valeurProtegee.charAt(0)) >= 0) {
            valeurProtegee = "'" + valeurProtegee;
        }
        if (valeurProtegee.contains(SEPARATEUR) || valeurProtegee.contains("\"")
                || valeurProtegee.contains("\n") || valeurProtegee.contains("\r")) {
            return "\"" + valeurProtegee.replace("\"", "\"\"").replace("\r", " ")
                    .replace("\n", " ") + "\"";
        }
        return valeurProtegee;
    }
}
