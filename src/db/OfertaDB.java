package db;

import componente.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class OfertaDB {

    private static LicitatieDB licitatieDB;
    private static UtilizatorDB utilizatorDB;

    public static void setLicitatieDB(LicitatieDB ldb) {
        licitatieDB = ldb;
    }

    public static void setUtilizatorDB(UtilizatorDB udb) {
        utilizatorDB = udb;
    }

    public Oferta adaugaOferta(int idLicitatie, UtilizatorMinimal utilizator, double suma, LocalDateTime data) throws SQLException {
        String sql = "INSERT INTO oferta (id_licitatie, id_utilizator, suma, data) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, idLicitatie);
            stmt.setInt(2, utilizator.getId());
            stmt.setDouble(3, suma);
            stmt.setTimestamp(4, Timestamp.valueOf(data));
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int ofertaId = generatedKeys.getInt(1);
                    return new Oferta(ofertaId, idLicitatie, utilizator, suma, data);
                } else {
                    throw new SQLException("Crearea ofertei a eșuat, nu s-a generat un ID.");
                }
            }
        }
    }

    public TreeSet<Oferta> getOfertePentruLicitatie(int idLicitatie) throws SQLException {
        TreeSet<Oferta> oferte = new TreeSet<>();
        String sql = "SELECT o.*, u.username FROM oferta o JOIN utilizator u ON o.id_utilizator = u.id WHERE o.id_licitatie = ?";

        try (Connection conn = DBManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idLicitatie);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                UtilizatorMinimal utilizator = new UtilizatorMinimal(
                        rs.getInt("id_utilizator"),
                        rs.getString("username")
                );

                oferte.add(new Oferta(
                        rs.getInt("id"),
                        idLicitatie,
                        utilizator,
                        rs.getDouble("suma"),
                        rs.getTimestamp("data").toLocalDateTime()
                ));
            }
        }

        return oferte;
    }


    public Oferta getOfertePentruIdOferta(int idOferta) throws SQLException {
        String sql = "SELECT * FROM oferta WHERE id = ?";

        try (Connection conn = DBManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idOferta);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    UtilizatorMinimal u = utilizatorDB.getUtilizatorById(rs.getInt("id_utilizator"));
                    return new Oferta(
                            idOferta,
                            rs.getInt("id_licitatie"),
                            u,
                            rs.getDouble("suma"),
                            rs.getTimestamp("data").toLocalDateTime()
                    );
                } else {
                    throw new SQLException("Nu există ofertă pentru ID-ul indicat.");
                }
            }
        }
    }

    public void stergeOferta(int idOferta) throws SQLException {
        String sql = "DELETE FROM oferta WHERE id = ?";

        try (Connection conn = DBManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idOferta);
            stmt.executeUpdate();
        }
    }

    public ArrayList<Oferta> getOfertePentruUtilizator(int idUtilizator) throws SQLException {
        ArrayList<Oferta> oferte = new ArrayList<>();
        String sql = "SELECT * FROM oferta WHERE id_utilizator = ?";

        try (Connection conn = DBManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUtilizator);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Licitatie licitatie = licitatieDB.getLicitatieById(rs.getInt("id_licitatie"));
                    UtilizatorMinimal utilizator = utilizatorDB.getUtilizatorById(idUtilizator);

                    oferte.add(new Oferta(
                            rs.getInt("id"),
                            licitatie.getId(),
                            utilizator,
                            rs.getDouble("suma"),
                            rs.getTimestamp("data").toLocalDateTime()
                    ));
                }
            }
        }

        return oferte;
    }

    public ArrayList<Oferta> getToateOfertele() throws SQLException {
        ArrayList<Oferta> oferte = new ArrayList<>();
        String sql = "SELECT * FROM oferta";

        try (Connection conn = DBManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Licitatie licitatie = licitatieDB.getLicitatieById(rs.getInt("id_licitatie"));
                int idUtilizator = rs.getInt("id_utilizator");
                UtilizatorMinimal utilizator = utilizatorDB.getUtilizatorById(idUtilizator);

                oferte.add(new Oferta(
                        rs.getInt("id"),
                        licitatie.getId(),
                        utilizator,
                        rs.getDouble("suma"),
                        rs.getTimestamp("data").toLocalDateTime()
                ));
            }
        }

        return oferte;
    }
}
