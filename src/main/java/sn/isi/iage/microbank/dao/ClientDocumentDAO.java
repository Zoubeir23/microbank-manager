package sn.isi.iage.microbank.dao;

import jakarta.persistence.EntityManager;
import sn.isi.iage.microbank.model.ClientDocument;

import java.util.Optional;

/** Acces a la piece d'identite numerisee d'un client (bonus 1). */
public class ClientDocumentDAO {

    public ClientDocument save(EntityManager entityManager, ClientDocument document) {
        if (document.getId() == null) {
            entityManager.persist(document);
            return document;
        }
        return entityManager.merge(document);
    }

    public Optional<ClientDocument> findByClientId(EntityManager entityManager, Long clientId) {
        return entityManager.createQuery(
                        "SELECT d FROM ClientDocument d WHERE d.client.id = :clientId",
                        ClientDocument.class)
                .setParameter("clientId", clientId)
                .getResultStream()
                .findFirst();
    }
}
