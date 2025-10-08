package db;

import componente.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MesajDB {

    public void trimiteMesaj(Mesaj mesaj) throws SQLException {
        String sql = """
            INSERT INTO mesaj (id_expeditor, id_destinatar, continut, data_trimiterii, citit)
            VALUES (
                (SELECT id FROM utilizator WHERE username = ?),
                (SELECT id FROM utilizator WHERE username = ?),
                ?, ?, false)
        """;

        try (Connection conn = DBManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, mesaj.getExpeditor().getUsername());
            stmt.setString(2, mesaj.getDestinatar().getUsername());
            stmt.setString(3, mesaj.getContinut());
            stmt.setTimestamp(4, Timestamp.valueOf(mesaj.getDataTrimiterii()));
            stmt.executeUpdate();
        }
    }

    public List<Mesaj> getMesajePrimite(String username) throws SQLException {
        List<Mesaj> mesaje = new ArrayList<>();
        String sql = """
        SELECT m.*, 
               ue.id AS expeditor_id, ue.username AS expeditor_username,
               ud.id AS destinatar_id, ud.username AS destinatar_username
        FROM mesaj m
        JOIN utilizator ue ON m.id_expeditor = ue.id
        JOIN utilizator ud ON m.id_destinatar = ud.id
        WHERE ud.username = ?
        """;

        try (Connection conn = DBManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Mesaj mesaj = construiesteMesajDinResultSet(rs);
                    mesaje.add(mesaj);
                    marcheazaCaCitit(conn, mesaj.getId());
                }
            }
        }

        return mesaje;
    }

    private void marcheazaCaCitit(Connection conn, int mesajId) throws SQLException {
        String updateSql = "UPDATE mesaj SET citit = true WHERE id = ?";
        try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
            updateStmt.setInt(1, mesajId);
            updateStmt.executeUpdate();
        }
    }

    public List<Mesaj> getMesajeNecitite(String username) throws SQLException {
        List<Mesaj> mesaje = new ArrayList<>();
        String sql = """
        SELECT m.*, 
               ue.id AS expeditor_id, ue.username AS expeditor_username,
               ud.id AS destinatar_id, ud.username AS destinatar_username
        FROM mesaj m
        JOIN utilizator ue ON m.id_expeditor = ue.id
        JOIN utilizator ud ON m.id_destinatar = ud.id
        WHERE ud.username = ? AND m.citit = false
        ORDER BY m.data_trimiterii DESC
        """;

        try (Connection conn = DBManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Mesaj mesaj = construiesteMesajDinResultSet(rs);
                    mesaje.add(mesaj);
                    marcheazaCaCitit(conn, mesaj.getId());
                }
            }
        }

        return mesaje;
    }

    private Mesaj construiesteMesajDinResultSet(ResultSet rs) throws SQLException {
        UtilizatorMinimal expeditor = new UtilizatorMinimal(
                rs.getInt("expeditor_id"),
                rs.getString("expeditor_username")
        );

        UtilizatorMinimal destinatar = new UtilizatorMinimal(
                rs.getInt("destinatar_id"),
                rs.getString("destinatar_username")
        );

        return new Mesaj(
                rs.getInt("id"),
                expeditor,
                destinatar,
                rs.getString("continut"),
                rs.getTimestamp("data_trimiterii").toLocalDateTime()
        );
    }

    public Mesaj trimiteMesaj(UtilizatorMinimal expeditor, UtilizatorMinimal destinatar, String continut) throws SQLException {
        String sql = "INSERT INTO mesaj (id_expeditor, id_destinatar, continut, data_trimiterii, citit) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            UtilizatorDB utilizatorDB = new UtilizatorDB();
            int idExpeditor = utilizatorDB.getIdByUsername(expeditor.getUsername());
            int idDestinatar = utilizatorDB.getIdByUsername(destinatar.getUsername());

            LocalDateTime data = LocalDateTime.now();

            stmt.setInt(1, idExpeditor);
            stmt.setInt(2, idDestinatar);
            stmt.setString(3, continut);
            stmt.setTimestamp(4, Timestamp.valueOf(data));
            stmt.setBoolean(5, false);
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int mesajId = generatedKeys.getInt(1);
                    return new Mesaj(mesajId, expeditor, destinatar, continut, data);
                } else {
                    throw new SQLException("Crearea mesajului a eșuat, nu s-a generat un ID.");
                }
            }
        }
    }
}
