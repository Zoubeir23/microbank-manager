<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="creation" value="${empty formulaire.id}"/>
<c:set var="titrePage" value="${creation ? 'Nouvel utilisateur' : 'Modifier l utilisateur'}"
       scope="request"/>
<c:set var="menuActif" value="users" scope="request"/>
<c:set var="contexte" value="${pageContext.request.contextPath}"/>
<jsp:include page="../layout/header.jsp"/>

<h1 class="h3 mb-3">${creation ? 'Nouvel utilisateur' : 'Modifier l\'utilisateur'}</h1>

<jsp:include page="../layout/messages.jsp"/>

<div class="card">
    <div class="card-body">
        <form method="post" action="${contexte}/users/save" novalidate>
            <input type="hidden" name="csrfToken" value="${csrfToken}">
            <input type="hidden" name="id" value="<c:out value='${formulaire.id}'/>">

            <div class="row g-3">
                <div class="col-12 col-md-6">
                    <label for="nom" class="form-label">Nom *</label>
                    <input type="text" id="nom" name="nom" required
                           class="form-control ${not empty erreurs['nom'] ? 'is-invalid' : ''}"
                           value="<c:out value='${formulaire.nom}'/>">
                    <div class="invalid-feedback"><c:out value="${erreurs['nom']}"/></div>
                </div>

                <div class="col-12 col-md-6">
                    <label for="prenom" class="form-label">Prenom *</label>
                    <input type="text" id="prenom" name="prenom" required
                           class="form-control ${not empty erreurs['prenom'] ? 'is-invalid' : ''}"
                           value="<c:out value='${formulaire.prenom}'/>">
                    <div class="invalid-feedback"><c:out value="${erreurs['prenom']}"/></div>
                </div>

                <div class="col-12 col-md-6">
                    <label for="login" class="form-label">Login *</label>
                    <input type="text" id="login" name="login" required autocomplete="off"
                           class="form-control ${not empty erreurs['login'] ? 'is-invalid' : ''}"
                           value="<c:out value='${formulaire.login}'/>">
                    <div class="invalid-feedback"><c:out value="${erreurs['login']}"/></div>
                </div>

                <div class="col-12 col-md-3">
                    <label for="role" class="form-label">Role</label>
                    <select id="role" name="role" class="form-select">
                        <c:forEach var="role" items="${roles}">
                            <option value="${role}" ${formulaire.role eq role.name() ? 'selected' : ''}>
                                <c:out value="${role.libelle}"/>
                            </option>
                        </c:forEach>
                    </select>
                </div>

                <div class="col-12 col-md-3">
                    <label for="statut" class="form-label">Statut</label>
                    <select id="statut" name="statut" class="form-select">
                        <c:forEach var="statut" items="${statuts}">
                            <option value="${statut}"
                                    ${formulaire.statut eq statut.name() ? 'selected' : ''}>
                                <c:out value="${statut.libelle}"/>
                            </option>
                        </c:forEach>
                    </select>
                </div>

                <div class="col-12 col-md-6">
                    <label for="motDePasse" class="form-label">
                        Mot de passe ${creation ? '*' : '(laisser vide pour ne pas changer)'}
                    </label>
                    <input type="password" id="motDePasse" name="motDePasse"
                           autocomplete="new-password"
                           class="form-control ${not empty erreurs['motDePasse'] ? 'is-invalid' : ''}">
                    <div class="invalid-feedback"><c:out value="${erreurs['motDePasse']}"/></div>
                </div>

                <div class="col-12 col-md-6">
                    <label for="confirmationMotDePasse" class="form-label">Confirmation</label>
                    <input type="password" id="confirmationMotDePasse"
                           name="confirmationMotDePasse" autocomplete="new-password"
                           class="form-control ${not empty erreurs['confirmationMotDePasse'] ? 'is-invalid' : ''}">
                    <div class="invalid-feedback">
                        <c:out value="${erreurs['confirmationMotDePasse']}"/>
                    </div>
                </div>
            </div>

            <div class="mt-4 d-flex gap-2">
                <button type="submit" class="btn btn-primary">Enregistrer</button>
                <a href="${contexte}/users" class="btn btn-outline-secondary">Annuler</a>
            </div>
        </form>
    </div>
</div>

<jsp:include page="../layout/footer.jsp"/>
