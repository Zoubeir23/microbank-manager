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
}
