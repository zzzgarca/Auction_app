package componente;

public class Sesiune {
    private static Sesiune instanta;
    private static Utilizator utilizatorCurent = null;

    private Sesiune() {
    }

    public static Sesiune getInstance() {
        if (instanta == null) {
            instanta = new Sesiune();
        }
        return instanta;
    }

    public static void autentifica(Utilizator utilizator) {
        utilizatorCurent = utilizator;
    }

    public static void logout() {
        utilizatorCurent = null;

    }

    public static Utilizator getUtilizatorCurent() {
        return utilizatorCurent;
    }

    public static void setUtilizatorCurent(Utilizator utilizator) {
        utilizatorCurent = utilizator;
    }

    public static boolean esteAutentificat() {
        return utilizatorCurent != null;
    }
}
