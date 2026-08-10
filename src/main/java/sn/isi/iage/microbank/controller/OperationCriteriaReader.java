package sn.isi.iage.microbank.controller;

import jakarta.servlet.http.HttpServletRequest;
import sn.isi.iage.microbank.dto.OperationSearchCriteria;
import sn.isi.iage.microbank.enums.TypeOperation;
import sn.isi.iage.microbank.util.ValueParser;

/**
 * Lit les criteres de filtrage de l'historique depuis la requete HTTP.
 * Partage par l'ecran d'historique, l'export CSV et le releve PDF pour que les trois
 * appliquent exactement les memes filtres.
 */
final class OperationCriteriaReader {

    private OperationCriteriaReader() {
    }

    static OperationSearchCriteria lire(HttpServletRequest request) {
        return new OperationSearchCriteria(
                ValueParser.versIdentifiant(request.getParameter("accountId")).orElse(null),
                ValueParser.versIdentifiant(request.getParameter("clientId")).orElse(null),
                ValueParser.versEnumeration(TypeOperation.class, request.getParameter("type"))
                        .orElse(null),
                ValueParser.versDate(request.getParameter("dateDebut")).orElse(null),
                ValueParser.versDate(request.getParameter("dateFin")).orElse(null),
                ValueParser.versMontant(request.getParameter("montantMinimum")).orElse(null),
                ValueParser.versMontant(request.getParameter("montantMaximum")).orElse(null));
    }

    /**
     * Reconstitue la chaine de parametres correspondant aux criteres, pour que les
     * liens de pagination et les boutons d'export conservent le filtre affiche.
     */
    static String versParametresDUrl(OperationSearchCriteria criteres) {
        StringBuilder parametres = new StringBuilder();
        ajouter(parametres, "accountId", criteres.compteId());
        ajouter(parametres, "clientId", criteres.clientId());
        ajouter(parametres, "type", criteres.type());
        ajouter(parametres, "dateDebut", criteres.dateDebut());
        ajouter(parametres, "dateFin", criteres.dateFin());
        ajouter(parametres, "montantMinimum", criteres.montantMinimum());
        ajouter(parametres, "montantMaximum", criteres.montantMaximum());
        return parametres.toString();
    }

    private static void ajouter(StringBuilder parametres, String nom, Object valeur) {
        if (valeur != null) {
            parametres.append('&').append(nom).append('=').append(valeur);
        }
    }
}
