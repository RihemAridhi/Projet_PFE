package isetb.project.parkingapp;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Adapter personnalisé pour afficher une liste d'employés dans un RecyclerView.
 */
public class EmployeeAdapter extends RecyclerView.Adapter<EmployeeAdapter.EmployeeViewHolder> {

    private final List<Employee> employeeList; // Liste des employés à afficher
    private final LayoutInflater inflater;     // Pour "gonfler" les vues XML
    private final OnItemClickListener listener; // Interface pour les actions sur les éléments

    /**
     * Interface pour gérer les clics sur les boutons "Update" et "Delete".
     */
    public interface OnItemClickListener {
        void onUpdate(Employee employee);  // Action mise à jour
        void onDelete(Employee employee);  // Action suppression
    }

    /**
     * Constructeur de l'adapter.
     */
    public EmployeeAdapter(Context context, List<Employee> employeeList, OnItemClickListener listener) {
        this.employeeList = employeeList;
        this.listener = listener;
        this.inflater = LayoutInflater.from(context); // Création de l'inflater à partir du contexte
    }

    /**
     * Crée et retourne un ViewHolder (un élément visuel de la liste).
     */
    @NonNull
    @Override
    public EmployeeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_employee, parent, false); // Liaison avec le layout XML d'un employé
        return new EmployeeViewHolder(view);
    }

    /**
     * Lie les données de l'employé à la vue correspondante.
     */
    @Override
    public void onBindViewHolder(@NonNull EmployeeViewHolder holder, int position) {
        holder.bind(employeeList.get(position), listener); // Remplit les TextViews avec les infos de l'employé
    }

    /**
     * Retourne le nombre total d'employés dans la liste.
     */
    @Override
    public int getItemCount() {
        return employeeList != null ? employeeList.size() : 0;
    }

    /**
     * ViewHolder : Classe interne représentant chaque élément de la liste (layout item_employee.xml).
     */
    static class EmployeeViewHolder extends RecyclerView.ViewHolder {
        private final TextView txtName;    // TextView pour le nom de l'employé
        private final TextView txtPlaque;  // TextView pour la plaque d'immatriculation

        /**
         * Constructeur du ViewHolder : initialise les TextViews.
         */
        public EmployeeViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtName);
            txtPlaque = itemView.findViewById(R.id.txtPlaque);
        }

        /**
         * Remplit les TextViews avec les données d'un employé et ajoute un listener pour le clic.
         */
        public void bind(Employee employee, OnItemClickListener listener) {
            txtName.setText(employee.getEmployee_name());         // Affiche le nom de l'employé
            txtPlaque.setText(employee.getPlate_number());        // Affiche la plaque

            // Clic sur l'élément : montre le menu contextuel (Update / Delete)
            itemView.setOnClickListener(v -> showPopupMenu(v, employee, listener));
        }

        /**
         * Affiche un menu contextuel (popup) avec les options "Modifier" et "Supprimer".
         */
        private void showPopupMenu(View view, Employee employee, OnItemClickListener listener) {
            PopupMenu popupMenu = new PopupMenu(view.getContext(), view); // Crée le menu
            MenuInflater inflater = popupMenu.getMenuInflater();
            inflater.inflate(R.menu.employee_popup_menu, popupMenu.getMenu()); // Récupère le menu XML

            // Gère les clics sur le menu
            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.menu_update) {
                    listener.onUpdate(employee);  // Appelle l'action de mise à jour
                    return true;
                } else if (id == R.id.menu_delete) {
                    // Affiche une boîte de dialogue de confirmation
                    new AlertDialog.Builder(view.getContext())
                            .setTitle("Supprimer")
                            .setMessage("Voulez-vous supprimer cet employé : " + employee.getEmployee_name() + " ?")
                            .setPositiveButton("Oui", (dialog, which) -> listener.onDelete(employee))
                            .setNegativeButton("Non", null)
                            .show();
                    return true;
                }
                return false;
            });

            // Optionnel : force l'affichage des icônes dans le menu (via réflexion)
            try {
                Field[] fields = popupMenu.getClass().getDeclaredFields();
                for (Field field : fields) {
                    if ("mPopup".equals(field.getName())) {
                        field.setAccessible(true);
                        Object menuPopupHelper = field.get(popupMenu);
                        Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                        Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                        setForceIcons.invoke(menuPopupHelper, true); // Force les icônes
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace(); // Affiche les erreurs en cas d'échec
            }

            popupMenu.show(); // Affiche le menu à l'utilisateur
        }
    }
}
