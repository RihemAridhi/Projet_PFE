package isetb.project.parkingapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class ListeAdminsActivity extends AppCompatActivity {

    // Déclaration des composants
    private RecyclerView recyclerView;
    private AdminAdapter adminAdapter;
    private ArrayList<Admin> adminList;
    private DatabaseReference adminRef;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liste_admins);

        // Gestion des marges système (barres de navigation/état)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialisation de la toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialisation du RecyclerView
        recyclerView = findViewById(R.id.recyclerAdmins);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialisation de la liste et de l'adapter
        adminList = new ArrayList<>();
        adminAdapter = new AdminAdapter(adminList, this::deleteAdmin);
        recyclerView.setAdapter(adminAdapter);

        // Référence à la base de données Firebase (noeud "admins")
        adminRef = FirebaseDatabase.getInstance().getReference("admins");

        // Chargement des admins depuis Firebase
        loadAdmins();
    }

    // Fonction pour charger les administrateurs depuis Firebase
    private void loadAdmins() {
        adminRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                adminList.clear(); // On vide la liste actuelle
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Admin admin = snap.getValue(Admin.class);
                    admin.setKey(snap.getKey()); // Sauvegarde la clé Firebase
                    adminList.add(admin); // Ajoute à la liste
                }
                adminAdapter.notifyDataSetChanged(); // Rafraîchir l'affichage
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ListeAdminsActivity.this, "Erreur de chargement", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Fonction appelée quand on veut supprimer un admin
    private void deleteAdmin(Admin admin) {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Confirmation")
                .setMessage("Voulez-vous vraiment supprimer cet administrateur ?")
                .setPositiveButton("Oui", (dialog, which) -> {
                    // Supprime l'admin de Firebase
                    adminRef.child(admin.getKey()).removeValue()
                            .addOnSuccessListener(unused ->
                                    Toast.makeText(this, "Admin supprimé", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Erreur : " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Non", null)
                .show();
    }

    // Menu principal (barre d'action)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }

    // Gestion des clics sur les éléments du menu principal
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.logout) {
            // 🔒 Déconnexion Firebase
            FirebaseAuth.getInstance().signOut();

            // Mise à jour des préférences partagées (utilisé pour vérifier l'état de connexion)
            getSharedPreferences("ParkingAppPrefs", MODE_PRIVATE)
                    .edit()
                    .putBoolean("isLoggedIn", false)
                    .apply();

            // Redirection vers l'activité de connexion
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return true;
        } else if (id == R.id.home) {
            // Redirection vers le Dashboard
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
            return true;
        } else if (id == R.id.menu1) {
            // Affichage du menu contextuel
            showPopupMenu(findViewById(R.id.menu1));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Affiche un menu contextuel avec des options supplémentaires
    private void showPopupMenu(View anchorView) {
        PopupMenu popupMenu = new PopupMenu(this, anchorView);
        popupMenu.getMenuInflater().inflate(R.menu.popup_menu_employees, popupMenu.getMenu());

        // Actions sur les éléments du popup menu
        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();

            if (id == R.id.menu_view_employees) {
                startActivity(new Intent(this, ViewEmployeesActivity.class));
                return true;
            } else if (id == R.id.menu_add_employee) {
                startActivity(new Intent(this, AddEmployeeActivity.class));
                return true;
            } else if (id == R.id.menu_admin_info) {
                startActivity(new Intent(this, AdminInfoActivity.class));
                return true;
            } else if (id == R.id.history) {
                startActivity(new Intent(this, HistoryActivity.class));
                return true;
            }
            return false;
        });

        // Astuce : forcer l'affichage des icônes dans le popup menu (via la réflexion)
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
}
