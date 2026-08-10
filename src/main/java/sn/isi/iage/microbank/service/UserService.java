package sn.isi.iage.microbank.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import sn.isi.iage.microbank.dao.TransactionExecutor;
import sn.isi.iage.microbank.dao.UserDAO;
import sn.isi.iage.microbank.dto.PageResult;
import sn.isi.iage.microbank.dto.UserForm;
import sn.isi.iage.microbank.model.User;
import sn.isi.iage.microbank.enums.Role;
import sn.isi.iage.microbank.enums.Statut;
import sn.isi.iage.microbank.exception.BusinessRuleException;
import sn.isi.iage.microbank.exception.ResourceNotFoundException;
import sn.isi.iage.microbank.util.FormValidator;
import sn.isi.iage.microbank.util.PasswordHasher;
import sn.isi.iage.microbank.util.ValueParser;

/** Gestion des comptes utilisateurs, reservee aux administrateurs. */
public class UserService {

    private static final int LONGUEUR_MINIMALE_MOT_DE_PASSE = 6;

    private final TransactionExecutor transactionExecutor;
    private final UserDAO userDAO = new UserDAO();

    public UserService() {
        this.transactionExecutor = new TransactionExecutor();
    }

    public UserService(EntityManagerFactory entityManagerFactory) {
        this.transactionExecutor = new TransactionExecutor(entityManagerFactory);
    }

    public PageResult<User> rechercher(String recherche, int numeroPage, int taillePage) {
        return transactionExecutor.executeQuery(entityManager ->
                userDAO.search(entityManager, recherche, numeroPage, taillePage));
    }

    public User consulter(Long id) {
        return transactionExecutor.executeQuery(entityManager ->
                userDAO.findById(entityManager, id)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Utilisateur introuvable (identifiant " + id + ")")));
    }

    public User enregistrer(UserForm formulaire) {
        return transactionExecutor.executeInTransaction(entityManager -> {
            Long identifiant = ValueParser.versIdentifiant(formulaire.id()).orElse(null);
            valider(entityManager, formulaire, identifiant);

            User utilisateur = identifiant == null
                    ? new User()
                    : userDAO.findById(entityManager, identifiant)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Utilisateur introuvable (identifiant " + identifiant + ")"));

            utilisateur.setNom(ValueParser.nettoyer(formulaire.nom()));
            utilisateur.setPrenom(ValueParser.nettoyer(formulaire.prenom()));
            utilisateur.setLogin(ValueParser.nettoyer(formulaire.login()));
            utilisateur.setRole(ValueParser.versEnumeration(Role.class, formulaire.role())
                    .orElse(Role.AGENT));
            utilisateur.setStatut(ValueParser.versEnumeration(Statut.class, formulaire.statut())
                    .orElse(Statut.ACTIF));

            if (formulaire.demandeChangementDeMotDePasse()) {
                utilisateur.setMotDePasse(PasswordHasher.hacher(formulaire.motDePasse()));
            }
            return userDAO.save(entityManager, utilisateur);
        });
    }

    /**
     * Active ou desactive un utilisateur.
     *
     * @param administrateurConnecte utilisateur en session, qui ne peut pas se desactiver lui-meme
     */
    public User basculerStatut(Long utilisateurId, User administrateurConnecte) {
        return transactionExecutor.executeInTransaction(entityManager -> {
            if (administrateurConnecte != null
                    && utilisateurId.equals(administrateurConnecte.getId())) {
                throw new BusinessRuleException(
                        "Vous ne pouvez pas desactiver votre propre compte.");
            }

            User utilisateur = userDAO.findById(entityManager, utilisateurId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Utilisateur introuvable (identifiant " + utilisateurId + ")"));

            utilisateur.setStatut(utilisateur.estActif() ? Statut.INACTIF : Statut.ACTIF);
            return userDAO.save(entityManager, utilisateur);
        });
    }

    private void valider(EntityManager entityManager, UserForm formulaire, Long identifiant) {
        FormValidator validateur = new FormValidator()
                .exigerObligatoire("nom", formulaire.nom(), "Le nom est obligatoire.")
                .exigerObligatoire("prenom", formulaire.prenom(), "Le prenom est obligatoire.")
                .exigerObligatoire("login", formulaire.login(), "Le login est obligatoire.");

        String login = ValueParser.nettoyer(formulaire.login());
        if (login != null) {
            validateur.exiger("login",
                    !userDAO.loginDejaUtilise(entityManager, login, identifiant),
                    "Ce login est deja utilise.");
        }

        boolean creation = identifiant == null;
        if (creation) {
            validateur.exigerObligatoire("motDePasse", formulaire.motDePasse(),
                    "Le mot de passe est obligatoire.");
        }
        if (formulaire.demandeChangementDeMotDePasse()) {
            validateur.exiger("motDePasse",
                    formulaire.motDePasse().length() >= LONGUEUR_MINIMALE_MOT_DE_PASSE,
                    "Le mot de passe doit contenir au moins "
                            + LONGUEUR_MINIMALE_MOT_DE_PASSE + " caracteres.");
            validateur.exiger("confirmationMotDePasse",
                    formulaire.motDePasse().equals(formulaire.confirmationMotDePasse()),
                    "La confirmation ne correspond pas au mot de passe.");
        }

        validateur.lancerSiErreurs();
    }
}
