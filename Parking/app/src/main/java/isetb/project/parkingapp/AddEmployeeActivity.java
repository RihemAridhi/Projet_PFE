package isetb.project.parkingapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class AddEmployeeActivity extends AppCompatActivity {

    // Déclaration des composants UI et de la référence Firebase
    private EditText editTextName, editTextPlate;
    private Button buttonSave;
    private DatabaseReference databaseEmployees;
    private Toolbar toolbar;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_employee); // Lien avec le fichier XML

        // Pour gérer les insets (barres système) et éviter que le contenu soit masqué
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialisation de la toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialisation de la référence Firebase vers le noeud "employees"
        databaseEmployees = FirebaseDatabase.getInstance().getReference("employees");

        // Liaison des composants de l'interface
        editTextName = findViewById(R.id.editTextName);
        editTextPlate = findViewById(R.id.editTextPlate);
        buttonSave = findViewById(R.id.buttonSave);

        // Définir l'action du bouton "Enregistrer"
        buttonSave.setOnClickListener(v -> saveEmployee());
    }

    // Fonction pour sauvegarder un employé dans Firebase
    private void saveEmployee() {
        String name = editTextName.getText().toString().trim();   // Récupérer le nom
        String plate = editTextPlate.getText().toString().trim(); // Récupérer la plaque

        // Vérification que les champs ne sont pas vides
        if (name.isEmpty() || plate.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        // Génération d'un ID unique et création de l'objet employé
        String id = databaseEmployees.push().getKey();
        Employee employee = new Employee(id, name, plate);

        // Sauvegarde dans Firebase sous le noeud "employees"
        databaseEmployees.child(id).setValue(employee);

        // Message de confirmation
        Toast.makeText(this, "Employé ajouté", Toast.LENGTH_SHORT).show();

        // Ferme l'activité et retourne à la précédente
        finish();
    }

    // Gère les éléments sélectionnés dans le menu de la toolbar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.logout) {
            // 🔓 Déconnexion de l'utilisateur Firebase
            FirebaseAuth.getInstance().signOut();

            // ❌ Marquer l'utilisateur comme déconnecté dans les SharedPreferences
            getSharedPreferences("ParkingAppPrefs", MODE_PRIVATE)
                    .edit()
                    .putBoolean("isLoggedIn", false)
                    .apply();

            // 🔁 Redirection vers l'écran de connexion
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return true;

        } else if (id == R.id.home) {
            // Redirection vers le tableau de bord
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

    // Affiche un menu contextuel avec plusieurs options
    private void showPopupMenu(View anchorView) {
        PopupMenu popupMenu = new PopupMenu(this, anchorView);
        popupMenu.getMenuInflater().inflate(R.menu.popup_menu_employees, popupMenu.getMenu());

        // Gestion des clics dans le menu contextuel
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

        // ⚙️ Astuce pour forcer l'affichage des icônes dans le menu contextuel
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

        // Affiche le menu
        popupMenu.show();
    }

    // Chargement du menu dans la toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }
}
