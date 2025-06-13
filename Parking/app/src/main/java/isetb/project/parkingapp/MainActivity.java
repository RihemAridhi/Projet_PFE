package isetb.project.parkingapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //  Activer le mode Edge-to-Edge pour que l'affichage soit sans bordures
        EdgeToEdge.enable(this);

        // Charger le layout de l'écran principal (splash screen)
        setContentView(R.layout.activity_main);

        //  Ajuster automatiquement les marges pour les barres système (ex : barre d'état, barre de navigation)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            // Récupérer les marges des barres système
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // Appliquer ces marges au padding du layout principal
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //  Délai de 3 secondes (3000 millisecondes) avant de passer à l'écran de connexion
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Créer une intention pour passer à l'écran de login (LoginActivity)
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent); // Démarrer l'activité de login
                finish(); // Fermer MainActivity pour que l'utilisateur ne puisse pas revenir à l'écran de splash via le bouton retour
            }
        }, 3000); // Délai de 3000 ms = 3 secondes
    }
}
