# Lecons tirees

Document vivant : ce qui a casse, pourquoi, et comment l'eviter la prochaine fois.

---

## 1. Verifier la faisabilite technique avant d'ecrire le code

**Ce qui aurait pu arriver.** Le poste ne dispose que d'un JDK 26, tres en avance sur les
versions supportees par les compilateurs de JSP. Ecrire d'abord 60 fichiers puis
decouvrir que les JSP ne compilent pas aurait coute une reecriture complete.

**Ce qui a ete fait.** Un projet jetable de trois fichiers (une JSP avec une boucle JSTL)
a ete lance avant toute autre chose pour verifier la chaine complete.

**A retenir.** Quand une hypothese technique conditionne toute l'architecture, la tester
en dix minutes sur un cas minimal, avant d'investir des heures.

---

## 2. Le perimetre transactionnel se decide avant d'ecrire les DAO

**Le probleme.** Le patron des TP du module (chaque repository ouvre son
`EntityManager` et sa transaction) rend un virement incorrect : si l'incident survient
entre le debit et le credit, l'argent disparait.

**La correction.** Les DAO recoivent l'`EntityManager` en parametre et n'ouvrent aucune
transaction ; un `TransactionExecutor` unique porte `begin` / `commit` / `rollback` pour
la couche service.

**A retenir.** La question "quelles ecritures doivent reussir ou echouer ensemble ?" se
pose avant la premiere ligne de la couche d'acces aux donnees, pas apres.

---

## 3. Les tests ecrits tot ont revele les erreurs de requetes

**Constat.** Les tests des services ont ete ecrits avant les servlets et les JSP. Ils
s'executent sur H2 en memoire en moins de trois secondes et ont valide chaque requete
JPQL, les filtres combines et surtout le rollback, bien avant qu'une page web existe.

**A retenir.** Sur une application web, tester la couche metier avant la couche web
raccourcit la boucle de retour : un test qui echoue en 3 secondes vaut mieux qu'un
clic dans un navigateur apres 60 secondes de redemarrage du serveur.

---

## 4. Un serveur oublie sur le port fait perdre du temps

**Le probleme.** Un `pkill` avec un motif trop precis n'a pas tue l'instance Jetty
existante. Le demarrage suivant a echoue silencieusement en arriere-plan
(`Address already in use`), et les tests ont ete joues contre l'ancien code : le
diagnostic a porte sur un faux bug applicatif.

**La correction.** Tuer par port (`lsof -ti:8080 | xargs kill -9`) et verifier que le
nouveau demarrage a bien produit son `Started ServerConnector` avant de tester.

**A retenir.** Devant un symptome incoherent avec le code source, verifier d'abord que
le code teste est bien celui qui tourne.

---

## 5. Un record Java n'est pas un JavaBean

**Le probleme.** Le langage d'expression des JSP cherche `getNombreDeClients()` et ne
reconnait pas l'accesseur de record `nombreDeClients()` : `PropertyNotFoundException`
a l'affichage.

**La correction.** Ajouter des accesseurs au format JavaBean aux records exposes aux
vues, tout en conservant leur immuabilite.

**A retenir.** Un objet destine a une technologie de vue doit respecter les conventions
de cette technologie, meme quand le langage propose plus moderne.

---

## 6. Sur PostgreSQL, `@Lob byte[]` n'est pas ce que l'on croit

**Le probleme.** Hibernate traduit `@Lob` sur un `byte[]` par une colonne `oid` :
le contenu est stocke dans un "large object" externe, qui n'est pas supprime avec la
ligne et fuit donc en stockage.

**La correction.** `@JdbcTypeCode(SqlTypes.VARBINARY)` pour obtenir une colonne `bytea`.

**A retenir.** Toujours relire le DDL reellement genere par l'ORM au lieu de supposer
la traduction des annotations.

---

## 7. Faire generer le script SQL par l'ORM, puis le verifier

**Le risque.** Un `database.sql` ecrit a la main derive du mapping JPA sans que personne
ne s'en apercoive, jusqu'a la demonstration.

**La methode retenue.** Generer le schema depuis le mapping Hibernate, l'enrichir
(commentaires, index, noms de contraintes explicites, jeu de donnees), puis creer une
base vierge a partir du script et demarrer Hibernate en mode `validate` dessus.

**A retenir.** Un livrable qui doit rester coherent avec le code merite une verification
automatisable, pas une relecture.
