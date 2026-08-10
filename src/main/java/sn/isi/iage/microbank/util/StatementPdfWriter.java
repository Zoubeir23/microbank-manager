package sn.isi.iage.microbank.util;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import sn.isi.iage.microbank.model.Account;
import sn.isi.iage.microbank.model.Operation;

import java.awt.Color;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Releve de compte au format PDF (§20), genere avec OpenPDF.
 * Le document reprend l'entete de l'institution, l'identite du titulaire, la periode,
 * le detail des operations puis les totaux.
 */
public final class StatementPdfWriter {

    private static final Font POLICE_TITRE =
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLACK);
    private static final Font POLICE_SOUS_TITRE =
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, Color.BLACK);
    private static final Font POLICE_ENTETE_TABLEAU =
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
    private static final Font POLICE_NORMALE =
            FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
    private static final Font POLICE_GRASSE =
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);
    private static final Color COULEUR_ENTETE = new Color(13, 71, 161);

    private StatementPdfWriter() {
    }

    /**
     * Ecrit le releve dans le flux fourni.
     *
     * @param fluxDeSortie flux de la reponse HTTP, ferme par l'appelant
     */
    public static void ecrire(OutputStream fluxDeSortie, Account compte,
                              List<Operation> operations, LocalDate dateDebut, LocalDate dateFin,
                              BigDecimal totalDesDepots, BigDecimal totalDesRetraits) {
        Document document = new Document(PageSize.A4, 40, 40, 40, 40);
        try {
            PdfWriter.getInstance(document, fluxDeSortie);
            document.open();

            ecrireEntete(document);
            ecrireIdentiteDuCompte(document, compte, dateDebut, dateFin);
            document.add(construireTableauDesOperations(operations));
            ecrireTotaux(document, compte, totalDesDepots, totalDesRetraits);
            ecrirePiedDePage(document);

            document.close();
        } catch (DocumentException erreurDeGeneration) {
            throw new IllegalStateException("Generation du releve PDF impossible",
                    erreurDeGeneration);
        }
    }

    private static void ecrireEntete(Document document) throws DocumentException {
        Paragraph nomDeLInstitution = new Paragraph("MICROBANK", POLICE_TITRE);
        nomDeLInstitution.setAlignment(Element.ALIGN_CENTER);
        document.add(nomDeLInstitution);

        Paragraph titre = new Paragraph("RELEVE DE COMPTE", POLICE_SOUS_TITRE);
        titre.setAlignment(Element.ALIGN_CENTER);
        titre.setSpacingAfter(20f);
        document.add(titre);
    }

    private static void ecrireIdentiteDuCompte(Document document, Account compte,
                                               LocalDate dateDebut, LocalDate dateFin)
            throws DocumentException {
        document.add(ligneDInformation("Client : ", compte.getClient().getNomComplet()));
        document.add(ligneDInformation("Compte : ", compte.getNumeroCompte()));
        document.add(ligneDInformation("Type : ", compte.getType().getLibelle()));
        document.add(ligneDInformation("Date d'ouverture : ",
                AmountFormatter.formaterDate(compte.getDateOuverture())));
        document.add(ligneDInformation("Periode : ", decrirePeriode(dateDebut, dateFin)));
        document.add(ligneDInformation("Statut : ", compte.getStatut().getLibelle()));

        Paragraph espacement = new Paragraph(" ");
        espacement.setSpacingAfter(10f);
        document.add(espacement);
    }

    private static PdfPTable construireTableauDesOperations(List<Operation> operations) {
        PdfPTable tableau = new PdfPTable(new float[]{2.2f, 3.0f, 2.0f, 2.4f, 2.4f});
        tableau.setWidthPercentage(100);
        tableau.setSpacingBefore(5f);
        tableau.setSpacingAfter(15f);

        ajouterEnteteDuTableau(tableau, "Date", "Reference", "Type", "Montant", "Solde");

        if (operations.isEmpty()) {
            PdfPCell celluleVide = new PdfPCell(
                    new Phrase("Aucune operation sur la periode", POLICE_NORMALE));
            celluleVide.setColspan(5);
            celluleVide.setHorizontalAlignment(Element.ALIGN_CENTER);
            celluleVide.setPadding(8f);
            tableau.addCell(celluleVide);
            return tableau;
        }

        for (Operation operation : operations) {
            tableau.addCell(cellule(
                    AmountFormatter.formaterDateHeure(operation.getDateOperation()),
                    Element.ALIGN_LEFT));
            tableau.addCell(cellule(operation.getReference(), Element.ALIGN_LEFT));
            tableau.addCell(cellule(operation.getType().getLibelle(), Element.ALIGN_LEFT));
            tableau.addCell(cellule(operation.getSens().getSigne()
                            + AmountFormatter.formaterMontant(operation.getMontant()),
                    Element.ALIGN_RIGHT));
            tableau.addCell(cellule(AmountFormatter.formaterMontant(operation.getSoldeApres()),
                    Element.ALIGN_RIGHT));
        }
        return tableau;
    }

    private static void ecrireTotaux(Document document, Account compte,
                                     BigDecimal totalDesDepots, BigDecimal totalDesRetraits)
            throws DocumentException {
        PdfPTable tableauDesTotaux = new PdfPTable(new float[]{6f, 4f});
        tableauDesTotaux.setWidthPercentage(60);
        tableauDesTotaux.setHorizontalAlignment(Element.ALIGN_RIGHT);

        ajouterLigneDeTotal(tableauDesTotaux, "Total des depots",
                AmountFormatter.formaterMontantAvecDevise(totalDesDepots), POLICE_NORMALE);
        ajouterLigneDeTotal(tableauDesTotaux, "Total des retraits",
                AmountFormatter.formaterMontantAvecDevise(totalDesRetraits), POLICE_NORMALE);
        ajouterLigneDeTotal(tableauDesTotaux, "Solde final",
                AmountFormatter.formaterMontantAvecDevise(compte.getSolde()), POLICE_GRASSE);

        document.add(tableauDesTotaux);
    }

    private static void ecrirePiedDePage(Document document) throws DocumentException {
        Paragraph piedDePage = new Paragraph(
                "Document genere le " + AmountFormatter.formaterDate(LocalDate.now())
                        + " par MicroBank Manager.", POLICE_NORMALE);
        piedDePage.setAlignment(Element.ALIGN_CENTER);
        piedDePage.setSpacingBefore(25f);
        document.add(piedDePage);
    }

    private static Paragraph ligneDInformation(String libelle, String valeur) {
        Paragraph ligne = new Paragraph();
        ligne.add(new Phrase(libelle, POLICE_GRASSE));
        ligne.add(new Phrase(valeur == null ? "" : valeur, POLICE_NORMALE));
        return ligne;
    }

    private static void ajouterEnteteDuTableau(PdfPTable tableau, String... intitules) {
        for (String intitule : intitules) {
            PdfPCell cellule = new PdfPCell(new Phrase(intitule, POLICE_ENTETE_TABLEAU));
            cellule.setBackgroundColor(COULEUR_ENTETE);
            cellule.setPadding(6f);
            tableau.addCell(cellule);
        }
    }

    private static PdfPCell cellule(String texte, int alignement) {
        PdfPCell cellule = new PdfPCell(new Phrase(texte == null ? "" : texte, POLICE_NORMALE));
        cellule.setPadding(5f);
        cellule.setHorizontalAlignment(alignement);
        return cellule;
    }

    private static void ajouterLigneDeTotal(PdfPTable tableau, String libelle,
                                            String valeur, Font police) {
        PdfPCell celluleLibelle = new PdfPCell(new Phrase(libelle, police));
        celluleLibelle.setPadding(5f);
        celluleLibelle.setHorizontalAlignment(Element.ALIGN_LEFT);

        PdfPCell celluleValeur = new PdfPCell(new Phrase(valeur, police));
        celluleValeur.setPadding(5f);
        celluleValeur.setHorizontalAlignment(Element.ALIGN_RIGHT);

        tableau.addCell(celluleLibelle);
        tableau.addCell(celluleValeur);
    }

    private static String decrirePeriode(LocalDate dateDebut, LocalDate dateFin) {
        if (dateDebut == null && dateFin == null) {
            return "Toutes les operations";
        }
        return (dateDebut == null ? "origine" : AmountFormatter.formaterDate(dateDebut))
                + " - "
                + (dateFin == null ? "aujourd'hui" : AmountFormatter.formaterDate(dateFin));
    }
}
