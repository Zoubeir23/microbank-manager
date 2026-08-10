<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="contexte" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>MicroBank Manager<c:if test="${not empty titrePage}"> - <c:out value="${titrePage}"/></c:if></title>
    <link rel="stylesheet" href="${contexte}/assets/css/bootstrap.min.css">
    <link rel="stylesheet" href="${contexte}/assets/css/microbank.css">
</head>
<body>

<nav class="navbar navbar-expand-lg navbar-microbank mb-4">
    <div class="container">
        <a class="navbar-brand fw-bold" href="${contexte}/dashboard">MicroBank Manager</a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse"
                data-bs-target="#menuPrincipal" aria-controls="menuPrincipal"
                aria-expanded="false" aria-label="Afficher le menu">
            <span class="navbar-toggler-icon"></span>
        </button>

        <div class="collapse navbar-collapse" id="menuPrincipal">
            <ul class="navbar-nav me-auto">
                <li class="nav-item">
                    <a class="nav-link ${menuActif eq 'dashboard' ? 'actif' : ''}"
                       href="${contexte}/dashboard">Tableau de bord</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link ${menuActif eq 'clients' ? 'actif' : ''}"
                       href="${contexte}/clients">Clients</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link ${menuActif eq 'accounts' ? 'actif' : ''}"
                       href="${contexte}/accounts">Comptes</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link ${menuActif eq 'operations' ? 'actif' : ''}"
                       href="${contexte}/operations">Operations</a>
                </li>
                <c:if test="${sessionScope.user.role eq 'ADMIN'}">
                    <li class="nav-item">
                        <a class="nav-link ${menuActif eq 'users' ? 'actif' : ''}"
                           href="${contexte}/users">Utilisateurs</a>
                    </li>
                </c:if>
            </ul>

            <ul class="navbar-nav">
                <li class="nav-item">
                    <span class="nav-link">
                        <c:out value="${sessionScope.user.nomComplet}"/>
                        <span class="badge text-bg-light ms-1">
                            <c:out value="${sessionScope.user.role.libelle}"/>
                        </span>
                    </span>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="${contexte}/logout">Deconnexion</a>
                </li>
            </ul>
        </div>
    </div>
</nav>

<main class="container pb-5">
