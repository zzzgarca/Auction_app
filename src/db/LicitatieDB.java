package db;

import componente.Licitatie;
import componente.Oferta;
import componente.Produs;
import componente.UtilizatorMinimal;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

public class LicitatieDB {

    private static ProdusDB produsDB;
    private static UtilizatorDB utilizatorDB;
    private static OfertaDB ofertaDB;


    public static void setProdusDB(ProdusDB pdb) {
        produsDB = pdb;
    }


    public static void setUtilizatorDB(UtilizatorDB udb) {
        utilizatorDB = udb;
    }


    public static void setOfertaDB(OfertaDB odb) {
        ofertaDB = odb;
    }


    public Licitatie adaugaLicitatie(Produs produs, UtilizatorMinimal vanzator, LocalDateTime dataLimita) throws SQLException {
        String sql = "INSERT INTO licitatie (id_produs, id_vanzator, data_limita) VALUES (?, ?, ?)";

        try (Connection conn = DBManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, produs.getId());
            stmt.setInt(2, vanzator.getId());
            stmt.setTimestamp(3, Timestamp.valueOf(dataLimita));

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int licitatieId = generatedKeys.getInt(1);
                    TreeSet oferte = new TreeSet<>();
                    return new Licitatie(licitatieId, produs, vanzator, dataLimita, oferte);
                } else {
                    throw new SQLException("Crearea licitației a eșuat, nu s-a generat un ID.");
                }
            }
        }
    }

    public void marcheazaLicitatie(int id) throws SQLException {
        String sql = "UPDATE licitatie SET procesata = ? WHERE id = ?";

        try (Connection conn = DBManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBoolean(1, true);  // or use 1 if your column is TINYINT(1)
            stmt.setInt(2, id);
            stmt.executeUpdate();
        }
    }



    public HashMap<Integer, Licitatie> getLicitatiiActive() throws SQLException {
        HashMap<Integer, Licitatie> list = new HashMap<>();
        String sql = "SELECT * FROM licitatie WHERE procesata = 0";

        try (Connection conn = DBManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int idLicitatie = rs.getInt("id");
                Produs produs = produsDB.getProdusById(rs.getInt("id_produs"));
                int idVanzator = rs.getInt("id_vanzator");
                String username = utilizatorDB.getUsernameById(idVanzator);
                UtilizatorMinimal vanzator = new UtilizatorMinimal(idVanzator, username);
                LocalDateTime dataLimita = rs.getTimestamp("data_limita").toLocalDateTime();

                TreeSet<Oferta> oferte = ofertaDB.getOfertePentruLicitatie(idLicitatie);
                Licitatie licitatie = new Licitatie(idLicitatie, produs, vanzator, dataLimita, oferte);
                list.put(idLicitatie, licitatie);
            }
        }

        return list;
    }




    public HashMap<Integer, Licitatie> getToateLicitatiile() throws SQLException {
        HashMap<Integer, Licitatie> list = new HashMap<>();
        String sql1 = "SELECT * FROM licitatie";

        try (Connection conn = DBManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs1 = stmt.executeQuery(sql1)) {

            while (rs1.next()) {
                int idLicitatie = rs1.getInt("id");
                Produs produs = produsDB.getProdusById(rs1.getInt("id_produs"));
                int idVanzator = rs1.getInt("id_vanzator");
                String username = utilizatorDB.getUsernameById(idVanzator);
                UtilizatorMinimal vanzator = new UtilizatorMinimal(idVanzator, username);
                LocalDateTime dataLimita = rs1.getTimestamp("data_limita").toLocalDateTime();

                TreeSet<Oferta> oferte = ofertaDB.getOfertePentruLicitatie(idLicitatie);

                Licitatie licitatie = new Licitatie(idLicitatie, produs, vanzator, dataLimita, oferte);
                list.put(idLicitatie, licitatie);
            }
        }

        return list;
    }

    public Licitatie getLicitatieById(int idLicitatie) throws SQLException {
        Licitatie l = null;
        String sql1 = "SELECT * FROM licitatie WHERE id = ?";

        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt1 = conn.prepareStatement(sql1)) {

            pstmt1.setInt(1, idLicitatie);

            try (ResultSet rs1 = pstmt1.executeQuery()) {
                if (rs1.next()) {
                    Produs produs = produsDB.getProdusById(rs1.getInt("id_produs"));
                    int idVanzator = rs1.getInt("id_vanzator");
                    String username = utilizatorDB.getUsernameById(idVanzator);
                    UtilizatorMinimal vanzator = new UtilizatorMinimal(idVanzator, username);
                    LocalDateTime dataLimita = rs1.getTimestamp("data_limita").toLocalDateTime();

                   TreeSet<Oferta> oferte = ofertaDB.getOfertePentruLicitatie(idLicitatie);

                    l = new Licitatie(idLicitatie, produs, vanzator, dataLimita, oferte);
                }
            }
        }

        return l;
    }


    public void stergeLicitatie(int id) throws SQLException {
        String sql = "DELETE FROM licitatie WHERE id = ?";

        try (Connection conn = DBManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

}
