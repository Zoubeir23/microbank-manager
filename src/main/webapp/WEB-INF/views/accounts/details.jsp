<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="mb" uri="http://microbank.isi.sn/functions" %>
<c:set var="titrePage" value="Compte ${compte.numeroCompte}" scope="request"/>
<c:set var="menuActif" value="accounts" scope="request"/>
<c:set var="contexte" value="${pageContext.request.contextPath}"/>
<jsp:include page="../layout/header.jsp"/>

<div class="d-flex justify-content-between align-items-center mb-3">
    <h1 class="h3 mb-0">Compte <c:out value="${compte.numeroCompte}"/></h1>
    <div class="d-flex gap-2 zone-non-imprimable">
        <a href="${contexte}/operations/deposit?compteId=${compte.id}"
           class="btn btn-outline-success">Depot</a>
        <a href="${contexte}/operations/withdraw?compteId=${compte.id}"
           class="btn btn-outline-warning">Retrait</a>
        <a href="${contexte}/operations/transfer?compteId=${compte.id}"
           class="btn btn-outline-secondary">Virement</a>
        <a href="${contexte}/operations/statement.pdf?accountId=${compte.id}"
           class="btn btn-primary">Telecharger le releve PDF</a>
    </div>
</div>

<jsp:include page="../layout/messages.jsp"/>

<div class="row g-3 mb-3">
    <div class="col-12 col-lg-7">
        <div class="card h-100">
            <div class="card-header bg-white fw-semibold">Caracteristiques</div>
            <div class="card-body">
                <dl class="row mb-0">
                    <dt class="col-5">Numero de compte</dt>
                    <dd class="col-7"><c:out value="${compte.numeroCompte}"/></dd>

                    <dt class="col-5">Titulaire</dt>
                    <dd class="col-7">
                        <a href="${contexte}/clients/details?id=${compte.client.id}">
                            <c:out value="${compte.client.nomComplet}"/>
                        </a>
                    </dd>

                    <dt class="col-5">Type</dt>
                    <dd class="col-7"><c:out value="${compte.type.libelle}"/></dd>

                    <dt class="col-5">Date d'ouverture</dt>
                    <dd class="col-7">${mb:date(compte.dateOuverture)}</dd>

                    <dt class="col-5">Agence</dt>
                    <dd class="col-7">
                        <c:choose>
                            <c:when test="${not empty compte.agence}">
                                <c:out value="${compte.agence.libelleComplet}"/>
                            </c:when>
                            <c:otherwise>-</c:otherwise>
                        </c:choose>
                    </dd>

                    <dt class="col-5">Statut</dt>
                    <dd class="col-7">
                        <span class="badge ${compte.statut eq 'ACTIF' ? 'text-bg-success' : (compte.statut eq 'BLOQUE' ? 'text-bg-warning' : 'text-bg-secondary')}">
                            <c:out value="${compte.statut.libelle}"/>
                        </span>
                    </dd>
                </dl>
            </div>
        </div>
    </div>

    <div class="col-12 col-lg-5">
        <div class="card carte-statistique carte-statistique-hero h-100">
            <div class="card-body">
                <div class="text-muted">Solde actuel</div>
                <div class="valeur">${mb:montant(compte.solde)}</div>
                <div class="text-muted small mb-3">FCFA</div>

                <form method="post" action="${contexte}/accounts/statut"
                      class="row g-2 zone-non-imprimable">
                    <input type="hidden" name="csrfToken" value="${csrfToken}">
                    <input type="hidden" name="id" value="${compte.id}">
                    <div class="col-7">
                        <select name="statut" class="form-select form-select-sm">
                            <c:forEach var="statut" items="${statutsPossibles}">
                                <option value="${statut}"
                                        ${compte.statut eq statut ? 'selected' : ''}>
                                    <c:out value="${statut.libelle}"/>
                                </option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="col-5">
                        <button type="submit" class="btn btn-sm btn-outline-primary w-100">
                            Changer le statut
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<div class="card">
    <div class="card-header bg-white d-flex justify-content-between align-items-center">
        <span class="fw-semibold">Dernieres operations</span>
        <a href="${contexte}/operations?accountId=${compte.id}"
           class="btn btn-sm btn-outline-secondary zone-non-imprimable">
            Voir tout l'historique
        </a>
    </div>
    <div class="table-responsive">
        <table class="table mb-0 align-middle tableau-donnees">
            <thead>
            <tr>
                <th>Date</th>
                <th>Reference</th>
                <th>Type</th>
                <th>Description</th>
                <th class="text-end">Montant</th>
                <th class="text-end">Solde</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="operation" items="${dernieresOperations}">
                <tr>
                    <td class="cellule-chiffre">${mb:dateHeure(operation.dateOperation)}</td>
                    <td class="cellule-chiffre"><c:out value="${operation.reference}"/></td>
                    <td><c:out value="${operation.type.libelle}"/></td>
                    <td><c:out value="${operation.description}"/></td>
                    <td class="text-end cellule-chiffre ${operation.sens eq 'CREDIT' ? 'montant-credit' : 'montant-debit'}">
                        <c:out value="${operation.sens.signe}"/>${mb:montant(operation.montant)}
                    </td>
                    <td class="text-end cellule-chiffre">${mb:montant(operation.soldeApres)}</td>
                </tr>
            </c:forEach>
            <c:if test="${empty dernieresOperations}">
                <tr>
                    <td colspan="6" class="text-center text-muted py-3">
                        Aucune operation sur ce compte.
                    </td>
                </tr>
            </c:if>
            </tbody>
        </table>
    </div>
</div>

<div class="mt-3 zone-non-imprimable">
    <a href="${contexte}/accounts" class="btn btn-link">Retour a la liste</a>
</div>

<jsp:include page="../layout/footer.jsp"/>
