package sn.isi.iage.microbank.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Agence de l'institution. Chaque compte est rattache a l'agence qui l'a ouvert
 * (fonctionnalite bonus 4 du cahier des charges).
 */
@Entity
@Table(name = "agencies")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@ToString(exclude = "comptes")
public class Agency extends BaseEntity {

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String ville;

    @OneToMany(mappedBy = "agence", fetch = FetchType.LAZY)
    @lombok.Builder.Default
    private List<Account> comptes = new ArrayList<>();

    public String getLibelleComplet() {
        return code + " - " + nom;
    }
}
