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
<body>

<div class="container" style="max-width: 420px; margin-top: 8vh;">

    <div class="text-center mb-4">
        <h1 class="h3 fw-bold" style="color: var(--microbank-bleu);">MICROBANK</h1>
        <p class="text-muted">Espace agent</p>
    </div>

    <div class="card shadow-sm">
        <div class="card-body p-4">

            <c:if test="${not empty erreurAuthentification}">
                <div class="alert alert-danger" role="alert">
                    <c:out value="${erreurAuthentification}"/>
                </div>
            </c:if>

            <form method="post" action="${contexte}/login">
                <div class="mb-3">
                    <label for="login" class="form-label">Login</label>
                    <input type="text" class="form-control" id="login" name="login"
                           value="<c:out value='${login}'/>" required autofocus>
                </div>

                <div class="mb-4">
                    <label for="motDePasse" class="form-label">Mot de passe</label>
                    <input type="password" class="form-control" id="motDePasse"
                           name="motDePasse" required>
                </div>

                <button type="submit" class="btn btn-primary w-100">Se connecter</button>
            </form>

        </div>
    </div>

    <p class="text-center text-muted small mt-4">
        Projet de fin de module - L3 IAGE - 2025/2026
    </p>
</div>

<script src="${contexte}/assets/js/bootstrap.bundle.min.js"></script>
</body>
</html>
