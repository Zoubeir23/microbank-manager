package sn.isi.iage.microbank.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sn.isi.iage.microbank.AbstractDatabaseTest;
import sn.isi.iage.microbank.dto.UserForm;
import sn.isi.iage.microbank.model.User;
import sn.isi.iage.microbank.enums.Role;
import sn.isi.iage.microbank.enums.Statut;
import sn.isi.iage.microbank.exception.BusinessRuleException;
import sn.isi.iage.microbank.exception.ValidationException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Verifie l'authentification (§5) et la gestion des utilisateurs par l'administrateur. */
class AuthenticationServiceTest extends AbstractDatabaseTest {

    private AuthenticationService authenticationService;
    private UserService userService;

    @BeforeEach
    void preparerLesServices() {
        authenticationService = new AuthenticationService(entityManagerFactory);
        userService = new UserService(entityManagerFactory);
    }

    private UserForm formulaireAgent(String login, String motDePasse) {
        return new UserForm(null, "DIOP", "Awa", login, motDePasse, motDePasse,
                Role.AGENT.name(), Statut.ACTIF.name());
    }

    @Test
    @DisplayName("un login et un mot de passe corrects ouvrent la session")
    void authentificationReussie() {
        userService.enregistrer(formulaireAgent("awa", "agent123"));

        Optional<User> utilisateur = authenticationService.authentifier("awa", "agent123");

        assertTrue(utilisateur.isPresent());
        assertEquals(Role.AGENT, utilisateur.get().getRole());
    }

    @Test
    @DisplayName("le login est insensible a la casse, pas le mot de passe")
    void loginInsensibleALaCasse() {
        userService.enregistrer(formulaireAgent("awa", "agent123"));

        assertTrue(authenticationService.authentifier("AWA", "agent123").isPresent());
        assertTrue(authenticationService.authentifier("awa", "AGENT123").isEmpty());
    }

    @Test
    @DisplayName("un mot de passe errone ou un login inconnu est rejete sans distinction")
    void authentificationEchouee() {
        userService.enregistrer(formulaireAgent("awa", "agent123"));

        assertTrue(authenticationService.authentifier("awa", "mauvais").isEmpty());
        assertTrue(authenticationService.authentifier("inconnu", "agent123").isEmpty());
        assertTrue(authenticationService.authentifier(null, null).isEmpty());
        assertTrue(authenticationService.authentifier("", "").isEmpty());
    }

    @Test
    @DisplayName("un utilisateur desactive ne peut plus se connecter")
    void utilisateurDesactiveRejete() {
        User agent = userService.enregistrer(formulaireAgent("awa", "agent123"));
        userService.basculerStatut(agent.getId(), null);

        assertThrows(BusinessRuleException.class,
                () -> authenticationService.authentifier("awa", "agent123"));
    }

    @Test
    @DisplayName("le mot de passe n'est jamais stocke en clair")
    void motDePasseJamaisEnClair() {
        User agent = userService.enregistrer(formulaireAgent("awa", "agent123"));

        assertFalse(agent.getMotDePasse().contains("agent123"));
        assertTrue(agent.getMotDePasse().startsWith("pbkdf2_sha256$"));
        assertFalse(agent.toString().contains("agent123"));
    }

    @Test
    @DisplayName("un login deja utilise est refuse")
    void loginEnDoubleRefuse() {
        userService.enregistrer(formulaireAgent("awa", "agent123"));

        ValidationException erreur = assertThrows(ValidationException.class,
                () -> userService.enregistrer(formulaireAgent("awa", "autre123")));

        assertTrue(erreur.getErreursParChamp().containsKey("login"));
    }

    @Test
    @DisplayName("un mot de passe trop court ou mal confirme est refuse")
    void motDePasseFaibleOuMalConfirme() {
        assertThrows(ValidationException.class,
                () -> userService.enregistrer(formulaireAgent("awa", "123")));

        UserForm confirmationDifferente = new UserForm(null, "DIOP", "Awa", "awa",
                "agent123", "agent999", Role.AGENT.name(), Statut.ACTIF.name());
        ValidationException erreur = assertThrows(ValidationException.class,
                () -> userService.enregistrer(confirmationDifferente));
        assertTrue(erreur.getErreursParChamp().containsKey("confirmationMotDePasse"));
    }

    @Test
    @DisplayName("en modification, un mot de passe vide laisse l'ancien inchange")
    void modificationSansChangementDeMotDePasse() {
        User agent = userService.enregistrer(formulaireAgent("awa", "agent123"));

        UserForm modification = new UserForm(String.valueOf(agent.getId()), "DIOP", "Awa Sophie",
                "awa", "", "", Role.ADMIN.name(), Statut.ACTIF.name());
        User agentModifie = userService.enregistrer(modification);

        assertEquals("Awa Sophie", agentModifie.getPrenom());
        assertEquals(Role.ADMIN, agentModifie.getRole());
        assertTrue(authenticationService.authentifier("awa", "agent123").isPresent());
    }

    @Test
    @DisplayName("un administrateur ne peut pas desactiver son propre compte")
    void administrateurNePeutPasSeDesactiver() {
        User administrateur = userService.enregistrer(new UserForm(null, "GAYE", "Abdoulaye",
                "admin", "admin123", "admin123", Role.ADMIN.name(), Statut.ACTIF.name()));

        assertThrows(BusinessRuleException.class,
                () -> userService.basculerStatut(administrateur.getId(), administrateur));
    }

    @Test
    @DisplayName("la bascule de statut active puis desactive")
    void basculeDuStatut() {
        User agent = userService.enregistrer(formulaireAgent("awa", "agent123"));

        assertEquals(Statut.INACTIF, userService.basculerStatut(agent.getId(), null).getStatut());
        assertEquals(Statut.ACTIF, userService.basculerStatut(agent.getId(), null).getStatut());
    }
}
