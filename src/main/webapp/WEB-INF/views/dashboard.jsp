<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="mb" uri="http://microbank.isi.sn/functions" %>
<c:set var="titrePage" value="Tableau de bord" scope="request"/>
<c:set var="menuActif" value="dashboard" scope="request"/>
<jsp:include page="layout/header.jsp"/>

<div class="d-flex justify-content-between align-items-end mb-4 flex-wrap gap-2">
    <div>
        <p class="sous-marque mb-1">Situation de l'institution</p>
        <h1 class="h3 mb-0">Tableau de bord</h1>
    </div>
    <span class="text-muted small">
        Bonjour <c:out value="${sessionScope.user.prenom}"/>,
        agence de Dakar
    </span>
</div>

<jsp:include page="layout/messages.jsp"/>

<div class="row g-3 mb-4">
    <div class="col-12 col-md-6 col-lg-3">
        <div class="card carte-statistique h-100">
            <div class="card-body">
                <div class="text-muted">Clients</div>
                <div class="valeur">
                    ${mb:nombre(statistiques.nombreDeClients)}
                </div>
            </div>
        </div>
    </div>

    <div class="col-12 col-md-6 col-lg-3">
        <div class="card carte-statistique h-100">
            <div class="card-body">
                <div class="text-muted">Comptes</div>
                <div class="valeur">
                    ${mb:nombre(statistiques.nombreDeComptes)}
                </div>
            </div>
        </div>
    </div>

    <div class="col-12 col-md-6 col-lg-3">
        <div class="card carte-statistique carte-statistique-hero h-100">
            <div class="card-body">
                <div class="text-muted">Solde total</div>
                <div class="valeur">
                    ${mb:montantArrondi(statistiques.soldeTotal)}
                </div>
                <div class="text-muted small">FCFA</div>
            </div>
        </div>
    </div>

    <div class="col-12 col-md-6 col-lg-3">
        <div class="card carte-statistique h-100">
            <div class="card-body">
                <div class="text-muted">Operations du jour</div>
                <div class="valeur">
                    ${mb:nombre(statistiques.nombreOperationsDuJour)}
                </div>
            </div>
        </div>
    </div>
</div>

<div class="row g-3 mb-4">
    <div class="col-12 col-lg-8">
        <div class="card h-100">
            <div class="card-header bg-white fw-semibold">Mouvements du jour</div>
            <div class="card-body">
                <div class="row">
                    <div class="col-6">
                        <div class="text-muted">Total des depots</div>
                        <div class="h4 montant-credit">
                            ${mb:montantArrondi(statistiques.totalDepotsDuJour)} FCFA
                        </div>
                    </div>
                    <div class="col-6">
                        <div class="text-muted">Total des retraits</div>
                        <div class="h4 montant-debit">
                            ${mb:montantArrondi(statistiques.totalRetraitsDuJour)} FCFA
                        </div>
                    </div>
                </div>
                <%-- Repartition depots / retraits de la journee.
                     Le total peut etre nul : on evite alors la division. --%>
                <c:set var="totalMouvements"
                       value="${statistiques.totalDepotsDuJour + statistiques.totalRetraitsDuJour}"/>
                <c:set var="partDesDepots"
                       value="${totalMouvements > 0
                                ? (statistiques.totalDepotsDuJour * 100) / totalMouvements : 0}"/>

                <div class="barre-repartition mt-4" role="img"
                     aria-label="Repartition des mouvements du jour entre depots et retraits">
                    <span class="part-credit" style="width: ${partDesDepots}%"></span>
                    <span class="part-debit" style="width: ${100 - partDesDepots}%"></span>
                </div>
                <div class="d-flex justify-content-between sous-marque mt-2">
                    <span>Depots</span>
                    <span>Retraits</span>
                </div>

                <hr class="mt-4">
                <div class="row text-muted small">
                    <div class="col-6">Utilisateurs : ${statistiques.nombreDUtilisateurs}</div>
                    <div class="col-6">Agences : ${statistiques.nombreDAgences}</div>
                </div>
            </div>
        </div>
    </div>

    <div class="col-12 col-lg-4">
        <div class="card h-100">
            <div class="card-header bg-white fw-semibold">Actions rapides</div>
            <div class="card-body d-grid gap-2">
                <a href="${pageContext.request.contextPath}/clients/nouveau"
                   class="btn btn-outline-primary">Nouveau client</a>
                <a href="${pageContext.request.contextPath}/accounts/nouveau"
                   class="btn btn-outline-primary">Ouvrir un compte</a>
                <a href="${pageContext.request.contextPath}/operations/deposit"
                   class="btn btn-outline-success">Effectuer un depot</a>
                <a href="${pageContext.request.contextPath}/operations/withdraw"
                   class="btn btn-outline-warning">Effectuer un retrait</a>
                <a href="${pageContext.request.contextPath}/operations/transfer"
                   class="btn btn-outline-secondary">Effectuer un virement</a>
            </div>
        </div>
    </div>
</div>

<jsp:include page="layout/footer.jsp"/>
