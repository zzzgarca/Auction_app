package componente;

import db.UtilizatorDB;
import java.sql.SQLException;

public class UtilizatorMinimal {
    private int id;
    private String username;

    public UtilizatorMinimal(int id, String username) {
        this.id = id;
        this.username = username;
    }

    public UtilizatorMinimal(String username) {
        this.username = username;

        try {
            UtilizatorDB utilizatorDB = new UtilizatorDB();
            this.id = utilizatorDB.getIdByUsername(username);
        } catch (SQLException e) {
            System.out.println("Eroare la obținerea ID-ului pentru username-ul " + username + ": " + e.getMessage());
            this.id = 0; // sau poți arunca o excepție personalizată
        }
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setId(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "UtilizatorMinimal{" +
                "id=" + id +
                ", username='" + username + '\'' +
                '}';
    }
}
