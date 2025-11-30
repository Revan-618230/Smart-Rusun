package database;

import java.sql.*;
import java.util.LinkedList;
import java.util.Queue;

public class DatabaseAccess {
    public static final String url =
        "jdbc:sqlserver://DESKTOP-OT4QP56:1433;"
      + "instanceName=SQLEXPRESS;"
      + "databaseName=MIBD;"
      + "integratedSecurity=true;"
      + "trustServerCertificate=true;"
      + "encrypt=false;";

    public static Queue<String> getDataSet(String query) {
        // Implementation same as original
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            Connection connection = DriverManager.getConnection(url);
            Statement state = connection.createStatement();
            ResultSet rs = state.executeQuery(query);
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();

            Queue<String> list = new LinkedList<>();

            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    list.add(rs.getString(i));
                }
            }

            if (!list.isEmpty())
                return list;
            else
                return null;
        } catch (Exception e) {
            System.out.println("Connection Fail");
            e.printStackTrace();
            return null;
        }
    }

    public static boolean editData(String query) {
        try {
            Connection connection = DriverManager.getConnection(url);
            connection.createStatement().executeUpdate(query);
            return true;
        } catch (Exception e) {
            System.out.println("Connection Fail");
            e.printStackTrace();
            return false;
        }
    }

    public static String getLatestNIK() {
        // Implementation same as original
        String NIK = null;
        String query = "SELECT TOP 1 NIK FROM LogPengguna WHERE aktivitas = 'Login' ORDER BY idLog DESC";

        try (Connection connection = DriverManager.getConnection(url);
                Statement stmt = connection.createStatement()) {

            ResultSet rs = stmt.executeQuery(query);

            if (rs.next()) {
                NIK = rs.getString("NIK");
            }
        } catch (SQLException ex) {
            System.out.println("Error: " + ex.getMessage());
            ex.printStackTrace();
        }

        return NIK;
    }

    public static String getUnitByNIK(String NIK) {
        // Implementation same as original
        String unit = null;
        String query = "SELECT idUnit FROM UnitSarusun WHERE NIK = ?";

        try (Connection connection = DriverManager.getConnection(url);
                PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setString(1, NIK);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                unit = rs.getString("idUnit");
            }
        } catch (SQLException ex) {
            System.out.println("Connection Fail");
            ex.printStackTrace();
        }

        return unit;
    }

    public static boolean checkIfNikExists(String nik) {
        // Implementation same as original
        String query = "SELECT COUNT(*) FROM Pemilik WHERE NIK = ?";

        try (Connection conn = DriverManager.getConnection(url);
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, nik);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    return count > 0;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }
}