package isetb.project.parkingapp;

/**
 * Classe représentant une entrée d'historique de détection de plaque d'immatriculation.
 * Utilisée pour stocker les événements de passage de véhicules dans le système.
 */
public class HistoryItem {

    // --- Attributs ---

    // Identifiant unique (peut être une clé Firebase ou générée localement)
    private String id;

    // Numéro de la plaque détectée
    private String plate;

    // Date de détection (ex: "2025-05-03")
    private String date;

    // Heure d'entrée du véhicule
    private String entry_time;

    // Heure de sortie du véhicule (peut être null ou vide si le véhicule est encore présent)
    private String exit_time;

    // Indique si la plaque est autorisée à entrer dans le parking
    private Boolean authorized;

    /**
     * Constructeur vide requis par Firebase pour instancier l'objet automatiquement.
     */
    public HistoryItem() {}

    // --- Getters et Setters ---

    /**
     * Récupère l'identifiant de l'historique.
     */
    public String getId() {
        return id;
    }

    /**
     * Définit l'identifiant de l'historique.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Récupère le numéro de plaque.
     */
    public String getPlate() {
        return plate;
    }

    /**
     * Définit le numéro de plaque.
     */
    public void setPlate(String plate) {
        this.plate = plate;
    }

    /**
     * Récupère la date de l'événement.
     */
    public String getDate() {
        return date;
    }

    /**
     * Définit la date de l'événement.
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * Récupère l'heure d'entrée du véhicule.
     */
    public String getEntry_time() {
        return entry_time;
    }

    /**
     * Définit l'heure d'entrée du véhicule.
     */
    public void setEntry_time(String entry_time) {
        this.entry_time = entry_time;
    }

    /**
     * Récupère l'heure de sortie du véhicule.
     */
    public String getExit_time() {
        return exit_time;
    }

    /**
     * Définit l'heure de sortie du véhicule.
     */
    public void setExit_time(String exit_time) {
        this.exit_time = exit_time;
    }

    /**
     * Indique si le véhicule était autorisé à entrer.
     */
    public Boolean getAuthorized() {
        return authorized;
    }

    /**
     * Définit si le véhicule était autorisé à entrer.
     */
    public void setAuthorized(Boolean authorized) {
        this.authorized = authorized;
    }
}
