# Plateforme bancaire - Test Adria PFE 27

Auteur : à compléter (nom et prénom)
Dépôt : nom-prenom-test-adria-pfe-27

## 1. Présentation

Ce projet met en place une plateforme bancaire basée sur une architecture microservices. Elle permet de consulter des comptes bancaires, d'enregistrer des transactions et de publier des événements métier. Un assistant conversationnel (Spring AI) permettra d'interroger les données depuis un client REST ou Telegram.

Ce document décrit le travail réalisé étape par étape. Il est complété au fur et à mesure de l'avancement.

## 2. Architecture cible

| Composant | Rôle | Port prévu | État |
|---|---|---|---|
| account-service | Gestion des comptes bancaires | 8081 | En cours |
| transaction-service | Enregistrement et consultation des transactions, virements | 8082 | À faire |
| discovery-service | Annuaire des services (Eureka) | 8761 | À faire |
| config-service | Configuration centralisée (bank.transaction.max-amount=1000) | 8888 | À faire |
| gateway-service | Point d'entrée unique (Spring Cloud Gateway) | 9999 | À faire |
| chatbot-service | Agent IA connecté aux services via MCP | 8090 | À faire |
| transaction-consumer | Consommation des événements Kafka | à définir | À faire |

## 3. Technologies

- Java 21
- Spring Boot 4.1.2
- Spring Cloud 2025.1.4
- Spring AI 2.0.1
- Maven (projet multi-modules)
- H2 (base de données en mémoire, pour le développement)
- Kafka, Resilience4J, OpenFeign, GraphQL, MCP (étapes suivantes)
## 4. Organisation du projet

Le projet est un projet Maven parent (`bank-platform`) qui contient un module par microservice.

```
bank-platform
  pom.xml                  (projet parent, versions communes)
  http/                    (fichiers de test HTTP)
  account-service/
    pom.xml
    src/main/java/net/hmi/bank/account
      domain/              (entités et énumérations)
      repository/          (accès aux données)
      service/             (logique métier, DTO, mapper)
      exception/           (exceptions métier)
      web/rest/            (contrôleurs REST)
      web/graphql/         (connecteur GraphQL)
      mcp/                 (outils MCP pour l'agent IA)
```

Les connecteurs REST, GraphQL et MCP appellent tous la même couche `service`. La logique métier n'est donc écrite qu'une seule fois.

## 5. Étapes réalisées

### Étape 1 - Préparation du projet (Partie A)

1. Création du dépôt GitHub privé `nom-prenom-test-adria-pfe-27`.
2. Création du projet Maven parent `bank-platform` avec IntelliJ (Java, Maven, JDK 21).
3. Configuration du `pom.xml` parent :
    - parent `spring-boot-starter-parent` 4.1.2
    - packaging `pom`
    - gestion centralisée des versions Spring Cloud et Spring AI (BOM)
    - configuration du processeur d'annotations Lombok
4. Ajout du module `account-service` dans la balise `<modules>` du parent. Chaque nouveau module sera ajouté de la même manière à sa création.

### Étape 2 - Microservice account-service (Partie C.1, REST)

#### Modèle

| Champ | Type | Remarque |
|---|---|---|
| id | UUID | généré automatiquement |
| customerReference | String | obligatoire |
| status | ACTIVE ou BLOCKED | ACTIVE à la création |
| currency | String | code de 3 lettres |
| balance | double | conforme à l'énoncé |
| version | Long | verrouillage optimiste |

#### Choix de conception

- Les mises à jour du solde passent par deux opérations explicites, `debit` et `credit`, et non par une modification directe du solde. Un virement se décompose ainsi en un débit et un crédit.
- Le service impose lui-même ses règles : le compte doit être actif et le solde ne peut pas devenir négatif.
- Le calcul du solde passe par `BigDecimal` avant d'être stocké en `double`, afin de limiter les erreurs d'arrondi.
- Le champ `version` (`@Version`) évite que deux opérations simultanées s'écrasent mutuellement.
- Les DTO d'entrée et de sortie sont séparés : le client n'envoie jamais l'identifiant, il le reçoit dans la réponse.
#### API REST

| Méthode | URL | Description | Code de succès |
|---|---|---|---|
| POST | /accounts | Créer un compte | 201 |
| GET | /accounts | Lister les comptes (filtre optionnel `customerReference`) | 200 |
| GET | /accounts/{id} | Consulter un compte | 200 |
| PATCH | /accounts/{id}/status | Bloquer ou activer un compte | 200 |
| POST | /accounts/{id}/debit | Débiter un compte | 200 |
| POST | /accounts/{id}/credit | Créditer un compte | 200 |

#### Gestion des erreurs

| Situation | Code HTTP |
|---|---|
| Données invalides (montant négatif, devise incorrecte) | 400 |
| Compte introuvable | 404 |
| Règle métier non respectée (solde insuffisant, compte bloqué) | 422 |
| Modification concurrente du même compte | 409 |

#### Lancement

```
mvn clean install
```

Puis lancer la classe `AccountServiceApplication` depuis IntelliJ. Le service écoute sur le port 8081.

#### Tests

Les tests manuels sont décrits dans le fichier `http/account.http` (client HTTP d'IntelliJ). Les requêtes sont exécutées dans l'ordre.

| N° | Scénario | Résultat attendu | Capture |
|---|---|---|---|
| 1 | Créer le compte A (500 EUR) | 201, compte ACTIVE | à ajouter |
| 2 | Créer le compte B (100 EUR) | 201 | à ajouter |
| 3 | Lister les comptes | 200, deux comptes | à ajouter |
| 4 | Consulter le compte A | 200 | à ajouter |
| 5 | Débiter A de 100 | 200, solde 400 | à ajouter |
| 6 | Créditer B de 50 | 200, solde 150 | à ajouter |
| 7 | Débiter A d'un montant supérieur au solde | 422 | à ajouter |
| 8 | Débiter A d'un montant négatif | 400 | à ajouter |
| 9 | Bloquer A | 200, statut BLOCKED | à ajouter |
| 10 | Débiter A (compte bloqué) | 422 | à ajouter |
| 11 | Consulter un compte inexistant | 404 | à ajouter |
| 12 | Créer un compte avec une devise invalide | 400 | à ajouter |

Les résultats réellement obtenus seront confirmés par les captures d'écran du rapport.

## 6. Hypothèses retenues

L'énoncé ne précise pas certains points. Les hypothèses suivantes sont retenues :

- Une transaction est un débit du compte (elle porte une catégorie de commerçant et un pays, ce qui correspond à un paiement).
- Le solde d'un compte ne peut pas devenir négatif.
- Le montant d'une transaction est comparé au plafond `bank.transaction.max-amount`, fourni par le config-service.
## 7. Suite du travail

- [x] Projet Maven parent et module account-service
- [ ] Tests de account-service (REST)
- [ ] account-service : GraphQL et MCP
- [ ] transaction-service : règles métier et virement
- [ ] Communication OpenFeign et tolérance aux pannes (Resilience4J)
- [ ] transaction-service : GraphQL et MCP
- [ ] Discovery service (Eureka)
- [ ] Config service
- [ ] Gateway (Spring Cloud Gateway)
- [ ] Kafka : producteur et consommateur
- [ ] Agent IA (Spring AI, MCP, Telegram)
- [ ] Diagramme de classes et schéma d'architecture
- [ ] Rapport PDF avec captures d'écran