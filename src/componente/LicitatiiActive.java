package componente;

import com.mysql.cj.protocol.Message;
import db.*;

import java.sql.Array;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LicitatiiActive extends Thread {
    private static LicitatieDB licitatieDB;
    private static UtilizatorDB utilizatorDB;
    private static MesajDB mesajDB;
    private static ProdusDB produsDB;
    private static OfertaDB ofertaDB;

    private static LicitatiiActive instanta;
    private static final ConcurrentHashMap<Integer, Licitatie> licitatii = new ConcurrentHashMap<>();
    private volatile boolean running = true;

    private LicitatiiActive() {
        incarcaLicitatiiActive();
        this.setDaemon(true);
        this.start();
    }

    public static synchronized void init(
            LicitatieDB ldb,
            UtilizatorDB udb,
            MesajDB mdb,
            ProdusDB pdb,
            OfertaDB odb
    ) {
        licitatieDB = ldb;
        utilizatorDB = udb;
        mesajDB = mdb;
        produsDB = pdb;
        ofertaDB = odb;
        if (instanta == null) {
            instanta = new LicitatiiActive();
        }
    }

    public static synchronized LicitatiiActive getInstance() {
        if (instanta == null) {
            throw new IllegalStateException("LicitatiiActive must be initialized using init(...) before use!");
        }
        return instanta;
    }

    private void incarcaLicitatiiActive() {
        try {
            HashMap<Integer, Licitatie> active = licitatieDB.getLicitatiiActive();
            licitatii.clear();
            licitatii.putAll(active);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static LicitatieDB getLicitatieDB() {
        return licitatieDB;
    }

    public static void setLicitatieDB(LicitatieDB licitatieDB) {
        LicitatiiActive.licitatieDB = licitatieDB;
    }

    public static UtilizatorDB getUtilizatorDB() {
        return utilizatorDB;
    }

    public static void setUtilizatorDB(UtilizatorDB utilizatorDB) {
        LicitatiiActive.utilizatorDB = utilizatorDB;
    }

    public static MesajDB getMesajDB() {
        return mesajDB;
    }

    public static void setMesajDB(MesajDB mesajDB) {
        LicitatiiActive.mesajDB = mesajDB;
    }

    public static ProdusDB getProdusDB() {
        return produsDB;
    }

    public static void setProdusDB(ProdusDB produsDB) {
        LicitatiiActive.produsDB = produsDB;
    }

    public static OfertaDB getOfertaDB() {
        return ofertaDB;
    }

    public static void setOfertaDB(OfertaDB ofertaDB) {
        LicitatiiActive.ofertaDB = ofertaDB;
    }

    public ConcurrentHashMap<Integer, Licitatie> getLicitatii() {
        return licitatii;
    }

    public Licitatie getLicitatieById(int id) {
        return licitatii.get(id);
    }

    public void stergeLicitatieById(int id) {
        licitatii.remove(id);
    }

    public void refresh() {
        incarcaLicitatiiActive();
    }

    public void adaugaOfertaLaLicitatie(int idLicitatie, Oferta o) {
        Licitatie l = licitatii.get(idLicitatie);
        if (l != null) {
            l.adaugaOferta(o);
        }
    }


    public void verificaSiCurata() throws SQLException {
        synchronized (licitatii) {

            LocalDateTime acum = LocalDateTime.now();
            ArrayList<Integer> idLicitatiiDeScos = new ArrayList<>();

            for (Map.Entry<Integer, Licitatie> entry : licitatii.entrySet()) {
                Licitatie l = entry.getValue();

                if (l.getDataLimita().isBefore(acum)) {
                    System.out.println("Licitație expirată: ID = " + entry.getKey());
                    idLicitatiiDeScos.add(entry.getKey());

                    Oferta ofertaCastigatoare = null;

                    for (Oferta o : l.getOferte()) {
                        double soldCandidat = utilizatorDB.checkSold(o.getUtilizator().getId());

                        double soldVanzator = utilizatorDB.checkSold(l.getVanzator().getId());

                        if (soldCandidat >= o.getSuma() && soldCandidat >= l.getProdus().getPretStart() ) {
                            ofertaCastigatoare = o;
                            utilizatorDB.modificaSold(o.getUtilizator().getId(), soldCandidat-o.getSuma());
                            produsDB.schimbaProprietar(l.getProdus().getId(), o.getUtilizator().getId());

                            utilizatorDB.modificaSold(l.getVanzator().getId(), soldVanzator+o.getSuma());


                            System.out.println("Oferta castigatoare este = " + ofertaCastigatoare);

                            for (Oferta o2 : l.getOferte() ) {
                                int idAdmin = utilizatorDB.getIdByUsername("admin");
                                UtilizatorMinimal expeditor = new UtilizatorMinimal(idAdmin, "admin");
                                UtilizatorMinimal destinatar = o2.getUtilizator();

                                String textMesaj = "Licitatia " + l.getId() + " pentru produsul " + l.getProdus().getTitlu() +
                                        " a fost castigata de " + o.getUtilizator().getUsername() + "cu suma " + o.getSuma();

                                mesajDB.trimiteMesaj(expeditor, destinatar, textMesaj);
                                ofertaDB.stergeOferta(o2.getId());
                            }

                            l.getOferte().clear();

                            break;
                        }
                    }
                    if  (ofertaCastigatoare == null) {
                        for (Oferta o3 : l.getOferte()){
                            int idAdmin = utilizatorDB.getIdByUsername("admin");
                            UtilizatorMinimal expeditor = new UtilizatorMinimal(idAdmin, "admin");

                            UtilizatorMinimal destinatar = o3.getUtilizator();

                            String textMesaj = "Licitatia " + l.getId() + " pentru produsul " + l.getProdus().getTitlu() +
                                    " nu a fost atribuita la finalul termenului limita";
                            mesajDB.trimiteMesaj(expeditor, destinatar, textMesaj);

                            ofertaDB.stergeOferta(o3.getId());

                        }

                        l.getOferte().clear();
                    }
                }
            }

            if (!idLicitatiiDeScos.isEmpty()) {
                for (Integer id : idLicitatiiDeScos) {
                    licitatii.remove(id);
                    licitatieDB.marcheazaLicitatie(id);
                }
            }
        }
    }

    public ArrayList<Licitatie> cautaLicitatiiActiveDupaTitlu(String text) {
        ArrayList<Licitatie> lista = new ArrayList<>();
        for (Map.Entry<Integer, Licitatie> l : licitatii.entrySet()) {
            if (l.getValue().getProdus().getTitlu().contains(text)) {
                lista.add(l.getValue());
            }
        }
        return lista;
    }

    public ArrayList<Integer> idLicitatiiDupaIdProdus(int idProdus) {
        ArrayList<Integer> lista = new ArrayList<>();
        for (Map.Entry<Integer, Licitatie> l : licitatii.entrySet()) {
            if (l.getValue().getProdus().getId() == idProdus) {
                lista.add(l.getKey());
            }
        }
        return lista;
    }

    public Integer idLicitatieDupaIdOferta(int idOferta) {
        for (Map.Entry<Integer, Licitatie> l : licitatii.entrySet()) {
            for (Oferta o : l.getValue().getOferte()) {
                if (o.getId() == idOferta) {
                    return l.getKey();
                }
            }
        }
        return null;
    }

    public ArrayList<Licitatie> cautaLicitatiiActiveDupaUtilizator(int userId) {
        ArrayList<Licitatie> lista = new ArrayList<>();
        for (Map.Entry<Integer, Licitatie> l : licitatii.entrySet()) {
            if (l.getValue().getVanzator().getId() == userId) {
                lista.add(l.getValue());
            }
        }
        return lista;
    }

    public void adaugaLicitatieLista(Licitatie l) {
        licitatii.put(l.getId(), l);
    }

    @Override
    public void run() {
        while (running) {
            try {
                verificaSiCurata();
                Thread.sleep(1000);
            } catch (InterruptedException | SQLException e) {
                System.out.println("Thread-ul de verificare a fost întrerupt.");
                running = false;
            }
        }
    }

    public void opresteThreadul() {
        running = false;
        this.interrupt();
    }
}
