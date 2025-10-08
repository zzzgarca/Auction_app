package componente;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LoggerActiuni {
    private static final String FILE_NAME = "log_actiuni.csv";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void log(String actiune) {
        try (FileWriter writer = new FileWriter(FILE_NAME, true)) {
            String linie = actiune + "," + LocalDateTime.now().format(FORMATTER) + "\n";
            writer.write(linie);
        } catch (IOException e) {
            System.err.println("Eroare la scrierea în fișierul de log: " + e.getMessage());
        }
    }
}
