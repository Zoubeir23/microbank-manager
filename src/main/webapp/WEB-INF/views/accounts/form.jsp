<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="titrePage" value="Ouverture d'un compte" scope="request"/>
<c:set var="menuActif" value="accounts" scope="request"/>
<c:set var="contexte" value="${pageContext.request.contextPath}"/>
<jsp:include page="../layout/header.jsp"/>

<h1 class="h3 mb-3">Ouverture d'un compte</h1>

<jsp:include page="../layout/messages.jsp"/>

<div class="card">
    <div class="card-body">
        <form method="post" action="${contexte}/accounts/create" novalidate>
            <input type="hidden" name="csrfToken" value="${csrfToken}">

            <div class="row g-3">
                <div class="col-12 col-md-6">
                    <label for="clientId" class="form-label">Client *</label>
                    <select id="clientId" name="clientId" required
                            class="form-select ${not empty erreurs['clientId'] ? 'is-invalid' : ''}">
                        <option value="">-- Choisir un client --</option>
                        <c:forEach var="client" items="${clients}">
                            <option value="${client.id}"
                                    ${formulaire.clientId eq client.id ? 'selected' : ''}>
                                <c:out value="${client.nomComplet}"/>
                                (<c:out value="${client.numeroPiece}"/>)
                            </option>
                        </c:forEach>
                    </select>
                    <div class="invalid-feedback"><c:out value="${erreurs['clientId']}"/></div>
                </div>

                <div class="col-12 col-md-6">
                    <label for="agenceId" class="form-label">Agence</label>
                    <select id="agenceId" name="agenceId" class="form-select">
                        <option value="">-- Aucune --</option>
                        <c:forEach var="agence" items="${agences}">
                            <option value="${agence.id}"
                                    ${formulaire.agenceId eq agence.id ? 'selected' : ''}>
                                <c:out value="${agence.libelleComplet}"/>
                            </option>
                        </c:forEach>
                    </select>
                </div>

                <div class="col-12">
                    <span class="form-label d-block">Type de compte *</span>
                    <c:forEach var="type" items="${typesDeCompte}">
                        <div class="form-check form-check-inline">
                            <input class="form-check-input" type="radio" name="type"
                                   id="type${type}" value="${type}"
                                   ${formulaire.type eq type.name() ? 'checked' : ''} required>
                            <label class="form-check-label" for="type${type}">
                                <c:out value="${type.libelle}"/>
                            </label>
                        </div>
                    </c:forEach>
                    <c:if test="${not empty erreurs['type']}">
                        <div class="text-danger small"><c:out value="${erreurs['type']}"/></div>
                    </c:if>
                </div>

                <div class="col-12 col-md-6">
                    <label for="depotInitial" class="form-label">Depot initial (FCFA)</label>
                    <input type="number" id="depotInitial" name="depotInitial" min="0" step="1"
                           class="form-control ${not empty erreurs['depotInitial'] ? 'is-invalid' : ''}"
                           value="<c:out value='${formulaire.depotInitial}'/>">
                    <div class="form-text">
                        Laisser a 0 pour ouvrir le compte sans versement.
                    </div>
                    <div class="invalid-feedback"><c:out value="${erreurs['depotInitial']}"/></div>
                </div>
            </div>

            <div class="mt-4 d-flex gap-2">
                <button type="submit" class="btn btn-primary">Creer le compte</button>
                <a href="${contexte}/accounts" class="btn btn-outline-secondary">Annuler</a>
            </div>
        </form>
    </div>
</div>

<jsp:include page="../layout/footer.jsp"/>
