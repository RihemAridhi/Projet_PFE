package isetb.project.parkingapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class UpdateEmployeeActivity extends AppCompatActivity {

    // Déclaration des éléments de l'interface utilisateur
    EditText editTextName, editTextPlateNumber;
    DatabaseReference databaseRef;
    String originalPlateNumber; // Plaque d'immatriculation originale de l'employé
    private Toolbar toolbar; // Barre d'outils pour la navigation

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_employee);

        // Gestion de la mise en page pour éviter les problèmes avec les barres de système
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialisation de la barre d'outils et des champs de saisie
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        editTextName = findViewById(R.id.editTxtName);
        editTextPlateNumber = findViewById(R.id.editTextPlateNumber);

        // Récupération des données passées par l'Intent (nom et plaque d'immatriculation de l'employé)
        originalPlateNumber = getIntent().getStringExtra("plate");
        editTextName.setText(getIntent().getStringExtra("name"));
        editTextPlateNumber.setText(originalPlateNumber);

        // Référence à la base de données Firebase pour les employés
        databaseRef = FirebaseDatabase.getInstance().getReference("employees");

        // Action du bouton de mise à jour
        findViewById(R.id.buttonUpdate).setOnClickListener(this::updateEmployee);
    }

    // Méthode pour mettre à jour les informations de l'employé dans la base de données Firebase
    private void updateEmployee(View view) {
        // Récupérer les données saisies par l'utilisateur
        String name = editTextName.getText().toString().trim();
        String plateNumber = editTextPlateNumber.getText().toString().trim();

        // Vérification que tous les champs sont remplis
        if (name.isEmpty() || plateNumber.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        // Recherche de l'employé dans Firebase basé sur la plaque d'immatriculation d'origine
        databaseRef.orderByChild("plate_number").equalTo(originalPlateNumber)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Si aucun employé n'est trouvé, afficher un message d'erreur
                        if (!snapshot.exists()) {
                            Toast.makeText(UpdateEmployeeActivity.this, "Aucun employé trouvé", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        // Si un employé est trouvé, mettre à jour ses informations
                        for (DataSnapshot empSnapshot : snapshot.getChildren()) {
                            empSnapshot.getRef().child("employee_name").setValue(name);
                            empSnapshot.getRef().child("plate_number").setValue(plateNumber);
                        }
                        // Afficher un message de confirmation et fermer l'activité
                        Toast.makeText(UpdateEmployeeActivity.this, "Employé mis à jour", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // En cas d'erreur avec Firebase, afficher un message d'erreur
                        Toast.makeText(UpdateEmployeeActivity.this, "Erreur Firebase", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Gestion des éléments de menu (déconnexion, retour à l'écran principal, etc.)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        // Option de déconnexion
        if (id == R.id.logout) {
            // 1. Déconnexion Firebase
            FirebaseAuth.getInstance().signOut();
            // 2. Mettre isLoggedIn à false dans SharedPreferences
            getSharedPreferences("ParkingAppPrefs", MODE_PRIVATE)
                    .edit()
                    .putBoolean("isLoggedIn", false)
                    .apply();
            // 3. Redirection vers l'écran de connexion
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return true;
        }
        // Option pour revenir au tableau de bord
        else if (id == R.id.home) {
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
            return true;
        }
        // Affichage du menu contextuel
        else if (id == R.id.menu1) {
            showPopupMenu(findViewById(R.id.menu1));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Affichage d'un menu contextuel (popup) avec diverses options
    private void showPopupMenu(View anchorView) {
        PopupMenu popupMenu = new PopupMenu(this, anchorView);
        popupMenu.getMenuInflater().inflate(R.menu.popup_menu_employees, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();

            // Gestion des actions du menu contextuel
            if (id == R.id.menu_view_employees) {
                startActivity(new Intent(this, ViewEmployeesActivity.class));
                return true;
            }
            else if (id == R.id.menu_add_employee) {
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

        // Astuce pour forcer l'affichage des icônes dans le menu (réflexion Java)
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

        // Affichage du popup menu
        popupMenu.show();
    }

    // Création du menu de l'activité
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }
}
