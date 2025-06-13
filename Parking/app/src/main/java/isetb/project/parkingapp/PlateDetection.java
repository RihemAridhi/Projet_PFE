package isetb.project.parkingapp;

/**
 * Classe représentant une détection de plaque d'immatriculation.
 * Elle est utilisée pour stocker les informations d'une détection dans Firebase.
 */
public class PlateDetection {

    // --- Champs (attributs) de la classe ---

    // Numéro de la plaque détectée
    private String plateNumber;

    // Heure à laquelle la plaque a été détectée
    private String detectionTime;

    // Nom du propriétaire du véhicule détecté
    private String ownerName;

    /**
     * Constructeur vide requis par Firebase pour désérialiser les objets.
     */
    public PlateDetection() {
        // Ne rien faire ici — requis pour Firebase
    }

    /**
     * Constructeur principal permettant d'initialiser une détection complète.
     *
     * @param plateNumber    Numéro de la plaque détectée
     * @param detectionTime  Heure de détection (format texte, ex. "2025-05-03 12:00")
     * @param ownerName      Nom de l'employée
     */
    public PlateDetection(String plateNumber, String detectionTime, String ownerName) {
        this.plateNumber = plateNumber;
        this.detectionTime = detectionTime;
        this.ownerName = ownerName;
    }

    // --- Getters et Setters ---

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public String getDetectionTime() {
        return detectionTime;
    }

    public void setDetectionTime(String detectionTime) {
        this.detectionTime = detectionTime;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }
}
