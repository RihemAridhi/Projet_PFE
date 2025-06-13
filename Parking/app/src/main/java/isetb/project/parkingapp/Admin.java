package isetb.project.parkingapp;

/**
 * Classe représentant un administrateur de l'application.
 * Elle contient les informations nécessaires à l'identification et à la gestion
 * des comptes administrateurs dans Firebase.
 */
public class Admin {

    // --- Attributs privés ---

    // Nom complet de l'administrateur
    private String fullName;

    // Adresse email utilisée pour l'identification (connexion)
    private String email;

    // Clé unique dans Firebase (généralement générée automatiquement)
    private String key;

    /**
     * Constructeur vide requis par Firebase pour la désérialisation automatique.
     */
    public Admin() {}

    // --- Getters ---

    /**
     * Récupère le nom complet de l'administrateur.
     * @return nom complet
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Récupère l'adresse email de l'administrateur.
     * @return adresse email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Récupère la clé unique de l'administrateur (clé Firebase).
     * @return clé Firebase
     */
    public String getKey() {
        return key;
    }

    // --- Setters ---

    /**
     * Définit le nom complet de l'administrateur.
     * @param fullName nom complet
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * Définit l'adresse email de l'administrateur.
     * @param email adresse email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Définit la clé Firebase de l'administrateur.
     * @param key clé unique
     */
    public void setKey(String key) {
        this.key = key;
    }
}
