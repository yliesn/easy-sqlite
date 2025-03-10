import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class App_test {
    public static void main(String[] args) {

        //! Test de la méthode executeInsert
        // Test d'une insertion normale
        String insertQuery = "INSERT INTO employees (nom, prenom, departement) VALUES (?, ?, ?)";
        
        boolean resultat = SqliteManager.executeInsert(insertQuery,
            "Ylies",
            "Nejara",
            "IT"
        );
        
        if (resultat) {
            System.out.println("Insertion réussie !");
        } else {
            System.out.println("L'insertion a échoué, vérifiez les logs pour plus de détails.");
        }

        // Test avec une erreur (par exemple, violation de contrainte)
        resultat = SqliteManager.executeInsert(insertQuery,
            null,  // Erreur si nom ne peut pas être null
            "TEST",
            "IT"
        );
        
        if (!resultat) {
            System.out.println("Échec attendu de l'insertion avec valeur null.");
        }

        // Test avec une requête mal formée
        String requeteInvalide = "INSERT INTO employees (colonne_inexistante) VALUES (?)";
        resultat = SqliteManager.executeInsert(requeteInvalide, "test");
        
        if (!resultat) {
            System.out.println("Échec attendu avec une requête invalide.");
        }

        //! Test de la méthode executeSelect
        
        // Exemple 1 : Utilisation de executeSelect avec ResultSet
        String query1 = "SELECT * FROM employees WHERE departement = ? AND statut = ?";
        try (ResultSet rs = SqliteManager.executeSelect(query1, "IT", 1)) {
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

        // Exemple 2 : Utilisation de executeSelectAndGetResults
        String query2 = "SELECT nom, prenom, salaire, departement FROM employees WHERE salaire > ?";
        List<Map<String, Object>> results2 = SqliteManager.executeSelectAndGetResults(query2, 50000.0);
        
        
        for (Map<String, Object> row : results2) {
            System.out.printf("%s %s %s- %.2f %n",
                row.get("prenom"),
                row.get("nom"),
                row.get("departement"),
                (Double) (row.get("salaire"))
            );
        }

        // ! Test de la méthode executeUpdate
        
        // Exemple de mise à jour : augmenter le salaire d'un employé
        String updateQuery = "UPDATE employees SET salaire = ? WHERE id = ?";
        boolean resultat3 = SqliteManager.executeUpdate(updateQuery, 60000.00, 1);
        
        if (resultat3) {
            System.out.println("Mise à jour du salaire réussie");
        } else {
            System.out.println("Échec de la mise à jour du salaire");
        }

        // Exemple de mise à jour multiple : augmenter le salaire des employés IT
        String updateMultipleQuery = "UPDATE employees SET salaire = salaire * 1.1 WHERE departement = ?";
        resultat3 = SqliteManager.executeUpdate(updateMultipleQuery, "IT");
        
        if (resultat3) {
            System.out.println("Augmentation des salaires IT réussie");
        }

        // ! Test de la méthode executeDelete

        // Exemple de suppression : supprimer un employé par son ID
        String deleteQuery = "DELETE FROM employees WHERE id = ?";
        resultat3 = SqliteManager.executeDelete(deleteQuery, 6);
        
        if (resultat3) {
            System.out.println("Suppression de l'employé réussie");
        } else {
            System.out.println("Échec de la suppression");
        }

        // Exemple de suppression multiple : supprimer tous les employés inactifs
        String deleteMultipleQuery = "DELETE FROM employees WHERE statut = ?";
        resultat3 = SqliteManager.executeDelete(deleteMultipleQuery, 1);
        
        if (resultat3) {
            System.out.println("Suppression des employés inactifs réussie");
        }
    }
}