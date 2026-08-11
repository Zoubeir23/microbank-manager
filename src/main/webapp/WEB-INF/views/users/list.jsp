<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="titrePage" value="Utilisateurs" scope="request"/>
<c:set var="menuActif" value="users" scope="request"/>
<c:set var="contexte" value="${pageContext.request.contextPath}"/>
<jsp:include page="../layout/header.jsp"/>

<div class="d-flex justify-content-between align-items-center mb-3">
    <h1 class="h3 mb-0">Utilisateurs</h1>
    <a href="${contexte}/users/nouveau" class="btn btn-primary">Nouvel utilisateur</a>
</div>

<jsp:include page="../layout/messages.jsp"/>

<div class="card mb-3">
    <div class="card-body">
        <form method="get" action="${contexte}/users" class="row g-2">
            <div class="col-12 col-md-8">
                <label for="search" class="form-label">Rechercher</label>
                <input type="text" class="form-control" id="search" name="search"
                       value="<c:out value='${recherche}'/>" placeholder="Nom, prenom ou login">
            </div>
            <div class="col-12 col-md-2 align-self-end">
                <button type="submit" class="btn btn-outline-primary w-100">Rechercher</button>
            </div>
            <div class="col-12 col-md-2 align-self-end">
                <a href="${contexte}/users" class="btn btn-outline-secondary w-100">Reinitialiser</a>
            </div>
        </form>
    </div>
</div>

<div class="card">
    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0 tableau-donnees">
            <thead>
            <tr>
                <th>Nom</th>
                <th>Prenom</th>
                <th>Login</th>
                <th>Role</th>
                <th>Statut</th>
                <th class="text-end">Actions</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="utilisateur" items="${page.contenu}">
                <tr>
                    <td><c:out value="${utilisateur.nom}"/></td>
                    <td><c:out value="${utilisateur.prenom}"/></td>
                    <td><c:out value="${utilisateur.login}"/></td>
                    <td>
                        <span class="badge ${utilisateur.role eq 'ADMIN' ? 'text-bg-dark' : 'text-bg-info'}">
                            <c:out value="${utilisateur.role.libelle}"/>
                        </span>
                    </td>
                    <td>
                        <span class="badge ${utilisateur.statut eq 'ACTIF' ? 'text-bg-success' : 'text-bg-secondary'}">
                            <c:out value="${utilisateur.statut.libelle}"/>
                        </span>
                    </td>
                    <c:set var="estSoiMeme" value="${utilisateur.id eq sessionScope.user.id}"/>
                    <td class="text-end text-nowrap">
                        <a href="${contexte}/users/edit?id=${utilisateur.id}"
                           class="btn btn-sm btn-outline-secondary">Modifier</a>
                        <form method="post" action="${contexte}/users/toggle" class="d-inline">
                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                            <input type="hidden" name="id" value="${utilisateur.id}">
                            <%-- Un administrateur ne peut pas desactiver son propre compte
                                 (§ regle metier) : le bouton est desactive plutot que de
                                 laisser cliquer puis afficher une erreur. Le controle reste
                                 fait aussi cote serveur, un bouton desactive n'etant qu'une
                                 aide visuelle et non une protection. --%>
                            <button type="submit"
                                    ${estSoiMeme ? 'disabled' : ''}
                                    title="${estSoiMeme ? 'Vous ne pouvez pas desactiver votre propre compte.' : ''}"
                                    class="btn btn-sm ${utilisateur.statut eq 'ACTIF' ? 'btn-outline-danger' : 'btn-outline-success'}">
                                    ${utilisateur.statut eq 'ACTIF' ? 'Desactiver' : 'Activer'}
                            </button>
                        </form>
                    </td>
                </tr>
            </c:forEach>

            <c:if test="${page.vide}">
                <tr>
                    <td colspan="6" class="text-center text-muted py-4">
                        Aucun utilisateur ne correspond a la recherche.
                    </td>
                </tr>
            </c:if>
            </tbody>
        </table>
    </div>
</div>

<div class="mt-3">
    <c:set var="urlPagination" value="/users" scope="request"/>
    <c:set var="parametresConserves"
           value="${empty recherche ? '' : '&search='.concat(recherche)}" scope="request"/>
    <jsp:include page="../layout/pagination.jsp"/>
</div>

<jsp:include page="../layout/footer.jsp"/>
