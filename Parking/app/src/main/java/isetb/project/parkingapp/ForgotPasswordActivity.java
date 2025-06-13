package isetb.project.parkingapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    // Déclaration des composants de l'interface
    EditText emailField;
    Button resetButton;
    FirebaseAuth auth;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Gestion des marges pour s'adapter aux barres système (Android 10+)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialisation et configuration de la toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Liaison avec les éléments du layout
        emailField = findViewById(R.id.emailField);
        resetButton = findViewById(R.id.resetButton);

        // Récupération de l'instance FirebaseAuth
        auth = FirebaseAuth.getInstance();

        // Gestion du clic sur le bouton de réinitialisation
        resetButton.setOnClickListener(v -> {
            String email = emailField.getText().toString().trim();

            // Vérifie si le champ email est vide
            if (email.isEmpty()) {
                emailField.setError("Email requis !");
                emailField.requestFocus();
                return;
            }

            // Envoi d'un email de réinitialisation via Firebase
            auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Succès : message + fermeture de l’activité
                            Toast.makeText(this, "Email de réinitialisation envoyé", Toast.LENGTH_LONG).show();
                            finish();
                        } else {
                            // Échec : message d'erreur
                            Toast.makeText(this, "Erreur : " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }

    // Méthode appelée lors d’un clic sur un élément du menu
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Redirection vers LoginActivity, en supprimant la pile d’activités précédentes
        startActivity(new Intent(this, LoginActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
        return true;
    }

    // Création du menu (dans la toolbar)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Associe le menu XML à cette activité
        getMenuInflater().inflate(R.menu.forget, menu);
        return true;
    }
}
