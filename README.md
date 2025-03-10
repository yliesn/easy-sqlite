# Utilitaire de connexion SQLite

Ce projet fournit une classe utilitaire pour gérer les connexions et opérations avec une base de données SQLite en Java.

## Structure du projet

```
BDD_Manager/
├── .vscode/
├── bin/
│   ├── App_test.class
│   ├── ConnectSqLite.class
│   └── ConnectSqLite$CustomFormatter.class
├── config/
│   └── database.properties
├── src/
│   ├── App_test.java
│   ├── ConnectSqLite.java
├── .gitignore
└── README.md
```

## Configuration

La configuration de la base de données est définie dans le fichier `config/database.properties` :

```properties
database.url=jdbc:sqlite:db/bdd.db
database.timeout=30
```

## Fonctionnalités

### Classe ConnectSqLite

La classe `ConnectSqLite` offre diverses méthodes pour interagir avec une base de données SQLite :

- **Connexion à la base de données** : Établit une connexion à la base de données SQLite
- **Journalisation** : Système de logs avancé pour tracer toutes les opérations et erreurs
- **Opérations CRUD** :
  - `executeInsert` : Exécution de requêtes INSERT
  - `executeSelect` : Exécution de requêtes SELECT
  - `executeSelectAndGetResults` : Exécution de requêtes SELECT avec conversion en List<Map>
  - `executeUpdate` : Exécution de requêtes UPDATE
  - `executeDelete` : Exécution de requêtes DELETE

### Gestion des paramètres

Toutes les méthodes supportent l'utilisation de requêtes paramétrées pour prévenir les injections SQL, avec prise en charge des types suivants :
- String
- Integer
- Double
- Boolean
- null (valeurs NULL)

### Système de journalisation

Le système de logs est configuré pour :
- Créer un nouveau fichier de log à chaque exécution (format : `database_YYYY-MM-DD_HH-mm-ss.log`)
- Formater les logs avec horodatage, niveau et message détaillé
- Consigner toutes les requêtes SQL, leurs paramètres et les résultats/erreurs

## Utilisation

### Exemple d'insertion

```java
String insertQuery = "INSERT INTO employees (nom, prenom, departement) VALUES (?, ?, ?)";
boolean resultat = ConnectSqLite.executeInsert(insertQuery, "Dupont", "Jean", "IT");
```

### Exemple de sélection

```java
// Méthode 1 : Utilisation de ResultSet
String query = "SELECT * FROM employees WHERE departement = ? AND statut = ?";
try (ResultSet rs = ConnectSqLite.executeSelect(query, "IT", 1)) {
    if (rs != null) {
        while (rs.next()) {
            System.out.printf("%s %s - %s%n", 
                rs.getString("prenom"),
                rs.getString("nom"),
                rs.getString("departement")
            );
        }
    }
} catch (SQLException e) {
    e.printStackTrace();
}

// Méthode 2 : Utilisation de List<Map<String, Object>>
String query = "SELECT nom, prenom, salaire FROM employees WHERE salaire > ?";
List<Map<String, Object>> results = ConnectSqLite.executeSelectAndGetResults(query, 50000.0);
for (Map<String, Object> row : results) {
    System.out.printf("%s %s - %.2f %n",
        row.get("prenom"),
        row.get("nom"),
        (Double) (row.get("salaire"))
    );
}
```

### Exemple de mise à jour

```java
String updateQuery = "UPDATE employees SET salaire = ? WHERE id = ?";
boolean success = ConnectSqLite.executeUpdate(updateQuery, 60000.00, 1);
```

### Exemple de suppression

```java
String deleteQuery = "DELETE FROM employees WHERE id = ?";
boolean success = ConnectSqLite.executeDelete(deleteQuery, 6);
```

## Prérequis

- Java 8 ou supérieur
- Driver JDBC SQLite (org.sqlite.JDBC)

## Installation

1. Clonez le dépôt
2. Assurez-vous que le driver SQLite est dans votre classpath
3. Créez un dossier `db` si vous utilisez le chemin par défaut
4. Personnalisez le fichier de configuration si nécessaire

## Tests

La classe `App_test.java` fournit des exemples complets pour tester toutes les fonctionnalités.
