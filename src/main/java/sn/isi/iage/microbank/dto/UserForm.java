package sn.isi.iage.microbank.dto;

/**
 * Donnees brutes du formulaire utilisateur.
 * En modification, un mot de passe laisse vide signifie "ne pas changer".
 */
public record UserForm(
        String id,
        String nom,
        String prenom,
        String login,
        String motDePasse,
        String confirmationMotDePasse,
        String role,
        String statut) {

    public boolean estCreation() {
        return id == null || id.isBlank();
    }

    public boolean demandeChangementDeMotDePasse() {
        return motDePasse != null && !motDePasse.isBlank();
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

    public String getLogin() {
        return login;
    }

    public String getRole() {
        return role;
    }

    public String getStatut() {
        return statut;
    }
}
