package sn.isi.iage.microbank.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sn.isi.iage.microbank.dto.OperationSearchCriteria;
import sn.isi.iage.microbank.entity.Operation;
import sn.isi.iage.microbank.enums.TypeOperation;
import sn.isi.iage.microbank.exception.BusinessRuleException;
import sn.isi.iage.microbank.exception.ResourceNotFoundException;
import sn.isi.iage.microbank.service.AccountService;
import sn.isi.iage.microbank.service.OperationService;
import sn.isi.iage.microbank.util.ValueParser;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * Operations bancaires et historique (§14, §17, §18, §19).
 * <p>
 * URL servies : /operations (historique filtre et pagine), /operations/deposit,
 * /operations/withdraw et /operations/transfer.
 */
@WebServlet(name = "operationServlet", urlPatterns = {"/operations", "/operations/*"})
public class OperationServlet extends BaseServlet {

    private final transient OperationService operationService = new OperationService();
    private final transient AccountService accountService = new AccountService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        switch (action(request)) {
            case "/deposit" -> afficherFormulaire(request, response, "deposit");
            case "/withdraw" -> afficherFormulaire(request, response, "withdraw");
            case "/transfer" -> afficherFormulaire(request, response, "transfer");
            default -> afficherHistorique(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = action(request);
        try {
            Operation operation = switch (action) {
                case "/deposit" -> operationService.deposer(
                        parametreIdentifiant(request, "compteId"),
                        montantSaisi(request),
                        parametre(request, "description"),
                        utilisateurConnecte(request));
                case "/withdraw" -> operationService.retirer(
                        parametreIdentifiant(request, "compteId"),
                        montantSaisi(request),
                        parametre(request, "description"),
                        utilisateurConnecte(request));
                case "/transfer" -> operationService.virer(
                        parametreIdentifiant(request, "compteSourceId"),
                        parametreIdentifiant(request, "compteDestinationId"),
                        montantSaisi(request),
                        parametre(request, "description"),
                        utilisateurConnecte(request));
                default -> throw new BusinessRuleException("Operation inconnue.");
            };

            definirMessageSucces(request, messageDeSucces(operation));
            rediriger(request, response,
                    "/operations?accountId=" + operation.getCompte().getId());

        } catch (BusinessRuleException | ResourceNotFoundException operationRefusee) {
            // La transaction a ete annulee : on reaffiche le formulaire avec l'erreur
            // et les valeurs saisies.
            request.setAttribute("erreurMetier", operationRefusee.getMessage());
            afficherFormulaire(request, response, action.substring(1));
        }
    }

    private void afficherHistorique(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        OperationSearchCriteria criteres = OperationCriteriaReader.lire(request);

        request.setAttribute("page", operationService.rechercher(
                criteres, numeroDePageDemande(request), tailleDePageDemandee(request)));
        request.setAttribute("criteres", criteres);
        request.setAttribute("parametresDuFiltre",
                OperationCriteriaReader.versParametresDUrl(criteres));
        request.setAttribute("totalDesDepots", operationService.totalDesDepots(criteres));
        request.setAttribute("totalDesRetraits", operationService.totalDesRetraits(criteres));
        request.setAttribute("typesDOperation", TypeOperation.values());

        if (criteres.compteId() != null) {
            request.setAttribute("compte", accountService.consulter(criteres.compteId()));
        }
        afficher(request, response, "operations/list");
    }

    private void afficherFormulaire(HttpServletRequest request, HttpServletResponse response,
                                    String vue) throws ServletException, IOException {
        request.setAttribute("comptes", accountService.listerComptesActifs());
        request.setAttribute("compteSelectionne", parametreIdentifiant(request, "compteId"));
        afficher(request, response, "operations/" + vue);
    }

    private BigDecimal montantSaisi(HttpServletRequest request) {
        return ValueParser.versMontant(request.getParameter("montant")).orElse(null);
    }

    private String messageDeSucces(Operation operation) {
        return switch (operation.getType()) {
            case DEPOT -> "Depot de " + operation.getMontant() + " FCFA enregistre (reference "
                    + operation.getReference() + ").";
            case RETRAIT -> "Retrait de " + operation.getMontant() + " FCFA enregistre (reference "
                    + operation.getReference() + ").";
            case VIREMENT -> "Virement de " + operation.getMontant() + " FCFA vers le compte "
                    + operation.getCompteContrepartie() + " effectue (reference "
                    + operation.getReference() + ").";
        };
    }
}
