package componente;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class Licitatie {
    private int id;
    private Produs produs;
    private UtilizatorMinimal vanzator;
    private LocalDateTime dataLimita;
    private TreeSet<Oferta> oferte;

    public Licitatie(int id, Produs produs, UtilizatorMinimal vanzator, LocalDateTime dataLimita, TreeSet<Oferta> oferte) {
        this.id = id;
        this.produs = produs;
        this.vanzator = vanzator;
        this.dataLimita = dataLimita;
        this.oferte= oferte;
    }

    public int getId() { return id; }
    public Produs getProdus() { return produs; }
    public UtilizatorMinimal getVanzator() { return vanzator; }
    public TreeSet<Oferta> getOferte() { return oferte; }
    public LocalDateTime getDataLimita() { return dataLimita; }

    public void setId(int id) { this.id = id; }
    public void setProdus(Produs produs) { this.produs = produs; }
    public void setVanzator(UtilizatorMinimal vanzator) { this.vanzator = vanzator; }
    public void setDataLimita(LocalDateTime dataLimita) { this.dataLimita = dataLimita; }

    public void adaugaOferta(Oferta o) {
        this.oferte.add(o);
    }

    public void stergeOferta(int idOferta) {
        oferte.removeIf(o -> o.getId() == idOferta);
    }

    @Override
    public String toString() {
        return "Licitatie{" +
                "id=" + id +
                ", produs=" + produs +
                ", vanzator=" + vanzator.getUsername() +
                ", dataLimita=" + dataLimita +
                '}';
    }
}
