package sn.isi.iage.microbank.service;

import jakarta.persistence.EntityManagerFactory;
import sn.isi.iage.microbank.dao.TransactionExecutor;
import sn.isi.iage.microbank.dao.UserDAO;
import sn.isi.iage.microbank.entity.User;
import sn.isi.iage.microbank.exception.BusinessRuleException;
import sn.isi.iage.microbank.util.PasswordHasher;

import java.util.Optional;

/**
 * Authentification des agents et administrateurs.
 * Le service ne connait pas HttpSession : c'est la servlet qui depose l'utilisateur
 * en session une fois l'identification reussie.
 */
public class AuthenticationService {

    private final TransactionExecutor transactionExecutor;
    private final UserDAO userDAO = new UserDAO();

    public AuthenticationService() {
        this.transactionExecutor = new TransactionExecutor();
    }

    public AuthenticationService(EntityManagerFactory entityManagerFactory) {
        this.transactionExecutor = new TransactionExecutor(entityManagerFactory);
    }

    /**
     * Verifie un couple login / mot de passe.
     *
     * @return l'utilisateur si l'identification reussit, {@code Optional.empty()} sinon
     * @throws BusinessRuleException si le compte existe mais a ete desactive
     */
    public Optional<User> authentifier(String login, String motDePasse) {
        if (login == null || login.isBlank() || motDePasse == null || motDePasse.isBlank()) {
            return Optional.empty();
        }

        Optional<User> utilisateurTrouve = transactionExecutor.executeQuery(
                entityManager -> userDAO.findByLogin(entityManager, login.trim()));

        if (utilisateurTrouve.isEmpty()) {
            return Optional.empty();
        }

        User utilisateur = utilisateurTrouve.get();
        if (!PasswordHasher.correspond(motDePasse, utilisateur.getMotDePasse())) {
            return Optional.empty();
        }
        if (!utilisateur.estActif()) {
            throw new BusinessRuleException(
                    "Ce compte utilisateur est desactive. Contactez un administrateur.");
        }
        return Optional.of(utilisateur);
    }
}
