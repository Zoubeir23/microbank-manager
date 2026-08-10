package sn.isi.iage.microbank.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Verifie le calcul d'affichage de la pagination (§10). */
class PageResultTest {

    private PageResult<String> page(int numeroPage, int taillePage, long totalElements) {
        return new PageResult<>(List.of("a", "b"), numeroPage, taillePage, totalElements);
    }

    @Test
    @DisplayName("le nombre de pages est arrondi vers le haut")
    void nombreDePages() {
        assertEquals(3, page(0, 10, 25).getTotalPages());
        assertEquals(2, page(0, 10, 20).getTotalPages());
        assertEquals(1, page(0, 10, 1).getTotalPages());
        assertEquals(0, page(0, 10, 0).getTotalPages());
    }

    @Test
    @DisplayName("la premiere et la derniere page sont identifiees")
    void bornesDePagination() {
        assertTrue(page(0, 10, 25).isPremierePage());
        assertFalse(page(0, 10, 25).isDernierePage());

        assertFalse(page(2, 10, 25).isPremierePage());
        assertTrue(page(2, 10, 25).isDernierePage());
    }

    @Test
    @DisplayName("une page unique est a la fois premiere et derniere")
    void pageUnique() {
        PageResult<String> pageUnique = page(0, 10, 2);

        assertTrue(pageUnique.isPremierePage());
        assertTrue(pageUnique.isDernierePage());
    }

    @Test
    @DisplayName("les indices affiches sont numerotes a partir de 1")
    void indicesAffiches() {
        PageResult<String> troisiemePage = page(2, 10, 25);

        assertEquals(21, troisiemePage.getPremierElementAffiche());
        assertEquals(25, troisiemePage.getDernierElementAffiche());
    }

    @Test
    @DisplayName("une page sans resultat affiche des indices a zero")
    void indicesSansResultat() {
        PageResult<String> pageVide = PageResult.vide(0, 10);

        assertEquals(0, pageVide.getPremierElementAffiche());
        assertEquals(0, pageVide.getDernierElementAffiche());
        assertTrue(pageVide.isVide());
    }

    @Test
    @DisplayName("le contenu est immuable meme si la liste source est modifiee ensuite")
    void contenuImmuable() {
        List<String> source = new ArrayList<>(List.of("a", "b"));
        PageResult<String> page = new PageResult<>(source, 0, 10, 2);

        source.add("c");

        assertEquals(2, page.contenu().size());
        assertThrows(UnsupportedOperationException.class, () -> page.contenu().add("d"));
    }

    @Test
    @DisplayName("un numero de page invalide est ramene a la premiere page")
    void normalisationDuNumeroDePage() {
        assertEquals(0, PageResult.normaliserNumeroPage(null));
        assertEquals(0, PageResult.normaliserNumeroPage(-5));
        assertEquals(4, PageResult.normaliserNumeroPage(4));
    }

    @Test
    @DisplayName("la taille de page est bornee pour proteger la base")
    void normalisationDeLaTailleDePage() {
        assertEquals(PageResult.TAILLE_PAGE_PAR_DEFAUT, PageResult.normaliserTaillePage(null));
        assertEquals(PageResult.TAILLE_PAGE_PAR_DEFAUT, PageResult.normaliserTaillePage(0));
        assertEquals(PageResult.TAILLE_PAGE_PAR_DEFAUT, PageResult.normaliserTaillePage(-3));
        assertEquals(25, PageResult.normaliserTaillePage(25));
        assertEquals(PageResult.TAILLE_PAGE_MAXIMALE, PageResult.normaliserTaillePage(100_000));
    }
}
