package isetb.project.parkingapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PlateDetectionAdapter extends RecyclerView.Adapter<PlateDetectionAdapter.ViewHolder> {

    // Liste des objets PlateDetection, qui contient les données des plaques détectées
    private List<PlateDetection> plateList;

    // Constructeur qui prend la liste des objets PlateDetection
    public PlateDetectionAdapter(List<PlateDetection> plateList) {
        this.plateList = plateList;
    }

    // ViewHolder pour encapsuler les vues de chaque élément de la liste (ici, chaque plaque détectée)
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Références vers les éléments TextView qui afficheront les informations
        TextView textViewPlateNumber, textViewEntryTime, textViewExitTime, textViewOwnerName;

        // Le constructeur du ViewHolder initialise les vues à partir de l'élément du layout
        public ViewHolder(View itemView) {
            super(itemView);
            textViewPlateNumber = itemView.findViewById(R.id.textViewPlateNumber);
            textViewEntryTime = itemView.findViewById(R.id.textViewEntryTime);
            textViewExitTime = itemView.findViewById(R.id.textViewExitTime);
            textViewOwnerName = itemView.findViewById(R.id.textViewOwnerName);
        }
    }

    // Cette méthode est appelée pour créer une nouvelle vue pour chaque élément de la liste
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Charger le layout pour chaque élément de la liste
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_plate_detection, parent, false);
        return new ViewHolder(view); // Retourner un nouveau ViewHolder contenant l'élément
    }

    // Cette méthode est appelée pour lier les données du modèle à la vue de l'élément
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // Récupérer l'objet PlateDetection à la position donnée
        PlateDetection plate = plateList.get(position);

        // Mettre à jour le TextView pour afficher le numéro de la plaque
        holder.textViewPlateNumber.setText("Plaque : " + plate.getPlateNumber());

        // Vérifier si l'entrée contient des données de type "Entrée : ..."
        if (plate.getDetectionTime().startsWith("Entrée")) {
            // Si oui, séparer les informations d'entrée et de sortie
            String[] parts = plate.getDetectionTime().split("\n");

            // Afficher l'heure d'entrée (si elle existe)
            if (parts.length >= 1) {
                holder.textViewEntryTime.setText(parts[0]); // "Entrée : ..."
            } else {
                holder.textViewEntryTime.setText("Entrée : -");
            }

            // Afficher l'heure de sortie (si elle existe)
            if (parts.length >= 2) {
                holder.textViewExitTime.setText(parts[1]); // "Sortie : ..."
            } else {
                holder.textViewExitTime.setText("Sortie : -");
            }

        } else {
            // Si la détection ne concerne pas une entrée, afficher une détection simple
            holder.textViewEntryTime.setText(plate.getDetectionTime()); // "Détectée : ..."
            holder.textViewExitTime.setText("Sortie : ---");
        }

        // Afficher le nom du propriétaire (employé) associé à la plaque
        holder.textViewOwnerName.setText("Employé : " + plate.getOwnerName());
    }

    // Retourner le nombre d'éléments dans la liste
    @Override
    public int getItemCount() {
        return plateList.size();
    }
}
