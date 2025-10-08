package componente;

import db.UtilizatorDB;

import java.sql.SQLException;


public class VerificatorSold extends Thread {
    private final Utilizator utilizator;
    private volatile boolean running = true;
    UtilizatorDB utilizatorDB;

    public VerificatorSold(Utilizator utilizator, UtilizatorDB utilizatorDB) {
        this.utilizator = utilizator;
        this.utilizatorDB = utilizatorDB;
        this.setDaemon(true);
    }

    public void opreste() {
        running = false;
        this.interrupt();
    }

    @Override
    public void run() {
        while (running) {
            try {
                double soldNou = utilizatorDB.checkSold(utilizator.getId());
                utilizator.setSold(soldNou);
                Thread.sleep(1000);
            } catch (SQLException | InterruptedException e) {
                running = false;
            }
        }
    }
}