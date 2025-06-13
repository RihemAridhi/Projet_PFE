package isetb.project.parkingapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.ToggleButton;

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
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Dashboard principal de l'application de gestion de parking.
 * Affiche les plaques détectées, les employés associés, le statut du parking, etc.
 */
public class DashboardActivity extends AppCompatActivity {

    // Composants de l'interface
    private TextView textViewLastDetectedPlate, textViewAvailablePlaces, textViewEmployeeName;
    private RecyclerView recyclerViewPlates;
    //private ToggleButton toggleRelay;
    private Toolbar toolbar;

    // Adaptateur RecyclerView
    private PlateDetectionAdapter adapter;
    private List<PlateDetection> plateList;

    // Firebase
    private DatabaseReference databaseReference;
    private String todayDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Gère les marges selon les barres système
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialisation de la barre d’outils (toolbar)
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialisation des vues
        //toggleRelay = findViewById(R.id.toggleRelay);
        textViewLastDetectedPlate = findViewById(R.id.textViewLastDetectedPlate);
        textViewAvailablePlaces = findViewById(R.id.textViewAvailablePlaces);
        textViewEmployeeName = findViewById(R.id.textViewEmployeeName);
        recyclerViewPlates = findViewById(R.id.recyclerViewPlates);

        // Configuration de la RecyclerView
        recyclerViewPlates.setLayoutManager(new LinearLayoutManager(this));
        plateList = new ArrayList<>();
        adapter = new PlateDetectionAdapter(plateList);
        recyclerViewPlates.setAdapter(adapter);

        // Référence Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference();
        todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Référence de la commande du relais
        DatabaseReference relayRef = databaseReference.child("relay_control");

        /*Gestion de l'état du bouton de relais
        toggleRelay.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String status = isChecked ? "ON" : "OFF";
            relayRef.child("status").setValue(status)
                    .addOnSuccessListener(aVoid -> Log.d("RelayControl", "Relay turned " + status))
                    .addOnFailureListener(e -> Log.e("RelayControl", "Failed to update relay status", e));
        });*/

        // Chargement initial des données
        loadDashboardData();
    }

    /**
     * Charge les données de statut du parking et les plaques détectées.
     */
    private void loadDashboardData() {
        loadParkingStatus();
        loadPlateDetections();
    }

    /**
     * Charge les données de statut du parking : dernière plaque et places disponibles.
     */
    private void loadParkingStatus() {
        databaseReference.child("parking_status").child(todayDate)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String lastPlate = snapshot.child("last_detected_plate").getValue(String.class);
                            Long availableSpots = snapshot.child("available_spots").getValue(Long.class);

                            if (lastPlate != null && !lastPlate.isEmpty()) {
                                textViewLastDetectedPlate.setText("Dernière plaque détectée : " + lastPlate);
                                findEmployeeName(lastPlate);
                            } else {
                                textViewLastDetectedPlate.setText("Dernière plaque détectée : Aucune");
                                textViewEmployeeName.setText("Employé : Inconnu");
                            }
                            textViewAvailablePlaces.setText("Places disponibles : " + (availableSpots != null ? availableSpots : "Inconnu"));
                        } else {
                            textViewLastDetectedPlate.setText("Dernière plaque détectée : Aucune");
                            textViewAvailablePlaces.setText("Places disponibles : Inconnu");
                            textViewEmployeeName.setText("Employé : Inconnu");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        textViewLastDetectedPlate.setText("Erreur de chargement");
                        textViewAvailablePlaces.setText("Erreur de chargement");
                        textViewEmployeeName.setText("Erreur de chargement");
                    }
                });
    }

    /**
     * Charge les plaques autorisées et non autorisées détectées aujourd'hui.
     */
    private void loadPlateDetections() {
        databaseReference.child("parking_access").child(todayDate)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<PlateDetection> tempList = new ArrayList<>();

                        // Plaques autorisées
                        if (snapshot.child("authorized").exists()) {
                            for (DataSnapshot ds : snapshot.child("authorized").getChildren()) {
                                String plateNumber = ds.getKey();
                                String entryTime = ds.child("entry_time").getValue(String.class);
                                String exitTime = ds.child("exit_time").getValue(String.class);
                                if (plateNumber != null && entryTime != null) {
                                    String detTime = "Entrée : " + entryTime;
                                    if (exitTime != null && !exitTime.isEmpty()) {
                                        detTime += "\nSortie : " + exitTime;
                                    }
                                    tempList.add(new PlateDetection(plateNumber, detTime, ""));
                                }
                            }
                        }

                        // Plaques non autorisées
                        if (snapshot.child("unauthorized").exists()) {
                            for (DataSnapshot ds : snapshot.child("unauthorized").getChildren()) {
                                String plateNumber = ds.child("plate_number").getValue(String.class);
                                String detTime = ds.child("detection_time").getValue(String.class);
                                if (plateNumber != null && detTime != null) {
                                    tempList.add(new PlateDetection(plateNumber, "Détectée : " + detTime, "Non autorisée"));
                                }
                            }
                        }

                        fetchEmployeeNames(tempList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        plateList.clear();
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    /**
     * Associe chaque plaque à un employé (si autorisée).
     */
    private void fetchEmployeeNames(List<PlateDetection> tempList) {
        plateList.clear();
        if (tempList.isEmpty()) {
            adapter.notifyDataSetChanged();
            return;
        }
        final int total = tempList.size();
        final int[] done = {0};

        for (PlateDetection plate : tempList) {
            // Si ce n'est pas une plaque non autorisée, chercher le nom de l'employé
            if (!"Non autorisée".equals(plate.getOwnerName())) {
                databaseReference.child("employees")
                        .orderByChild("plate_number")
                        .equalTo(plate.getPlateNumber())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snap) {
                                String empName = "Employé inconnu";
                                if (snap.exists()) {
                                    for (DataSnapshot es : snap.getChildren()) {
                                        String name = es.child("employee_name").getValue(String.class);
                                        if (name != null) empName = name;
                                        break;
                                    }
                                }
                                plate.setOwnerName(empName);
                                plateList.add(plate);
                                if (++done[0] == total) adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                plate.setOwnerName("Erreur");
                                plateList.add(plate);
                                if (++done[0] == total) adapter.notifyDataSetChanged();
                            }
                        });
            } else {
                plateList.add(plate);
                if (++done[0] == total) adapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * Recherche le nom d’un employé à partir d’une plaque.
     */
    private void findEmployeeName(String plate) {
        databaseReference.child("employees")
                .orderByChild("plate_number")
                .equalTo(plate)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snap) {
                        if (snap.exists()) {
                            for (DataSnapshot ds : snap.getChildren()) {
                                String name = ds.child("employee_name").getValue(String.class);
                                textViewEmployeeName.setText("Employé : " + (name != null ? name : "Inconnu"));
                                break;
                            }
                        } else {
                            textViewEmployeeName.setText("Employé : Non autorisé");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        textViewEmployeeName.setText("Erreur de chargement");
                    }
                });
    }

    /**
     * Gère les actions de menu (déconnexion, accueil, popup).
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.logout) {
            FirebaseAuth.getInstance().signOut();
            getSharedPreferences("ParkingAppPrefs", MODE_PRIVATE)
                    .edit().putBoolean("isLoggedIn", false).apply();
            Intent i = new Intent(DashboardActivity.this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            return true;
        } else if (id == R.id.home) {
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
            return true;
        } else if (id == R.id.menu1) {
            showPopupMenu(findViewById(R.id.menu1));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Affiche un menu contextuel avec les options employé / admin / historique.
     */
    private void showPopupMenu(View anchor) {
        PopupMenu popupMenu = new PopupMenu(this, anchor);
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
        popupMenu.show();
    }
    // Chargement du menu dans la toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }
}