package db;

import componente.Administrator;
import componente.Utilizator;

import java.sql.*;

public class AdministratorDB {

    public boolean esteAdministrator(int idUtilizator) throws SQLException {
        String sql = "SELECT * FROM administrator WHERE id_utilizator = ?";

        try (Connection conn = DBManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUtilizator);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    public Administrator autentificare(String username, String parola) throws SQLException {
        String sql = "SELECT * FROM utilizator WHERE username = ? AND parola = ?";

        try (Connection conn = DBManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, parola);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Administrator(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("parola"),
                            rs.getString("email"),
                            rs.getDouble("sold")
                    );
                }
            }
        }
        return null;
    }

    public void adaugaAdministrator(int idUtilizator) throws SQLException {
        String sql = "INSERT INTO administrator (id_utilizator) VALUES (?)";

        try (Connection conn = DBManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUtilizator);
            stmt.executeUpdate();
        }
    }

    public void stergeAdministrator(int idUtilizator) throws SQLException {
        String sql = "DELETE FROM administrator WHERE id_utilizator = ?";

        try (Connection conn = DBManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUtilizator);
            stmt.executeUpdate();
        }
    }

}
