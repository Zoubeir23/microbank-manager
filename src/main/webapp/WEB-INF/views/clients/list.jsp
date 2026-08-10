<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="mb" uri="http://microbank.isi.sn/functions" %>
<c:set var="titrePage" value="Clients" scope="request"/>
<c:set var="menuActif" value="clients" scope="request"/>
<c:set var="contexte" value="${pageContext.request.contextPath}"/>
<jsp:include page="../layout/header.jsp"/>

<div class="d-flex justify-content-between align-items-center mb-3">
    <h1 class="h3 mb-0">Clients</h1>
    <a href="${contexte}/clients/nouveau" class="btn btn-primary">Nouveau client</a>
</div>

<jsp:include page="../layout/messages.jsp"/>

<div class="card mb-3">
    <div class="card-body">
        <form method="get" action="${contexte}/clients" class="row g-2">
            <div class="col-12 col-md-8">
                <label for="search" class="form-label">Rechercher</label>
                <input type="text" class="form-control" id="search" name="search"
                       value="<c:out value='${recherche}'/>"
                       placeholder="Nom, prenom, telephone ou numero de piece">
            </div>
            <div class="col-12 col-md-2 align-self-end">
                <button type="submit" class="btn btn-outline-primary w-100">Rechercher</button>
            </div>
            <div class="col-12 col-md-2 align-self-end">
                <a href="${contexte}/clients" class="btn btn-outline-secondary w-100">Reinitialiser</a>
            </div>
        </form>
    </div>
</div>

<div class="card">
    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0 tableau-donnees">
            <thead>
            <tr>
                <th>N°</th>
                <th>Nom</th>
                <th>Prenom</th>
                <th>Telephone</th>
                <th>Email</th>
                <th>Piece</th>
                <th>Statut</th>
                <th class="text-end">Actions</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="client" items="${page.contenu}">
                <tr>
                    <td>C${client.id}</td>
                    <td><c:out value="${client.nom}"/></td>
                    <td><c:out value="${client.prenom}"/></td>
                    <td><c:out value="${client.telephone}"/></td>
                    <td><c:out value="${client.email}"/></td>
                    <td><c:out value="${client.numeroPiece}"/></td>
                    <td>
                        <span class="badge ${client.statut eq 'ACTIF' ? 'text-bg-success' : 'text-bg-secondary'}">
                            <c:out value="${client.statut.libelle}"/>
                        </span>
                    </td>
                    <td class="text-end text-nowrap">
                        <a href="${contexte}/clients/details?id=${client.id}"
                           class="btn btn-sm btn-outline-primary">Voir</a>
                        <a href="${contexte}/clients/edit?id=${client.id}"
                           class="btn btn-sm btn-outline-secondary">Modifier</a>
                        <a href="${contexte}/clients/delete?id=${client.id}"
                           class="btn btn-sm btn-outline-danger"
                           onclick="return confirm('Supprimer definitivement ce client ?');">
                            Supprimer
                        </a>
                    </td>
                </tr>
            </c:forEach>

            <c:if test="${page.vide}">
                <tr>
                    <td colspan="8" class="text-center text-muted py-4">
                        Aucun client ne correspond a la recherche.
                    </td>
                </tr>
            </c:if>
            </tbody>
        </table>
    </div>
</div>

<div class="mt-3">
    <c:set var="urlPagination" value="/clients" scope="request"/>
    <c:set var="parametresConserves"
           value="${empty recherche ? '' : '&search='.concat(recherche)}" scope="request"/>
    <jsp:include page="../layout/pagination.jsp"/>
</div>

<jsp:include page="../layout/footer.jsp"/>
