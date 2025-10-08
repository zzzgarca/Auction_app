import componente.*;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Scanner;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final ActiuniAplicatie actiuni = new ActiuniAplicatie();

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Aplicatia se închide. Oprire fire de fundal...");

            LicitatiiActive.getInstance().opresteThreadul();

            if (Sesiune.esteAutentificat()) {
                actiuni.logout();
                System.out.println("Sesiunea utilizatorului a fost închisă și firul VerificatorSold a fost oprit.");
            }
        }));


        while (true) {
            if (!Sesiune.esteAutentificat()) {
                afiseazaMeniuLogin();
            } else {
                afiseazaMeniuUtilizator();
            }
        }
    }

    private static void afiseazaMeniuLogin() {
        System.out.println("\n=== Meniu Autentificare ===");
        System.out.println("1. Login");
        System.out.println("2. Înregistrare");
        System.out.println("3. Ieșire");

        int optiune = citesteOptiune();

        switch (optiune) {
            case 1 -> login();
            case 2 -> inregistrare();
            case 3 ->
                System.exit(0);
            default -> System.out.println("Opțiune invalidă.");
        }
    }

    private static void afiseazaMeniuUtilizator() {
        Utilizator utilizatorCurent = Sesiune.getUtilizatorCurent();

        System.out.println("\n=== Meniu Utilizator ===");
        System.out.println("Autentificat ca: " + utilizatorCurent.getUsername() +
                (utilizatorCurent instanceof Administrator ? " (Administrator)" : ""));


        System.out.println("1. Trimite mesaj");
        System.out.println("2. Afișează toate mesajele");
        System.out.println("3. Afișează mesaje necitite");
        System.out.println("4. Creează produs");
        System.out.println("5. Afișează produsele proprii");
        System.out.println("6. Creează licitație");
        System.out.println("7. Caută licitații");
        System.out.println("8. Oferă sumă într-o licitație");
        System.out.println("9. Afișează licitațiile tale");
        System.out.println("10. Afișează ofertele tale");
        System.out.println("11. Logout");
        System.out.println("12. Modifică sold (adunare/scădere)");
        System.out.println("13. Verifică soldul actual din baza de date");

        if (utilizatorCurent instanceof Administrator) {
            System.out.println("14. Șterge utilizator");
            System.out.println("15. Afișează toate produsele");
            System.out.println("16. Afișează istoricul licitațiilor");
        }

        int optiune = citesteOptiune();

        try {
            switch (optiune) {
                case 1 -> trimiteMesaj();
                case 2 -> actiuni.afiseazaToateMesajele();
                case 3 -> actiuni.afiseazaMesajeNecitite();
                case 4 -> {
                    System.out.print("Titlu: ");
                    String titlu = scanner.nextLine();
                    System.out.print("Descriere: ");
                    String descriere = scanner.nextLine();
                    System.out.print("Preț de pornire: ");
                    double pret = Double.parseDouble(scanner.nextLine());

                    actiuni.creazaProdus(titlu, descriere, pret);
                }
                case 5 -> actiuni.afiseazaListaProduseUtilizator();
                case 6 -> {
                    System.out.print("ID produs: ");
                    int idProdus = citesteOptiune();
                    System.out.print("Dată limită (yyyy-MM-ddTHH:mm): ");
                    LocalDateTime dataLimita = LocalDateTime.parse(scanner.nextLine());

                    actiuni.creeazaLicitatie(idProdus, dataLimita);
                }
                case 7 -> {
                    System.out.print("Cuvânt cheie titlu: ");
                    String text = scanner.nextLine();
                    for (Licitatie l : actiuni.cautaLicitatii(text)) {
                        System.out.println(l);
                    }
                }
                case 8 -> {
                    System.out.print("ID licitație: ");
                    int idLic = citesteOptiune();
                    System.out.print("Suma: ");
                    double suma = Double.parseDouble(scanner.nextLine());
                    actiuni.creazaOferta(idLic, suma);
                }
                case 9 -> actiuni.afiseazaListaLicitatiiUtilizator();
                case 10 -> actiuni.afiseazaListaOferteUtilizator();
                case 11 -> {
                    Sesiune.logout();
                    actiuni.logout();
                    System.out.println("Delogat cu succes.");
                }
                case 12 -> {
                    System.out.print("Introdu valoarea (pozitivă sau negativă): ");
                    double diferenta = Double.parseDouble(scanner.nextLine());
                    actiuni.modificaCredit(diferenta);
                    System.out.println("Sold modificat.");
                }
                case 13 -> actiuni.verificaCredit();
                case 14 -> {
                    if (utilizatorCurent instanceof Administrator) {
                        stergeUtilizator();
                    } else System.out.println("Opțiune invalidă.");
                }
                case 15 -> {
                    if (utilizatorCurent instanceof Administrator) {
                        actiuni.afiseazaToateProduseleIstorice();
                    } else System.out.println("Opțiune invalidă.");
                }
                case 16 -> {
                    if (utilizatorCurent instanceof Administrator) {
                        actiuni.afiseazaListaIstorieLicitatii();
                    } else System.out.println("Opțiune invalidă.");
                }
                default -> System.out.println("Opțiune invalidă.");
            }
        } catch (Exception e) {
            System.out.println("Eroare: " + e.getMessage());
        }
    }


    private static void login() {
        try {
            System.out.print("Username: ");
            String username = scanner.nextLine();
            System.out.print("Parola: ");
            String parola = scanner.nextLine();

            Utilizator utilizator = actiuni.login(username, parola);

            if (utilizator != null) {
                Sesiune.setUtilizatorCurent(utilizator);
                System.out.println("Autentificare reușită!");
            } else {
                System.out.println("Autentificare eșuată: date incorecte.");
            }

        } catch (SQLException e) {
            System.out.println("Eroare la autentificare: " + e.getMessage());
        }
    }

    private static void inregistrare() {
        try {
            System.out.print("Username: ");
            String username = scanner.nextLine();
            System.out.print("Parola: ");
            String parola = scanner.nextLine();
            System.out.print("Email: ");
            String email = scanner.nextLine();



            actiuni.inregistrare(username, parola, email, 0);
            System.out.println("Utilizator înregistrat cu succes.");

        } catch (SQLException e) {
            System.out.println("Eroare la înregistrare: " + e.getMessage());
        }
    }

    private static void trimiteMesaj() throws SQLException {

        System.out.print("Către (username): ");
        String usernameDestinatar = scanner.nextLine();
        System.out.print("Conținut mesaj: ");
        String continut = scanner.nextLine();

        if (actiuni.getUtilizatorMinimal(usernameDestinatar) == null) {
            System.out.println("Destinatarul nu există.");
            return;
        }

        actiuni.expediereMesaj(continut, usernameDestinatar);
    }


    private static void stergeUtilizator() throws SQLException {
        Utilizator admin = Sesiune.getUtilizatorCurent();

        System.out.print("ID-ul utilizatorului de șters: ");
        int id = citesteOptiune();
        if (id > 0) {
            actiuni.stergeUtilizator(admin, id);
        } else {
            System.out.println("ID invalid.");
        }
    }

    private static int citesteOptiune() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
