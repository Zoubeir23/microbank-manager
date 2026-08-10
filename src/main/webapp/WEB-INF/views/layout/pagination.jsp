<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%--
  Barre de pagination reutilisable.
  Attributs attendus dans la requete :
    page              : objet PageResult
    urlPagination     : chemin de base, par exemple "/clients"
    parametresConserves : parametres a reporter sur chaque lien, par exemple "&search=diop"
--%>
<c:if test="${page.totalPages > 1}">
    <nav aria-label="Pagination" class="zone-non-imprimable">
        <ul class="pagination justify-content-center">
            <li class="page-item ${page.premierePage ? 'disabled' : ''}">
                <a class="page-link"
                   href="${pageContext.request.contextPath}${urlPagination}?page=${page.numeroPage - 1}&size=${page.taillePage}${parametresConserves}">
                    Precedent
                </a>
            </li>

            <c:forEach var="numero" begin="0" end="${page.totalPages - 1}">
                <li class="page-item ${numero eq page.numeroPage ? 'active' : ''}">
                    <a class="page-link"
                       href="${pageContext.request.contextPath}${urlPagination}?page=${numero}&size=${page.taillePage}${parametresConserves}">
                        ${numero + 1}
                    </a>
                </li>
            </c:forEach>

            <li class="page-item ${page.dernierePage ? 'disabled' : ''}">
                <a class="page-link"
                   href="${pageContext.request.contextPath}${urlPagination}?page=${page.numeroPage + 1}&size=${page.taillePage}${parametresConserves}">
                    Suivant
                </a>
            </li>
        </ul>
    </nav>
</c:if>

<p class="text-center text-muted small">
    <c:choose>
        <c:when test="${page.totalElements eq 0}">Aucun resultat</c:when>
        <c:otherwise>
            Resultats ${page.premierElementAffiche} a ${page.dernierElementAffiche}
            sur ${page.totalElements}
        </c:otherwise>
    </c:choose>
</p>
