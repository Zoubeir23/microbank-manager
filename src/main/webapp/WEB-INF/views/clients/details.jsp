<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="mb" uri="http://microbank.isi.sn/functions" %>
<c:set var="titrePage" value="Fiche client" scope="request"/>
<c:set var="menuActif" value="clients" scope="request"/>
<c:set var="contexte" value="${pageContext.request.contextPath}"/>
<jsp:include page="../layout/header.jsp"/>

<div class="d-flex justify-content-between align-items-center mb-3">
    <h1 class="h3 mb-0"><c:out value="${client.nomComplet}"/></h1>
    <div class="d-flex gap-2">
        <a href="${contexte}/clients/edit?id=${client.id}"
           class="btn btn-outline-secondary">Modifier</a>
        <a href="${contexte}/accounts/nouveau?clientId=${client.id}"
           class="btn btn-primary">Ouvrir un compte</a>
    </div>
</div>

<jsp:include page="../layout/messages.jsp"/>

<div class="row g-3">
    <div class="col-12 col-lg-5">
        <div class="card h-100">
            <div class="card-header bg-white fw-semibold">Informations</div>
            <div class="card-body">
                <dl class="row mb-0">
                    <dt class="col-5">Nom</dt>
                    <dd class="col-7"><c:out value="${client.nom}"/></dd>

                    <dt class="col-5">Prenom</dt>
                    <dd class="col-7"><c:out value="${client.prenom}"/></dd>

                    <dt class="col-5">Date de naissance</dt>
                    <dd class="col-7">${mb:date(client.dateNaissance)}</dd>

                    <dt class="col-5">Telephone</dt>
                    <dd class="col-7"><c:out value="${client.telephone}"/></dd>

                    <dt class="col-5">Email</dt>
                    <dd class="col-7"><c:out value="${client.email}"/></dd>

                    <dt class="col-5">Adresse</dt>
                    <dd class="col-7"><c:out value="${client.adresse}"/></dd>

                    <dt class="col-5">Numero de piece</dt>
                    <dd class="col-7"><c:out value="${client.numeroPiece}"/></dd>

                    <dt class="col-5">Date de creation</dt>
                    <dd class="col-7">${mb:date(client.dateCreation)}</dd>

                    <dt class="col-5">Statut</dt>
                    <dd class="col-7">
                        <span class="badge ${client.statut eq 'ACTIF' ? 'text-bg-success' : 'text-bg-secondary'}">
                            <c:out value="${client.statut.libelle}"/>
                        </span>
                    </dd>
                </dl>
            </div>
        </div>
    </div>

    <div class="col-12 col-lg-7">
        <div class="card mb-3">
            <div class="card-header bg-white fw-semibold">Comptes du client</div>
            <div class="table-responsive">
                <table class="table mb-0 align-middle tableau-donnees">
                    <thead>
                    <tr>
                        <th>Numero</th>
                        <th>Type</th>
                        <th class="text-end">Solde</th>
                        <th>Statut</th>
                        <th class="text-end">Actions</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="compte" items="${comptes}">
                        <tr>
                            <td class="cellule-chiffre"><c:out value="${compte.numeroCompte}"/></td>
                            <td><c:out value="${compte.type.libelle}"/></td>
                            <td class="text-end cellule-chiffre">${mb:montant(compte.solde)}</td>
                            <td>
                                <span class="badge ${compte.statut eq 'ACTIF' ? 'text-bg-success' : 'text-bg-secondary'}">
                                    <c:out value="${compte.statut.libelle}"/>
                                </span>
                            </td>
                            <td class="text-end">
                                <a href="${contexte}/accounts/details?id=${compte.id}"
                                   class="btn btn-sm btn-outline-primary">Voir</a>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty comptes}">
                        <tr>
                            <td colspan="5" class="text-center text-muted py-3">
                                Ce client n'a encore aucun compte.
                            </td>
                        </tr>
                    </c:if>
                    </tbody>
                </table>
            </div>
        </div>

        <div class="card">
            <div class="card-header bg-white fw-semibold">Piece d'identite</div>
            <div class="card-body">
                <c:choose>
                    <c:when test="${not empty client.document}">
                        <p class="mb-2">
                            Document enregistre :
                            <a href="${contexte}/clients/document?clientId=${client.id}"
                               target="_blank">
                                <c:out value="${client.document.nomFichier}"/>
                            </a>
                        </p>
                    </c:when>
                    <c:otherwise>
                        <p class="text-muted mb-2">Aucune piece enregistree.</p>
                    </c:otherwise>
                </c:choose>

                <a href="${contexte}/clients/edit?id=${client.id}"
                   class="btn btn-outline-primary btn-sm">
                    ${empty client.document ? 'Ajouter la piece' : 'Remplacer la piece'}
                </a>
            </div>
        </div>
    </div>
</div>

<div class="mt-3">
    <a href="${contexte}/clients" class="btn btn-link">Retour a la liste</a>
</div>

<jsp:include page="../layout/footer.jsp"/>
