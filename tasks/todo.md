# MicroBank Manager — Plan d'implémentation

Projet de fin de module — Développement Web Java / Jakarta EE (L3 IAGE, 2025-2026).
Deadline : 25 août 2026.

## 1. Terrain (constaté, pas supposé)

| Élément | Constat |
|---|---|
| Dépôt | `ProjetFinEtude`, vide, branche `main` sans commit |
| JDK | OpenJDK 26.0.1 (Homebrew) — seul JDK installé |
| Maven | 3.9.14 |
| Base de données | PostgreSQL 17 en écoute sur `localhost:5432`, user `zoubeir`, sans mot de passe |
| Serveur | Aucun Tomcat/Jetty installé localement |
| Projets du cours | `tp-java-jpa`, `book-management-java_jpa`, `demo-java-jpa` |

### Conventions héritées des TP du module (à respecter)
- `groupId` = `sn.isi.iage`, packages `sn.isi.iage.*`
- Packages `entity`, `repository`, `enums`
- Lombok (`@Getter/@Setter/@SuperBuilder/@NoArgsConstructor`)
- `BaseEntity` `@MappedSuperclass` : `id`, `createdAt`, `updatedAt`, `@PrePersist/@PreUpdate`
- Hibernate ORM 7.3.4.Final, driver PostgreSQL 42.7.3
- `persistence.xml` : `RESOURCE_LOCAL`, `<class>` listés explicitement, `PostgreSQLDialect`
- `JpaUtil` : `EntityManagerFactory` statique

### Écart assumé par rapport aux TP
Dans les TP, chaque `Repository` ouvre son `EntityManager` **et** sa transaction.
Impossible ici : un virement doit débiter, créditer et journaliser dans **une seule**
transaction (§16 du cahier des charges). Donc :
- les DAO reçoivent l'`EntityManager` en paramètre (aucune transaction dedans) ;
- la couche `service` ouvre l'`EntityManager`, fait `begin/commit/rollback`.

## 2. Décisions techniques

| Sujet | Choix | Raison |
|---|---|---|
| Serveur | WAR + Tomcat 10.1 ; dev via plugin Maven | Jakarta EE 10 = `jakarta.*`, exigé par le sujet |
| Version Java | `release` figé (pas 26) | Jasper/ecj doit pouvoir compiler les JSP |
| Base | PostgreSQL | Déjà installée et utilisée dans les TP |
| Schéma | `database.sql` + `hibernate.hbm2ddl.auto=validate` | Livrable exigé (§28) |
| PDF | OpenPDF (fork LGPL d'iText) | Tableaux simples, licence saine |
| Mots de passe | PBKDF2WithHmacSHA256 (JDK, 0 dépendance) | Jamais de mot de passe en clair |
| Bootstrap | Vendored dans `webapp/assets` | Démo hors ligne possible en soutenance |
| Tests | JUnit 5 + H2 en mémoire | Règles métier + DAO/pagination testables |

## 3. Modèle de données

```
User 1 ────* Operation
Client 1 ───* Account 1 ───* Operation
Client 1 ───1 ClientDocument   (@OneToOne, bonus 1 : pièce d'identité)
Agency 1 ───* Account          (bonus 4)
```

- `Operation` porte `sens` (DEBIT/CREDIT), `soldeApres`, `compteContrepartie`.
- Un virement = **2 lignes** `Operation` (débit source + crédit destination), même
  transaction, même référence de groupe.
- Montants en `BigDecimal(15,2)`.

## 4. Découpage (chaque étape = 1 ou plusieurs commits atomiques)

### Phase 0 — Socle
- [x] 0.1 Analyse du terrain et des conventions du module
- [x] 0.2 Plan (ce fichier)
- [ ] 0.3 Spike : vérifier que les JSP compilent sur ce poste (JDK 26)
- [ ] 0.4 `pom.xml`, arborescence Maven webapp, `.gitignore`

### Phase 1 — Persistance
- [ ] 1.1 `enums` : Role, Statut, TypeCompte, StatutCompte, TypeOperation, SensOperation
- [ ] 1.2 `BaseEntity`
- [ ] 1.3 Entités : User, Client, Account, Operation, ClientDocument, Agency
- [ ] 1.4 `persistence.xml` (unité principale + unité de test H2)
- [ ] 1.5 `JpaUtil` + exécuteur de transaction (`begin/commit/rollback`)
- [ ] 1.6 `database.sql` (schéma + jeu de données + comptes de test)

### Phase 2 — DAO
- [ ] 2.1 `PageResult<T>` (immutable)
- [ ] 2.2 `UserDAO`
- [ ] 2.3 `ClientDAO` (recherche + pagination JPA)
- [ ] 2.4 `AccountDAO`
- [ ] 2.5 `OperationDAO` (filtres combinés + pagination JPA)
- [ ] 2.6 `DashboardDAO` (agrégats)

### Phase 3 — Services (règles métier + transactions)
- [ ] 3.1 `PasswordHasher` + `AuthenticationService`
- [ ] 3.2 `ClientService` (validation formulaire)
- [ ] 3.3 `AccountService` (numéro de compte unique, dépôt initial)
- [ ] 3.4 `OperationService` : dépôt, retrait, virement — 1 transaction, rollback
- [ ] 3.5 `UserService` (admin : CRUD + activer/désactiver)
- [ ] 3.6 `DashboardService`

### Phase 4 — Web (contrôleurs)
- [ ] 4.1 `AuthenticationFilter` + `AdminAuthorizationFilter`
- [ ] 4.2 `LoginServlet`, `LogoutServlet`
- [ ] 4.3 `DashboardServlet`
- [ ] 4.4 `ClientServlet` (list/search/pagination/create/update/delete/details)
- [ ] 4.5 `AccountServlet`
- [ ] 4.6 `OperationServlet` (deposit/withdraw/transfer/history/filtres)
- [ ] 4.7 `StatementPdfServlet` + `OperationCsvExportServlet`
- [ ] 4.8 `UserServlet` (admin)
- [ ] 4.9 `ClientDocumentServlet` (upload, bonus 1)

### Phase 5 — Vues
- [ ] 5.1 Assets Bootstrap 5 + layout (header/footer/navbar/messages)
- [ ] 5.2 `login.jsp`, `dashboard.jsp`
- [ ] 5.3 `clients/` list, form, details
- [ ] 5.4 `accounts/` list, form, details
- [ ] 5.5 `operations/` list, deposit, withdraw, transfer, statement imprimable
- [ ] 5.6 `users/` list, form
- [ ] 5.7 `error.jsp` + pages 403/404

### Phase 6 — Vérification
- [ ] 6.1 Tests unitaires règles métier (dépôt/retrait/virement, rollback)
- [ ] 6.2 Tests DAO (recherche, pagination, filtres)
- [ ] 6.3 `mvn test` vert
- [ ] 6.4 Lancement réel + parcours complet dans le navigateur
- [ ] 6.5 `README.md` + `docs/documentation.md` (2 pages)

## 5. Risques et parade

| Risque | Parade |
|---|---|
| JSP incompilables sous JDK 26 (Jasper/ecj) | Spike 0.3 avant tout code ; `release` figé ; sinon installer un JDK LTS |
| `LazyInitializationException` dans les JSP | Charger explicitement (`JOIN FETCH`) ou mapper vers des DTO avant de fermer l'EM |
| Numéro de compte dupliqué | Contrainte `unique` en base + génération séquentielle dans la transaction |
| Solde incohérent en cas d'erreur | `rollback` systématique dans le `catch`, testé |
| Rollback | Chaque phase est commitée séparément ; `git revert` d'un commit isolé possible |
