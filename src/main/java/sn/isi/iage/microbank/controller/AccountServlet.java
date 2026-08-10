package sn.isi.iage.microbank.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sn.isi.iage.microbank.dto.AccountForm;
import sn.isi.iage.microbank.model.Account;
import sn.isi.iage.microbank.enums.StatutCompte;
import sn.isi.iage.microbank.enums.TypeCompte;
import sn.isi.iage.microbank.exception.BusinessRuleException;
import sn.isi.iage.microbank.exception.ResourceNotFoundException;
import sn.isi.iage.microbank.exception.ValidationException;
import sn.isi.iage.microbank.service.AccountService;
import sn.isi.iage.microbank.service.ClientService;
import sn.isi.iage.microbank.util.SessionAttributes;

import java.io.IOException;

/** Gestion des comptes bancaires (§11 a §13). */
@WebServlet(name = "accountServlet", urlPatterns = {"/accounts", "/accounts/*"})
public class AccountServlet extends BaseServlet {

    private static final int NOMBRE_DERNIERES_OPERATIONS = 10;

    private final transient AccountService accountService = new AccountService();
    private final transient ClientService clientService = new ClientService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            switch (action(request)) {
                case "/nouveau" -> afficherFormulaireDOuverture(request, response);
                case "/details" -> afficherDetails(request, response);
                default -> afficherListe(request, response);
            }
        } catch (ResourceNotFoundException compteIntrouvable) {
            definirMessageErreur(request, compteIntrouvable.getMessage());
            rediriger(request, response, "/accounts");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if ("/statut".equals(action(request))) {
            changerStatut(request, response);
            return;
        }
        ouvrirCompte(request, response);
    }

    private void afficherListe(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String recherche = parametre(request, "search");
        request.setAttribute("page", accountService.rechercher(
                recherche, numeroDePageDemande(request), tailleDePageDemandee(request)));
        request.setAttribute("recherche", recherche);
        afficher(request, response, "accounts/list");
    }

    private void afficherFormulaireDOuverture(HttpServletRequest request,
                                              HttpServletResponse response)
            throws ServletException, IOException {
        preparerFormulaire(request, new AccountForm(null, parametre(request, "clientId"),
                null, TypeCompte.COURANT.name(), null, "0"));
        afficher(request, response, "accounts/form");
    }

    private void afficherDetails(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Long identifiant = parametreIdentifiant(request, "id");
        Account compte = accountService.consulter(identifiant);

        request.setAttribute("compte", compte);
        request.setAttribute("dernieresOperations", accountService.listerDernieresOperations(
                identifiant, NOMBRE_DERNIERES_OPERATIONS));
        request.setAttribute("statutsPossibles", StatutCompte.values());
        afficher(request, response, "accounts/details");
    }

    private void ouvrirCompte(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        AccountForm formulaire = new AccountForm(
                null,
                parametre(request, "clientId"),
                parametre(request, "agenceId"),
                parametre(request, "type"),
                null,
                parametre(request, "depotInitial"));
        try {
            Account compte = accountService.ouvrirCompte(formulaire, utilisateurConnecte(request));
            definirMessageSucces(request,
                    "Compte " + compte.getNumeroCompte() + " ouvert avec succes.");
            rediriger(request, response, "/accounts/details?id=" + compte.getId());
        } catch (ValidationException formulaireInvalide) {
            request.setAttribute(SessionAttributes.ERREURS_DE_VALIDATION,
                    formulaireInvalide.getErreursParChamp());
            preparerFormulaire(request, formulaire);
            afficher(request, response, "accounts/form");
        } catch (BusinessRuleException regleViolee) {
            request.setAttribute("erreurMetier", regleViolee.getMessage());
            preparerFormulaire(request, formulaire);
            afficher(request, response, "accounts/form");
        }
    }

    private void changerStatut(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Long identifiant = parametreIdentifiant(request, "id");
        try {
            Account compte = accountService.changerStatut(
                    identifiant, parametre(request, "statut"));
            definirMessageSucces(request, "Statut du compte " + compte.getNumeroCompte()
                    + " change en " + compte.getStatut().getLibelle() + ".");
        } catch (BusinessRuleException regleViolee) {
            definirMessageErreur(request, regleViolee.getMessage());
        }
        rediriger(request, response, "/accounts/details?id=" + identifiant);
    }

    private void preparerFormulaire(HttpServletRequest request, AccountForm formulaire) {
        request.setAttribute("formulaire", formulaire);
        request.setAttribute("clients", clientService.listerTous());
        request.setAttribute("agences", accountService.listerAgences());
        request.setAttribute("typesDeCompte", TypeCompte.values());
    }
}
