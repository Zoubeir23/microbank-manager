<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="contexte" value="${pageContext.request.contextPath}"/>
<c:set var="codeErreur" value="${requestScope['jakarta.servlet.error.status_code']}"/>
<c:set var="messageErreurTechnique" value="${requestScope['jakarta.servlet.error.message']}"/>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>MicroBank Manager - Erreur</title>
    <link rel="stylesheet" href="${contexte}/assets/css/bootstrap.min.css">
    <link rel="stylesheet" href="${contexte}/assets/css/microbank.css">
</head>
<body>

<div class="container" style="max-width: 640px; margin-top: 10vh;">
    <div class="card shadow-sm">
        <div class="card-body p-4 text-center">

            <h1 class="display-5 fw-bold">
                <c:choose>
                    <c:when test="${not empty codeErreur}">${codeErreur}</c:when>
                    <c:otherwise>Erreur</c:otherwise>
                </c:choose>
            </h1>

            <p class="lead">
                <c:choose>
                    <c:when test="${codeErreur eq 403}">
                        Vous n'avez pas les droits necessaires pour acceder a cette page.
                    </c:when>
                    <c:when test="${codeErreur eq 404}">
                        La page demandee n'existe pas.
                    </c:when>
                    <c:otherwise>
                        Une erreur est survenue pendant le traitement de votre demande.
                    </c:otherwise>
                </c:choose>
            </p>

            <%-- Le detail technique reste volontairement sobre : aucune trace
                 d'exception n'est exposee a l'utilisateur. --%>
            <c:if test="${not empty messageErreurTechnique and codeErreur ne 500}">
                <p class="text-muted"><c:out value="${messageErreurTechnique}"/></p>
            </c:if>

            <div class="mt-4 d-flex gap-2 justify-content-center">
                <a href="${contexte}/dashboard" class="btn btn-primary">Tableau de bord</a>
                <a href="${contexte}/login" class="btn btn-outline-secondary">Connexion</a>
            </div>

        </div>
    </div>
</div>

</body>
</html>
