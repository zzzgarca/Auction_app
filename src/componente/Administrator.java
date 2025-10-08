package componente;

public class Administrator extends Utilizator {

    public Administrator(int id, String username, String parola, String email, double sold) {
        super(id, username, parola, email, sold);
    }

    @Override
    public String toString() {
        return "Administrator: " + getUsername();
    }
}
