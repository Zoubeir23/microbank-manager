<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="mb" uri="http://microbank.isi.sn/functions" %>
<c:set var="titrePage" value="Depot" scope="request"/>
<c:set var="menuActif" value="operations" scope="request"/>
<c:set var="contexte" value="${pageContext.request.contextPath}"/>
<jsp:include page="../layout/header.jsp"/>

<h1 class="h3 mb-3">Effectuer un depot</h1>

<jsp:include page="../layout/messages.jsp"/>

<div class="card">
    <div class="card-body">
        <form method="post" action="${contexte}/operations/deposit" novalidate>
            <input type="hidden" name="csrfToken" value="${csrfToken}">

            <div class="row g-3">
                <div class="col-12 col-md-8">
                    <label for="compteId" class="form-label">Compte a crediter *</label>
                    <select id="compteId" name="compteId" class="form-select" required>
                        <option value="">-- Choisir un compte --</option>
                        <c:forEach var="compte" items="${comptes}">
                            <option value="${compte.id}"
                                    ${compteSelectionne eq compte.id ? 'selected' : ''}>
                                <c:out value="${compte.numeroCompte}"/> -
                                <c:out value="${compte.client.nomComplet}"/>
                                (solde ${mb:montant(compte.solde)} FCFA)
                            </option>
                        </c:forEach>
                    </select>
                </div>

                <div class="col-12 col-md-4">
                    <label for="montant" class="form-label">Montant (FCFA) *</label>
                    <input type="number" id="montant" name="montant" min="1" step="1"
                           class="form-control" required>
                </div>

                <div class="col-12">
                    <label for="description" class="form-label">Description</label>
                    <input type="text" id="description" name="description" class="form-control"
                           maxlength="255" placeholder="Depot en especes">
                </div>
            </div>

            <div class="mt-4 d-flex gap-2">
                <button type="submit" class="btn btn-success">Valider le depot</button>
                <a href="${contexte}/operations" class="btn btn-outline-secondary">Annuler</a>
            </div>
        </form>
    </div>
</div>

<jsp:include page="../layout/footer.jsp"/>
