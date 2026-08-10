package sn.isi.iage.microbank.exception;

/** Ressource demandee introuvable (client, compte ou utilisateur inexistant). */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
