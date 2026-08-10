# MicroBank Manager

Application web de gestion d'une institution de microfinance.

Projet de fin de module — **Developpement Web Java / Jakarta EE** — Licence 3 IAGE, 2025/2026.

L'application permet a un agent de gerer les clients, leurs comptes et les operations
bancaires (depot, retrait, virement), de consulter l'historique filtre et pagine,
de generer un releve PDF et d'exporter les operations en CSV.

---

## 1. Prerequis

| Outil | Version | Verification |
|---|---|---|
| JDK | 17 ou superieur | `java -version` |
| Maven | 3.8 ou superieur | `mvn -version` |
| PostgreSQL | 13 ou superieur | `psql --version` |

Aucun serveur d'application n'a besoin d'etre installe : le plugin Maven Jetty
embarque le serveur. Un fichier WAR deployable sur Tomcat 10.1 est egalement produit.

> Le projet compile en bytecode Java 17 (`maven.compiler.release`) : il fonctionne sur
> un JDK recent tout en restant lisible par le compilateur de JSP du serveur.

---

## 2. Creation de la base de donnees

```bash
createdb microbank
psql -d microbank -f database.sql
```

Le script `database.sql` cree les six tables, leurs contraintes, leurs index, puis
insere un jeu de donnees de depart (2 utilisateurs, 2 agences, 3 clients, 3 comptes).

Verification :

```bash
psql -d microbank -c "SELECT login, role FROM users;"
```

---

## 3. Configuration de persistence.xml

Le fichier `src/main/resources/META-INF/persistence.xml` contient l'unite de
persistance `microbank-pu`. Adaptez les trois lignes de connexion a votre poste :

```xml
<property name="jakarta.persistence.jdbc.url"
          value="jdbc:postgresql://localhost:5432/microbank"/>
<property name="jakarta.persistence.jdbc.user"     value="postgres"/>
<property name="jakarta.persistence.jdbc.password" value="votre_mot_de_passe"/>
```

Autres proprietes utiles :

| Propriete | Valeur livree | Role |
|---|---|---|
| `hibernate.dialect` | `PostgreSQLDialect` | Dialecte SQL genere par Hibernate |
| `hibernate.hbm2ddl.auto` | `update` | Complete le schema si une table manque |
| `hibernate.show_sql` | `true` | Affiche les requetes SQL dans la console |

Le schema de reference reste `database.sql`. Passer `hbm2ddl.auto` a `validate`
permet de verifier que la base correspond exactement aux entites.

---

## 4. Lancement de l'application

### En developpement (aucun serveur a installer)

```bash
mvn jetty:run
```

Puis ouvrir <http://localhost:8080/microbank/login>.

### Deploiement sur Tomcat 10.1

```bash
mvn clean package
cp target/microbank.war $CATALINA_HOME/webapps/
```

L'application est alors disponible sur <http://localhost:8080/microbank/>.

> Tomcat 10.1 est requis (Jakarta EE 10, paquets `jakarta.*`). Tomcat 9 ne convient pas.

---

## 5. Comptes de test

| Login | Mot de passe | Role | Droits |
|---|---|---|---|
| `admin` | `admin123` | ADMIN | Tout, y compris la gestion des utilisateurs |
| `agent` | `agent123` | AGENT | Clients, comptes, operations, releves |

Les mots de passe ne sont jamais stockes en clair : la base contient une empreinte
PBKDF2-HMAC-SHA256 (120 000 iterations, sel aleatoire par utilisateur).

Si la table `users` est vide au demarrage, l'application recree automatiquement ces
deux comptes (`DatabaseSeedListener`).

---

## 6. Parcours de demonstration

1. **Connexion** avec `admin` / `admin123`
2. **Tableau de bord** : nombre de clients, de comptes, solde total, operations du jour
3. **Clients** : creation, recherche (nom, prenom, telephone, numero de piece), pagination
4. **Comptes** : ouverture d'un compte avec depot initial, consultation du detail
5. **Operations** : depot, retrait, virement
6. **Historique** : filtres par type, periode et montant, pagination
7. **Releve PDF** puis **export CSV** depuis l'historique
8. **Utilisateurs** (admin uniquement) : creation, activation, desactivation

---

## 7. Tests

```bash
mvn test
```

89 tests automatises s'executent sur une base H2 en memoire : aucun PostgreSQL n'est
necessaire pour les lancer. Ils couvrent les regles metier (§15), le comportement
transactionnel et le rollback (§16), la recherche, la pagination, les filtres de
l'historique, l'authentification, la mise en forme des montants, l'export CSV
et la generation du releve PDF.

---

## 8. Interface

Bootstrap 5.3 fournit la grille et les composants (navbar, formulaires, tableaux,
alertes, pagination) ; `assets/css/microbank.css` pose l'identite visuelle par-dessus,
sans modifier le balisage.

Parti pris : **un registre comptable edite**, pas un tableau de bord generique.

| Choix | Raison |
|---|---|
| Fond papier chaud, encre vert foret, accent terre cuite | Identite sobre et institutionnelle, lisible en salle de soutenance |
| Titres en Fraunces, texte en IBM Plex Sans | Une serif de caractere pour la hierarchie, une sans technique pour la lecture |
| Montants, dates et references en IBM Plex Mono | Chiffres tabulaires : les colonnes s'alignent verticalement comme dans un livre de comptes |
| Vert pour les credits, rouge hachure pour les debits | Le sens d'un mouvement se lit sans lire le libelle |
| Polices, CSS et JS embarques dans le WAR | La demonstration fonctionne sans connexion internet |

Les animations se limitent a l'ouverture de page (apparition decalee des compteurs,
barre de repartition) et sont desactivees si le systeme demande
`prefers-reduced-motion`.

---

## 9. Structure du projet

```
src/main/java/sn/isi/iage/microbank/
├── config/       DatabaseSeedListener (demarrage et arret de l'application)
├── controller/   Servlets : Login, Logout, Dashboard, Client, Account, Operation,
│                 User, StatementPdf, OperationCsvExport, ClientDocument
├── dao/          Couche d'acces aux donnees (repositories) + JpaUtil + TransactionExecutor
├── dto/          Objets de transport : PageResult, formulaires, criteres de recherche
├── model/        Entites JPA : User, Client, ClientDocument, Agency, Account, Operation
├── enums/        Role, Statut, TypeCompte, StatutCompte, TypeOperation, SensOperation
├── exception/    BusinessRuleException, ValidationException, ResourceNotFoundException
├── filter/       AuthenticationFilter, AdminAuthorizationFilter
├── service/      Regles metier et perimetre des transactions
└── util/         PasswordHasher, FormValidator, ValueParser, CsrfTokenManager,
                  AmountFormatter, StatementPdfWriter, OperationCsvWriter

src/main/webapp/
├── assets/
│   ├── css/      Bootstrap 5.3, microbank.css (identite visuelle), polices.css
│   ├── fonts/    Fraunces et IBM Plex embarques (licence SIL OFL 1.1)
│   └── js/       Bootstrap bundle
└── WEB-INF/
    ├── web.xml           Sessions, pages d'erreur, declaration du taglib
    ├── microbank.tld     Fonctions de formatage utilisables dans les JSP
    └── views/            login, dashboard, clients/, accounts/, operations/, users/
```

---

## 10. Principales URL

| Methode | URL | Role |
|---|---|---|
| GET / POST | `/login` | Formulaire de connexion |
| GET | `/logout` | Deconnexion |
| GET | `/dashboard` | Tableau de bord |
| GET | `/clients?search=&page=0&size=10` | Liste, recherche et pagination |
| POST | `/clients/create`, `/clients/update` | Creation et modification |
| GET | `/clients/details?id=`, `/clients/delete?id=` | Consultation et suppression |
| POST / GET | `/clients/document` | Depot et consultation de la piece d'identite |
| GET | `/accounts`, `/accounts/details?id=` | Comptes |
| POST | `/accounts/create`, `/accounts/statut` | Ouverture, changement de statut |
| POST | `/operations/deposit`, `/withdraw`, `/transfer` | Operations bancaires |
| GET | `/operations?accountId=&type=&dateDebut=&dateFin=&page=&size=` | Historique filtre |
| GET | `/operations/statement.pdf?accountId=` | Releve PDF |
| GET | `/operations/export.csv?accountId=` | Export CSV |
| GET / POST | `/users` | Gestion des utilisateurs (ADMIN) |

---

## 11. Documentation

- `docs/documentation.md` : architecture MVC, modele de donnees, fonctionnalites et
  difficultes rencontrees.
- `tasks/todo.md` : plan d'implementation et decisions techniques.
