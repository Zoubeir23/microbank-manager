# MicroBank Manager — Documentation technique

Projet de fin de module — Developpement Web Java / Jakarta EE — L3 IAGE 2025/2026.

---

## 1. Architecture MVC

L'application suit une architecture MVC en quatre couches, chacune avec une seule
responsabilite. Aucune couche ne saute la suivante.

```
Navigateur
    |  requete HTTP
    v
[ Filtre ]  AuthenticationFilter : session obligatoire + verification anti-CSRF
    |       AdminAuthorizationFilter : /users/* reserve aux ADMIN
    v
[ Controleur ]  Servlets : lisent les parametres, appellent un service,
    |           choisissent la vue. Aucune regle metier, aucun SQL.
    v
[ Service ]  Regles metier et perimetre des transactions.
    |        C'est ici que commence et se termine une transaction.
    v
[ DAO ]  Requetes JPQL. Recoit l'EntityManager, n'ouvre jamais de transaction.
    |
    v
[ JPA / Hibernate ] --> PostgreSQL
    ^
    |
[ Vue ]  JSP + JSTL. Affichage uniquement, aucun appel a un service.
```

**Repartition des roles**

| Element | Role |
|---|---|
| **Servlet** | Point d'entree HTTP. Lit la requete, delegue, redirige ou affiche une JSP. |
| **JSP** | Vue. Utilise JSTL (`c:forEach`, `c:if`, `c:out`) et n'execute aucune logique metier. |
| **HttpSession** | Conserve l'utilisateur connecte entre deux requetes (`session.setAttribute("user", user)`). |
| **Service** | Regles metier, validation, transactions. |
| **DAO** | Acces aux donnees via `EntityManager` et JPQL. |
| **EntityManager** | Gere le contexte de persistance : `persist`, `merge`, `find`, requetes. |
| **persistence.xml** | Declare l'unite de persistance : connexion, dialecte, entites. |

**Ecart assume par rapport aux TP du module.** Dans les TP, chaque repository ouvrait
son propre `EntityManager` **et** sa propre transaction. Ce decoupage est impossible ici :
un virement doit debiter un compte, en crediter un autre et ecrire deux lignes
d'historique **dans une seule transaction**. Les DAO recoivent donc l'`EntityManager`
en parametre, et la classe `TransactionExecutor` porte le `begin()`, le `commit()` et
le `rollback()` pour toute la couche service.

---

## 2. Modele de donnees

```
                    +-------------+
                    |    User     |
                    +-------------+
                    | login       |
                    | motDePasse  |  (empreinte PBKDF2)
                    | role        |
                    | statut      |
                    +------+------+
                           | 1
                           |
                           | *
+----------+ 1      * +----+------+ 1     * +-------------+
| Client   |----------|  Account  |---------|  Operation  |
+----------+          +-----------+         +-------------+
| nom      |          | numeroCpt |         | reference   |
| prenom   |          | type      |         | type        |
| telephone|          | solde     |         | sens        |
| numPiece |          | statut    |         | montant     |
| statut   |          | version   |         | soldeApres  |
+----+-----+          +-----+-----+         | dateOper.   |
     | 1                    | *             +-------------+
     |                      |
     | 1                    | 1
+----+-----------+    +-----+-----+
| ClientDocument |    |  Agency   |
+----------------+    +-----------+
| nomFichier     |    | code      |
| typeContenu    |    | nom       |
| contenu (bytea)|    | ville     |
+----------------+    +-----------+
```

**Relations JPA**

| Relation | Annotation | Explication |
|---|---|---|
| `Client 1 — * Account` | `@OneToMany(mappedBy="client")` cote Client, `@ManyToOne` cote Account | Un client detient plusieurs comptes. La cle etrangere `client_id` est portee par `accounts` : c'est le cote proprietaire. |
| `Account 1 — * Operation` | `@OneToMany(mappedBy="compte")` / `@ManyToOne` | Chaque ligne d'historique appartient a un compte. |
| `User 1 — * Operation` | `@OneToMany(mappedBy="user")` / `@ManyToOne` | On sait quel agent a saisi chaque operation. |
| `Client 1 — 1 ClientDocument` | `@OneToOne` | Un client possede au plus une piece d'identite numerisee. |
| `Agency 1 — * Account` | `@OneToMany` / `@ManyToOne` | Rattachement d'un compte a une agence (bonus 4). |

Toutes les associations sont en `FetchType.LAZY`. Les donnees necessaires a une vue
sont chargees explicitement avec `JOIN FETCH`, ce qui evite a la fois les
`LazyInitializationException` et le probleme des N+1 requetes.

**Choix de modelisation notables**

- **Le virement produit deux lignes `Operation`** partageant la meme `reference` :
  un `DEBIT` sur le compte source, un `CREDIT` sur le compte destination. Chaque compte
  a donc un historique complet et lisible, et le releve PDF n'a aucun cas particulier
  a traiter.
- **`soldeApres` est fige dans l'operation**, comme sur un vrai releve bancaire :
  l'historique reste exact meme si le compte evolue ensuite.
- **`@Version` sur `Account`** : verrou optimiste. Deux operations simultanees sur le
  meme compte ne peuvent pas ecraser silencieusement le solde de l'autre.
- **Montants en `BigDecimal(15,2)`**, jamais en `double` : pas d'erreur d'arrondi.

---

## 3. Fonctionnalites

| Fonctionnalite | Mise en oeuvre |
|---|---|
| Authentification | `LoginServlet` + `HttpSession`. La session est recreee apres connexion (protection contre la fixation de session). |
| Protection des pages | `AuthenticationFilter` sur `/*` : sans session, redirection vers `/login`. |
| Roles | `AdminAuthorizationFilter` : `/users/*` renvoie 403 pour un agent. |
| Gestion des clients | CRUD complet, validation champ par champ, unicite du numero de piece. |
| Recherche | Requete JPQL sur nom, prenom, telephone ou numero de piece. |
| Pagination | `setFirstResult()` / `setMaxResults()` + requete `COUNT` : la base ne renvoie qu'une page. |
| Comptes | Ouverture avec numero unique sequentiel, depot initial journalise, changement de statut. |
| Depot / retrait / virement | `OperationService`, une transaction par operation. |
| Regles metier | Montant strictement positif, compte existant et actif, solde suffisant, comptes source et destination differents. |
| Historique | Filtres combinables (type, periode, montant min/max, client), pagination, totaux. |
| Releve PDF | OpenPDF : entete, identite du titulaire, periode, tableau des operations, totaux. |
| Export CSV | Separateur `;`, BOM UTF-8 pour Excel, protection contre l'injection de formules. |
| Tableau de bord | Agregats calcules en base (`COUNT`, `SUM`). |
| Bonus | Upload de la piece d'identite, filtre avance, statistiques supplementaires, agences, version imprimable du releve. |

**Transactions (§16).** Exemple du virement, integralement dans
`OperationService.virer()` :

```java
return transactionExecutor.executeInTransaction(entityManager -> {
    // 1. verifications : montant, comptes differents, comptes actifs, solde suffisant
    // 2. debit du compte source      -> solde et ligne d'historique
    // 3. credit du compte destination -> solde et ligne d'historique
    // 4. les deux operations sont persistees
});
```

`TransactionExecutor` ouvre la transaction, la valide en sortie et, si une exception
survient a n'importe quelle etape, execute un `rollback()` : aucun compte n'est
modifie, aucune ligne d'historique n'est ecrite. Le test
`virementAuSoldeInsuffisantEstAnnule` verifie precisement ce comportement.

**Securite.** Mots de passe haches en PBKDF2-HMAC-SHA256 (120 000 iterations, sel
aleatoire) et jamais transmis aux JSP ; jeton anti-CSRF sur chaque formulaire ;
echappement systematique avec `<c:out>` contre les injections HTML ; requetes JPQL
toujours parametrees, jamais concatenees avec la saisie utilisateur.

---

## 4. Difficultes rencontrees

**1. Perimetre des transactions.** Le modele des TP, ou chaque repository gerait sa
propre transaction, rendait le virement impossible a implementer correctement : un
incident entre le debit et le credit aurait fait disparaitre de l'argent. Il a fallu
remonter la transaction d'un cran, dans la couche service, et retirer toute gestion
de transaction des DAO.

**2. Chargement paresseux et JSP.** Les premieres pages de detail levaient une
`LazyInitializationException` : la JSP tentait de lire une association alors que
l'`EntityManager` etait deja ferme. Corrige en chargeant explicitement les donnees
necessaires avec `JOIN FETCH` (`findByIdAvecClient`, `findByIdAvecDocument`) plutot
qu'en laissant la session ouverte pendant le rendu.

**3. Records Java et langage d'expression des JSP.** Les DTO ont ete ecrits comme des
`record` (immuables). Or l'EL de Jakarta EE 10 cherche des accesseurs `getXxx()` et ne
reconnait pas les accesseurs de record `xxx()`, ce qui produisait une
`PropertyNotFoundException`. Des accesseurs au format JavaBean ont ete ajoutes aux
records exposes aux vues.

**4. Stockage du fichier de la piece d'identite.** L'annotation `@Lob` sur un `byte[]`
produit sous PostgreSQL une colonne `oid`, c'est-a-dire un "large object" stocke a part
qui n'est pas supprime avec la ligne. Remplacee par `@JdbcTypeCode(SqlTypes.VARBINARY)`
pour obtenir une vraie colonne `bytea`.

**5. Jeton anti-CSRF et envoi de fichier.** Sur une requete `multipart/form-data`, le
filtre lit les parametres avant que la servlet n'ait analyse le corps de la requete.
Le jeton du formulaire d'upload est donc transmis dans l'URL, ou il reste lisible par
le filtre en toute circonstance.

**6. Coherence entre `database.sql` et les entites.** Plutot que d'ecrire le script SQL
a la main et d'esperer qu'il corresponde, le schema a ete genere depuis le mapping
Hibernate, puis commente et enrichi (index, noms de contraintes explicites, jeu de
donnees). La correspondance a ensuite ete verifiee en demarrant Hibernate en mode
`validate` sur une base creee uniquement a partir du script.
