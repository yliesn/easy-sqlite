import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.*;

/**
 * Classe utilitaire pour gérer les connexions et opérations avec une base de données SQLite.
 * Cette classe fournit des méthodes pour exécuter des requêtes SQL et gérer les logs.
 */
public class Sqlite {
    private static final Logger LOGGER = Logger.getLogger(Sqlite.class.getName());
    private static final String CONFIG_FILE = "config/database.properties";
    private static String LOG_FILE;
    private static String LOG_DIRECTORY = "logs";
    private static String databaseUrl;
    private static Properties properties;

    // Dans la classe Sqlite
    private static String getLogFileName() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        return LOG_DIRECTORY + "/database_" + now.format(formatter) + ".log";
    }

    /**
     * Formateur personnalisé pour les logs.
     * Format: [DATE] [LEVEL] [Thread-ID] Message
     */
    private static class CustomFormatter extends Formatter {
        private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

        @Override
        public String format(LogRecord record) {
            LocalDateTime datetime = LocalDateTime.now();
            StringBuilder sb = new StringBuilder();
            
            // Format: [DATE] [LEVEL] [Thread-ID] Message
            sb.append("[").append(dateFormatter.format(datetime)).append("] ");
            sb.append("[").append(record.getLevel()).append("] ");
            // sb.append("[Thread-").append(Thread.currentThread().getId()).append("] ");
            sb.append(formatMessage(record));
            
            // Ajouter les détails de l'exception si elle existe
            if (record.getThrown() != null) {
                sb.append("\nException: ").append(record.getThrown().toString());
                for (StackTraceElement element : record.getThrown().getStackTrace()) {
                    sb.append("\n\tat ").append(element);
                }
            }
            
            sb.append("\n");
            return sb.toString();
        }
    }

    static {
        setupLogger();
        loadConfiguration();
    }

    /**
     * Écrit un message dans les logs avec un niveau spécifié.
     * 
     * @param message Le message à logger
     * @param level Le niveau de log (INFO, WARNING, SEVERE, etc.)
     */
    public static void writeLog(String message, Level level) {
        LOGGER.log(level, message);
    }

    /**
     * Écrit un message et une exception dans les logs avec un niveau spécifié.
     * 
     * @param message Le message à logger
     * @param level Le niveau de log (INFO, WARNING, SEVERE, etc.)
     * @param thrown L'exception à logger
     */
    public static void writeLog(String message, Level level, Throwable thrown) {
        LOGGER.log(level, message, thrown);
    }

    /**
     * Configure le système de logging.
     * Crée le dossier de logs s'il n'existe pas et configure le format des logs.
     */
    private static void setupLogger() {
        properties = new Properties();
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            // Créer le dossier logs s'il n'existe pas
            properties.load(fis);
            LOG_DIRECTORY = properties.getProperty("database.logURL", "logs");
            Path logPath = Paths.get(LOG_DIRECTORY);
            if (!Files.exists(logPath)) {
                Files.createDirectories(logPath);
            }
            // Générer le nom du fichier avec horodatage
            LOG_FILE = getLogFileName();
            
            // Nettoyer les handlers existants
            for (Handler handler : LOGGER.getHandlers()) {
                LOGGER.removeHandler(handler);
            }
            
            // Configuration du FileHandler avec encodage UTF-8
            FileHandler fileHandler = new FileHandler(LOG_FILE, true);
            fileHandler.setEncoding("UTF-8");  // Ajout de cette ligne
            fileHandler.setFormatter(new CustomFormatter());
            
            LOGGER.setUseParentHandlers(false);
            LOGGER.addHandler(fileHandler);
            LOGGER.setLevel(Level.ALL);
            
            LOGGER.info("Nouveau fichier de log créé: " + LOG_FILE);
            
        } catch (IOException e) {
            System.err.println("Erreur lors de la configuration du fichier de log: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Charge la configuration de la base de données depuis le fichier properties.
     * Si le fichier n'est pas trouvé, utilise les valeurs par défaut.
     * 
     * @throws RuntimeException si le driver SQLite n'est pas trouvé
     */
    private static void loadConfiguration() {
        properties = new Properties();
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            properties.load(fis);
            databaseUrl = properties.getProperty("database.url", "jdbc:sqlite:db/db.db");
            
            // Chargement du pilote SQLite
            Class.forName("org.sqlite.JDBC");
            writeLog("Configuration chargée avec succès. URL de la base: " + databaseUrl, Level.INFO);
        
        } catch (IOException e) {
            writeLog("Impossible de charger le fichier de configuration. Utilisation des valeurs par défaut.", Level.WARNING, e);
        } catch (ClassNotFoundException e) {
            writeLog("Pilote SQLite introuvable", Level.SEVERE, e);
            throw new RuntimeException("Initialisation de la base de données impossible", e);
        }
    }

    /**
     * Établit une connexion à la base de données SQLite.
     * 
     * @return Connection l'objet connexion établie
     * @throws SQLException si la connexion échoue
     */
    public static Connection connect() throws SQLException {
        try {
            Connection conn = DriverManager.getConnection(databaseUrl);
            writeLog("Connexion à SQLite établie avec succès", Level.INFO);
            return conn;
        } catch (SQLException e) {
            writeLog("Erreur lors de la connexion à la base de données", Level.SEVERE, e);
            throw e;
        }
    }

    /**
     * Ferme la connexion à la base de données.
     * 
     * @param conn La connexion à fermer
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
                writeLog("Connexion fermée avec succès", Level.INFO);
            } catch (SQLException e) {
                writeLog("Erreur lors de la fermeture de la connexion", Level.WARNING, e);
            }
        }
    }

    /**
     * Exécute une requête INSERT dans la base de données.
     * 
     * @param insertQuery La requête INSERT à exécuter
     * @param params Les paramètres à insérer dans la requête (optional)
     * @return boolean true si l'insertion a réussi, false sinon
     * 
     * @example
     * boolean success = executeInsert("INSERT INTO employees (nom, age) VALUES (?, ?)", "John", 30);
     */
    public static boolean executeInsert(String insertQuery, Object... params) {
        Connection connection = null;
        PreparedStatement pstmt = null;
        boolean success = false;

        try {
            connection = connect();
            pstmt = connection.prepareStatement(insertQuery);

            // Configuration des paramètres
            for (int i = 0; i < params.length; i++) {
                Object param = params[i];
                if (param == null) {
                    pstmt.setNull(i + 1, Types.NULL);
                } else {
                    switch (param.getClass().getSimpleName()) {
                        case "String":
                            pstmt.setString(i + 1, (String) param);
                            break;
                        case "Integer":
                            pstmt.setInt(i + 1, (Integer) param);
                            break;
                        case "Double":
                            pstmt.setDouble(i + 1, (Double) param);
                            break;
                        case "Boolean":
                            pstmt.setBoolean(i + 1, (Boolean) param);
                            break;
                        default:
                            writeLog("Type non supporté: " + param.getClass().getSimpleName(), Level.WARNING);
                            return false;
                    }
                }
            }

            // Exécution de la requête
            writeLog( "Requête :  "+insertQuery, Level.INFO);
            writeLog("Params" + Arrays.toString(params), Level.INFO);
            int rowsAffected = pstmt.executeUpdate();
            success = (rowsAffected > 0);
            
            if (success) {
                writeLog("Insertion réussie: " + rowsAffected + " ligne(s) affectée(s)", Level.INFO);
            } else {
                writeLog("Aucune ligne insérée", Level.WARNING);
            }

        } catch (SQLException e) {
            String errorCode = String.valueOf(e.getErrorCode());
            String sqlState = e.getSQLState();
            
            writeLog("Erreur SQL lors de l'insertion - Code: " + errorCode + 
                    ", État: " + sqlState + 
                    ", Message: " + e.getMessage(), Level.SEVERE, e);
                    
            // Gestion des erreurs spécifiques
            switch (e.getErrorCode()) {
                case 19: // SQLITE_CONSTRAINT
                    writeLog("Violation de contrainte (clé primaire ou unique)", Level.WARNING);
                    break;
                case 1: // SQLITE_ERROR
                    writeLog("Erreur de syntaxe dans la requête", Level.WARNING);
                    writeLog( "Requête :  "+insertQuery, Level.WARNING);
                    writeLog("Params" + Arrays.toString(params), Level.INFO);

                    break;
                // Ajoutez d'autres cas selon vos besoins
            }
            
            success = false;
            
        } catch (Exception e) {
            writeLog("Erreur inattendue lors de l'insertion", Level.SEVERE, e);
            success = false;
            
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (connection != null) closeConnection(connection);
            } catch (SQLException e) {
                writeLog("Erreur lors de la fermeture des ressources", Level.WARNING, e);
            }
        }

        return success;
    }
    
    /**
     * Exécute une requête SELECT et retourne un ResultSet.
     * Note: Le ResultSet doit être fermé par l'appelant.
     * 
     * @param selectQuery La requête SELECT à exécuter
     * @param params Les paramètres de la requête (optional)
     * @return ResultSet les résultats de la requête, null en cas d'erreur
     * 
     * @example
     * ResultSet rs = executeSelect("SELECT * FROM employees WHERE age > ?", 25);
     */
    public static ResultSet executeSelect(String selectQuery, Object... params) {
        Connection connection = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            connection = connect();
            pstmt = connection.prepareStatement(selectQuery);

            // Configuration des paramètres
            for (int i = 0; i < params.length; i++) {
                Object param = params[i];
                if (param == null) {
                    pstmt.setNull(i + 1, Types.NULL);
                } else {
                    switch (param.getClass().getSimpleName()) {
                        case "String":
                            pstmt.setString(i + 1, (String) param);
                            break;
                        case "Integer":
                            pstmt.setInt(i + 1, (Integer) param);
                            break;
                        case "Double":
                            pstmt.setDouble(i + 1, (Double) param);
                            break;
                        case "Boolean":
                            pstmt.setBoolean(i + 1, (Boolean) param);
                            break;
                        default:
                            writeLog("Type non supporté: " + param.getClass().getSimpleName(), Level.WARNING);
                            return null;
                    }
                }
            }


            writeLog( "Requête :  "+selectQuery, Level.INFO);
            writeLog("Params" + Arrays.toString(params), Level.INFO);
            rs = pstmt.executeQuery();
            writeLog("Requête SELECT exécutée avec succès", Level.INFO);
            return rs;

        } catch (SQLException e) {
            writeLog("Erreur lors de l'exécution de la requête SELECT: " + e.getMessage(), Level.SEVERE, e);
            writeLog( "Requête :  "+selectQuery , Level.WARNING);
            writeLog("Params" + Arrays.toString(params), Level.INFO);

            return null;
        }
    }

    /**
     * Exécute une requête SELECT et retourne les résultats sous forme de List de Map.
     * Gère automatiquement la fermeture des ressources.
     * 
     * @param selectQuery La requête SELECT à exécuter
     * @param params Les paramètres de la requête (optional)
     * @return List<Map<String, Object>> les résultats, chaque ligne est une Map
     * 
     * @example
     * List<Map<String, Object>> results = executeSelectAndGetResults(
     *     "SELECT nom, age FROM employees WHERE departement = ?", 
     *     "IT"
     * );
     */
    public static List<Map<String, Object>> executeSelectAndGetResults(String selectQuery, Object... params) {
        List<Map<String, Object>> results = new ArrayList<>();
        Connection connection = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            connection = connect();
            pstmt = connection.prepareStatement(selectQuery);

            // Configuration des paramètres
            for (int i = 0; i < params.length; i++) {
                Object param = params[i];
                if (param == null) {
                    pstmt.setNull(i + 1, Types.NULL);
                } else {
                    switch (param.getClass().getSimpleName()) {
                        case "String":
                            pstmt.setString(i + 1, (String) param);
                            break;
                        case "Integer":
                            pstmt.setInt(i + 1, (Integer) param);
                            break;
                        case "Double":
                            pstmt.setDouble(i + 1, (Double) param);
                            break;
                        case "Boolean":
                            pstmt.setBoolean(i + 1, (Boolean) param);
                            break;
                        default:
                            writeLog("Type non supporté: " + param.getClass().getSimpleName(), Level.WARNING);
                            return results;
                    }
                }
            }

            writeLog( "Requête :  "+selectQuery , Level.INFO);
            writeLog("Params" + Arrays.toString(params), Level.INFO);
            rs = pstmt.executeQuery();
            
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    Object value = rs.getObject(i);
                    row.put(columnName, value);
                }
                results.add(row);
            }

            writeLog("Requête SELECT exécutée avec succès, " + results.size() + " résultats récupérés", Level.INFO);
            return results;

        } catch (SQLException e) {
            writeLog("Erreur lors de l'exécution de la requête SELECT: " + e.getMessage(), Level.SEVERE, e);
            writeLog( "Requête :  "+selectQuery , Level.WARNING);
            writeLog("Params" + Arrays.toString(params), Level.INFO);
            return results;
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (connection != null) closeConnection(connection);
            } catch (SQLException e) {
                writeLog("Erreur lors de la fermeture des ressources", Level.WARNING, e);
            }
        }
    }

    /**
     * Exécute une requête UPDATE dans la base de données.
     * 
     * @param updateQuery La requête UPDATE à exécuter
     * @param params Les paramètres à mettre à jour (optional)
     * @return boolean true si au moins une ligne a été modifiée, false sinon
     * 
     * @example
     * boolean success = executeUpdate(
     *     "UPDATE employees SET salaire = ? WHERE id = ?",
     *     50000.00,
     *     1
     * );
     */
    public static boolean executeUpdate(String updateQuery, Object... params) {
        Connection connection = null;
        PreparedStatement pstmt = null;
        boolean success = false;

        try {
            connection = connect();
            pstmt = connection.prepareStatement(updateQuery);

            // Configuration des paramètres
            for (int i = 0; i < params.length; i++) {
                Object param = params[i];
                if (param == null) {
                    pstmt.setNull(i + 1, Types.NULL);
                } else {
                    switch (param.getClass().getSimpleName()) {
                        case "String":
                            pstmt.setString(i + 1, (String) param);
                            break;
                        case "Integer":
                            pstmt.setInt(i + 1, (Integer) param);
                            break;
                        case "Double":
                            pstmt.setDouble(i + 1, (Double) param);
                            break;
                        case "Boolean":
                            pstmt.setBoolean(i + 1, (Boolean) param);
                            break;
                        default:
                            writeLog("Type non supporté: " + param.getClass().getSimpleName(), Level.WARNING);
                            return false;
                    }
                }
            }

            writeLog( "Requête :  "+updateQuery , Level.INFO);
            writeLog("Params" + Arrays.toString(params), Level.INFO);
            int rowsAffected = pstmt.executeUpdate();
            success = (rowsAffected > 0);

            if (success) {
                writeLog("Mise à jour réussie: " + rowsAffected + " ligne(s) modifiée(s)", Level.INFO);
            } else {
                writeLog("Aucune ligne modifiée", Level.WARNING);
            }

        } catch (SQLException e) {
            writeLog("Erreur lors de la mise à jour: " + e.getMessage(), Level.SEVERE, e);
            writeLog("Params" + Arrays.toString(params), Level.INFO);
            writeLog( "Requête :  "+updateQuery , Level.WARNING);
            success = false;
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (connection != null) closeConnection(connection);
            } catch (SQLException e) {
                writeLog("Erreur lors de la fermeture des ressources", Level.WARNING, e);
            }
        }

        return success;
    }

    /**
     * Exécute une requête DELETE dans la base de données.
     * 
     * @param deleteQuery La requête DELETE à exécuter
     * @param params Les paramètres de la condition de suppression (optional)
     * @return boolean true si au moins une ligne a été supprimée, false sinon
     * 
     * @example
     * boolean success = executeDelete("DELETE FROM employees WHERE id = ?", 1);
     */
    public static boolean executeDelete(String deleteQuery, Object... params) {
        Connection connection = null;
        PreparedStatement pstmt = null;
        boolean success = false;

        try {
            connection = connect();
            pstmt = connection.prepareStatement(deleteQuery);

            // Configuration des paramètres
            for (int i = 0; i < params.length; i++) {
                Object param = params[i];
                if (param == null) {
                    pstmt.setNull(i + 1, Types.NULL);
                } else {
                    switch (param.getClass().getSimpleName()) {
                        case "String":
                            pstmt.setString(i + 1, (String) param);
                            break;
                        case "Integer":
                            pstmt.setInt(i + 1, (Integer) param);
                            break;
                        case "Double":
                            pstmt.setDouble(i + 1, (Double) param);
                            break;
                        case "Boolean":
                            pstmt.setBoolean(i + 1, (Boolean) param);
                            break;
                        default:
                            writeLog("Type non supporté: " + param.getClass().getSimpleName(), Level.WARNING);
                            return false;
                    }
                }
            }


            writeLog( "Requête :  "+deleteQuery , Level.INFO);
            writeLog("Params" + Arrays.toString(params), Level.INFO);
            int rowsAffected = pstmt.executeUpdate();
            success = (rowsAffected > 0);

            if (success) {
                writeLog("Suppression réussie: " + rowsAffected + " ligne(s) supprimée(s)", Level.INFO);
            } else {
                writeLog("Aucune ligne supprimée", Level.WARNING);
            }

        } catch (SQLException e) {
            writeLog("Erreur lors de la suppression: " + e.getMessage(), Level.SEVERE, e);
            writeLog( "Requête :  "+deleteQuery, Level.WARNING);
            writeLog("Params" + Arrays.toString(params), Level.INFO);
            success = false;
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (connection != null) closeConnection(connection);
            } catch (SQLException e) {
                writeLog("Erreur lors de la fermeture des ressources", Level.WARNING, e);
            }
        }

        return success;
    }
}