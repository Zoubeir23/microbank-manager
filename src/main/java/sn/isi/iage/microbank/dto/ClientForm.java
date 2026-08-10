package sn.isi.iage.microbank.dto;

/**
 * Donnees brutes du formulaire client, telles que recues du navigateur.
 * On garde des chaines : la conversion et la validation sont faites par le service,
 * ce qui permet de reafficher le formulaire avec les valeurs saisies en cas d'erreur.
 */
public record ClientForm(
        String id,
        String nom,
        String prenom,
        String dateNaissance,
        String telephone,
        String email,
        String adresse,
        String numeroPiece,
        String statut) {

    public boolean estCreation() {
        return id == null || id.isBlank();
    }
}
