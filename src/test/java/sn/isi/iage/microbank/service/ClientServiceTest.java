package sn.isi.iage.microbank.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sn.isi.iage.microbank.AbstractDatabaseTest;
import sn.isi.iage.microbank.dto.ClientForm;
import sn.isi.iage.microbank.dto.PageResult;
import sn.isi.iage.microbank.entity.Client;
import sn.isi.iage.microbank.enums.StatutCompte;
import sn.isi.iage.microbank.exception.BusinessRuleException;
import sn.isi.iage.microbank.exception.ValidationException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Verifie la validation des formulaires client, la recherche (§9) et la pagination (§10). */
class ClientServiceTest extends AbstractDatabaseTest {

    private ClientService clientService;

    @BeforeEach
    void preparerLeService() {
        clientService = new ClientService(entityManagerFactory);
    }

    private ClientForm formulaireValide(String nom, String prenom, String numeroPiece) {
        return new ClientForm(null, nom, prenom, "1990-05-12", "770000000",
                prenom.toLowerCase() + "@example.com", "Dakar", numeroPiece, "ACTIF");
    }

    @Test
    @DisplayName("un formulaire valide cree le client")
    void creationDUnClientValide() {
        Client client = clientService.enregistrer(
                formulaireValide("GAYE", "Abdoulaye", "PIECE-100"));

        assertEquals("GAYE", client.getNom());
        assertEquals("Abdoulaye GAYE", client.getNomComplet());
        assertEquals(1, clientService.rechercher(null, 0, 10).totalElements());
    }

    @Test
    @DisplayName("les champs obligatoires manquants sont tous signales d'un coup")
    void champsObligatoiresManquants() {
        ClientForm formulaire = new ClientForm(null, "  ", "", null, "", null, null, "", "ACTIF");

        ValidationException erreur = assertThrows(ValidationException.class,
                () -> clientService.enregistrer(formulaire));

        assertTrue(erreur.getErreursParChamp().containsKey("nom"));
        assertTrue(erreur.getErreursParChamp().containsKey("prenom"));
        assertTrue(erreur.getErreursParChamp().containsKey("telephone"));
        assertTrue(erreur.getErreursParChamp().containsKey("numeroPiece"));
    }

    @Test
    @DisplayName("un email mal forme est refuse")
    void emailInvalideRefuse() {
        ClientForm formulaire = new ClientForm(null, "GAYE", "Abdoulaye", null, "770000000",
                "pas-un-email", "Dakar", "PIECE-101", "ACTIF");

        ValidationException erreur = assertThrows(ValidationException.class,
                () -> clientService.enregistrer(formulaire));

        assertTrue(erreur.getErreursParChamp().containsKey("email"));
    }

    @Test
    @DisplayName("un numero de piece deja enregistre est refuse")
    void numeroDePieceEnDoubleRefuse() {
        clientService.enregistrer(formulaireValide("GAYE", "Abdoulaye", "PIECE-102"));

        ValidationException erreur = assertThrows(ValidationException.class,
                () -> clientService.enregistrer(formulaireValide("DIOP", "Awa", "PIECE-102")));

        assertTrue(erreur.getErreursParChamp().containsKey("numeroPiece"));
    }

    @Test
    @DisplayName("la modification conserve le numero de piece du client lui-meme")
    void modificationSansChangementDeNumeroDePiece() {
        Client client = clientService.enregistrer(
                formulaireValide("GAYE", "Abdoulaye", "PIECE-103"));

        ClientForm modification = new ClientForm(String.valueOf(client.getId()), "GAYE",
                "Abdoulaye Junior", "1990-05-12", "771111111", "a@example.com", "Thies",
                "PIECE-103", "ACTIF");
        Client clientModifie = clientService.enregistrer(modification);

        assertEquals("Abdoulaye Junior", clientModifie.getPrenom());
        assertEquals("771111111", clientModifie.getTelephone());
    }

    @Test
    @DisplayName("la recherche porte sur le nom, le prenom, le telephone et le numero de piece")
    void rechercheMultiCritere() {
        clientService.enregistrer(formulaireValide("GAYE", "Abdoulaye", "PIECE-104"));
        clientService.enregistrer(formulaireValide("DIOP", "Awa", "PIECE-105"));

        assertEquals(1, clientService.rechercher("abdou", 0, 10).totalElements());
        assertEquals(1, clientService.rechercher("DIOP", 0, 10).totalElements());
        assertEquals(1, clientService.rechercher("PIECE-105", 0, 10).totalElements());
        assertEquals(2, clientService.rechercher("77", 0, 10).totalElements());
        assertEquals(0, clientService.rechercher("inexistant", 0, 10).totalElements());
    }

    @Test
    @DisplayName("la pagination decoupe les resultats et calcule le nombre de pages")
    void paginationDesResultats() {
        for (int index = 1; index <= 25; index++) {
            clientService.enregistrer(formulaireValide(
                    "CLIENT" + index, "Prenom" + index, "PIECE-2" + index));
        }

        PageResult<Client> premierePage = clientService.rechercher(null, 0, 10);
        PageResult<Client> dernierePage = clientService.rechercher(null, 2, 10);

        assertEquals(25, premierePage.totalElements());
        assertEquals(3, premierePage.getTotalPages());
        assertEquals(10, premierePage.contenu().size());
        assertTrue(premierePage.isPremierePage());
        assertFalse(premierePage.isDernierePage());

        assertEquals(5, dernierePage.contenu().size());
        assertTrue(dernierePage.isDernierePage());
    }

    @Test
    @DisplayName("la page demandee au-dela du dernier resultat est vide, pas en erreur")
    void pageAuDelaDesResultats() {
        clientService.enregistrer(formulaireValide("GAYE", "Abdoulaye", "PIECE-106"));

        PageResult<Client> page = clientService.rechercher(null, 10, 10);

        assertTrue(page.isVide());
        assertEquals(1, page.totalElements());
    }

    @Test
    @DisplayName("supprimer un client qui detient un compte est refuse")
    void suppressionRefuseeSiComptesExistants() {
        Client client = clientService.enregistrer(
                formulaireValide("GAYE", "Abdoulaye", "PIECE-107"));
        creerCompte(client, "100001", new BigDecimal("1000.00"), StatutCompte.ACTIF);

        assertThrows(BusinessRuleException.class, () -> clientService.supprimer(client.getId()));
        assertEquals(1, clientService.rechercher(null, 0, 10).totalElements());
    }

    @Test
    @DisplayName("supprimer un client sans compte fonctionne")
    void suppressionDUnClientSansCompte() {
        Client client = clientService.enregistrer(
                formulaireValide("GAYE", "Abdoulaye", "PIECE-108"));

        clientService.supprimer(client.getId());

        assertEquals(0, clientService.rechercher(null, 0, 10).totalElements());
    }
}
