package sn.isi.iage.microbank.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import sn.isi.iage.microbank.dao.AccountDAO;
import sn.isi.iage.microbank.dao.ClientDAO;
import sn.isi.iage.microbank.dao.TransactionExecutor;
import sn.isi.iage.microbank.dto.ClientForm;
import sn.isi.iage.microbank.dto.PageResult;
import sn.isi.iage.microbank.entity.Client;
import sn.isi.iage.microbank.enums.Statut;
import sn.isi.iage.microbank.exception.BusinessRuleException;
import sn.isi.iage.microbank.exception.ResourceNotFoundException;
import sn.isi.iage.microbank.util.FormValidator;
import sn.isi.iage.microbank.util.ValueParser;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Regles de gestion des clients : validation, unicite de la piece, suppression protegee. */
public class ClientService {

    private final TransactionExecutor transactionExecutor;
    private final ClientDAO clientDAO = new ClientDAO();
    private final AccountDAO accountDAO = new AccountDAO();

    public ClientService() {
        this.transactionExecutor = new TransactionExecutor();
    }

    public ClientService(EntityManagerFactory entityManagerFactory) {
        this.transactionExecutor = new TransactionExecutor(entityManagerFactory);
    }

    public PageResult<Client> rechercher(String recherche, int numeroPage, int taillePage) {
        return transactionExecutor.executeQuery(entityManager ->
                clientDAO.search(entityManager, recherche, numeroPage, taillePage));
    }

    public List<Client> listerTous() {
        return transactionExecutor.executeQuery(clientDAO::findAllOrdonnes);
    }

    public Client consulter(Long id) {
        return transactionExecutor.executeQuery(entityManager ->
                clientDAO.findById(entityManager, id)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Client introuvable (identifiant " + id + ")")));
    }

    /** Cree ou met a jour un client dans une seule transaction. */
    public Client enregistrer(ClientForm formulaire) {
        return transactionExecutor.executeInTransaction(entityManager -> {
            Long identifiant = ValueParser.versIdentifiant(formulaire.id()).orElse(null);
            valider(entityManager, formulaire, identifiant);

            Client client = identifiant == null
                    ? nouveauClient()
                    : clientDAO.findById(entityManager, identifiant)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Client introuvable (identifiant " + identifiant + ")"));

            appliquerFormulaire(client, formulaire);
            return clientDAO.save(entityManager, client);
        });
    }

    /**
     * Supprime un client. Refuse si le client detient encore des comptes : on ne
     * detruit pas un historique bancaire par effet de bord.
     */
    public void supprimer(Long id) {
        transactionExecutor.executeInTransaction(entityManager -> {
            Client client = clientDAO.findById(entityManager, id)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Client introuvable (identifiant " + id + ")"));

            if (!accountDAO.findByClientId(entityManager, id).isEmpty()) {
                throw new BusinessRuleException(
                        "Impossible de supprimer ce client : il detient encore des comptes.");
            }
            clientDAO.delete(entityManager, client);
            return null;
        });
    }

    private Client nouveauClient() {
        Client client = new Client();
        client.setDateCreation(LocalDate.now());
        client.setStatut(Statut.ACTIF);
        return client;
    }

    private void valider(EntityManager entityManager, ClientForm formulaire, Long identifiant) {
        FormValidator validateur = new FormValidator()
                .exigerObligatoire("nom", formulaire.nom(), "Le nom est obligatoire.")
                .exigerObligatoire("prenom", formulaire.prenom(), "Le prenom est obligatoire.")
                .exigerObligatoire("telephone", formulaire.telephone(),
                        "Le telephone est obligatoire.")
                .exigerTelephoneValide("telephone", formulaire.telephone(),
                        "Le telephone doit contenir entre 6 et 30 chiffres.")
                .exigerObligatoire("numeroPiece", formulaire.numeroPiece(),
                        "Le numero de piece est obligatoire.")
                .exigerEmailValideSiRenseigne("email", formulaire.email(),
                        "L'adresse email n'est pas valide.")
                .exigerLongueurMaximale("adresse", formulaire.adresse(), 255,
                        "L'adresse ne doit pas depasser 255 caracteres.");

        Optional<LocalDate> dateNaissance = ValueParser.versDate(formulaire.dateNaissance());
        if (formulaire.dateNaissance() != null && !formulaire.dateNaissance().isBlank()) {
            validateur.exiger("dateNaissance", dateNaissance.isPresent(),
                    "La date de naissance n'est pas valide.");
            dateNaissance.ifPresent(date -> validateur.exiger("dateNaissance",
                    !date.isAfter(LocalDate.now()),
                    "La date de naissance ne peut pas etre dans le futur."));
        }

        String numeroPiece = ValueParser.nettoyer(formulaire.numeroPiece());
        if (numeroPiece != null) {
            validateur.exiger("numeroPiece",
                    !clientDAO.numeroPieceDejaUtilise(entityManager, numeroPiece, identifiant),
                    "Ce numero de piece est deja enregistre pour un autre client.");
        }

        validateur.lancerSiErreurs();
    }

    private void appliquerFormulaire(Client client, ClientForm formulaire) {
        client.setNom(ValueParser.nettoyer(formulaire.nom()));
        client.setPrenom(ValueParser.nettoyer(formulaire.prenom()));
        client.setTelephone(ValueParser.nettoyer(formulaire.telephone()));
        client.setEmail(ValueParser.nettoyer(formulaire.email()));
        client.setAdresse(ValueParser.nettoyer(formulaire.adresse()));
        client.setNumeroPiece(ValueParser.nettoyer(formulaire.numeroPiece()));
        client.setDateNaissance(ValueParser.versDate(formulaire.dateNaissance()).orElse(null));

        ValueParser.versEnumeration(Statut.class, formulaire.statut())
                .ifPresent(client::setStatut);
        if (client.getStatut() == null) {
            client.setStatut(Statut.ACTIF);
        }
        if (client.getDateCreation() == null) {
            client.setDateCreation(LocalDate.now());
        }
    }
}
