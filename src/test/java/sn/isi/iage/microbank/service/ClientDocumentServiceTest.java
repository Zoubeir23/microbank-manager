package sn.isi.iage.microbank.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sn.isi.iage.microbank.AbstractDatabaseTest;
import sn.isi.iage.microbank.model.Client;
import sn.isi.iage.microbank.model.ClientDocument;
import sn.isi.iage.microbank.exception.BusinessRuleException;
import sn.isi.iage.microbank.exception.ResourceNotFoundException;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Verifie le depot de la piece d'identite d'un client (bonus 1). */
class ClientDocumentServiceTest extends AbstractDatabaseTest {

    private static final byte[] CONTENU = "%PDF-1.4 contenu de test".getBytes(StandardCharsets.UTF_8);

    private ClientDocumentService clientDocumentService;
    private Client client;

    @BeforeEach
    void preparerLeService() {
        clientDocumentService = new ClientDocumentService(entityManagerFactory);
        client = creerClient("GAYE", "Abdoulaye", "PIECE-500");
    }

    @Test
    @DisplayName("un document valide est enregistre et relu a l'identique")
    void enregistrementEtRelecture() {
        clientDocumentService.enregistrer(client.getId(), "piece.pdf", "application/pdf", CONTENU);

        Optional<ClientDocument> document =
                clientDocumentService.consulterParClient(client.getId());

        assertTrue(document.isPresent());
        assertEquals("piece.pdf", document.get().getNomFichier());
        assertEquals("application/pdf", document.get().getTypeContenu());
        assertEquals(CONTENU.length, document.get().getTailleOctets());
        assertArrayEquals(CONTENU, document.get().getContenu());
    }

    @Test
    @DisplayName("un second envoi remplace le document precedent")
    void secondEnvoiRemplaceLePremier() {
        clientDocumentService.enregistrer(client.getId(), "ancienne.pdf", "application/pdf", CONTENU);
        byte[] nouveauContenu = "nouvelle version".getBytes(StandardCharsets.UTF_8);

        clientDocumentService.enregistrer(client.getId(), "nouvelle.png", "image/png", nouveauContenu);

        ClientDocument document = clientDocumentService.consulterParClient(client.getId())
                .orElseThrow();
        assertEquals("nouvelle.png", document.getNomFichier());
        assertArrayEquals(nouveauContenu, document.getContenu());
        assertEquals(1, compterDocuments());
    }

    @Test
    @DisplayName("un format non autorise est refuse")
    void formatRefuse() {
        assertThrows(BusinessRuleException.class, () -> clientDocumentService.enregistrer(
                client.getId(), "virus.exe", "application/x-msdownload", CONTENU));

        assertTrue(clientDocumentService.consulterParClient(client.getId()).isEmpty());
    }

    @Test
    @DisplayName("un fichier trop volumineux est refuse")
    void fichierTropVolumineux() {
        byte[] tropGros = new byte[(int) ClientDocumentService.TAILLE_MAXIMALE_OCTETS + 1];

        assertThrows(BusinessRuleException.class, () -> clientDocumentService.enregistrer(
                client.getId(), "scan.png", "image/png", tropGros));
    }

    @Test
    @DisplayName("un fichier vide est refuse")
    void fichierVide() {
        assertThrows(BusinessRuleException.class, () -> clientDocumentService.enregistrer(
                client.getId(), "vide.pdf", "application/pdf", new byte[0]));
    }

    @Test
    @DisplayName("un client inexistant est refuse")
    void clientInexistant() {
        assertThrows(ResourceNotFoundException.class, () -> clientDocumentService.enregistrer(
                999_999L, "piece.pdf", "application/pdf", CONTENU));
    }

    private long compterDocuments() {
        return lire(entityManager -> entityManager
                .createQuery("SELECT COUNT(d) FROM ClientDocument d", Long.class)
                .getSingleResult());
    }
}
