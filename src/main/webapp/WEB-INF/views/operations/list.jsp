<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="mb" uri="http://microbank.isi.sn/functions" %>
<c:set var="titrePage" value="Historique des operations" scope="request"/>
<c:set var="menuActif" value="operations" scope="request"/>
<c:set var="contexte" value="${pageContext.request.contextPath}"/>
<jsp:include page="../layout/header.jsp"/>

<div class="d-flex justify-content-between align-items-center mb-3">
    <h1 class="h3 mb-0">
        Historique des operations
        <c:if test="${not empty compte}">
            <small class="text-muted">- compte <c:out value="${compte.numeroCompte}"/></small>
        </c:if>
    </h1>
    <div class="d-flex gap-2 zone-non-imprimable">
        <a href="${contexte}/operations/deposit" class="btn btn-outline-success">Depot</a>
        <a href="${contexte}/operations/withdraw" class="btn btn-outline-warning">Retrait</a>
        <a href="${contexte}/operations/transfer" class="btn btn-outline-secondary">Virement</a>
    </div>
</div>

<jsp:include page="../layout/messages.jsp"/>

<%-- Filtres §18 : type, periode, montant minimum et maximum. --%>
<div class="card mb-3 zone-non-imprimable">
    <div class="card-body">
        <form method="get" action="${contexte}/operations" class="row g-2">
            <input type="hidden" name="accountId" value="${criteres.compteId}">
            <input type="hidden" name="clientId" value="${criteres.clientId}">

            <div class="col-6 col-md-2">
                <label for="type" class="form-label">Type</label>
                <select id="type" name="type" class="form-select">
                    <option value="">Tous</option>
                    <c:forEach var="type" items="${typesDOperation}">
                        <option value="${type}" ${criteres.type eq type ? 'selected' : ''}>
                            <c:out value="${type.libelle}"/>
                        </option>
                    </c:forEach>
                </select>
            </div>

            <div class="col-6 col-md-2">
                <label for="dateDebut" class="form-label">Du</label>
                <input type="date" id="dateDebut" name="dateDebut" class="form-control"
                       value="${criteres.dateDebut}">
            </div>

            <div class="col-6 col-md-2">
                <label for="dateFin" class="form-label">Au</label>
                <input type="date" id="dateFin" name="dateFin" class="form-control"
                       value="${criteres.dateFin}">
            </div>

            <div class="col-6 col-md-2">
                <label for="montantMinimum" class="form-label">Montant min.</label>
                <input type="number" step="1" min="0" id="montantMinimum" name="montantMinimum"
                       class="form-control" value="${criteres.montantMinimum}">
            </div>

            <div class="col-6 col-md-2">
                <label for="montantMaximum" class="form-label">Montant max.</label>
                <input type="number" step="1" min="0" id="montantMaximum" name="montantMaximum"
                       class="form-control" value="${criteres.montantMaximum}">
            </div>

            <div class="col-6 col-md-2 align-self-end d-grid">
                <button type="submit" class="btn btn-outline-primary">Rechercher</button>
            </div>
        </form>

        <div class="d-flex gap-2 mt-3">
            <a href="${contexte}/operations" class="btn btn-sm btn-outline-secondary">
                Reinitialiser les filtres
            </a>
            <a href="${contexte}/operations/export.csv?${parametresDuFiltre}"
               class="btn btn-sm btn-success">Exporter CSV</a>
            <c:if test="${not empty criteres.compteId}">
                <a href="${contexte}/operations/statement.pdf?${parametresDuFiltre}"
                   class="btn btn-sm btn-primary">Telecharger le releve PDF</a>
                <button type="button" class="btn btn-sm btn-outline-dark" onclick="window.print();">
                    Version imprimable
                </button>
            </c:if>
        </div>
    </div>
</div>

<div class="row g-3 mb-3">
    <div class="col-6">
        <div class="card">
            <div class="card-body py-2">
                <span class="text-muted">Total des depots :</span>
                <span class="montant-credit">${mb:montant(totalDesDepots)} FCFA</span>
            </div>
        </div>
    </div>
    <div class="col-6">
        <div class="card">
            <div class="card-body py-2">
                <span class="text-muted">Total des retraits :</span>
                <span class="montant-debit">${mb:montant(totalDesRetraits)} FCFA</span>
            </div>
        </div>
    </div>
</div>

<div class="card">
    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0 tableau-donnees">
            <thead>
            <tr>
                <th>Date</th>
                <th>Reference</th>
                <th>Type</th>
                <th>Compte</th>
                <th>Client</th>
                <th>Description</th>
                <th>Agent</th>
                <th class="text-end">Montant</th>
                <th class="text-end">Solde</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="operation" items="${page.contenu}">
                <tr>
                    <td class="cellule-chiffre">${mb:dateHeure(operation.dateOperation)}</td>
                    <td class="cellule-chiffre"><c:out value="${operation.reference}"/></td>
                    <td><c:out value="${operation.type.libelle}"/></td>
                    <td>
                        <a href="${contexte}/accounts/details?id=${operation.compte.id}">
                            <c:out value="${operation.compte.numeroCompte}"/>
                        </a>
                    </td>
                    <td><c:out value="${operation.compte.client.nomComplet}"/></td>
                    <td><c:out value="${operation.description}"/></td>
                    <td><c:out value="${operation.user.login}"/></td>
                    <td class="text-end cellule-chiffre ${operation.sens eq 'CREDIT' ? 'montant-credit' : 'montant-debit'}">
                        <c:out value="${operation.sens.signe}"/>${mb:montant(operation.montant)}
                    </td>
                    <td class="text-end cellule-chiffre">${mb:montant(operation.soldeApres)}</td>
                </tr>
            </c:forEach>

            <c:if test="${page.vide}">
                <tr>
                    <td colspan="9" class="text-center text-muted py-4">
                        Aucune operation ne correspond aux filtres.
                    </td>
                </tr>
            </c:if>
            </tbody>
        </table>
    </div>
</div>

<div class="mt-3">
    <c:set var="urlPagination" value="/operations" scope="request"/>
    <c:set var="parametresConserves" value="${parametresDuFiltre}" scope="request"/>
    <jsp:include page="../layout/pagination.jsp"/>
</div>

<jsp:include page="../layout/footer.jsp"/>
