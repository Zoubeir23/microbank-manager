<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="mb" uri="http://microbank.isi.sn/functions" %>
<c:set var="titrePage" value="Comptes" scope="request"/>
<c:set var="menuActif" value="accounts" scope="request"/>
<c:set var="contexte" value="${pageContext.request.contextPath}"/>
<jsp:include page="../layout/header.jsp"/>

<div class="d-flex justify-content-between align-items-center mb-3">
    <h1 class="h3 mb-0">Comptes</h1>
    <a href="${contexte}/accounts/nouveau" class="btn btn-primary">Ouvrir un compte</a>
</div>

<jsp:include page="../layout/messages.jsp"/>

<div class="card mb-3">
    <div class="card-body">
        <form method="get" action="${contexte}/accounts" class="row g-2">
            <div class="col-12 col-md-8">
                <label for="search" class="form-label">Rechercher</label>
                <input type="text" class="form-control" id="search" name="search"
                       value="<c:out value='${recherche}'/>"
                       placeholder="Numero de compte ou titulaire">
            </div>
            <div class="col-12 col-md-2 align-self-end">
                <button type="submit" class="btn btn-outline-primary w-100">Rechercher</button>
            </div>
            <div class="col-12 col-md-2 align-self-end">
                <a href="${contexte}/accounts" class="btn btn-outline-secondary w-100">Reinitialiser</a>
            </div>
        </form>
    </div>
</div>

<div class="card">
    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0 tableau-donnees">
            <thead>
            <tr>
                <th>Numero</th>
                <th>Titulaire</th>
                <th>Type</th>
                <th class="text-end">Solde (FCFA)</th>
                <th>Ouverture</th>
                <th>Statut</th>
                <th class="text-end">Actions</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="compte" items="${page.contenu}">
                <tr>
                    <td class="cellule-chiffre fw-semibold"><c:out value="${compte.numeroCompte}"/></td>
                    <td><c:out value="${compte.client.nomComplet}"/></td>
                    <td><c:out value="${compte.type.libelle}"/></td>
                    <td class="text-end cellule-chiffre">${mb:montant(compte.solde)}</td>
                    <td class="cellule-chiffre">${mb:date(compte.dateOuverture)}</td>
                    <td>
                        <span class="badge ${compte.statut eq 'ACTIF' ? 'text-bg-success' : (compte.statut eq 'BLOQUE' ? 'text-bg-warning' : 'text-bg-secondary')}">
                            <c:out value="${compte.statut.libelle}"/>
                        </span>
                    </td>
                    <td class="text-end text-nowrap">
                        <a href="${contexte}/accounts/details?id=${compte.id}"
                           class="btn btn-sm btn-outline-primary">Voir</a>
                        <a href="${contexte}/operations?accountId=${compte.id}"
                           class="btn btn-sm btn-outline-secondary">Historique</a>
                    </td>
                </tr>
            </c:forEach>

            <c:if test="${page.vide}">
                <tr>
                    <td colspan="7" class="text-center text-muted py-4">
                        Aucun compte ne correspond a la recherche.
                    </td>
                </tr>
            </c:if>
            </tbody>
        </table>
    </div>
</div>

<div class="mt-3">
    <c:set var="urlPagination" value="/accounts" scope="request"/>
    <c:set var="parametresConserves"
           value="${empty recherche ? '' : '&search='.concat(recherche)}" scope="request"/>
    <jsp:include page="../layout/pagination.jsp"/>
</div>

<jsp:include page="../layout/footer.jsp"/>
