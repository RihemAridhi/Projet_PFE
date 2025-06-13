package isetb.project.parkingapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Adaptateur pour afficher une liste d'admins dans un RecyclerView.
 */
public class AdminAdapter extends RecyclerView.Adapter<AdminAdapter.AdminViewHolder> {

    // Liste des administrateurs à afficher
    private final List<Admin> adminList;

    // Interface pour gérer les clics sur un élément de la liste
    private final OnAdminClickListener listener;

    /**
     * Interface pour écouter les clics sur un administrateur.
     */
    public interface OnAdminClickListener {
        void onAdminClick(Admin admin);  // méthode appelée lorsqu'un item est cliqué
    }

    /**
     * Constructeur de l'adaptateur.
     *
     * @param adminList liste des administrateurs
     * @param listener  listener de clics
     */
    public AdminAdapter(List<Admin> adminList, OnAdminClickListener listener) {
        this.adminList = adminList;
        this.listener = listener;
    }

    /**
     * Crée un nouveau ViewHolder pour chaque item (appelé quand nécessaire).
     */
    @NonNull
    @Override
    public AdminViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // On utilise un LayoutInflater pour "gonfler" l'interface XML d'un item
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin, parent, false);
        return new AdminViewHolder(view);
    }

    /**
     * Remplit les données dans un ViewHolder donné à une position donnée.
     */
    @Override
    public void onBindViewHolder(@NonNull AdminViewHolder holder, int position) {
        // Récupère l'objet admin à cette position
        Admin admin = adminList.get(position);

        // Affecte les données dans les TextViews
        holder.fullName.setText(admin.getFullName());
        holder.email.setText(admin.getEmail());

        // Ajoute un écouteur de clic sur l'élément entier
        holder.itemView.setOnClickListener(v -> listener.onAdminClick(admin));
    }

    /**
     * Retourne le nombre total d'éléments dans la liste.
     */
    @Override
    public int getItemCount() {
        return adminList.size();
    }

    /**
     * Classe interne représentant un ViewHolder (conteneur d'éléments graphiques).
     */
    public static class AdminViewHolder extends RecyclerView.ViewHolder {
        TextView fullName, email;  // Les vues à afficher dans chaque item

        public AdminViewHolder(@NonNull View itemView) {
            super(itemView);
            // Récupération des vues dans le layout XML
            fullName = itemView.findViewById(R.id.textFullName);
            email = itemView.findViewById(R.id.textEmail);
        }
    }
}
