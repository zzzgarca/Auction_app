package componente;

import java.time.LocalDateTime;

public class Mesaj {
    private int id;
    private UtilizatorMinimal expeditor;
    private UtilizatorMinimal destinatar;
    private String continut;
    private LocalDateTime dataTrimiterii;

    public Mesaj(int id, UtilizatorMinimal expeditor, UtilizatorMinimal destinatar, String continut, LocalDateTime dataTrimiterii) {
        this.id = id;
        this.expeditor = expeditor;
        this.destinatar = destinatar;
        this.continut = continut;
        this.dataTrimiterii = dataTrimiterii;
    }

    public Mesaj(UtilizatorMinimal expeditor, UtilizatorMinimal destinatar, String continut) {
        this(0, expeditor, destinatar, continut, LocalDateTime.now());
    }

    public int getId() {
        return id;
    }

    public UtilizatorMinimal getExpeditor() {
        return expeditor;
    }

    public UtilizatorMinimal getDestinatar() {
        return destinatar;
    }

    public String getContinut() {
        return continut;
    }

    public LocalDateTime getDataTrimiterii() {
        return dataTrimiterii;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setContinut(String continut) {
        this.continut = continut;
    }

    @Override
    public String toString() {
        return "De la: " + expeditor.getUsername() + "\n" +
                "Către: " + destinatar.getUsername() + "\n" +
                "Data: " + dataTrimiterii + "\n" +
                "Mesaj: " + continut;
    }
}
