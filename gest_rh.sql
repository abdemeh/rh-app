-- MySQL dump 10.13  Distrib 9.4.0, for macos15.4 (arm64)
--
-- Host: localhost    Database: gest_rh
-- ------------------------------------------------------
-- Server version	9.4.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `audit_log`
--

DROP TABLE IF EXISTS `audit_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `audit_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `utilisateur_id` int DEFAULT NULL,
  `action` enum('CREATE','READ','UPDATE','DELETE','LOGIN','LOGOUT') NOT NULL,
  `table_name` varchar(64) NOT NULL,
  `record_id` varchar(64) DEFAULT NULL,
  `ip` varchar(45) DEFAULT NULL,
  `user_agent` varchar(255) DEFAULT NULL,
  `occurred_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_audit_table_time` (`table_name`,`occurred_at`),
  KEY `fk_audit_user` (`utilisateur_id`),
  CONSTRAINT `fk_audit_user` FOREIGN KEY (`utilisateur_id`) REFERENCES `utilisateurs` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `audit_log`
--

LOCK TABLES `audit_log` WRITE;
/*!40000 ALTER TABLE `audit_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `audit_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `auth_refresh_tokens`
--

DROP TABLE IF EXISTS `auth_refresh_tokens`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `auth_refresh_tokens` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `utilisateur_id` int NOT NULL,
  `token_hash` char(64) NOT NULL,
  `user_agent` varchar(255) DEFAULT NULL,
  `ip` varchar(45) DEFAULT NULL,
  `expires_at` datetime NOT NULL,
  `revoked_at` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_refresh_token` (`token_hash`),
  KEY `idx_art_user` (`utilisateur_id`,`expires_at`),
  CONSTRAINT `fk_art_user` FOREIGN KEY (`utilisateur_id`) REFERENCES `utilisateurs` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `auth_refresh_tokens`
--

LOCK TABLES `auth_refresh_tokens` WRITE;
/*!40000 ALTER TABLE `auth_refresh_tokens` DISABLE KEYS */;
/*!40000 ALTER TABLE `auth_refresh_tokens` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `calendrier_evenements`
--

DROP TABLE IF EXISTS `calendrier_evenements`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `calendrier_evenements` (
  `id` int NOT NULL AUTO_INCREMENT,
  `titre` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `date_evenement` date NOT NULL,
  `cree_par` int DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_cal_date` (`date_evenement`),
  KEY `idx_cal_creator` (`cree_par`),
  CONSTRAINT `fk_cal_user` FOREIGN KEY (`cree_par`) REFERENCES `utilisateurs` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `calendrier_evenements`
--

LOCK TABLES `calendrier_evenements` WRITE;
/*!40000 ALTER TABLE `calendrier_evenements` DISABLE KEYS */;
INSERT INTO `calendrier_evenements` VALUES (1,'Réunion mensuelle','Suivre KPIs et priorités','2025-09-05',1,'2025-08-30 04:02:08'),(2,'Afterwork','Cafe & jeux','2025-09-12',2,'2025-08-30 04:02:08'),(3,'Clôture sprint','Revue + rétrospective','2025-09-20',1,'2025-08-30 04:02:08'),(4,'Journée formation Java','Atelier Jakarta EE','2025-09-25',2,'2025-08-30 04:02:08'),(5,'Maintenance serveur','Redémarrage Payara','2025-09-03',NULL,'2025-08-30 04:02:23'),(6,'Démo client','Présentation module RH','2025-09-10',NULL,'2025-08-30 04:02:23'),(7,'test','','2025-08-31',2,'2025-08-30 04:11:30'),(8,'Reunion','Test','2025-09-27',2,'2025-09-01 13:11:19');
/*!40000 ALTER TABLE `calendrier_evenements` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `conges`
--

DROP TABLE IF EXISTS `conges`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `conges` (
  `id` int NOT NULL AUTO_INCREMENT,
  `utilisateur_id` int NOT NULL,
  `type_id` int NOT NULL,
  `date_debut` date NOT NULL,
  `date_fin` date NOT NULL,
  `nb_jours` decimal(5,2) NOT NULL,
  `statut` enum('brouillon','en_attente','approuve','rejete','annule') NOT NULL DEFAULT 'en_attente',
  `motif` varchar(255) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at` datetime DEFAULT NULL,
  `justificatif_path` varchar(255) DEFAULT NULL,
  `justificatif_name` varchar(255) DEFAULT NULL,
  `justificatif_type` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_conges_user` (`utilisateur_id`),
  KEY `idx_conges_type` (`type_id`),
  KEY `idx_conges_statut` (`statut`),
  KEY `idx_conges_dates` (`date_debut`,`date_fin`),
  CONSTRAINT `fk_conges_type` FOREIGN KEY (`type_id`) REFERENCES `conges_types` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_conges_user` FOREIGN KEY (`utilisateur_id`) REFERENCES `utilisateurs` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `conges_chk_1` CHECK ((`date_fin` >= `date_debut`))
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `conges`
--

LOCK TABLES `conges` WRITE;
/*!40000 ALTER TABLE `conges` DISABLE KEYS */;
INSERT INTO `conges` VALUES (1,4,1,'2025-09-01','2025-09-05',5.00,'en_attente','Vacances','2025-08-27 01:53:31',NULL,NULL,NULL,NULL,NULL),(2,2,4,'2000-01-01','2002-01-01',732.00,'en_attente','','2025-08-27 22:12:10',NULL,NULL,NULL,NULL,NULL),(3,2,3,'2023-01-01','2023-02-10',41.00,'en_attente','Malade - Ci dessus la justification de docteur','2025-08-29 00:15:42',NULL,NULL,'/Users/elmah/gest-rh-uploads/dddec99e-768b-4591-9e5d-dd4410afefa6.pdf','pdf maladie test.pdf','application/pdf'),(4,2,3,'2011-11-11','2011-12-11',31.00,'approuve','','2025-08-29 00:29:43','2025-08-29 11:39:36',NULL,'/Users/elmah/gest-rh-uploads/0427d474-404e-433e-a69c-95fc5c74680d.pdf','pdf maladie test.pdf','application/pdf'),(5,2,3,'2011-11-11','2011-11-12',2.00,'rejete','teeessss','2025-08-29 01:12:38','2025-08-29 11:39:35',NULL,NULL,NULL,NULL),(6,2,3,'2022-11-11','2022-12-11',31.00,'rejete','','2025-08-29 02:31:02','2025-08-29 11:47:50',NULL,NULL,NULL,NULL),(7,2,3,'2019-01-01','2019-02-01',32.00,'approuve','111','2025-08-29 11:01:51','2025-08-29 11:39:32',NULL,'/Users/elmah/gest-rh-uploads/533fe771-3629-47ed-9276-222f11b195b5.pdf','pdf maladie test.pdf','application/pdf');
/*!40000 ALTER TABLE `conges` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `conges_approbations`
--

DROP TABLE IF EXISTS `conges_approbations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `conges_approbations` (
  `id` int NOT NULL AUTO_INCREMENT,
  `conge_id` int NOT NULL,
  `level_idx` tinyint NOT NULL,
  `approbateur_id` int NOT NULL,
  `statut` enum('en_attente','approuve','rejete') NOT NULL DEFAULT 'en_attente',
  `commentaire` varchar(255) DEFAULT NULL,
  `decided_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_conge_level` (`conge_id`,`level_idx`),
  KEY `idx_conge_approbateur` (`approbateur_id`),
  CONSTRAINT `fk_ca_conge` FOREIGN KEY (`conge_id`) REFERENCES `conges` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_ca_user` FOREIGN KEY (`approbateur_id`) REFERENCES `utilisateurs` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `conges_approbations`
--

LOCK TABLES `conges_approbations` WRITE;
/*!40000 ALTER TABLE `conges_approbations` DISABLE KEYS */;
INSERT INTO `conges_approbations` VALUES (1,1,1,3,'en_attente',NULL,NULL);
/*!40000 ALTER TABLE `conges_approbations` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `conges_types`
--

DROP TABLE IF EXISTS `conges_types`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `conges_types` (
  `id` int NOT NULL AUTO_INCREMENT,
  `code` varchar(30) NOT NULL,
  `libelle` varchar(100) NOT NULL,
  `max_jours_an` decimal(5,2) DEFAULT NULL,
  `approval_levels` tinyint NOT NULL DEFAULT '1',
  `requires_doc` tinyint(1) NOT NULL DEFAULT '0',
  `actif` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_conges_type_code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `conges_types`
--

LOCK TABLES `conges_types` WRITE;
/*!40000 ALTER TABLE `conges_types` DISABLE KEYS */;
INSERT INTO `conges_types` VALUES (1,'CP','Congés payés',25.00,1,0,1),(2,'RTT','RTT',12.00,1,0,1),(3,'MAL','Maladie',NULL,1,1,1),(4,'MAT','Maternité',NULL,2,1,1),(5,'PAT','Paternité',NULL,2,1,1);
/*!40000 ALTER TABLE `conges_types` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `departements`
--

DROP TABLE IF EXISTS `departements`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `departements` (
  `id` int NOT NULL AUTO_INCREMENT,
  `nom_departement` varchar(100) NOT NULL,
  `responsable_id` int DEFAULT NULL,
  `deleted_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_departement_nom` (`nom_departement`),
  KEY `fk_dept_responsable` (`responsable_id`),
  CONSTRAINT `fk_dept_responsable` FOREIGN KEY (`responsable_id`) REFERENCES `utilisateurs` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `departements`
--

LOCK TABLES `departements` WRITE;
/*!40000 ALTER TABLE `departements` DISABLE KEYS */;
INSERT INTO `departements` VALUES (1,'Ressources Humaines',2,NULL),(2,'Informatique',NULL,NULL),(3,'Finance',NULL,NULL),(4,'Marketing',4,NULL),(5,'Design',4,'2025-08-30 02:57:33');
/*!40000 ALTER TABLE `departements` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `documents`
--

DROP TABLE IF EXISTS `documents`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `documents` (
  `id` int NOT NULL AUTO_INCREMENT,
  `titre` varchar(150) NOT NULL,
  `categorie` enum('contrat','attestation','politique','autre') NOT NULL DEFAULT 'autre',
  `chemin` varchar(255) NOT NULL,
  `owner_id` int DEFAULT NULL,
  `checksum_sha256` char(64) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_doc_owner` (`owner_id`),
  CONSTRAINT `fk_doc_owner` FOREIGN KEY (`owner_id`) REFERENCES `utilisateurs` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `documents`
--

LOCK TABLES `documents` WRITE;
/*!40000 ALTER TABLE `documents` DISABLE KEYS */;
INSERT INTO `documents` VALUES (1,'Contrat Chloe','contrat','/files/contrats/contrat_chloe.pdf',4,'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa','2025-08-27 01:53:31',NULL);
/*!40000 ALTER TABLE `documents` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `documents_liens`
--

DROP TABLE IF EXISTS `documents_liens`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `documents_liens` (
  `id` int NOT NULL AUTO_INCREMENT,
  `document_id` int NOT NULL,
  `objet_type` enum('UTILISATEUR','CONGE','AUTRE') NOT NULL,
  `objet_id` int NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_doc_link` (`objet_type`,`objet_id`),
  KEY `fk_dl_document` (`document_id`),
  CONSTRAINT `fk_dl_document` FOREIGN KEY (`document_id`) REFERENCES `documents` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `documents_liens`
--

LOCK TABLES `documents_liens` WRITE;
/*!40000 ALTER TABLE `documents_liens` DISABLE KEYS */;
/*!40000 ALTER TABLE `documents_liens` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jours_feries`
--

DROP TABLE IF EXISTS `jours_feries`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jours_feries` (
  `id` int NOT NULL AUTO_INCREMENT,
  `pays` varchar(2) NOT NULL,
  `jour` date NOT NULL,
  `libelle` varchar(120) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_jour_pays` (`pays`,`jour`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jours_feries`
--

LOCK TABLES `jours_feries` WRITE;
/*!40000 ALTER TABLE `jours_feries` DISABLE KEYS */;
INSERT INTO `jours_feries` VALUES (1,'FR','2025-01-01','Jour de l’an'),(2,'FR','2025-05-01','Fête du Travail'),(3,'MA','2025-01-11','Manifeste de l’Indépendance'),(4,'MA','2025-05-01','Fête du Travail');
/*!40000 ALTER TABLE `jours_feries` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notifications`
--

DROP TABLE IF EXISTS `notifications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notifications` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `utilisateur_id` int NOT NULL,
  `type` varchar(50) NOT NULL,
  `message` varchar(255) NOT NULL,
  `lu` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_notif_user` (`utilisateur_id`,`lu`,`created_at`),
  CONSTRAINT `fk_notif_user` FOREIGN KEY (`utilisateur_id`) REFERENCES `utilisateurs` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notifications`
--

LOCK TABLES `notifications` WRITE;
/*!40000 ALTER TABLE `notifications` DISABLE KEYS */;
INSERT INTO `notifications` VALUES (1,4,'CONGE','Votre demande de congé a été soumise.',0,'2025-08-27 01:53:31');
/*!40000 ALTER TABLE `notifications` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `password_reset_requests`
--

DROP TABLE IF EXISTS `password_reset_requests`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `password_reset_requests` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `utilisateur_id` int NOT NULL,
  `selector` char(16) NOT NULL,
  `verifier_hash` char(64) NOT NULL,
  `expires_at` datetime NOT NULL,
  `used_at` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_prr_selector` (`selector`),
  KEY `idx_prr_user` (`utilisateur_id`),
  CONSTRAINT `fk_prr_user` FOREIGN KEY (`utilisateur_id`) REFERENCES `utilisateurs` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `password_reset_requests`
--

LOCK TABLES `password_reset_requests` WRITE;
/*!40000 ALTER TABLE `password_reset_requests` DISABLE KEYS */;
/*!40000 ALTER TABLE `password_reset_requests` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `permissions`
--

DROP TABLE IF EXISTS `permissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `permissions` (
  `id` int NOT NULL AUTO_INCREMENT,
  `code` varchar(80) NOT NULL,
  `libelle` varchar(150) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_perm_code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `permissions`
--

LOCK TABLES `permissions` WRITE;
/*!40000 ALTER TABLE `permissions` DISABLE KEYS */;
INSERT INTO `permissions` VALUES (1,'USER_READ','Lire utilisateurs'),(2,'USER_WRITE','Créer/Mettre à jour utilisateurs'),(3,'LEAVE_REQUEST','Demander un congé'),(4,'LEAVE_APPROVE','Approuver un congé'),(5,'DOC_SIGN','Signer un document');
/*!40000 ALTER TABLE `permissions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `postes`
--

DROP TABLE IF EXISTS `postes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `postes` (
  `id` int NOT NULL AUTO_INCREMENT,
  `intitule_poste` varchar(100) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `departement_id` int NOT NULL,
  `deleted_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_poste_intitule` (`intitule_poste`),
  KEY `idx_poste_dept` (`departement_id`),
  CONSTRAINT `fk_poste_dept` FOREIGN KEY (`departement_id`) REFERENCES `departements` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `postes`
--

LOCK TABLES `postes` WRITE;
/*!40000 ALTER TABLE `postes` DISABLE KEYS */;
INSERT INTO `postes` VALUES (1,'DRH','Directeur RH',1,NULL),(2,'Développeur Backend','Java / Jakarta EE',2,NULL),(3,'Développeur Frontend','React/Tailwind',2,NULL),(4,'Comptable','Comptabilité fournisseurs',3,NULL),(5,'Chef de Produit','Marketing produit',4,NULL);
/*!40000 ALTER TABLE `postes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `presence_intervals`
--

DROP TABLE IF EXISTS `presence_intervals`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `presence_intervals` (
  `id` int NOT NULL AUTO_INCREMENT,
  `presence_id` int NOT NULL,
  `start_time` datetime NOT NULL,
  `end_time` datetime DEFAULT NULL,
  `minutes` int GENERATED ALWAYS AS (timestampdiff(MINUTE,`start_time`,`end_time`)) STORED,
  `commentaire` varchar(255) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_presence_open` (`presence_id`,`end_time`),
  KEY `idx_presence_start` (`presence_id`,`start_time`),
  CONSTRAINT `fk_presence_intervals_presence` FOREIGN KEY (`presence_id`) REFERENCES `presences` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `presence_intervals`
--

LOCK TABLES `presence_intervals` WRITE;
/*!40000 ALTER TABLE `presence_intervals` DISABLE KEYS */;
INSERT INTO `presence_intervals` (`id`, `presence_id`, `start_time`, `end_time`, `commentaire`, `created_at`) VALUES (1,2,'2025-08-31 19:01:34','2025-08-31 19:01:37',NULL,'2025-08-31 19:01:33'),(2,2,'2025-08-31 19:01:38','2025-08-31 19:01:41',NULL,'2025-08-31 19:01:38'),(3,2,'2025-08-31 19:01:42','2025-08-31 19:01:44',NULL,'2025-08-31 19:01:41'),(4,2,'2025-08-31 19:01:47','2025-08-31 19:01:49',NULL,'2025-08-31 19:01:46'),(5,2,'2025-08-31 19:01:51','2025-08-31 19:01:55',NULL,'2025-08-31 19:01:50'),(6,2,'2025-08-31 19:03:26','2025-08-31 19:05:02',NULL,'2025-08-31 19:03:26'),(7,2,'2025-08-31 19:21:44','2025-08-31 19:21:48',NULL,'2025-08-31 19:21:43'),(8,2,'2025-08-31 23:14:50','2025-08-31 23:14:51',NULL,'2025-08-31 23:14:50'),(9,2,'2025-08-31 23:16:11','2025-08-31 23:26:13',NULL,'2025-08-31 23:16:10'),(10,2,'2025-08-31 23:26:14','2025-08-31 23:26:16',NULL,'2025-08-31 23:26:14'),(11,2,'2025-08-31 23:26:18','2025-08-31 23:26:21',NULL,'2025-08-31 23:26:18'),(12,5,'2025-09-01 01:40:12','2025-09-01 01:40:15',NULL,'2025-09-01 01:40:12'),(13,5,'2025-09-01 13:10:51','2025-09-01 13:10:57',NULL,'2025-09-01 13:10:51');
/*!40000 ALTER TABLE `presence_intervals` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `presences`
--

DROP TABLE IF EXISTS `presences`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `presences` (
  `id` int NOT NULL AUTO_INCREMENT,
  `utilisateur_id` int NOT NULL,
  `jour` date NOT NULL,
  `heure_entree` time DEFAULT NULL,
  `heure_sortie` time DEFAULT NULL,
  `statut` enum('present','absent','retard','teletravail') NOT NULL DEFAULT 'present',
  `commentaire` varchar(255) DEFAULT NULL,
  `duree_minutes` int GENERATED ALWAYS AS ((case when ((`heure_entree` is not null) and (`heure_sortie` is not null)) then (time_to_sec(timediff(`heure_sortie`,`heure_entree`)) / 60) else NULL end)) STORED,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_presence_user_day` (`utilisateur_id`,`jour`),
  KEY `idx_presence_user` (`utilisateur_id`),
  CONSTRAINT `fk_presence_user` FOREIGN KEY (`utilisateur_id`) REFERENCES `utilisateurs` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `presences`
--

LOCK TABLES `presences` WRITE;
/*!40000 ALTER TABLE `presences` DISABLE KEYS */;
INSERT INTO `presences` (`id`, `utilisateur_id`, `jour`, `heure_entree`, `heure_sortie`, `statut`, `commentaire`) VALUES (1,4,'2025-08-27','09:01:00','17:06:00','present',NULL),(2,2,'2025-08-31','18:25:29','18:25:41','absent',NULL),(5,2,'2025-09-01',NULL,NULL,'absent',NULL);
/*!40000 ALTER TABLE `presences` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `role_permissions`
--

DROP TABLE IF EXISTS `role_permissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `role_permissions` (
  `role_id` int NOT NULL,
  `permission_id` int NOT NULL,
  PRIMARY KEY (`role_id`,`permission_id`),
  KEY `fk_rp_perm` (`permission_id`),
  CONSTRAINT `fk_rp_perm` FOREIGN KEY (`permission_id`) REFERENCES `permissions` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_rp_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `role_permissions`
--

LOCK TABLES `role_permissions` WRITE;
/*!40000 ALTER TABLE `role_permissions` DISABLE KEYS */;
INSERT INTO `role_permissions` VALUES (1,1),(2,1),(3,1),(1,2),(2,2),(1,3),(4,3),(1,4),(2,4),(3,4),(1,5),(2,5),(4,5);
/*!40000 ALTER TABLE `role_permissions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `roles`
--

DROP TABLE IF EXISTS `roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `roles` (
  `id` int NOT NULL AUTO_INCREMENT,
  `code` varchar(50) NOT NULL,
  `libelle` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_role_code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `roles`
--

LOCK TABLES `roles` WRITE;
/*!40000 ALTER TABLE `roles` DISABLE KEYS */;
INSERT INTO `roles` VALUES (1,'ADMIN','Administrateur Système'),(2,'RH','Ressources Humaines'),(3,'MANAGER','Manager'),(4,'EMPLOYE','Employé');
/*!40000 ALTER TABLE `roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `services_rh`
--

DROP TABLE IF EXISTS `services_rh`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `services_rh` (
  `id` int NOT NULL AUTO_INCREMENT,
  `nom_service` varchar(100) NOT NULL,
  `email` varchar(120) DEFAULT NULL,
  `responsable_id` int DEFAULT NULL,
  `deleted_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_service_nom` (`nom_service`),
  UNIQUE KEY `uq_service_email` (`email`),
  KEY `fk_service_responsable` (`responsable_id`),
  CONSTRAINT `fk_service_responsable` FOREIGN KEY (`responsable_id`) REFERENCES `utilisateurs` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `services_rh`
--

LOCK TABLES `services_rh` WRITE;
/*!40000 ALTER TABLE `services_rh` DISABLE KEYS */;
INSERT INTO `services_rh` VALUES (1,'Support RH','support.rh@gest.local',2,NULL);
/*!40000 ALTER TABLE `services_rh` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `signatures`
--

DROP TABLE IF EXISTS `signatures`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `signatures` (
  `id` int NOT NULL AUTO_INCREMENT,
  `utilisateur_id` int NOT NULL,
  `document_id` int NOT NULL,
  `hash_document` char(64) NOT NULL,
  `signe_le` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `signature_img` longblob,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_signature_user_doc` (`utilisateur_id`,`document_id`),
  KEY `fk_signature_document` (`document_id`),
  CONSTRAINT `fk_signature_document` FOREIGN KEY (`document_id`) REFERENCES `documents` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_signature_user` FOREIGN KEY (`utilisateur_id`) REFERENCES `utilisateurs` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `signatures`
--

LOCK TABLES `signatures` WRITE;
/*!40000 ALTER TABLE `signatures` DISABLE KEYS */;
INSERT INTO `signatures` VALUES (1,4,1,'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb','2025-08-27 01:53:31',NULL);
/*!40000 ALTER TABLE `signatures` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `solde_conges`
--

DROP TABLE IF EXISTS `solde_conges`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `solde_conges` (
  `id` int NOT NULL AUTO_INCREMENT,
  `utilisateur_id` int NOT NULL,
  `type_id` int NOT NULL,
  `annee` year NOT NULL,
  `solde_initial` decimal(5,2) NOT NULL DEFAULT '0.00',
  `solde_utilise` decimal(5,2) NOT NULL DEFAULT '0.00',
  `solde_restant` decimal(5,2) NOT NULL DEFAULT '0.00',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_solde_user_type_year` (`utilisateur_id`,`type_id`,`annee`),
  KEY `fk_solde_type` (`type_id`),
  CONSTRAINT `fk_solde_type` FOREIGN KEY (`type_id`) REFERENCES `conges_types` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_solde_user` FOREIGN KEY (`utilisateur_id`) REFERENCES `utilisateurs` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `solde_conges`
--

LOCK TABLES `solde_conges` WRITE;
/*!40000 ALTER TABLE `solde_conges` DISABLE KEYS */;
INSERT INTO `solde_conges` VALUES (1,4,1,2025,25.00,0.00,25.00);
/*!40000 ALTER TABLE `solde_conges` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_roles`
--

DROP TABLE IF EXISTS `user_roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_roles` (
  `utilisateur_id` int NOT NULL,
  `role_id` int NOT NULL,
  PRIMARY KEY (`utilisateur_id`,`role_id`),
  KEY `fk_ur_role` (`role_id`),
  CONSTRAINT `fk_ur_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_ur_user` FOREIGN KEY (`utilisateur_id`) REFERENCES `utilisateurs` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_roles`
--

LOCK TABLES `user_roles` WRITE;
/*!40000 ALTER TABLE `user_roles` DISABLE KEYS */;
INSERT INTO `user_roles` VALUES (1,1),(2,2),(3,3),(4,4);
/*!40000 ALTER TABLE `user_roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `utilisateurs`
--

DROP TABLE IF EXISTS `utilisateurs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `utilisateurs` (
  `id` int NOT NULL AUTO_INCREMENT,
  `nom` varchar(100) NOT NULL,
  `prenom` varchar(100) NOT NULL,
  `email` varchar(120) NOT NULL,
  `mot_de_passe` varchar(255) NOT NULL,
  `poste_id` int DEFAULT NULL,
  `departement_id` int DEFAULT NULL,
  `manager_id` int DEFAULT NULL,
  `adresse` varchar(255) DEFAULT NULL,
  `telephone` varchar(20) DEFAULT NULL,
  `statut` enum('actif','inactif','suspendu') NOT NULL DEFAULT 'actif',
  `contrat_type` enum('CDI','CDD','Alternance','Stage','Freelance') DEFAULT NULL,
  `date_embauche` date DEFAULT NULL,
  `date_sortie` date DEFAULT NULL,
  `salaire_base` decimal(10,2) DEFAULT NULL,
  `date_creation` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `last_login` datetime DEFAULT NULL,
  `deleted_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_user_email` (`email`),
  KEY `idx_user_dept` (`departement_id`),
  KEY `idx_user_poste` (`poste_id`),
  KEY `idx_user_manager` (`manager_id`),
  CONSTRAINT `fk_user_dept` FOREIGN KEY (`departement_id`) REFERENCES `departements` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_user_manager` FOREIGN KEY (`manager_id`) REFERENCES `utilisateurs` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_user_poste` FOREIGN KEY (`poste_id`) REFERENCES `postes` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `utilisateurs`
--

LOCK TABLES `utilisateurs` WRITE;
/*!40000 ALTER TABLE `utilisateurs` DISABLE KEYS */;
INSERT INTO `utilisateurs` VALUES (1,'Root','Admin','admin@gest.local','$2a$12$ImhBJz6nWE5IVrxsvNSDdexzErizmHnHqjn4vvGZzxqw0PF1zWteS',1,1,NULL,NULL,NULL,'actif','CDI',NULL,NULL,NULL,'2025-08-27 01:53:30',NULL,NULL),(2,'Durand','Alice','alice.rh@gest.local','$2a$12$ImhBJz6nWE5IVrxsvNSDdexzErizmHnHqjn4vvGZzxqw0PF1zWteS',1,1,NULL,'','','actif','CDI',NULL,NULL,700.00,'2025-08-27 01:53:30','2025-09-01 13:10:57',NULL),(3,'Martin','Boba','bob.manager@gest.local','$2a$12$ImhBJz6nWE5IVrxsvNSDdexzErizmHnHqjn4vvGZzxqw0PF1zWteS',2,2,NULL,'','','actif','CDI',NULL,NULL,2000.00,'2025-08-27 01:53:30',NULL,NULL),(4,'Petit','Chloe','chloe.user@gest.local','$2a$12$ImhBJz6nWE5IVrxsvNSDdexzErizmHnHqjn4vvGZzxqw0PF1zWteS',3,2,3,'','','actif','CDD',NULL,NULL,1200.00,'2025-08-27 01:53:30',NULL,NULL);
/*!40000 ALTER TABLE `utilisateurs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Temporary view structure for view `v_presence_day_totals`
--

DROP TABLE IF EXISTS `v_presence_day_totals`;
/*!50001 DROP VIEW IF EXISTS `v_presence_day_totals`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_presence_day_totals` AS SELECT 
 1 AS `presence_id`,
 1 AS `utilisateur_id`,
 1 AS `jour`,
 1 AS `total_minutes`*/;
SET character_set_client = @saved_cs_client;

--
-- Final view structure for view `v_presence_day_totals`
--

/*!50001 DROP VIEW IF EXISTS `v_presence_day_totals`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_presence_day_totals` AS select `p`.`id` AS `presence_id`,`p`.`utilisateur_id` AS `utilisateur_id`,`p`.`jour` AS `jour`,coalesce(sum(`pi`.`minutes`),0) AS `total_minutes` from (`presences` `p` left join `presence_intervals` `pi` on(((`pi`.`presence_id` = `p`.`id`) and (`pi`.`end_time` is not null)))) group by `p`.`id`,`p`.`utilisateur_id`,`p`.`jour` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-09-01 13:36:31
