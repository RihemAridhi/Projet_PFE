package isetb.project.parkingapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class AdminInfoActivity extends AppCompatActivity {

    // Déclaration des composants de l'interface
    private TextView textViewFullName, textViewEmail, textViewRole, listeAdmins;
    private EditText editTextNewPassword, editTextCurrentPassword;
    private Button buttonUpdatePassword;
    private Toolbar toolbar;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_info);

        // Gestion des marges système pour le layout principal
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialisation de la toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialisation des vues
        textViewFullName = findViewById(R.id.textViewFullName);
        textViewEmail = findViewById(R.id.textViewEmail);
        editTextNewPassword = findViewById(R.id.editTextNewPassword);
        editTextCurrentPassword = findViewById(R.id.editTextCurrentPassword);
        buttonUpdatePassword = findViewById(R.id.buttonUpdatePassword);
        listeAdmins = findViewById(R.id.listeAdmins);

        ImageView eyeIcon = findViewById(R.id.eyeIcon);   // Pour mot de passe actuel
        ImageView eyeIconn = findViewById(R.id.eyeIco);   // Pour nouveau mot de passe

// Afficher/Masquer mot de passe actuel
        eyeIcon.setOnClickListener(v -> {
            if (editTextCurrentPassword.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                editTextCurrentPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                eyeIcon.setImageResource(R.drawable.baseline_remove_red_eye_24); // œil ouvert
            } else {
                editTextCurrentPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                eyeIcon.setImageResource(R.drawable.baseline_mark_email_read_24); // œil fermé
            }
            editTextCurrentPassword.setSelection(editTextCurrentPassword.length());
        });

// Afficher/Masquer nouveau mot de passe
        eyeIconn.setOnClickListener(v -> {
            if (editTextNewPassword.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                editTextNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                eyeIconn.setImageResource(R.drawable.baseline_remove_red_eye_24); // œil ouvert
            } else {
                editTextNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                eyeIconn.setImageResource(R.drawable.baseline_remove_red_eye_24); // œil fermé
            }
            editTextNewPassword.setSelection(editTextNewPassword.length());
        });


        // Initialisation Firebase
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Si un utilisateur est connecté
        if (currentUser != null) {
            String currentEmail = currentUser.getEmail(); // Récupération de son email
            userRef = FirebaseDatabase.getInstance().getReference("admins");

            // Rechercher l'admin par email dans Firebase
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    boolean found = false;
                    for (DataSnapshot adminSnapshot : snapshot.getChildren()) {
                        String email = adminSnapshot.child("email").getValue(String.class);
                        if (email != null && email.equalsIgnoreCase(currentEmail)) {
                            // Si trouvé, afficher les infos
                            String fullName = adminSnapshot.child("fullName").getValue(String.class);

                            textViewFullName.setText("Nom complet : " + fullName);
                            textViewEmail.setText("Email : " + email);

                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        Toast.makeText(AdminInfoActivity.this, "Admin non trouvé", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(AdminInfoActivity.this, "Erreur de chargement", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Lien vers l'activité ListeAdmins
        listeAdmins.setOnClickListener(view -> {
            startActivity(new Intent(AdminInfoActivity.this, ListeAdminsActivity.class));
        });

        // Gestion de la mise à jour du mot de passe
        buttonUpdatePassword.setOnClickListener(v -> {
            String currentPassword = editTextCurrentPassword.getText().toString().trim();
            String newPassword = editTextNewPassword.getText().toString().trim();

            // Vérifications de validité
            if (TextUtils.isEmpty(currentPassword)) {
                editTextCurrentPassword.setError("Entrez votre mot de passe actuel");
                return;
            }

            if (TextUtils.isEmpty(newPassword) || newPassword.length() < 6) {
                editTextNewPassword.setError("Mot de passe trop court (min 6 caractères)");
                return;
            }

            // Ré-authentification avec le mot de passe actuel
            if (currentUser != null && currentUser.getEmail() != null) {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                auth.signInWithEmailAndPassword(currentUser.getEmail(), currentPassword)
                        .addOnSuccessListener(authResult -> {
                            // Mise à jour du mot de passe
                            currentUser.updatePassword(newPassword)
                                    .addOnSuccessListener(unused -> {
                                        Toast.makeText(AdminInfoActivity.this, "Mot de passe mis à jour", Toast.LENGTH_SHORT).show();
                                        editTextCurrentPassword.setText("");
                                        editTextNewPassword.setText("");
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(AdminInfoActivity.this, "Erreur : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(AdminInfoActivity.this, "Mot de passe actuel incorrect", Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }

    // Gestion des éléments du menu de la Toolbar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.logout) {
            // 🔓 Déconnexion de l'utilisateur
            FirebaseAuth.getInstance().signOut();

            // ❌ Mise à jour de SharedPreferences
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

    // Affichage du menu contextuel avec actions supplémentaires
    private void showPopupMenu(View anchorView) {
        PopupMenu popupMenu = new PopupMenu(this, anchorView);
        popupMenu.getMenuInflater().inflate(R.menu.popup_menu_employees, popupMenu.getMenu());

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
            }
            else if (id == R.id.history) {
                // Accès à l'historique
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

        popupMenu.show();
    }

    // Chargement du menu de la Toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }
}