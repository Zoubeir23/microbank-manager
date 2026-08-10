package sn.isi.iage.microbank.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import sn.isi.iage.microbank.enums.Role;
import sn.isi.iage.microbank.enums.Statut;

import java.util.ArrayList;
import java.util.List;

/**
 * Utilisateur de l'application (agent ou administrateur).
 * Le mot de passe n'est jamais stocke en clair : la colonne contient une empreinte
 * PBKDF2 produite par {@code PasswordHasher}.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@ToString(exclude = {"motDePasse", "operations"})
public class User extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String prenom;

    @Column(nullable = false, unique = true, length = 50)
    private String login;

    @Column(name = "mot_de_passe", nullable = false, length = 255)
    private String motDePasse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Statut statut;

    /** Operations saisies par cet utilisateur (relation User 1 --- * Operation). */
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @lombok.Builder.Default
    private List<Operation> operations = new ArrayList<>();

    public String getNomComplet() {
        return prenom + " " + nom;
    }

    public boolean estAdministrateur() {
        return role == Role.ADMIN;
    }

    public boolean estActif() {
        return statut == Statut.ACTIF;
    }
}
