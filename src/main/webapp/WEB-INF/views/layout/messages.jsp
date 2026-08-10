<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%--
  Messages affiches une seule fois : ils sont lus depuis la session puis retires,
  pour ne pas reapparaitre a chaque rechargement de page.
--%>
<c:if test="${not empty sessionScope.messageSucces}">
    <div class="alert alert-success alert-dismissible fade show zone-non-imprimable" role="alert">
        <c:out value="${sessionScope.messageSucces}"/>
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Fermer"></button>
    </div>
    <c:remove var="messageSucces" scope="session"/>
</c:if>

<c:if test="${not empty sessionScope.messageErreur}">
    <div class="alert alert-danger alert-dismissible fade show zone-non-imprimable" role="alert">
        <c:out value="${sessionScope.messageErreur}"/>
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Fermer"></button>
    </div>
    <c:remove var="messageErreur" scope="session"/>
</c:if>

<c:if test="${not empty erreurMetier}">
    <div class="alert alert-danger" role="alert"><c:out value="${erreurMetier}"/></div>
</c:if>

<c:if test="${not empty erreurs}">
    <div class="alert alert-warning" role="alert">
        <strong>Le formulaire contient des erreurs :</strong>
        <ul class="mb-0 mt-2">
            <c:forEach var="erreur" items="${erreurs}">
                <li><c:out value="${erreur.value}"/></li>
            </c:forEach>
        </ul>
    </div>
</c:if>
