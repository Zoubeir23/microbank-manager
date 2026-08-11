package sn.isi.iage.microbank.service;

import jakarta.persistence.EntityManagerFactory;
import sn.isi.iage.microbank.dao.ClientDAO;
import sn.isi.iage.microbank.dao.ClientDocumentDAO;
import sn.isi.iage.microbank.dao.TransactionExecutor;
import sn.isi.iage.microbank.model.Client;
import sn.isi.iage.microbank.model.ClientDocument;
import sn.isi.iage.microbank.exception.BusinessRuleException;
import sn.isi.iage.microbank.exception.ResourceNotFoundException;

import java.util.Optional;
import java.util.Set;

/**
 * Depot de la copie de la piece d'identite d'un client (bonus 1).
 * Un client ne detient qu'un seul document : un nouvel envoi remplace le precedent.
 */
public class ClientDocumentService {

    public static final long TAILLE_MAXIMALE_OCTETS = 2L * 1024 * 1024;
    private static final Set<String> TYPES_AUTORISES =
            Set.of("image/jpeg", "image/png", "application/pdf");

    private final TransactionExecutor transactionExecutor;
    private final ClientDocumentDAO clientDocumentDAO = new ClientDocumentDAO();
    private final ClientDAO clientDAO = new ClientDAO();

    public ClientDocumentService() {
        this.transactionExecutor = new TransactionExecutor();
    }

    public ClientDocumentService(EntityManagerFactory entityManagerFactory) {
        this.transactionExecutor = new TransactionExecutor(entityManagerFactory);
    }

    public ClientDocument enregistrer(Long clientId, String nomFichier, String typeContenu,
                                      byte[] contenu) {
        validerFichier(nomFichier, typeContenu, contenu);

        return transactionExecutor.executeInTransaction(entityManager -> {
            Client client = clientDAO.findById(entityManager, clientId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Client introuvable (identifiant " + clientId + ")"));

            ClientDocument document = clientDocumentDAO
                    .findByClientId(entityManager, clientId)
                    .orElseGet(ClientDocument::new);

            document.setClient(client);
            document.setNomFichier(nomFichier);
            document.setTypeContenu(typeContenu);
            document.setTailleOctets(contenu.length);
            document.setContenu(contenu);

            return clientDocumentDAO.save(entityManager, document);
        });
    }

    public Optional<ClientDocument> consulterParClient(Long clientId) {
        return transactionExecutor.executeQuery(entityManager ->
                clientDocumentDAO.findByClientId(entityManager, clientId));
    }

    /**
     * Verifie un fichier avant tout envoi en base.
     * Publique pour que {@code ClientServlet} puisse valider la piece jointe optionnelle
     * du formulaire client avant meme de savoir si le client sera cree ou modifie.
     */
    public void validerFichier(String nomFichier, String typeContenu, byte[] contenu) {
        if (contenu == null || contenu.length == 0) {
            throw new BusinessRuleException("Aucun fichier n'a ete envoye.");
        }
        if (contenu.length > TAILLE_MAXIMALE_OCTETS) {
            throw new BusinessRuleException("Le fichier depasse la taille maximale de 2 Mo.");
        }
        if (typeContenu == null || !TYPES_AUTORISES.contains(typeContenu.toLowerCase())) {
            throw new BusinessRuleException(
                    "Format non accepte. Formats autorises : JPEG, PNG ou PDF.");
        }
        if (nomFichier == null || nomFichier.isBlank()) {
            throw new BusinessRuleException("Le nom du fichier est manquant.");
        }
    }
}
