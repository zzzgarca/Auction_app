package db;

import componente.Utilizator;
import componente.UtilizatorMinimal;
import java.sql.*;

public class UtilizatorDB {

    public void adaugaUtilizator(String username, String parola, String email, double sold) throws SQLException {
        String sql = "INSERT INTO utilizator (username, parola, email, sold) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, parola);
            stmt.setString(3, email);
            stmt.setDouble(4, sold);

            stmt.executeUpdate();
        }
    }

    public Utilizator autentificare(String username, String parola)
            throws SQLException {

        String sql = "SELECT id, username, parola, email, sold " +
                "FROM utilizator WHERE username = ? AND parola = ?";

        try (Connection conn = DBManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, parola);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Utilizator(
                            rs.getInt   ("id"),
                            rs.getString("username"),
                            rs.getString("parola"),
                            rs.getString("email"),
                            rs.getDouble("sold"));
                }
            }
        }
        return null;
    }


    public UtilizatorMinimal getUtilizatorById(int id) throws SQLException {
        String sql = "SELECT * FROM utilizator WHERE id = ?";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new UtilizatorMinimal(
                            rs.getInt("id"),
                            rs.getString("username")
                    );
                }
            }
        }
        return null;
    }

    public void stergeUtilizator(int id) throws SQLException {
        String sql = "DELETE FROM utilizator WHERE id = ?";

        try (Connection conn = DBManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public int getIdByUsername(String username) throws SQLException {
        String sql = "SELECT id FROM utilizator WHERE username = ?";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }
        }
        return -1;
    }

    public String getUsernameById(int id) throws SQLException {
        String sql = "SELECT username FROM utilizator WHERE id = ?";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getString("username");
            }
        }
        return null;
    }

    public UtilizatorMinimal getUtilizatorMinimalById(int id) throws SQLException {
        String sql = "SELECT id, username FROM utilizator WHERE id = ?";

        try (Connection conn = DBManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new UtilizatorMinimal(
                            rs.getInt("id"),
                            rs.getString("username")
                    );
                }
            }
        }
        return null;
    }

    public double checkSold(int userdId) throws SQLException {
        String sql = "SELECT sold FROM utilizator WHERE id = ?";

        try (Connection conn = DBManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userdId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("sold");
                } else {
                    throw new SQLException("Nu am găsit utilizatorul cu ID-ul indicat");
                }
            }
        }
    }

    public void modificaSold(int userdId, double soldNou) throws SQLException {
        String sql = "UPDATE utilizator SET sold = ? WHERE id = ?";

        try (Connection conn = DBManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, soldNou);
            stmt.setInt(2, userdId);

            stmt.executeUpdate();
            System.out.println("Am actualizat soldul utilizatorului " + userdId + " la valoarea " + soldNou);
        }
    }
}
