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
import java.util.List;

public class ViewEmployeesActivity extends AppCompatActivity {

    RecyclerView recyclerView;                    // Affichage des employés dans une liste
    List<Employee> employeeList;                  // Liste contenant tous les employés
    EmployeeAdapter adapter;                      // Adaptateur personnalisé pour afficher les employés
    DatabaseReference databaseRef;                // Référence à la base de données Firebase
    private Toolbar toolbar;                      // Barre d'outils (Toolbar)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_employees);

        // Gestion du padding pour s'adapter aux barres système (haut/bas)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialisation de la Toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Configuration de la RecyclerView
        recyclerView = findViewById(R.id.recyclerViewEmployees);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialisation de la liste et de l'adaptateur
        employeeList = new ArrayList<>();
        adapter = new EmployeeAdapter(this, employeeList, new EmployeeAdapter.OnItemClickListener() {
            @Override
            public void onUpdate(Employee employee) {
                openUpdateEmployeeActivity(employee);
            }

            @Override
            public void onDelete(Employee employee) {
                deleteEmployee(employee);
            }
        });

        recyclerView.setAdapter(adapter);

        // Connexion à la base Firebase "employees"
        databaseRef = FirebaseDatabase.getInstance().getReference("employees");

        // Chargement des employés depuis Firebase
        loadEmployees();
    }

    // Méthode pour charger les employés depuis Firebase
    private void loadEmployees() {
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                employeeList.clear(); // Vider la liste avant de la remplir à nouveau
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Employee employee = dataSnapshot.getValue(Employee.class);
                    if (employee != null) {
                        employeeList.add(employee);
                    }
                }
                adapter.notifyDataSetChanged(); // Rafraîchir l'affichage
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ViewEmployeesActivity.this, "Erreur de chargement", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Ouvre l'écran de mise à jour d'un employé
    private void openUpdateEmployeeActivity(Employee employee) {
        Intent intent = new Intent(ViewEmployeesActivity.this, UpdateEmployeeActivity.class);
        intent.putExtra("name", employee.getEmployee_name());
        intent.putExtra("plate", employee.getPlate_number());
        startActivity(intent);
    }

    // Supprime un employé via son numéro d'immatriculation
    void deleteEmployee(Employee employee) {
        databaseRef.orderByChild("plate_number").equalTo(employee.getPlate_number())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot empSnapshot : snapshot.getChildren()) {
                            empSnapshot.getRef().removeValue(); // Suppression dans Firebase
                            Toast.makeText(ViewEmployeesActivity.this, "Employé supprimé", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ViewEmployeesActivity.this, "Erreur de suppression", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Gère les options du menu dans la Toolbar (ex: déconnexion, accueil)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.logout) {
            // 1. Déconnexion de Firebase
            FirebaseAuth.getInstance().signOut();

            // 2. Marquer l'utilisateur comme déconnecté (SharedPreferences)
            getSharedPreferences("ParkingAppPrefs", MODE_PRIVATE)
                    .edit()
                    .putBoolean("isLoggedIn", false)
                    .apply();

            // 3. Redirection vers l'écran de login
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
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

    // Affiche un menu contextuel (Popup) avec différentes options
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
            } else if (id == R.id.history) {
                startActivity(new Intent(this, HistoryActivity.class));
                return true;
            }
            return false;
        });

        // Forcer l'affichage des icônes du menu (hack avec réflexion Java)
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

    // Permet d'afficher le menu dans la Toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }
}
