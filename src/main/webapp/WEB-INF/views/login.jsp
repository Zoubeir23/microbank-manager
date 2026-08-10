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
<body class="page-connexion">

<div class="container">
    <div class="row justify-content-center align-items-center g-4 g-lg-5">

        <%-- Colonne de gauche : identite de l'institution. --%>
        <div class="col-lg-6 d-none d-lg-block">
            <p class="sous-marque mb-2">Institution de microfinance</p>
            <h1 class="marque-connexion mb-3">MicroBank<span class="point">.</span></h1>
            <p class="text-muted mb-4" style="max-width: 38ch;">
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

        <%-- Colonne de droite : formulaire de connexion. --%>
        <div class="col-12 col-md-8 col-lg-5 offset-lg-1">

            <div class="d-lg-none text-center mb-4">
                <h1 class="marque-connexion">MicroBank<span class="point">.</span></h1>
                <p class="sous-marque">Espace agent</p>
            </div>

            <div class="card carte-connexion">
                <div class="card-body p-4 p-md-5">

                    <h2 class="h5 mb-1">Connexion</h2>
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

                </div>
            </div>

            <p class="text-center sous-marque mt-4 mb-0">
                L3 IAGE &middot; 2025 / 2026
            </p>
        </div>
    </div>
</div>

<script src="${contexte}/assets/js/bootstrap.bundle.min.js"></script>
</body>
</html>
