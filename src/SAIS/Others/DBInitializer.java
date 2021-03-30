package SAIS.Others;



import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


//invokes this class whenever the need for the SAIS Database
public class DBInitializer {
    private static final String URL = "jdbc:mysql://localhost:3306/SAISDatabase";
   // private static String query;
   private static Connection connection = null;

    public static Connection initializeConnection(){

        try {
            connection = DriverManager.getConnection(URL, "root", "password");
        } catch (Exception ex){
            ex.printStackTrace();
        }
        return connection;
    }
    public static void closeConnection() throws SQLException {
        connection.close();
    }

//    public static void setQuery(String query){
//        DBInitializer.query = query;
//    }
}