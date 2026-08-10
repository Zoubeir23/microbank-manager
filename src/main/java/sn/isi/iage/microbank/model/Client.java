package sn.isi.iage.microbank.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import sn.isi.iage.microbank.enums.Statut;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Client de l'institution de microfinance. Un client peut detenir plusieurs comptes. */
@Entity
@Table(name = "clients")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@ToString(exclude = {"comptes", "document"})
public class Client extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String prenom;

    @Column(name = "date_naissance")
    private LocalDate dateNaissance;

    @Column(nullable = false, length = 30)
    private String telephone;

    @Column(length = 150)
    private String email;

    @Column(length = 255)
    private String adresse;

    @Column(name = "numero_piece", nullable = false, unique = true, length = 50)
    private String numeroPiece;

    @Column(name = "date_creation", nullable = false)
    private LocalDate dateCreation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Statut statut;

    /** Relation Client 1 --- * Account. */
    @OneToMany(mappedBy = "client", fetch = FetchType.LAZY)
    @lombok.Builder.Default
    private List<Account> comptes = new ArrayList<>();

    /** Relation Client 1 --- 1 ClientDocument (copie de la piece d'identite). */
    @OneToOne(mappedBy = "client", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private ClientDocument document;

    public String getNomComplet() {
        return prenom + " " + nom;
    }
}
