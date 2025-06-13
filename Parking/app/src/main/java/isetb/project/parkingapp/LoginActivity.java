package isetb.project.parkingapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    // Références aux éléments de l'UI
    EditText editTextLogEmail, editTextLogPassword;
    Button logbutton;
    TextView register, forgotPasswordLink;
    private FirebaseAuth mAuth; // Instance FirebaseAuth pour l'authentification

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // Charger le layout de l'écran de login

        // Appliquer un padding automatique pour ajuster les barres système (comme la barre d'état et de navigation)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom); // Appliquer les marges de système
            return insets;
        });

        //  Vérifier si l'utilisateur est déjà connecté et rediriger vers le DashboardActivity
        boolean isLoggedIn = getSharedPreferences("ParkingAppPrefs", MODE_PRIVATE)
                .getBoolean("isLoggedIn", false);
        if (isLoggedIn) {
            startActivity(new Intent(LoginActivity.this, DashboardActivity.class)); // Démarrer le dashboard si déjà connecté
            finish(); // Terminer l'activité actuelle (Login)
            return;
        }

        //  Activer le mode Edge-to-Edge pour un affichage sans bordures
        EdgeToEdge.enable(this);

        //  Configurer le lien de mot de passe oublié
        forgotPasswordLink = findViewById(R.id.forgotPasswordLink);
        forgotPasswordLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class); // Ouvrir l'écran de réinitialisation de mot de passe
            startActivity(intent);
        });

        // Références aux champs d'entrée et aux boutons
        editTextLogEmail = findViewById(R.id.editTexLogEmail);
        editTextLogPassword = findViewById(R.id.editTexLogPassword);
        logbutton = findViewById(R.id.logbutton);
        register = findViewById(R.id.register);
        ImageView eyeIcon = findViewById(R.id.eyeIcon);

// Gestion de l'icône œil pour afficher/masquer le mot de passe
        eyeIcon.setOnClickListener(v -> {
            if (editTextLogPassword.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                editTextLogPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                eyeIcon.setImageResource(R.drawable.baseline_remove_red_eye_24); // œil ouvert
            } else {
                editTextLogPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                eyeIcon.setImageResource(R.drawable.baseline_remove_red_eye_24); // œil barré
            }
            editTextLogPassword.setSelection(editTextLogPassword.length()); // garde le curseur à la fin
        });


        mAuth = FirebaseAuth.getInstance(); // Initialiser Firebase Auth

        //  Action pour rediriger vers l'écran d'inscription
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class)); // Démarrer l'écran d'inscription
            }
        });

        //  Action lors du clic sur le bouton de connexion
        logbutton.setOnClickListener(view -> {
            String email = editTextLogEmail.getText().toString().trim(); // Récupérer l'email
            String password = editTextLogPassword.getText().toString().trim(); // Récupérer le mot de passe

            // Validation de l'email et du mot de passe
            if (email.isEmpty()) {
                editTextLogEmail.setError("Email is required");
                editTextLogEmail.requestFocus();
                return;
            }

            if (password.isEmpty()) {
                editTextLogPassword.setError("Password is required");
                editTextLogPassword.requestFocus();
                return;
            }

            // Tentative de connexion avec Firebase
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser(); // Récupérer l'utilisateur actuel
                            if (user != null) {
                                //  Sauvegarder l'état de connexion dans les SharedPreferences
                                getSharedPreferences("ParkingAppPrefs", MODE_PRIVATE)
                                        .edit()
                                        .putBoolean("isLoggedIn", true) // Marquer l'utilisateur comme connecté
                                        .apply();

                                Toast.makeText(LoginActivity.this, "Connexion réussie", Toast.LENGTH_SHORT).show(); // Afficher un toast de succès
                                startActivity(new Intent(LoginActivity.this, DashboardActivity.class)); // Démarrer le Dashboard
                                finish(); // Terminer l'activité de login
                            }
                        } else {
                            // Erreur de connexion, afficher un message
                            Toast.makeText(LoginActivity.this, "Échec de la connexion : " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }
}