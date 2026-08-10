package sn.isi.iage.microbank.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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

    /**
     * Contenu binaire du fichier, stocke dans une colonne bytea.
     * On evite volontairement {@code @Lob} : sur PostgreSQL il produit une colonne oid,
     * c'est-a-dire un "large object" externe qui n'est pas supprime avec la ligne.
     */
    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(nullable = false)
    private byte[] contenu;
}
