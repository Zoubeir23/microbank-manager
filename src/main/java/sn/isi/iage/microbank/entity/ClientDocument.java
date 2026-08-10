package sn.isi.iage.microbank.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Copie numerisee de la piece d'identite d'un client (bonus 1).
 * Le contenu binaire est stocke en base pour que la demonstration ne depende
 * d'aucun repertoire externe.
 */
@Entity
@Table(name = "client_documents")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@ToString(exclude = {"contenu", "client"})
public class ClientDocument extends BaseEntity {

    /** Relation ClientDocument 1 --- 1 Client (cote proprietaire). */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false, unique = true)
    private Client client;

    @Column(name = "nom_fichier", nullable = false, length = 255)
    private String nomFichier;

    @Column(name = "type_contenu", nullable = false, length = 100)
    private String typeContenu;

    @Column(name = "taille_octets", nullable = false)
    private long tailleOctets;

    @Lob
    @Column(nullable = false)
    private byte[] contenu;
}
