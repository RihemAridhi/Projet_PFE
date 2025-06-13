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

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    // Déclaration des vues de l'interface utilisateur
    private EditText emailEditText, nameEditText, passwordEditText, confirmPasswordEditText;
    private Button registerButton;
    private TextView login;

    // Initialisation de Firebase Auth et la référence à la base de données Firebase
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register); // Chargement du layout XML

        // Gestion des espacements des barres système (status bar, navigation bar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialisation des éléments d'interface utilisateur
        emailEditText = findViewById(R.id.editTexRegEmail);
        nameEditText = findViewById(R.id.editTexRegPasswo); // Nom (corrigé)
        passwordEditText = findViewById(R.id.editTexRegPasswor); // Mot de passe
        confirmPasswordEditText = findViewById(R.id.editTexRegPassword); // Confirmation
        registerButton = findViewById(R.id.Regbutton);
        login = findViewById(R.id.log); // Redirection vers Login

        ImageView eyeIcon = findViewById(R.id.eyeIcon);     // Pour le champ mot de passe
        ImageView eyeIconn = findViewById(R.id.eyeIconn);   // Pour le champ confirmation

// Afficher/Masquer mot de passe
        eyeIcon.setOnClickListener(v -> {
            if (passwordEditText.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                eyeIcon.setImageResource(R.drawable.baseline_remove_red_eye_24); // œil ouvert
            } else {
                passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                eyeIcon.setImageResource(R.drawable.baseline_remove_red_eye_24); // œil barré
            }
            passwordEditText.setSelection(passwordEditText.length());
        });

// Afficher/Masquer confirmation mot de passe
        eyeIconn.setOnClickListener(v -> {
            if (confirmPasswordEditText.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                confirmPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                eyeIconn.setImageResource(R.drawable.baseline_remove_red_eye_24); // œil ouvert
            } else {
                confirmPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                eyeIconn.setImageResource(R.drawable.baseline_remove_red_eye_24); // œil barré
            }
            confirmPasswordEditText.setSelection(confirmPasswordEditText.length());
        });

// Initialisation de Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("admins");


        // Initialisation de Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("admins");


        // Lorsque l'utilisateur clique sur le lien de connexion, il est redirigé vers LoginActivity
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent); // Redirection vers l'écran de connexion
            }
        });

        // Lorsque l'utilisateur clique sur le bouton d'inscription, l'inscription est lancée
        registerButton.setOnClickListener(v -> registerAdmin());
    }

    // Méthode pour enregistrer un nouvel administrateur
    private void registerAdmin() {
        // Récupération des données saisies dans les champs de texte
        String email = emailEditText.getText().toString().trim();
        String name = nameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        // Vérification que tous les champs sont remplis
        if (email.isEmpty() || name.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        // Vérification que les mots de passe correspondent
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Les mots de passe ne correspondent pas", Toast.LENGTH_SHORT).show();
            return;
        }

        // Création de l'utilisateur Firebase avec l'email et le mot de passe
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser(); // Récupérer l'utilisateur après une inscription réussie
                    if (user != null) {
                        String userId = user.getUid(); // Récupération de l'UID de l'utilisateur

                        // Création d'une HashMap pour enregistrer les informations de l'administrateur dans Firebase Database
                        HashMap<String, Object> adminMap = new HashMap<>();
                        adminMap.put("fullName", name); // Ajouter le nom
                        adminMap.put("email", email); // Ajouter l'email
                        // Enregistrement des informations dans la base de données Firebase sous l'ID de l'utilisateur
                        databaseReference.child(userId).setValue(adminMap)
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(this, "Admin créé avec succès", Toast.LENGTH_SHORT).show();

                                    // Déconnexion manuelle après création de l'administrateur
                                    firebaseAuth.signOut();

                                    // Redirection vers LoginActivity pour permettre à l'administrateur de se connecter
                                    Intent intent = new Intent(this, LoginActivity.class);
                                    startActivity(intent);
                                    finish(); // Fermer l'activité d'inscription pour éviter de revenir dessus avec le bouton retour
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Erreur enregistrement DB: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erreur Authentification: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}