package isetb.project.parkingapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;
import android.app.AlertDialog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private HistoryAdapter adapter;
    private final List<HistoryItem> itemList = new ArrayList<>();

    private DatabaseReference dbRootRef;
    private DatabaseReference historyRef;

    private Toolbar toolbar;

    // 🔹 Méthode appelée à la création de l'activité
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // Gère les marges système pour les écrans plein écran (API 21+)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Configuration de la barre d’outils
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialisation de la RecyclerView
        recyclerView = findViewById(R.id.historyRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialisation de l'adaptateur avec gestion du clic de suppression
        adapter = new HistoryAdapter(itemList, this::confirmDeleteHistoryNode);
        recyclerView.setAdapter(adapter);

        // Références Firebase
        dbRootRef = FirebaseDatabase.getInstance().getReference();
        historyRef = dbRootRef.child("history");

        // Chargement initial des données
        loadHistory();
    }

    // 🔹 Chargement des données depuis Firebase
    private void loadHistory() {
        historyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                itemList.clear(); // Vide la liste existante
                for (DataSnapshot child : snapshot.getChildren()) {
                    HistoryItem item = child.getValue(HistoryItem.class);
                    if (item != null) {
                        item.setId(child.getKey()); // Stocke l’ID Firebase
                        itemList.add(item);
                    }
                }
                adapter.notifyDataSetChanged(); // Met à jour la vue
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HistoryActivity.this, "Erreur de chargement Firebase", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 🔹 Affichage d’un dialogue de confirmation pour suppression individuelle
    private void confirmDeleteHistoryNode(HistoryItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Supprimer cet élément ?")
                .setMessage("Voulez-vous vraiment supprimer cet enregistrement de l'historique ?")
                .setPositiveButton("Oui", (dialog, which) -> deleteHistoryNode(item))
                .setNegativeButton("Non", null)
                .show();
    }

    // 🔹 Suppression d’un élément spécifique de l’historique
    private void deleteHistoryNode(HistoryItem item) {
        historyRef.child(item.getId()).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                itemList.remove(item);
                adapter.notifyDataSetChanged();
                Toast.makeText(this, "Élément supprimé", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Échec de la suppression", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 🔹 Création du menu principal (barre supérieure)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.history_menu, menu);
        return true;
    }

    // 🔹 Réactions aux éléments du menu
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.logout) {
            logout(); // Déconnexion
            return true;
        } else if (id == R.id.home) {
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
            return true;
        } else if (id == R.id.action_delete_history) {
            showDeleteConfirmationDialog(); // Suppression totale
            return true;
        } else if (id == R.id.menu1) {
            showPopupMenu(findViewById(R.id.menu1)); // Affiche le menu contextuel
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // 🔹 Affichage du menu contextuel administrateur
    private void showPopupMenu(View anchorView) {
        PopupMenu popupMenu = new PopupMenu(this, anchorView);
        popupMenu.getMenuInflater().inflate(R.menu.popup_menu_employees, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_view_employees) {
                startActivity(new Intent(this, ViewEmployeesActivity.class));
                return true;
            } else if (id == R.id.history) {
                startActivity(new Intent(this, HistoryActivity.class));
                return true;
            } else if (id == R.id.menu_add_employee) {
                startActivity(new Intent(this, AddEmployeeActivity.class));
                return true;
            } else if (id == R.id.menu_admin_info) {
                startActivity(new Intent(this, AdminInfoActivity.class));
                return true;
            }
            return false;
        });

        // Affichage forcé des icônes via réflexion (non recommandé mais fonctionnel)
        try {
            Field[] fields = popupMenu.getClass().getDeclaredFields();
            for (Field field : fields) {
                if ("mPopup".equals(field.getName())) {
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popupMenu);
                    Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                    Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                    setForceIcons.invoke(menuPopupHelper, true);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        popupMenu.show();
    }

    // 🔹 Déconnexion de l'utilisateur
    private void logout() {
        FirebaseAuth.getInstance().signOut();
        getSharedPreferences("ParkingAppPrefs", MODE_PRIVATE)
                .edit()
                .putBoolean("isLoggedIn", false)
                .apply();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    // 🔹 Affichage d’un dialogue pour confirmer la suppression de toutes les données
    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Confirmation de suppression")
                .setMessage("Supprimer tout l'historique ? Cela effacera aussi les données 'parking_status' et 'parking_access'.")
                .setPositiveButton("Oui", (dialog, which) -> deleteAllData())
                .setNegativeButton("Annuler", null)
                .show();
    }

    // 🔹 Suppression de toutes les données de Firebase
    private void deleteAllData() {
        dbRootRef.child("history").removeValue();
        dbRootRef.child("parking_status").removeValue();
        dbRootRef.child("parking_access").removeValue();

        itemList.clear();
        adapter.notifyDataSetChanged();
        Toast.makeText(this, "Données supprimées avec succès", Toast.LENGTH_SHORT).show();
    }
}
