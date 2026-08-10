package sn.isi.iage.microbank.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Resultat immuable d'une requete paginee.
 * Le decoupage est fait par la base (setFirstResult / setMaxResults) : cette classe
 * ne fait que transporter la tranche deja calculee et les informations d'affichage.
 *
 * @param contenu       elements de la page courante
 * @param numeroPage    index de la page, commence a 0
 * @param taillePage    nombre maximum d'elements par page
 * @param totalElements nombre total d'elements, toutes pages confondues
 */
public record PageResult<T>(List<T> contenu, int numeroPage, int taillePage, long totalElements) {

    public static final int TAILLE_PAGE_PAR_DEFAUT = 10;
    public static final int TAILLE_PAGE_MAXIMALE = 100;

    public PageResult {
        contenu = contenu == null
                ? List.of()
                : Collections.unmodifiableList(new ArrayList<>(contenu));
    }

    public static <T> PageResult<T> vide(int numeroPage, int taillePage) {
        return new PageResult<>(List.of(), numeroPage, taillePage, 0L);
    }

    // Accesseurs au format JavaBean : le langage d'expression des JSP (EL) reconnait
    // getXxx(), pas les accesseurs de record xxx().

    public List<T> getContenu() {
        return contenu;
    }

    public int getNumeroPage() {
        return numeroPage;
    }

    public int getTaillePage() {
        return taillePage;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public int getTotalPages() {
        if (taillePage <= 0) {
            return 0;
        }
        return (int) Math.ceil((double) totalElements / taillePage);
    }

    public boolean isPremierePage() {
        return numeroPage <= 0;
    }

    public boolean isDernierePage() {
        return numeroPage >= getTotalPages() - 1;
    }

    public boolean isVide() {
        return contenu.isEmpty();
    }

    /** Index du premier element de la page, en numerotation humaine (1-based). */
    public long getPremierElementAffiche() {
        return totalElements == 0 ? 0 : (long) numeroPage * taillePage + 1;
    }

    public long getDernierElementAffiche() {
        return Math.min((long) (numeroPage + 1) * taillePage, totalElements);
    }

    /** Normalise un numero de page recu d'un formulaire. */
    public static int normaliserNumeroPage(Integer numeroPage) {
        return numeroPage == null || numeroPage < 0 ? 0 : numeroPage;
    }

    /** Normalise une taille de page recue d'un formulaire, bornes comprises. */
    public static int normaliserTaillePage(Integer taillePage) {
        if (taillePage == null || taillePage <= 0) {
            return TAILLE_PAGE_PAR_DEFAUT;
        }
        return Math.min(taillePage, TAILLE_PAGE_MAXIMALE);
    }
}
