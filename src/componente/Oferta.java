package componente;

import java.time.LocalDateTime;

public class Oferta implements Comparable<Oferta> {
    private int id;
    int idLicitatie;
    private UtilizatorMinimal utilizator;
    private double suma;
    private LocalDateTime data;

    public Oferta(int id, int idLicitatie, UtilizatorMinimal utilizator, double suma, LocalDateTime data) {
        this.id = id;
        this.idLicitatie = idLicitatie;
        this.utilizator = new UtilizatorMinimal(utilizator.getId(), utilizator.getUsername());
        this.suma = suma;
        this.data = data;
    }


    public int getId() {
        return id;
    }

    public int getIdLicitatie() {
        return idLicitatie;
    }

    public UtilizatorMinimal getUtilizator() {
        return utilizator;
    }

    public double getSuma() {
        return suma;
    }

    public LocalDateTime getData() {
        return data;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setLicitatie(int idLicitatie) {
        this.idLicitatie = idLicitatie;
    }

    public void setUtilizator(UtilizatorMinimal utilizator) {
        this.utilizator = utilizator;
    }

    public void setSuma(double suma) {
        this.suma = suma;
    }

    public void setData(LocalDateTime data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Oferta{" +
                "id=" + id +
                ", licitatie=" + idLicitatie +
                ", utilizator=" + utilizator +
                ", suma=" + suma +
                ", data=" + data +
                '}';
    }

    public int compareTo(Oferta alta) {
        return Double.compare(alta.suma, this.suma);
    }
}
