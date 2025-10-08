package componente;

import db.*;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

public class ActiuniAplicatie extends Thread {
    private static final AdministratorDB administratorDB = new AdministratorDB();
    private static final UtilizatorDB utilizatorDB = new UtilizatorDB();
    private static final ProdusDB produsDB = new ProdusDB();
    private static final LicitatieDB licitatieDB = new LicitatieDB();
    private static final OfertaDB ofertaDB = new OfertaDB();
    private static final MesajDB mesajDB = new MesajDB();
    private VerificatorSold verificatorSold;

    private final LicitatiiActive licitatiiIncarcate;


    static {
        OfertaDB.setLicitatieDB(licitatieDB);
        OfertaDB.setUtilizatorDB(utilizatorDB);
        LicitatieDB.setUtilizatorDB(utilizatorDB);
        LicitatieDB.setProdusDB(produsDB);
        LicitatieDB.setOfertaDB(ofertaDB);
    }

    static {
        LicitatiiActive.setLicitatieDB(licitatieDB);
        LicitatiiActive.setUtilizatorDB(utilizatorDB);
        LicitatiiActive.setMesajDB(mesajDB);
        LicitatiiActive.setProdusDB(produsDB);
        LicitatiiActive.setOfertaDB(ofertaDB);
    }

    public ActiuniAplicatie() {
        LicitatiiActive.init(licitatieDB, utilizatorDB, mesajDB, produsDB, ofertaDB);
        licitatiiIncarcate = LicitatiiActive.getInstance();
    }


    public Utilizator login(String username, String parola) throws SQLException {
        Utilizator u = utilizatorDB.autentificare(username, parola);


        if (u != null && administratorDB.esteAdministrator(u.getId())) {
            Administrator admin = new Administrator(
                    u.getId(), u.getUsername(), u.getParola(), u.getEmail(), u.getSold()
            );

            Sesiune.setUtilizatorCurent(admin);
            LoggerActiuni.log("login");

            verificatorSold = new VerificatorSold(admin, utilizatorDB);
            verificatorSold.start();

            return admin;
        }

        if (u != null) {
            Sesiune.setUtilizatorCurent(u);
            LoggerActiuni.log("login");

            verificatorSold = new VerificatorSold(u, utilizatorDB);
            verificatorSold.start();

            return u;
        }

        return null;
    }


    public void inregistrare(String username, String parola, String email, double sold) throws SQLException {
        utilizatorDB.adaugaUtilizator(username, parola, email, sold);
        LoggerActiuni.log("inregistrare");
    }


    public void logout() {
        if (verificatorSold != null) {
            verificatorSold.opreste();
            verificatorSold = null;
        }
        Sesiune.setUtilizatorCurent(null);
        LoggerActiuni.log("logout");
    }


    public UtilizatorMinimal getUtilizatorMinimal(String username) {
        try {
            int id = utilizatorDB.getIdByUsername(username);
            if (id > 0) {
                return new UtilizatorMinimal(id, username);
            }
        } catch (SQLException e) {
            System.out.println("Eroare la căutarea utilizatorului: " + e.getMessage());
        }
        return null;
    }

    public Produs creazaProdus(String titlu, String descriere, double pretStart) throws SQLException {
        Utilizator utilizator = Sesiune.getUtilizatorCurent();
        Produs p = produsDB.adaugaProdus(titlu, descriere, pretStart, utilizator);
        LoggerActiuni.log("adauga_produs");
        return p;
    }


    public Licitatie creeazaLicitatie(int idProdus, LocalDateTime dataLimita) throws SQLException {
        Utilizator utilizator = Sesiune.getUtilizatorCurent();
        Produs produs = produsDB.getProdusById(idProdus);

        if (produs.getProprietar().getId() == utilizator.getId()){
            Licitatie l = licitatieDB.adaugaLicitatie(produs, utilizator, dataLimita);
            licitatiiIncarcate.adaugaLicitatieLista(l);
            LoggerActiuni.log("Se creeaza licitatia " + produs.getTitlu() + ", de către utilizatorul " + utilizator.getUsername());
            return l;
        } else {
            System.out.println("Nu poti crea o licitatie pentru un produs care nu este al tau");
            return null;
        }

    }


    public Oferta creazaOferta(int licitatieId, double suma) throws SQLException {
        Utilizator utilizator = Sesiune.getUtilizatorCurent();
        Licitatie l = licitatiiIncarcate.getLicitatieById(licitatieId);
        LocalDateTime acum = LocalDateTime.now();

        if (l.getVanzator().getId() != utilizator.getId()){
            LoggerActiuni.log("A fost creata oferta pentru " + l.getProdus().getTitlu() + ", cu suma " + suma);
            System.out.println("A fost creata oferta pentru " + l.getProdus().getTitlu() + ", cu suma " + suma);
            Oferta o = ofertaDB.adaugaOferta(l.getId(), utilizator, suma, acum);
            licitatiiIncarcate.adaugaOfertaLaLicitatie(licitatieId, o);
            return o;
        } else {
            System.out.println("Nu poti crea o oferta pentru o licitatie deschisa de tine");
            return null;
        }

    }

    public void stergeProdus(int idProdus) {
        Utilizator utilizator = Sesiune.getUtilizatorCurent();

        try {
            Produs produs = produsDB.getProdusById(idProdus);

            if (produs == null) {
                System.out.println("Produsul nu există.");
                return;
            }

            if (utilizator instanceof Administrator || produs.getProprietar().getUsername().equals(utilizator.getUsername())) {

                ArrayList<Integer> listaIdLicitatii = licitatiiIncarcate.idLicitatiiDupaIdProdus(idProdus);
                if (!listaIdLicitatii.isEmpty()) {
                    for (Integer licitatiiId : listaIdLicitatii) {
                        stergeLicitatie(licitatiiId);
                    }
                }

                produsDB.stergeProdus(idProdus);

                LoggerActiuni.log("sterge_produs");
                System.out.println("Produsul cu ID " + idProdus + " a fost șters.");
            } else {
                System.out.println("Eroare: nu aveți permisiunea de a șterge acest produs.");
            }

        } catch (SQLException e) {
            System.out.println("Eroare la ștergerea produsului: " + e.getMessage());
        }
    }


    public void stergeLicitatie(int idLicitatie) {
        Utilizator utilizator = Sesiune.getUtilizatorCurent();

        try {
            Licitatie licitatie = licitatiiIncarcate.getLicitatieById(idLicitatie);

            if (licitatie == null) {
                System.out.println("Licitația nu există.");
                return;
            }

            int idVanzator = licitatie.getVanzator().getId();
            if (utilizator instanceof Administrator || idVanzator == utilizator.getId()) {

                TreeSet<Oferta> oferte = licitatie.getOferte();

                if (!oferte.isEmpty()) {
                    for (Oferta o : oferte) {
                        stergeOferta(o.getId());
                    }
                }

                licitatieDB.stergeLicitatie(idLicitatie);
                licitatiiIncarcate.stergeLicitatieById(idLicitatie);

                LoggerActiuni.log("sterge_licitatie");
                System.out.println("Licitația cu ID " + idLicitatie + " a fost ștearsă.");
            } else {
                System.out.println("Eroare: nu aveți permisiunea de a șterge această licitație.");
            }

        } catch (SQLException e) {
            System.out.println("Eroare la ștergerea licitației: " + e.getMessage());
        }
    }


    public void stergeOferta(int idOferta) throws SQLException {
        Utilizator utilizator = Sesiune.getUtilizatorCurent();
        Oferta o = ofertaDB.getOfertePentruIdOferta(idOferta);


        if (!(utilizator instanceof Administrator || utilizator.getId() == o.getId())) {
            System.out.println("Eroare: nu aveți dreptul să ștergeți această ofertă.");
            return;
        }

        try {
            ofertaDB.stergeOferta(idOferta);
            licitatiiIncarcate.getLicitatieById(o.getIdLicitatie()).stergeOferta(idOferta);
            LoggerActiuni.log("sterge_oferta");
            System.out.println("Oferta cu ID " + idOferta + " a fost ștearsă.");
        } catch (SQLException e) {
            System.out.println("Eroare la ștergerea ofertei: " + e.getMessage());
        }
    }

     public List<Licitatie> cautaLicitatii(String text) throws SQLException {
        LoggerActiuni.log("cauta_licitatii");
        return licitatiiIncarcate.cautaLicitatiiActiveDupaTitlu(text);
    }

    public void stergeUtilizator(Utilizator utilizatorCurent, int idDeSters) throws SQLException {

        if (!(utilizatorCurent instanceof Administrator)) {
            System.out.println("Eroare: doar administratorii pot șterge utilizatori.");
            return;
        }

        ArrayList<Oferta> oferte = ofertaDB.getOfertePentruUtilizator(utilizatorCurent.getId());
        for (Oferta o : oferte) {
            stergeLicitatie(o.getId());

        }

        ArrayList<Produs> produse = produsDB.getProdusePentruUtilizator(utilizatorCurent.getUsername());
        for (Produs p : produse) {
            stergeProdus(p.getId());
        }

        try {
            utilizatorDB.stergeUtilizator(idDeSters);
            LoggerActiuni.log("sterge_utilizator");
            System.out.println("Utilizatorul cu ID " + idDeSters + " a fost șters.");
        } catch (SQLException e) {
            System.out.println("Eroare la ștergerea utilizatorului: " + e.getMessage());
        }
    }

    public void afiseazaListaProduseUtilizator() throws SQLException {
        Utilizator utilizator = Sesiune.getUtilizatorCurent();
        ArrayList<Produs> produse = produsDB.getProdusePentruUtilizator(utilizator.getUsername());

        for (Produs p : produse)
            System.out.println(p);
    }

    public void afiseazaToateProduseleIstorice() throws SQLException {
        Utilizator utilizator = Sesiune.getUtilizatorCurent();

        if (utilizator instanceof Administrator) {
            ArrayList<Produs> produse = produsDB.getToateProdusele();
            for (Produs p : produse)
                System.out.println(p);
        } else {
            System.out.println("Doar administratorii pot vedea istoricul produselor.");
            return;
        }
    }

    public void afiseazaListaLicitatiiUtilizator() throws SQLException {
        Utilizator utilizator = Sesiune.getUtilizatorCurent();
        ArrayList<Licitatie> licitatii = licitatiiIncarcate.cautaLicitatiiActiveDupaUtilizator(utilizator.getId());

        for (Licitatie l : licitatii)
            System.out.println(l);
    }

    public void afiseazaListaOferteUtilizator() throws SQLException {
        Utilizator utilizator = Sesiune.getUtilizatorCurent();
        Collection<Licitatie> toateLicitatiile = licitatiiIncarcate.getLicitatii().values();

        boolean gasit = false;

        for (Licitatie l : toateLicitatiile){
            for (Oferta o : l.getOferte()){
                if (o.getUtilizator().getId() == utilizator.getId()) {
                    System.out.println(o);
                    gasit = true;
                }
            }
        }

        if (!gasit) {
            System.out.println("Nu ai oferte active în acest moment.");
        }
    }


    public void afiseazaListaIstorieLicitatii() throws SQLException {
        HashMap<Integer, Licitatie> licitatii = null;
        Utilizator utilizator = Sesiune.getUtilizatorCurent();

        if (utilizator instanceof Administrator) {
            licitatii = licitatieDB.getToateLicitatiile();
        } else {
            System.out.println("Doar administratorii pot vedea istoricul licitațiilor.");
            return;
        }

        for (Map.Entry<Integer, Licitatie> l : licitatii.entrySet()) {
            System.out.println(l.getValue());
        }
    }


    public void afiseazaToateMesajele() {
        Utilizator utilizator = Sesiune.getUtilizatorCurent();
        try {
            List<Mesaj> mesaje = mesajDB.getMesajePrimite(utilizator.getUsername());
            LoggerActiuni.log("citeste_toate_mesajele");

            if (mesaje.isEmpty()) {
                System.out.println("Nu aveți mesaje.");
            } else {
                System.out.println("Toate mesajele primite:");
                for (Mesaj m : mesaje) {
                    System.out.println(m);
                }
            }
        } catch (SQLException e) {
            System.out.println("Eroare la afișarea mesajelor: " + e.getMessage());
        }
    }

    public void afiseazaMesajeNecitite() {
        Utilizator utilizator = Sesiune.getUtilizatorCurent();
        try {
            List<Mesaj> mesaje = mesajDB.getMesajeNecitite(utilizator.getUsername());
            LoggerActiuni.log("citeste_mesaje_necitite");

            if (mesaje.isEmpty()) {
                System.out.println("Nu aveți mesaje necitite.");
            } else {
                System.out.println("Mesaje necitite:");
                for (Mesaj m : mesaje) {
                    System.out.println(m);
                }
            }
        } catch (SQLException e) {
            System.out.println("Eroare la afișarea mesajelor necitite: " + e.getMessage());
        }
    }

    public void modificaCredit (double diferenta) throws SQLException {
        Utilizator utilizator = Sesiune.getUtilizatorCurent();

        double soldCurent = utilizator.getSold();
        double soldNou = soldCurent + diferenta;

        utilizatorDB.modificaSold(utilizator.getId(), soldNou);
    }

    public void verificaCredit () throws SQLException {
        Utilizator utilizator = Sesiune.getUtilizatorCurent();

        double soldCurent = utilizatorDB.checkSold(utilizator.getId());

        System.out.println("Sold curent : " + soldCurent);
    }


    public Mesaj expediereMesaj(String text, String username) throws SQLException {
        Utilizator utilizator = Sesiune.getUtilizatorCurent();
        UtilizatorMinimal destinatar = getUtilizatorMinimal(username);

        Mesaj mesaj = mesajDB.trimiteMesaj(utilizator, destinatar, text);

        LoggerActiuni.log("expediere_mesaj");
        System.out.println("Mesaj trimis către " + username);
        return mesaj;
    }




}
