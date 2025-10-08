package componente;

public class Produs {
    private int id;
    private String titlu;
    private String descriere;
    private double pretStart;
    private UtilizatorMinimal proprietar;

    public Produs(int id, String titlu, String descriere, double pretStart, UtilizatorMinimal proprietar) {
        this.id = id;
        this.titlu = titlu;
        this.descriere = descriere;
        this.pretStart = pretStart;
        this.proprietar = proprietar;
    }


    public int getId() {
        return id;
    }

    public String getTitlu() {
        return titlu;
    }

    public String getDescriere() {
        return descriere;
    }

    public double getPretStart() {
        return pretStart;
    }

    public UtilizatorMinimal getProprietar() {
        return proprietar;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTitlu(String titlu) {
        this.titlu = titlu;
    }

    public void setDescriere(String descriere) {
        this.descriere = descriere;
    }

    public void setPretStart(double pretStart) {
        this.pretStart = pretStart;
    }

    public void setProprietar(UtilizatorMinimal proprietar) {
        this.proprietar = proprietar;
    }




    @Override
    public String toString() {
        return "Produs{" +
                "id=" + id +
                ", titlu='" + titlu + '\'' +
                ", desciere='" + descriere + '\'' +
                ", pretStart=" + pretStart +
                ", proprietar=" + proprietar.getUsername() +
                '}';
    }
}
