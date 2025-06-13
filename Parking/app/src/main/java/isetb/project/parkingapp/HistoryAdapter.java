package isetb.project.parkingapp;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Adapter personnalisé pour afficher les éléments d'historique dans une RecyclerView.
 */
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<HistoryItem> historyList; // Liste des éléments de l'historique
    private OnDeleteClickListener listener; // Interface pour gérer la suppression

    /**
     * Interface pour notifier lorsqu'un utilisateur souhaite supprimer un élément.
     */
    public interface OnDeleteClickListener {
        void onDeleteClick(HistoryItem item);
    }

    /**
     * Constructeur de l'adapter.
     *
     * @param list     La liste des éléments à afficher.
     * @param listener Le gestionnaire d'événements pour les suppressions.
     */
    public HistoryAdapter(List<HistoryItem> list, OnDeleteClickListener listener) {
        this.historyList = list;
        this.listener = listener;
    }

    /**
     * Classe ViewHolder qui contient les vues pour chaque élément.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView plateView, dateView, timeView;

        public ViewHolder(View view) {
            super(view);
            plateView = view.findViewById(R.id.plateText);  // Affiche la plaque
            dateView = view.findViewById(R.id.dateText);    // Affiche la date
            timeView = view.findViewById(R.id.timeText);    // Affiche l'heure d'entrée/sortie
        }

        /**
         * Méthode pour lier les données à la vue.
         */
        public void bind(HistoryItem item, OnDeleteClickListener listener) {
            plateView.setText(item.getPlate());
            dateView.setText(item.getDate());

            // Affiche soit les heures d'entrée/sortie, soit "Unauthorized"
            String entryExit = item.getEntry_time() != null ?
                    "Entrée: " + item.getEntry_time() + " | Sortie: " + item.getExit_time() :
                    "Non autorisé";
            timeView.setText(entryExit);

            // Lorsque l'élément est cliqué, demander confirmation avant suppression
            itemView.setOnClickListener(v -> {
                new AlertDialog.Builder(itemView.getContext())
                        .setTitle("Confirmation")
                        .setMessage("Voulez-vous supprimer cette entrée de l'historique ?")
                        .setPositiveButton("Oui", (dialog, which) -> listener.onDeleteClick(item))
                        .setNegativeButton("Non", null)
                        .show();
            });
        }
    }

    /**
     * Création de la vue de chaque élément (appelé lorsque nécessaire par RecyclerView).
     */
    @Override
    public HistoryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.history_item, parent, false); // Charge le layout XML pour un item
        return new ViewHolder(view);
    }

    /**
     * Remplit les vues avec les données à une position donnée.
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(historyList.get(position), listener); // Lier les données à la vue
    }

    /**
     * Retourne le nombre total d'éléments à afficher.
     */
    @Override
    public int getItemCount() {
        return historyList.size();
    }
}
