<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="creation" value="${empty formulaire.id}"/>
<c:set var="titrePage" value="${creation ? 'Nouveau client' : 'Modifier le client'}" scope="request"/>
<c:set var="menuActif" value="clients" scope="request"/>
<c:set var="contexte" value="${pageContext.request.contextPath}"/>
<jsp:include page="../layout/header.jsp"/>

<h1 class="h3 mb-3">${creation ? 'Nouveau client' : 'Modifier le client'}</h1>

<jsp:include page="../layout/messages.jsp"/>

<div class="card">
    <div class="card-body">
        <%-- Formulaire multipart : le jeton anti-CSRF passe par l'URL, pas par un champ
             cache, car le filtre lit les parametres avant que la servlet n'ait analyse
             le corps multipart (voir AuthenticationFilter). --%>
        <form method="post"
              action="${contexte}/clients/${creation ? 'create' : 'update'}?csrfToken=${csrfToken}"
              enctype="multipart/form-data" novalidate>
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
                    <label for="dateNaissance" class="form-label">Date de naissance</label>
                    <input type="date" id="dateNaissance" name="dateNaissance"
                           class="form-control ${not empty erreurs['dateNaissance'] ? 'is-invalid' : ''}"
                           value="<c:out value='${formulaire.dateNaissance}'/>">
                    <div class="invalid-feedback"><c:out value="${erreurs['dateNaissance']}"/></div>
                </div>

                <div class="col-12 col-md-6">
                    <label for="telephone" class="form-label">Telephone *</label>
                    <input type="text" id="telephone" name="telephone" required
                           class="form-control ${not empty erreurs['telephone'] ? 'is-invalid' : ''}"
                           value="<c:out value='${formulaire.telephone}'/>">
                    <div class="invalid-feedback"><c:out value="${erreurs['telephone']}"/></div>
                </div>

                <div class="col-12 col-md-6">
                    <label for="email" class="form-label">Email</label>
                    <input type="email" id="email" name="email"
                           class="form-control ${not empty erreurs['email'] ? 'is-invalid' : ''}"
                           value="<c:out value='${formulaire.email}'/>">
                    <div class="invalid-feedback"><c:out value="${erreurs['email']}"/></div>
                </div>

                <div class="col-12 col-md-6">
                    <label for="numeroPiece" class="form-label">Numero de piece *</label>
                    <input type="text" id="numeroPiece" name="numeroPiece" required
                           class="form-control ${not empty erreurs['numeroPiece'] ? 'is-invalid' : ''}"
                           value="<c:out value='${formulaire.numeroPiece}'/>">
                    <div class="invalid-feedback"><c:out value="${erreurs['numeroPiece']}"/></div>
                </div>

                <div class="col-12 col-md-8">
                    <label for="adresse" class="form-label">Adresse</label>
                    <input type="text" id="adresse" name="adresse"
                           class="form-control ${not empty erreurs['adresse'] ? 'is-invalid' : ''}"
                           value="<c:out value='${formulaire.adresse}'/>">
                    <div class="invalid-feedback"><c:out value="${erreurs['adresse']}"/></div>
                </div>

                <div class="col-12 col-md-4">
                    <label for="statut" class="form-label">Statut</label>
                    <select id="statut" name="statut" class="form-select">
                        <option value="ACTIF" ${formulaire.statut eq 'ACTIF' ? 'selected' : ''}>
                            Actif
                        </option>
                        <option value="INACTIF" ${formulaire.statut eq 'INACTIF' ? 'selected' : ''}>
                            Inactif
                        </option>
                    </select>
                </div>

                <%-- Piece d'identite (bonus 1) : toujours facultative, un client peut
                     etre cree ou modifie sans document. --%>
                <div class="col-12">
                    <label for="document" class="form-label">Piece d'identite</label>
                    <input type="file" id="document" name="document"
                           accept="image/jpeg,image/png,application/pdf"
                           class="form-control ${not empty erreurs['document'] ? 'is-invalid' : ''}">
                    <div class="invalid-feedback"><c:out value="${erreurs['document']}"/></div>
                    <div class="form-text">
                        JPEG, PNG ou PDF, 2 Mo maximum.
                        <c:if test="${not empty documentActuel}">
                            Document actuel : <c:out value="${documentActuel.nomFichier}"/>
                            &mdash; laisser ce champ vide pour ne pas le changer.
                        </c:if>
                    </div>
                </div>
            </div>

            <div class="mt-4 d-flex gap-2">
                <button type="submit" class="btn btn-primary">Enregistrer</button>
                <a href="${contexte}/clients" class="btn btn-outline-secondary">Annuler</a>
            </div>
        </form>
    </div>
</div>

<jsp:include page="../layout/footer.jsp"/>
