# GestRH – Application de Gestion des Ressources Humaines

GestRH est une application web développée en **Java EE (JSP/Servlets)** avec **MySQL**, permettant de gérer les employés, départements, congés, présences et rapports dans une organisation.  
L’application propose deux espaces distincts : **Administrateur** et **Employé**.

---

## 🚀 Fonctionnalités

### 👨‍💼 Espace Administrateur
- Tableau de bord avec indicateurs clés et graphiques (employés, départements, congés, masse salariale).
- Gestion des **employés** : création, édition, suppression.
- Gestion des **départements** avec attribution d’un responsable.
- Validation des **congés en attente** (approuver, rejeter, consulter justificatifs PDF).
- Définition des **types de congés** (code, libellé, max/an, justificatif obligatoire).
- Suivi des **présences** en temps réel et export PDF.
- Génération de **rapports PDF** (par utilisateur, département, poste ou mois donné).

### 👩‍💻 Espace Employé
- Authentification sécurisée (email + mot de passe).
- Demande de **congés en ligne** avec justificatif PDF.
- Suivi du statut de ses demandes (approuvées, rejetées, en attente).
- **Check-in / Check-out** pour gérer sa présence quotidienne.
- Accès au **calendrier partagé** de l’entreprise (ajout d’événements).
- Génération de ses propres **rapports PDF** (congés et rémunération).
- Mise à jour de son **profil utilisateur** (informations personnelles, mot de passe).

---

## 🛠️ Technologies utilisées
- **Backend** : Java EE (Servlets, JSP)
- **Frontend** : HTML, CSS, Bootstrap / Tailwind, Chart.js
- **Base de données** : MySQL
- **Génération de PDF** : iText
- **Serveur d’application** : Payara / Tomcat
- **Gestion de projet** : Maven

---

## ⚙️ Installation et exécution

1. **Cloner le dépôt** :
   ```bash
   git clone https://github.com/votre-repo/gest-rh.git
   cd gest-rh
   
2. Configurer la base MySQL :

3. Créer une base gest_rh.

4. Importer le script SQL présent dans db/schema.sql.

5. Configurer le projet Java EE.

6. Déployer l’application :

 ```bash
mvn clean package
asadmin deploy target/gest-rh.war
ou via Tomcat (copier le .war dans webapps).
 ```
7. Accéder à l’application :

 ```bash
http://localhost:8080/gest-rh
 ```
📂 Structure du projet
```bash
gest-rh/
├── .idea/
├── .mvn/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/gestrh/
│       │       ├── config/
│       │       │   ├── AppBootstrap.java
│       │       │   └── JaxrsConfig.java
│       │       ├── entity/
│       │       │   ├── CalendrierEvenement.java
│       │       │   ├── Conge.java
│       │       │   ├── CongeApprobation.java
│       │       │   ├── CongeType.java
│       │       │   ├── Departement.java
│       │       │   ├── Poste.java
│       │       │   ├── Presence.java
│       │       │   ├── PresenceInterval.java
│       │       │   └── Utilisateur.java
│       │       ├── report/
│       │       ├── rest/
│       │       │   └── HealthResource.java
│       │       ├── service/
│       │       └── web/
│       │           ├── account/
│       │           ├── admin/
│       │           │   ├── AdminApproveLeaveServlet.java
│       │           │   ├── AdminCongeTypesServlet.java
│       │           │   ├── AdminDashboardServlet.java
│       │           │   ├── AdminDepartementsServlet.java
│       │           │   ├── AdminPendingLeavesServlet.java
│       │           │   ├── AdminPresenceServlet.java
│       │           │   ├── AdminReportsServlet.java
│       │           │   ├── AdminUserEditServlet.java
│       │           │   ├── AdminUserSaveServlet.java
│       │           │   └── AdminUsersServlet.java
│       │           ├── calendar/
│       │           │   ├── CalendarSaveServlet.java
│       │           │   └── CalendarServlet.java
│       │           ├── secure/
│       │           │   ├── PingServlet.java
│       │           │   ├── PresenceCheckServlet.java
│       │           │   ├── PresenceListServlet.java
│       │           │   └── UserReportsServlet.java
│       │           ├── AuthFilter.java
│       │           ├── CongeApproveServlet.java
│       │           ├── CongeCreateServlet.java
│       │           ├── CongeListServlet.java
│       │           ├── FileDownloadServlet.java
│       │           ├── HomeRouterServlet.java
│       │           ├── JustificatifViewServlet.java
│       │           ├── LoginServlet.java
│       │           ├── LogoutServlet.java
│       │           ├── MyLeavesReportServlet.java
│       │           ├── PresenceListServlet.java
│       │           ├── RoleFilter.java
│       │           ├── SecurityUtil.java
│       │           └── HelloServlet.java
│       ├── resources/
│       │   └── META-INF/
│       │       └── persistence.xml
│       └── webapp/
│           ├── account/
│           │   └── profile.jsp
│           ├── admin/
│           │   ├── _layout.jspf
│           │   ├── conges_pending.jsp
│           │   ├── conges_types.jsp
│           │   ├── departements.jsp
│           │   ├── index.jsp
│           │   ├── presences.jsp
│           │   ├── reports.jsp
│           │   ├── user_form.jsp
│           │   └── users.jsp
│           ├── calendar/
│           │   └── index.jsp
│           ├── css/
│           │   └── styles.css
│           ├── img/
│           │   ├── logo.png
│           │   └── logo-light.png
│           ├── secure/
│           │   ├── _layout.jspf
│           │   ├── conge_form.jsp
│           │   ├── conges.jsp
│           │   ├── presences.jsp
│           │   └── reports.jsp
│           ├── WEB-INF/
│           │   └── web.xml
│           ├── index.jsp
│           └── login.jsp
├── test/
└── target/
 ```
👥 Auteurs
Abdellatif El Mahdaoui
INFOSAT Agadir

📜 Licence
Ce projet est distribué sous licence MIT – libre d’utilisation et de modification.
