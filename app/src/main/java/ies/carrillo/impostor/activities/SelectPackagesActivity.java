package ies.carrillo.impostor.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import ies.carrillo.impostor.R;
import ies.carrillo.impostor.dataBase.DataBase;
import ies.carrillo.impostor.model.Categoria;
import ies.carrillo.impostor.adapters.PackagesExpandableAdapter;

// Implementamos la nueva interfaz del adaptador
public class SelectPackagesActivity extends AppCompatActivity
        implements PackagesExpandableAdapter.OnPackageActionListener, ExpandableListView.OnGroupClickListener {

    // Vistas
    private ExpandableListView expandablePackagesList; // ¡Nueva Vista!
    private MaterialButton btnConfirmPackages;

    // Adaptador
    private PackagesExpandableAdapter adapter;

    // Data para el juego
    private List<Categoria> selectedCategories;

    // Constantes de nombres
    private final String PACKAGE_SEMANA_SANTA = "Semana Santa";
    private final String PACKAGE_NATURALEZA = "Naturaleza";
    private final String PACKAGE_CUSTOM_GROUP = "Personalizadas";

    // Launcher para volver de crear paquete
    private ActivityResultLauncher<Intent> createPackageLauncher;

    // Launcher para editar paquete (para diferenciar si volvemos de crear vs editar)
    private Categoria currentEditingCategory = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_select_packages);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar la lista de selección, por defecto vacía.
        this.selectedCategories = new ArrayList<>();

        inicializarVistas();
        setupActivityLauncher();

        // Cargar datos y configurar la lista
        setupExpandableList();

        updateConfirmButton();
    }

    private void inicializarVistas() {
        // cardSemanaSanta, cbSemanaSanta, etc. ELIMINADOS
        expandablePackagesList = findViewById(R.id.expandable_packages_list);
        btnConfirmPackages = findViewById(R.id.btn_confirm_packages);

        btnConfirmPackages.setOnClickListener(v -> confirmSelection());
    }

    // --- Lógica del ExpandableListView ---

    private void setupExpandableList() {
        // 1. Nombres de los Grupos (Orden Fijo)
        List<String> groupNames = Arrays.asList(PACKAGE_SEMANA_SANTA, PACKAGE_NATURALEZA, PACKAGE_CUSTOM_GROUP);

        // 2. Mapeo de Grupos a Hijos
        HashMap<String, List<Categoria>> groupChildren = getPackageData();

        // 3. Crear el adaptador
        adapter = new PackagesExpandableAdapter(this, groupNames, groupChildren, selectedCategories, this);
        expandablePackagesList.setAdapter(adapter);

        // 4. Configurar listeners
        expandablePackagesList.setOnGroupClickListener(this);
    }

    /**
     * Carga las categorías del DataBase y las prepara para el adaptador.
     */
    private HashMap<String, List<Categoria>> getPackageData() {
        DataBase db = DataBase.getInstance();
        HashMap<String, List<Categoria>> data = new HashMap<>();

        // 1. Paquetes predefinidos (los hijos están vacíos, se gestionan en el GroupView)
        data.put(PACKAGE_SEMANA_SANTA, new ArrayList<>());
        data.put(PACKAGE_NATURALEZA, new ArrayList<>());

        // 2. Paquetes personalizados (los hijos son las categorías personalizadas)
        // ASUMIMOS que DataBase tiene un método getCustomCategories()
        List<Categoria> customCats = db.getCustomCategories();
        data.put(PACKAGE_CUSTOM_GROUP, customCats);

        // 3. Inicializar selectedCategories.
        // Si el DataBase guarda un estado de selección inicial, cargar aquí.
        // Si no, la lista está vacía por defecto.

        return data;
    }

    /**
     * Recarga los datos y notifica al adaptador.
     */
    private void reloadDataAndRefreshUI() {
        HashMap<String, List<Categoria>> newChildren = getPackageData();
        adapter.updateData(newChildren);
        updateConfirmButton();
    }

    // --- Implementación de OnGroupClickListener ---

    @Override
    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
        String groupName = (String) adapter.getGroup(groupPosition);

        // Permitir que el grupo 'Personalizadas' se expanda/colapse
        // Impedir expansión de los grupos predefinidos (solo usa el CheckBox dentro)
        return !groupName.equals(PACKAGE_CUSTOM_GROUP);
    }


    // --- Implementación de OnPackageActionListener (Eventos de la Lista) ---

    @Override
    public void onGroupCheckboxToggled(String groupName, boolean isChecked) {
        DataBase db = DataBase.getInstance();
        // Encuentra la categoría predefinida en la lista principal del DB
        Categoria category = db.getCategorias().stream()
                .filter(cat -> cat.getName().equals(groupName))
                .findFirst().orElse(null);

        if (category == null) return;

        if (isChecked) {
            if (!selectedCategories.contains(category)) {
                selectedCategories.add(category);
            }
        } else {
            selectedCategories.remove(category);
        }
        updateConfirmButton();
        // No refrescamos el adaptador aquí porque solo se actualiza la casilla.
    }

    @Override
    public void onChildCheckboxToggled(Categoria category, boolean isChecked) {
        if (isChecked) {
            if (!selectedCategories.contains(category)) {
                selectedCategories.add(category);
            }
        } else {
            selectedCategories.remove(category);
        }
        updateConfirmButton();
    }

    @Override
    public void onEditPackageClicked(Categoria category) {
        currentEditingCategory = category; // Guardamos la categoría que estamos editando
        Intent intent = new Intent(this, CreatePackageActivity.class);
        // Pasamos la categoría existente para que la Activity la cargue
        intent.putExtra("CATEGORY_TO_EDIT", category);
        createPackageLauncher.launch(intent);
    }

    @Override
    public void onDeletePackageClicked(Categoria category) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar Borrado")
                .setMessage("¿Estás seguro de que deseas borrar permanentemente el paquete '" + category.getName() + "'?")
                .setPositiveButton("BORRAR", (dialog, which) -> {
                    // ASUMIMOS que DataBase tiene un método deleteCustomCategory()
                    DataBase.getInstance().deleteCustomCategory(category);
                    selectedCategories.remove(category); // Asegurarse de que se deselecciona si estaba seleccionada
                    Toast.makeText(this, "Paquete '" + category.getName() + "' borrado.", Toast.LENGTH_SHORT).show();
                    reloadDataAndRefreshUI();
                })
                .setNegativeButton("CANCELAR", null)
                .show();
    }

    @Override
    public void onAddNewPackageClicked() {
        currentEditingCategory = null; // Indicamos que es una creación nueva
        Intent intent = new Intent(this, CreatePackageActivity.class);
        createPackageLauncher.launch(intent);
    }

    // --- Lógica del Activity Launcher ---

    private void setupActivityLauncher() {
        createPackageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // El paquete (ya sea nuevo o editado) debe guardarse en el DataBase en CreatePackageActivity.

                        // Si era una edición, debemos recargar los datos
                        if (currentEditingCategory != null) {
                            Toast.makeText(this, "Paquete editado y guardado.", Toast.LENGTH_SHORT).show();
                            // currentEditingCategory ya apunta al objeto en selectedCategories/DB, solo refrescamos la lista
                        } else {
                            Toast.makeText(this, "Nuevo paquete creado y guardado.", Toast.LENGTH_SHORT).show();
                            // Si es un paquete nuevo, asumimos que DataBase lo añade a su lista.
                            // Si deseamos seleccionarlo automáticamente al crearlo:
                            // Categoria newCat = // (Forma de obtener el nuevo paquete si el DB no lo retorna inmediatamente)
                            // selectedCategories.add(newCat);
                        }

                        // Recargamos y actualizamos la vista
                        reloadDataAndRefreshUI();
                        currentEditingCategory = null;
                    }
                }
        );
    }

    // --- Lógica Final ---

    /**
     * Actualiza el texto y estado del botón Confirmar.
     */
    private void updateConfirmButton() {
        int count = selectedCategories.size();

        btnConfirmPackages.setText("Continuar (" + count + " Paquetes)");

        boolean enabled = count > 0;
        btnConfirmPackages.setEnabled(enabled);
        btnConfirmPackages.setAlpha(enabled ? 1.0f : 0.5f);
    }

    /**
     * Confirma la selección y pasa a la siguiente Activity.
     */
    private void confirmSelection() {
        if (selectedCategories.isEmpty()) {
            Toast.makeText(this, "Selecciona al menos un paquete.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Pasar la lista de categorías seleccionadas a la siguiente Activity
        Intent intent = new Intent(SelectPackagesActivity.this, TimeSelectorActivity.class);
        intent.putExtra("CATEGORIAS_SELECCIONADAS", (Serializable) selectedCategories);
        startActivity(intent);

        finish();
    }
}