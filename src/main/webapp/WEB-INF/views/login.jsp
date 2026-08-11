<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="contexte" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>MicroBank Manager - Connexion</title>
    <link rel="stylesheet" href="${contexte}/assets/css/bootstrap.min.css">
    <link rel="stylesheet" href="${contexte}/assets/css/microbank.css">
</head>
<body class="corps-connexion">

<div class="ecran-connexion">

    <%-- Panneau noir : identite de l'institution. Masque sur mobile pour laisser
         toute la place au formulaire. --%>
    <div class="panneau-marque d-none d-lg-flex">
        <div>
            <p class="sous-marque mb-2">Institution de microfinance</p>
            <h1 class="marque-connexion mb-3">MicroBank<span class="point">.</span></h1>
            <p class="mb-5" style="max-width: 34ch; color: rgba(255,255,255,.65);">
                Gestion des clients, des comptes et des operations courantes.
                Espace reserve aux agents et aux administrateurs.
            </p>

            <dl class="row mb-0 liste-capacites" style="max-width: 26rem;">
                <dt class="col-6">Guichet</dt>
                <dd class="col-6 text-end">Depots &middot; Retraits</dd>
                <dt class="col-6">Virements</dt>
                <dd class="col-6 text-end">Compte a compte</dd>
                <dt class="col-6">Releves</dt>
                <dd class="col-6 text-end">PDF &middot; CSV</dd>
            </dl>
        </div>
    </div>

    <%-- Panneau blanc : formulaire de connexion. --%>
    <div class="panneau-formulaire">
        <div class="formulaire-connexion">

            <div class="d-lg-none text-center mb-4">
                <h1 class="marque-connexion" style="color: var(--noir);">
                    MicroBank<span class="point" style="color: var(--gris);">.</span>
                </h1>
                <p class="sous-marque" style="color: var(--gris);">Espace agent</p>
            </div>

            <h2 class="h4 mb-1">Connexion</h2>
            <p class="text-muted small mb-4">Identifiez-vous pour acceder au guichet.</p>

            <c:if test="${not empty erreurAuthentification}">
                <div class="alert alert-danger" role="alert">
                    <c:out value="${erreurAuthentification}"/>
                </div>
            </c:if>

            <form method="post" action="${contexte}/login">
                <div class="mb-3">
                    <label for="login" class="form-label">Login</label>
                    <input type="text" class="form-control" id="login" name="login"
                           value="<c:out value='${login}'/>" required autofocus
                           autocomplete="username">
                </div>

                <div class="mb-4">
                    <label for="motDePasse" class="form-label">Mot de passe</label>
                    <input type="password" class="form-control" id="motDePasse"
                           name="motDePasse" required autocomplete="current-password">
                </div>

                <button type="submit" class="btn btn-primary w-100">Se connecter</button>
            </form>

            <p class="text-center sous-marque mt-4 mb-0" style="color: var(--gris);">
                L3 IAGE &middot; 2025 / 2026
            </p>
        </div>
    </div>

</div>

<script src="${contexte}/assets/js/bootstrap.bundle.min.js"></script>
</body>
</html>
