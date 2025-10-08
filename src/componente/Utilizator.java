package componente;

public class Utilizator extends UtilizatorMinimal {
    private String parola;
    private String email;
    private double sold;

    public Utilizator(int id, String username, String parola, String email, double sold) {
        super(id, username);
        this.parola = parola;
        this.email = email;
        this.sold = sold;
    }


    public String getParola() {
        return parola;
    }

    public String getEmail() {
        return email;
    }

    public double getSold() {
        return sold;
    }

    public void setParola(String parola) {
        this.parola = parola;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setSold(double sold) {
        this.sold = sold;
    }


    @Override
    public String toString() {
        return "Utilizator{" +
                super.toString() +
                ", email='" + email + '\'' +
                ", sold=" + sold +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof UtilizatorMinimal)) return false;
        UtilizatorMinimal other = (UtilizatorMinimal) obj;
        return this.getId() == other.getId() &&
                this.getUsername().equals(other.getUsername());
    }




    @Override
    public int hashCode() {
        return 31 * this.getId() + getUsername().hashCode();
    }
}
