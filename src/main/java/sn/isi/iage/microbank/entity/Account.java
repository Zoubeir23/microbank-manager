package sn.isi.iage.microbank.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import sn.isi.iage.microbank.enums.StatutCompte;
import sn.isi.iage.microbank.enums.TypeCompte;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Compte bancaire detenu par un client. */
@Entity
@Table(name = "accounts")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@ToString(exclude = {"client", "operations"})
public class Account extends BaseEntity {

    @Column(name = "numero_compte", nullable = false, unique = true, length = 20)
    private String numeroCompte;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TypeCompte type;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal solde;

    @Column(name = "date_ouverture", nullable = false)
    private LocalDate dateOuverture;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutCompte statut;

    /** Relation Account * --- 1 Client (cote proprietaire de Client 1 --- * Account). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agency_id")
    private Agency agence;

    /** Relation Account 1 --- * Operation. */
    @OneToMany(mappedBy = "compte", fetch = FetchType.LAZY)
    @lombok.Builder.Default
    private List<Operation> operations = new ArrayList<>();

    /**
     * Verrou optimiste : deux operations concurrentes sur le meme compte ne peuvent
     * pas ecraser silencieusement le solde de l'autre.
     */
    @Version
    @Column(name = "version_optimiste")
    private Long versionOptimiste;

    public boolean estActif() {
        return statut != null && statut.accepteOperation();
    }

    public boolean soldeSuffisantPour(BigDecimal montant) {
        return solde.compareTo(montant) >= 0;
    }
}
