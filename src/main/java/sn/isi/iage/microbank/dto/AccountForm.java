package sn.isi.iage.microbank.dto;

/** Donnees brutes du formulaire d'ouverture ou de modification d'un compte. */
public record AccountForm(
        String id,
        String clientId,
        String agenceId,
        String type,
        String statut,
        String depotInitial) {

    public boolean estOuverture() {
        return id == null || id.isBlank();
    }

    // Accesseurs au format JavaBean : le langage d'expression des JSP (EL) reconnait
    // getXxx(), pas les accesseurs de record xxx().

    public String getId() {
        return id;
    }

    public String getClientId() {
        return clientId;
    }

    public String getAgenceId() {
        return agenceId;
    }

    public String getType() {
        return type;
    }

    public String getStatut() {
        return statut;
    }

    public String getDepotInitial() {
        return depotInitial;
    }
}
