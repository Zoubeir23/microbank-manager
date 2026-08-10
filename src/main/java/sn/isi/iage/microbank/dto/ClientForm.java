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

    // Accesseurs au format JavaBean : le langage d'expression des JSP (EL) reconnait
    // getXxx(), pas les accesseurs de record xxx().

    public String getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public String getDateNaissance() {
        return dateNaissance;
    }

    public String getTelephone() {
        return telephone;
    }

    public String getEmail() {
        return email;
    }

    public String getAdresse() {
        return adresse;
    }

    public String getNumeroPiece() {
        return numeroPiece;
    }

    public String getStatut() {
        return statut;
    }
}
