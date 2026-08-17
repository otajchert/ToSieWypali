-- MySQL dump 10.13  Distrib 8.0.46, for Linux (x86_64)
--
-- Host: localhost    Database: TSW_db
-- ------------------------------------------------------
-- Server version	8.0.46

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
-- Table structure for table `address`
--

DROP TABLE IF EXISTS `address`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `address` (
  `UniqueID` char(36) NOT NULL,
  `city` varchar(100) DEFAULT NULL,
  `flat` varchar(20) DEFAULT NULL,
  `postal_code` varchar(20) DEFAULT NULL,
  `region` varchar(100) DEFAULT NULL,
  `street_number` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`UniqueID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `address`
--

LOCK TABLES `address` WRITE;
/*!40000 ALTER TABLE `address` DISABLE KEYS */;
INSERT INTO `address` VALUES ('7b840adf-e21c-4930-b191-6ff8a277469e','warszawa','','01-755','mazowieckie','Krasińskiego 41'),('dca93786-0a0a-41fa-b78b-4c7814c74023','Poznań',NULL,'61-001','Wielkopolskie','ul. Półwiejska 42');
/*!40000 ALTER TABLE `address` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cart_item`
--

DROP TABLE IF EXISTS `cart_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cart_item` (
  `UniqueID` char(36) NOT NULL,
  `qty` int NOT NULL,
  `cart_id` char(36) NOT NULL,
  `product_id` char(36) NOT NULL,
  PRIMARY KEY (`UniqueID`),
  UNIQUE KEY `uk_cart_item_cart_product` (`cart_id`,`product_id`),
  KEY `FKjcyd5wv4igqnw413rgxbfu4nv` (`product_id`),
  CONSTRAINT `FKf9e1brwb4ea9cxtvxbs0wugdt` FOREIGN KEY (`cart_id`) REFERENCES `shopping_cart` (`UniqueID`),
  CONSTRAINT `FKjcyd5wv4igqnw413rgxbfu4nv` FOREIGN KEY (`product_id`) REFERENCES `product` (`UniqueID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cart_item`
--

LOCK TABLES `cart_item` WRITE;
/*!40000 ALTER TABLE `cart_item` DISABLE KEYS */;
INSERT INTO `cart_item` VALUES ('566b46ae-bb69-424f-8229-0117eba5921a',1,'c502d356-7b46-42e8-885a-f0f55ea40e7e','00000000-0000-0000-0000-000000001001'),('90574d1c-0357-41a1-96da-94ff8689545e',1,'df8546f6-ae70-424f-b2b5-8f78ecefaaeb','00000000-0000-0000-0000-000000001001'),('9d7d6782-85b8-4c4c-b69b-61a44aaeb143',2,'df8546f6-ae70-424f-b2b5-8f78ecefaaeb','7285f127-013e-4f8b-941e-eefb60b474cb'),('a3b8b6dc-de13-4a57-8bd2-4b5a62edb0c6',2,'89244bd1-9219-45ca-bf43-c2fa9fb9d207','00000000-0000-0000-0000-000000001001'),('ba75c419-d07f-47cc-ba8d-e1313fb8a4a0',1,'df8546f6-ae70-424f-b2b5-8f78ecefaaeb','f3ddf446-9d30-4c89-8061-2970b41255db');
/*!40000 ALTER TABLE `cart_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `category`
--

DROP TABLE IF EXISTS `category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `category` (
  `UniqueID` char(36) NOT NULL,
  `category_name` varchar(100) NOT NULL,
  `parent_category_id` char(36) DEFAULT NULL,
  PRIMARY KEY (`UniqueID`),
  KEY `FKs2ride9gvilxy2tcuv7witnxc` (`parent_category_id`),
  CONSTRAINT `FKs2ride9gvilxy2tcuv7witnxc` FOREIGN KEY (`parent_category_id`) REFERENCES `category` (`UniqueID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `category`
--

LOCK TABLES `category` WRITE;
/*!40000 ALTER TABLE `category` DISABLE KEYS */;
INSERT INTO `category` VALUES ('00000000-0000-0000-0000-000000000001','Kubki',NULL),('d05ac181-9f81-4164-a8f1-14382895afb0','Kafle',NULL);
/*!40000 ALTER TABLE `category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `client`
--

DROP TABLE IF EXISTS `client`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `client` (
  `UniqueID` char(36) NOT NULL,
  `email` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `phone_number` varchar(20) DEFAULT NULL,
  `first_name` varchar(100) DEFAULT NULL,
  `last_name` varchar(100) DEFAULT NULL,
  `role` varchar(20) NOT NULL,
  PRIMARY KEY (`UniqueID`),
  UNIQUE KEY `UKbfgjs3fem0hmjhvih80158x29` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `client`
--

LOCK TABLES `client` WRITE;
/*!40000 ALTER TABLE `client` DISABLE KEYS */;
INSERT INTO `client` VALUES ('448ffe9b-d90c-4299-a5ef-ee632652305a','abc@gmail.com','$2a$10$9xA2Dn4cPkG1EGUeZ5g7aeK5Clui.LENjX9wNWBG3rPNHwhmlmxzC',NULL,'abc','abc','CLIENT'),('85cfefac-7bf4-4891-b15e-e55de4fb0e9d','otajchert@gmail.com','$2a$10$QWuk4UXeTLXc/BwyDxbf1u.Ztov62/l648XmBTvaneA5/Zjn1f2GG',NULL,'Olga','Tajchert','CLIENT'),('a19e06f3-1b29-4c17-b4e5-fb07079092ca','admin@gmail.com','$2a$10$g.IUgmXamq4OmBRpMctTB.cIb5wgDN0S2kOlvzG1Hh0G3TKIi1N0y',NULL,'Admin','TSW','ADMIN'),('a5a39e7b-640b-4b62-801e-179d1c715999','piotr.laski@gmail.com','$2a$10$T2Y5d/CavQ.jZam/f/HuMusTuiwqv8VT2ul4IZvjXdYYkVHBw/QVK','661116226','Chrumo','Chrumski','CLIENT');
/*!40000 ALTER TABLE `client` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `client_address`
--

DROP TABLE IF EXISTS `client_address`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `client_address` (
  `is_default` bit(1) DEFAULT NULL,
  `name` varchar(100) DEFAULT NULL,
  `address_id` char(36) NOT NULL,
  `client_id` char(36) NOT NULL,
  PRIMARY KEY (`address_id`,`client_id`),
  KEY `FK8syw6n1lu2fspywj86fumxb9i` (`client_id`),
  CONSTRAINT `FK60w4whaiqagwthnmpg3kfs1e7` FOREIGN KEY (`address_id`) REFERENCES `address` (`UniqueID`),
  CONSTRAINT `FK8syw6n1lu2fspywj86fumxb9i` FOREIGN KEY (`client_id`) REFERENCES `client` (`UniqueID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `client_address`
--

LOCK TABLES `client_address` WRITE;
/*!40000 ALTER TABLE `client_address` DISABLE KEYS */;
INSERT INTO `client_address` VALUES (_binary '\0','dom','7b840adf-e21c-4930-b191-6ff8a277469e','85cfefac-7bf4-4891-b15e-e55de4fb0e9d'),(_binary '','Dom','dca93786-0a0a-41fa-b78b-4c7814c74023','448ffe9b-d90c-4299-a5ef-ee632652305a');
/*!40000 ALTER TABLE `client_address` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_product`
--

DROP TABLE IF EXISTS `order_product`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_product` (
  `price` decimal(10,2) NOT NULL,
  `qty` int NOT NULL,
  `order_id` char(36) NOT NULL,
  `product_id` char(36) NOT NULL,
  PRIMARY KEY (`order_id`,`product_id`),
  KEY `FKhnfgqyjx3i80qoymrssls3kno` (`product_id`),
  CONSTRAINT `FK7vgj17gqypvw29ybqh0571e9s` FOREIGN KEY (`order_id`) REFERENCES `shop_order` (`UniqueID`),
  CONSTRAINT `FKhnfgqyjx3i80qoymrssls3kno` FOREIGN KEY (`product_id`) REFERENCES `product` (`UniqueID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_product`
--

LOCK TABLES `order_product` WRITE;
/*!40000 ALTER TABLE `order_product` DISABLE KEYS */;
INSERT INTO `order_product` VALUES (55.00,2,'9b365605-15fa-4c79-9884-e3a40046f929','00000000-0000-0000-0000-000000001003'),(400.00,1,'bcc16fa7-3aa6-4618-aa70-c36a6f15c956','28444347-1e35-467c-99c1-f760114de471'),(100.00,1,'d8de24c1-07ee-4582-ab77-6baa72bd1624','00000000-0000-0000-0000-000000001003'),(200.02,1,'d8de24c1-07ee-4582-ab77-6baa72bd1624','87bfcf33-19e1-41a2-9fa8-66ac051b8123'),(150.00,3,'ea9f9c85-ef52-42db-b0a6-d8d23fb446d4','7285f127-013e-4f8b-941e-eefb60b474cb'),(65.00,1,'eaf90c85-5c6c-46f1-aa20-2064d5a87e6a','00000000-0000-0000-0000-000000001001'),(100.00,1,'eb539f8e-ae8e-43d9-b8ac-3192ed3b6aec','00000000-0000-0000-0000-000000001003');
/*!40000 ALTER TABLE `order_product` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_status`
--

DROP TABLE IF EXISTS `order_status`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_status` (
  `UniqueID` char(36) NOT NULL,
  `name` varchar(50) NOT NULL,
  PRIMARY KEY (`UniqueID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_status`
--

LOCK TABLES `order_status` WRITE;
/*!40000 ALTER TABLE `order_status` DISABLE KEYS */;
INSERT INTO `order_status` VALUES ('00000000-0000-0000-0000-000000000021','Zamówienie złożone'),('00000000-0000-0000-0000-000000000022','W realizacji'),('00000000-0000-0000-0000-000000000023','Wyslane'),('00000000-0000-0000-0000-000000000024','Dostarczone'),('00000000-0000-0000-0000-000000000025','Anulowane');
/*!40000 ALTER TABLE `order_status` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product`
--

DROP TABLE IF EXISTS `product`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product` (
  `UniqueID` char(36) NOT NULL,
  `description` text,
  `height` varchar(50) DEFAULT NULL,
  `material` varchar(100) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `photo` varchar(500) DEFAULT NULL,
  `price` decimal(10,2) NOT NULL,
  `product_length` varchar(50) DEFAULT NULL,
  `qty_in_stock` int NOT NULL,
  `SKU` varchar(100) DEFAULT NULL,
  `width` varchar(50) DEFAULT NULL,
  `weight` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`UniqueID`),
  UNIQUE KEY `UKhj4wjxok1d1eq4gnr6lnho1y6` (`SKU`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product`
--

LOCK TABLES `product` WRITE;
/*!40000 ALTER TABLE `product` DISABLE KEYS */;
INSERT INTO `product` VALUES ('00000000-0000-0000-0000-000000001001','kubek z jasnej gliny z rysunkami pieskow.','15cm','Kamionka','Kubek ceramiczny w pieski','https://kriocushjyphhprzosml.supabase.co/storage/v1/object/public/products/Screenshot%202026-03-04%20174020.png',85.04,'8cm',4,'MUG-001','15cm','350 g'),('00000000-0000-0000-0000-000000001002','na świeczkę typu t-light','25cm','Kamionka','świecznik muchomor','https://kriocushjyphhprzosml.supabase.co/storage/v1/object/public/products/Zrzut%20ekranu%202025-12-14%20215009.png',103.13,'18cm',0,'DEC-001','18cm','300 g'),('00000000-0000-0000-0000-000000001003','duża mydelniczka, wyprofilowana i perforowana','11cm','Kamionka','Mydelniczka','https://kriocushjyphhprzosml.supabase.co/storage/v1/object/public/products/Zrzut%20ekranu%202026-01-22%20215901.png',100.00,'16cm',10,'BWL-001','16cm','480 g'),('28444347-1e35-467c-99c1-f760114de471','','','','wazon','https://kriocushjyphhprzosml.supabase.co/storage/v1/object/public/products/Zrzut%20ekranu%202025-12-14%20215052.png',400.00,'',0,'TSW-FD43AFBB','','1200'),('7285f127-013e-4f8b-941e-eefb60b474cb','ociekacz z grzybkiem do trzymania gąbki kuchennej','10cm',NULL,'ociekacz na gąbkę kuchenną','https://kriocushjyphhprzosml.supabase.co/storage/v1/object/public/products/Zrzut%20ekranu%202025-12-16%20151806.png',150.00,'8cm',2,'TSW-74EBD647','10cm','200g'),('87bfcf33-19e1-41a2-9fa8-66ac051b8123','kolorowa figurka ceramiczna','12',NULL,'stworek - figurka','https://kriocushjyphhprzosml.supabase.co/storage/v1/object/public/products/Zrzut%20ekranu%202025-12-16%20151328.png',200.02,'20',0,'TSW-84F711EF','8','400g'),('f3ddf446-9d30-4c89-8061-2970b41255db','kafelki na ścianę','10','ceramika','Kafelki Warszawskie','https://kriocushjyphhprzosml.supabase.co/storage/v1/object/public/products/Zrzut%20ekranu%202025-12-14%20214801.png',150.00,'1',3,'','10','');
/*!40000 ALTER TABLE `product` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product_category`
--

DROP TABLE IF EXISTS `product_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_category` (
  `product_id` char(36) NOT NULL,
  `category_id` char(36) NOT NULL,
  KEY `FKkud35ls1d40wpjb5htpp14q4e` (`category_id`),
  KEY `FK2k3smhbruedlcrvu6clued06x` (`product_id`),
  CONSTRAINT `FK2k3smhbruedlcrvu6clued06x` FOREIGN KEY (`product_id`) REFERENCES `product` (`UniqueID`),
  CONSTRAINT `FKkud35ls1d40wpjb5htpp14q4e` FOREIGN KEY (`category_id`) REFERENCES `category` (`UniqueID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_category`
--

LOCK TABLES `product_category` WRITE;
/*!40000 ALTER TABLE `product_category` DISABLE KEYS */;
INSERT INTO `product_category` VALUES ('00000000-0000-0000-0000-000000001001','00000000-0000-0000-0000-000000000001');
/*!40000 ALTER TABLE `product_category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product_image`
--

DROP TABLE IF EXISTS `product_image`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_image` (
  `UniqueID` char(36) NOT NULL,
  `image_url` varchar(500) NOT NULL,
  `sort_order` int DEFAULT NULL,
  `product_id` char(36) NOT NULL,
  PRIMARY KEY (`UniqueID`),
  KEY `FK6oo0cvcdtb6qmwsga468uuukk` (`product_id`),
  CONSTRAINT `FK6oo0cvcdtb6qmwsga468uuukk` FOREIGN KEY (`product_id`) REFERENCES `product` (`UniqueID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_image`
--

LOCK TABLES `product_image` WRITE;
/*!40000 ALTER TABLE `product_image` DISABLE KEYS */;
INSERT INTO `product_image` VALUES ('4c90d288-4b91-4fe8-8b00-9110f7e31454','https://kriocushjyphhprzosml.supabase.co/storage/v1/object/public/products/Zrzut%20ekranu%202026-01-22%20213951.png',1,'00000000-0000-0000-0000-000000001003'),('556b7d56-d1f1-4f22-b96d-fe0a39e2345d','https://kriocushjyphhprzosml.supabase.co/storage/v1/object/public/products/Zrzut%20ekranu%202026-01-04%20215523.png',0,'f3ddf446-9d30-4c89-8061-2970b41255db'),('a5ea5d67-4dda-4d16-ac8c-e0920c9f7e1c','https://kriocushjyphhprzosml.supabase.co/storage/v1/object/public/products/Zrzut%20ekranu%202026-01-22%20214031.png',0,'00000000-0000-0000-0000-000000001003'),('a7a72716-5ec4-4cb9-961e-3c480856633b','https://kriocushjyphhprzosml.supabase.co/storage/v1/object/public/products/Zrzut%20ekranu%202026-01-22%20213833.png',2,'00000000-0000-0000-0000-000000001003');
/*!40000 ALTER TABLE `product_image` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shipping_method`
--

DROP TABLE IF EXISTS `shipping_method`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `shipping_method` (
  `UniqueID` char(36) NOT NULL,
  `name` varchar(100) NOT NULL,
  `price` decimal(10,2) NOT NULL,
  PRIMARY KEY (`UniqueID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shipping_method`
--

LOCK TABLES `shipping_method` WRITE;
/*!40000 ALTER TABLE `shipping_method` DISABLE KEYS */;
INSERT INTO `shipping_method` VALUES ('00000000-0000-0000-0000-000000000031','Kurier DPD',15.99),('00000000-0000-0000-0000-000000000032','Poczta Polska',12.50),('00000000-0000-0000-0000-000000000033','Odbior osobisty',0.00);
/*!40000 ALTER TABLE `shipping_method` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shop_order`
--

DROP TABLE IF EXISTS `shop_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `shop_order` (
  `UniqueID` char(36) NOT NULL,
  `order_date` datetime(6) DEFAULT NULL,
  `order_total` decimal(10,2) NOT NULL,
  `client_id` char(36) NOT NULL,
  `order_status` char(36) DEFAULT NULL,
  `shipping_address` char(36) DEFAULT NULL,
  `shipping_method` char(36) DEFAULT NULL,
  PRIMARY KEY (`UniqueID`),
  KEY `FK81r7tfjoftcfe0jgkiykiu7ek` (`client_id`),
  KEY `FKsvuvwj22iiq2rwkg0wocxrvb2` (`order_status`),
  KEY `FK4h6u0q1ojjitonokuu2dt5dex` (`shipping_address`),
  KEY `FKatxhfqwimvpshgr64ifo6dx2o` (`shipping_method`),
  CONSTRAINT `FK4h6u0q1ojjitonokuu2dt5dex` FOREIGN KEY (`shipping_address`) REFERENCES `address` (`UniqueID`),
  CONSTRAINT `FK81r7tfjoftcfe0jgkiykiu7ek` FOREIGN KEY (`client_id`) REFERENCES `client` (`UniqueID`),
  CONSTRAINT `FKatxhfqwimvpshgr64ifo6dx2o` FOREIGN KEY (`shipping_method`) REFERENCES `shipping_method` (`UniqueID`),
  CONSTRAINT `FKsvuvwj22iiq2rwkg0wocxrvb2` FOREIGN KEY (`order_status`) REFERENCES `order_status` (`UniqueID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shop_order`
--

LOCK TABLES `shop_order` WRITE;
/*!40000 ALTER TABLE `shop_order` DISABLE KEYS */;
INSERT INTO `shop_order` VALUES ('9b365605-15fa-4c79-9884-e3a40046f929','2026-06-08 11:44:39.377289',125.99,'448ffe9b-d90c-4299-a5ef-ee632652305a','00000000-0000-0000-0000-000000000023','dca93786-0a0a-41fa-b78b-4c7814c74023','00000000-0000-0000-0000-000000000031'),('bcc16fa7-3aa6-4618-aa70-c36a6f15c956','2026-06-18 22:13:32.667390',400.00,'448ffe9b-d90c-4299-a5ef-ee632652305a','00000000-0000-0000-0000-000000000022',NULL,'00000000-0000-0000-0000-000000000033'),('d8de24c1-07ee-4582-ab77-6baa72bd1624','2026-06-18 22:46:00.777053',300.02,'a5a39e7b-640b-4b62-801e-179d1c715999',NULL,NULL,'00000000-0000-0000-0000-000000000033'),('ea9f9c85-ef52-42db-b0a6-d8d23fb446d4','2026-06-18 23:03:41.739545',450.00,'a5a39e7b-640b-4b62-801e-179d1c715999','00000000-0000-0000-0000-000000000021',NULL,'00000000-0000-0000-0000-000000000033'),('eaf90c85-5c6c-46f1-aa20-2064d5a87e6a','2026-06-01 11:44:39.332088',80.99,'448ffe9b-d90c-4299-a5ef-ee632652305a','00000000-0000-0000-0000-000000000024','dca93786-0a0a-41fa-b78b-4c7814c74023','00000000-0000-0000-0000-000000000031'),('eb539f8e-ae8e-43d9-b8ac-3192ed3b6aec','2026-06-18 23:11:10.023838',100.00,'a5a39e7b-640b-4b62-801e-179d1c715999','00000000-0000-0000-0000-000000000021',NULL,'00000000-0000-0000-0000-000000000033');
/*!40000 ALTER TABLE `shop_order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shopping_cart`
--

DROP TABLE IF EXISTS `shopping_cart`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `shopping_cart` (
  `UniqueID` char(36) NOT NULL,
  `client_id` char(36) NOT NULL,
  PRIMARY KEY (`UniqueID`),
  UNIQUE KEY `uk_shopping_cart_client` (`client_id`),
  CONSTRAINT `FKlegqpf0apoematndm58vxssri` FOREIGN KEY (`client_id`) REFERENCES `client` (`UniqueID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shopping_cart`
--

LOCK TABLES `shopping_cart` WRITE;
/*!40000 ALTER TABLE `shopping_cart` DISABLE KEYS */;
INSERT INTO `shopping_cart` VALUES ('df8546f6-ae70-424f-b2b5-8f78ecefaaeb','448ffe9b-d90c-4299-a5ef-ee632652305a'),('c502d356-7b46-42e8-885a-f0f55ea40e7e','85cfefac-7bf4-4891-b15e-e55de4fb0e9d'),('89244bd1-9219-45ca-bf43-c2fa9fb9d207','a19e06f3-1b29-4c17-b4e5-fb07079092ca'),('e4e3d493-0b0c-41f9-8ff8-88e08dcdde59','a5a39e7b-640b-4b62-801e-179d1c715999');
/*!40000 ALTER TABLE `shopping_cart` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-08-17 12:03:25
