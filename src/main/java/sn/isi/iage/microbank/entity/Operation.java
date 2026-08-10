package sn.isi.iage.microbank.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import sn.isi.iage.microbank.enums.SensOperation;
import sn.isi.iage.microbank.enums.TypeOperation;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Ligne d'historique bancaire, immuable une fois enregistree.
 * Un virement produit deux lignes partageant la meme {@code reference} :
 * un DEBIT sur le compte source et un CREDIT sur le compte destination.
 */
@Entity
@Table(name = "operations")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@ToString(exclude = {"compte", "user"})
public class Operation extends BaseEntity {

    @Column(nullable = false, length = 30)
    private String reference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TypeOperation type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private SensOperation sens;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal montant;

    /** Solde du compte apres application de l'operation, fige pour l'historique. */
    @Column(name = "solde_apres", nullable = false, precision = 15, scale = 2)
    private BigDecimal soldeApres;

    @Column(name = "date_operation", nullable = false)
    private LocalDateTime dateOperation;

    @Column(length = 255)
    private String description;

    /** Numero du compte en face lors d'un virement, null sinon. */
    @Column(name = "compte_contrepartie", length = 20)
    private String compteContrepartie;

    /** Relation Operation * --- 1 Account. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account compte;

    /** Relation Operation * --- 1 User : agent auteur de la saisie. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Montant signe, utilise pour l'affichage et les totaux. */
    public BigDecimal getMontantSigne() {
        return sens == SensOperation.DEBIT ? montant.negate() : montant;
    }

    public boolean estCredit() {
        return sens == SensOperation.CREDIT;
    }
}
