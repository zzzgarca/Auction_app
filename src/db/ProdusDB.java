package db;

import componente.Licitatie;
import componente.Oferta;
import componente.Produs;
import componente.UtilizatorMinimal;

import java.sql.*;
import java.util.ArrayList;

public class ProdusDB {

    public Produs adaugaProdus(String titlu, String descriere, double pretStart, UtilizatorMinimal utilizator) throws SQLException {
        String sql = """
        INSERT INTO produs (titlu, descriere, pret_start, id_utilizator)
        VALUES (?, ?, ?, (
            SELECT id FROM utilizator WHERE username = ?
        ))
        """;

        try (Connection conn = DBManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, titlu);
            stmt.setString(2, descriere);
            stmt.setDouble(3, pretStart);
            stmt.setString(4, utilizator.getUsername());

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int idProdus = generatedKeys.getInt(1);
                    return new Produs(idProdus, titlu, descriere, pretStart, utilizator);
                } else {
                    throw new SQLException("Crearea produsului a eșuat, nu s-a generat un ID.");
                }
            }
        }
    }

    public ArrayList<Produs> getProdusePentruUtilizator(String username) throws SQLException {
        ArrayList<Produs> produse = new ArrayList<>();
        String sql = """
        SELECT p.*, u.id AS user_id FROM produs p
        JOIN utilizator u ON p.id_utilizator = u.id
        WHERE u.username = ?
        """;

        try (Connection conn = DBManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    UtilizatorMinimal utilizator = new UtilizatorMinimal(
                            rs.getInt("user_id"),
                            username
                    );

                    produse.add(new Produs(
                            rs.getInt("id"),
                            rs.getString("titlu"),
                            rs.getString("descriere"),
                            rs.getDouble("pret_start"),
                            utilizator
                    ));
                }
            }
        }

        return produse;
    }

    public ArrayList<Produs> getToateProdusele() throws SQLException {
        ArrayList<Produs> produse = new ArrayList<>();
        String sql = """
        SELECT p.*, u.id AS user_id, u.username 
        FROM produs p 
        JOIN utilizator u ON p.id_utilizator = u.id
        """;

        try (Connection conn = DBManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                UtilizatorMinimal utilizator = new UtilizatorMinimal(
                        rs.getInt("user_id"),
                        rs.getString("username")
                );

                produse.add(new Produs(
                        rs.getInt("id"),
                        rs.getString("titlu"),
                        rs.getString("descriere"),
                        rs.getDouble("pret_start"),
                        utilizator
                ));
            }
        }

        return produse;
    }

    public ArrayList<Produs> cautaProduseDupaTitlu(String text) throws SQLException {
        ArrayList<Produs> rezultate = new ArrayList<>();
        String sql = """
        SELECT p.*, u.id AS user_id, u.username, l.id AS licitatie_id, l.data_limita
        FROM produs p
        JOIN utilizator u ON p.id_utilizator = u.id
        LEFT JOIN licitatie l ON p.id = l.id_produs
        WHERE LOWER(p.titlu) LIKE ?
        """;

        try (Connection conn = DBManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + text.toLowerCase() + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    UtilizatorMinimal utilizator = new UtilizatorMinimal(
                            rs.getInt("user_id"),
                            rs.getString("username")
                    );

                    Produs produs = new Produs(
                            rs.getInt("id"),
                            rs.getString("titlu"),
                            rs.getString("descriere"),
                            rs.getDouble("pret_start"),
                            utilizator
                    );

                    rezultate.add(produs);

                    System.out.println("Produs: " + produs);
                    int licitatieId = rs.getInt("licitatie_id");
                    Timestamp dataLimita = rs.getTimestamp("data_limita");

                    if (licitatieId > 0 && dataLimita != null) {
                        System.out.println(" → Este într-o licitație (ID: " + licitatieId + ", limită: " + dataLimita.toLocalDateTime() + ")");
                    } else {
                        System.out.println(" → Nu este într-o licitație.");
                    }

                    System.out.println();
                }
            }
        }

        return rezultate;
    }

    public Produs getProdusById(int id) throws SQLException {
        String sql = """
        SELECT p.*, u.id AS user_id, u.username 
        FROM produs p
        JOIN utilizator u ON p.id_utilizator = u.id
        WHERE p.id = ?
        """;

        try (Connection conn = DBManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    UtilizatorMinimal utilizator = new UtilizatorMinimal(
                            rs.getInt("user_id"),
                            rs.getString("username")
                    );

                    return new Produs(
                            rs.getInt("id"),
                            rs.getString("titlu"),
                            rs.getString("descriere"),
                            rs.getDouble("pret_start"),
                            utilizator
                    );
                }
            }
        }

        return null;
    }

    public void schimbaProprietar(int idProdus, int idProprietar) throws SQLException {
        String sql = "UPDATE produs SET id_utilizator = ? WHERE id = ?";

        try (Connection conn = DBManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idProprietar);
            stmt.setInt(2, idProdus);
            stmt.executeUpdate();
        }
    }

    public void stergeProdus(int id) throws SQLException {
        String sql = "DELETE FROM produs WHERE id = ?";

        try (Connection conn = DBManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}
