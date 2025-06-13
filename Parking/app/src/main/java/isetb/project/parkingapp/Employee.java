package isetb.project.parkingapp;

/**
 * Classe représentant un employé autorisé à accéder au parking.
 * Chaque employé est identifié par un ID, un nom et un numéro de plaque.
 */
public class Employee {

    // --- Attributs ---

    // Nom complet de l'employé
    private String employee_name;

    // Identifiant unique de l'employé (peut être une clé Firebase ou une ID manuelle)
    private String id;

    // Numéro de plaque du véhicule appartenant à l'employé
    private String plate_number;

    /**
     * Constructeur vide requis par Firebase lors de la désérialisation des objets.
     */
    public Employee() {}

    /**
     * Constructeur avec paramètres pour instancier un objet Employee avec ses données.
     *
     * @param id            Identifiant unique de l'employé
     * @param employee_name Nom complet de l'employé
     * @param plate_number  Numéro de plaque du véhicule
     */
    public Employee(String id, String employee_name, String plate_number) {
        this.id = id;
        this.employee_name = employee_name;
        this.plate_number = plate_number;
    }

    // --- Getters ---

    /**
     * Récupère le nom de l'employé.
     */
    public String getEmployee_name() {
        return employee_name;
    }

    /**
     * Récupère l'identifiant de l'employé.
     */
    public String getId() {
        return id;
    }

    /**
     * Récupère le numéro de plaque du véhicule de l'employé.
     */
    public String getPlate_number() {
        return plate_number;
    }


    // public void setEmployee_name(String employee_name) { this.employee_name = employee_name; }
    // public void setId(String id) { this.id = id; }
    // public void setPlate_number(String plate_number) { this.plate_number = plate_number; }
}
