package sn.isi.iage.microbank.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sn.isi.iage.microbank.dto.UserForm;
import sn.isi.iage.microbank.entity.User;
import sn.isi.iage.microbank.enums.Role;
import sn.isi.iage.microbank.enums.Statut;
import sn.isi.iage.microbank.exception.BusinessRuleException;
import sn.isi.iage.microbank.exception.ResourceNotFoundException;
import sn.isi.iage.microbank.exception.ValidationException;
import sn.isi.iage.microbank.service.UserService;
import sn.isi.iage.microbank.util.SessionAttributes;

import java.io.IOException;

/**
 * Gestion des utilisateurs, reservee aux administrateurs (§3 et §5).
 * L'acces est verrouille par {@code AdminAuthorizationFilter}.
 */
@WebServlet(name = "userServlet", urlPatterns = {"/users", "/users/*"})
public class UserServlet extends BaseServlet {

    private final transient UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            switch (action(request)) {
                case "/nouveau" -> afficherFormulaire(request, response,
                        new UserForm(null, null, null, null, null, null,
                                Role.AGENT.name(), Statut.ACTIF.name()));
                case "/edit" -> afficherFormulaire(request, response,
                        versFormulaire(userService.consulter(
                                parametreIdentifiant(request, "id"))));
                default -> afficherListe(request, response);
            }
        } catch (ResourceNotFoundException utilisateurIntrouvable) {
            definirMessageErreur(request, utilisateurIntrouvable.getMessage());
            rediriger(request, response, "/users");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if ("/toggle".equals(action(request))) {
            basculerStatut(request, response);
            return;
        }
        enregistrer(request, response);
    }

    private void afficherListe(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String recherche = parametre(request, "search");
        request.setAttribute("page", userService.rechercher(
                recherche, numeroDePageDemande(request), tailleDePageDemandee(request)));
        request.setAttribute("recherche", recherche);
        afficher(request, response, "users/list");
    }

    private void afficherFormulaire(HttpServletRequest request, HttpServletResponse response,
                                    UserForm formulaire) throws ServletException, IOException {
        request.setAttribute("formulaire", formulaire);
        request.setAttribute("roles", Role.values());
        request.setAttribute("statuts", Statut.values());
        afficher(request, response, "users/form");
    }

    private void enregistrer(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        UserForm formulaire = new UserForm(
                parametre(request, "id"),
                parametre(request, "nom"),
                parametre(request, "prenom"),
                parametre(request, "login"),
                request.getParameter("motDePasse"),
                request.getParameter("confirmationMotDePasse"),
                parametre(request, "role"),
                parametre(request, "statut"));
        try {
            userService.enregistrer(formulaire);
            definirMessageSucces(request, formulaire.estCreation()
                    ? "Utilisateur cree avec succes."
                    : "Utilisateur mis a jour avec succes.");
            rediriger(request, response, "/users");
        } catch (ValidationException formulaireInvalide) {
            request.setAttribute(SessionAttributes.ERREURS_DE_VALIDATION,
                    formulaireInvalide.getErreursParChamp());
            afficherFormulaire(request, response, formulaire);
        }
    }

    private void basculerStatut(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            User utilisateur = userService.basculerStatut(
                    parametreIdentifiant(request, "id"), utilisateurConnecte(request));
            definirMessageSucces(request, "Utilisateur " + utilisateur.getLogin() + " "
                    + (utilisateur.estActif() ? "active." : "desactive."));
        } catch (BusinessRuleException | ResourceNotFoundException basculeRefusee) {
            definirMessageErreur(request, basculeRefusee.getMessage());
        }
        rediriger(request, response, "/users");
    }

    private UserForm versFormulaire(User utilisateur) {
        // Le mot de passe n'est jamais renvoye vers la vue.
        return new UserForm(
                String.valueOf(utilisateur.getId()),
                utilisateur.getNom(),
                utilisateur.getPrenom(),
                utilisateur.getLogin(),
                null,
                null,
                utilisateur.getRole().name(),
                utilisateur.getStatut().name());
    }
}
