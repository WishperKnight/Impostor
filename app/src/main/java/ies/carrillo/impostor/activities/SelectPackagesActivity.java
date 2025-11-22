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

// Implementamos la interfaz del adaptador y el click del grupo
public class SelectPackagesActivity extends AppCompatActivity
        implements PackagesExpandableAdapter.OnPackageActionListener, ExpandableListView.OnGroupClickListener {

    // Vistas
    private ExpandableListView expandablePackagesList;
    private MaterialButton btnConfirmPackages;

    // Adaptador
    private PackagesExpandableAdapter adapter;

    // Data para el juego
    // CRÍTICO: Inicializada como lista vacía, pero se reasigna en recuperarEstadoSeleccion()
    private List<Categoria> selectedCategories = new ArrayList<>();

    // Constantes de nombres
    private final String PACKAGE_CUSTOM_GROUP = "Personalizadas";

    // Launcher para volver de crear/editar paquete
    private ActivityResultLauncher<Intent> createPackageLauncher;

    // Variable temporal para saber si el resultado es de una edición
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

        inicializarVistas();
        setupActivityLauncher();

        // CORRECCIÓN CRÍTICA: Recuperar la lista de categorías que viene de MainActivity
        recuperarEstadoSeleccion();

        // Cargar datos y configurar la lista
        setupExpandableList();

        updateConfirmButton();
    }

    /**
     * Recupera la lista de categorías seleccionadas de MainActivity (si existe).
     */
    private void recuperarEstadoSeleccion() {
        Intent intent = getIntent();
        // Usamos la constante de clave de MainActivity
        Serializable categoriesExtra = intent.getSerializableExtra(MainActivity.KEY_SELECTED_CATEGORIES);

        if (categoriesExtra instanceof List) {
            // CRÍTICO: Asignamos la lista recibida a la variable de clase.
            // El adaptador usará esta misma referencia.
            this.selectedCategories = (List<Categoria>) categoriesExtra;
        } else {
            // Si no se recibió nada (o se recibió null), aseguramos que la lista sea una nueva y vacía.
            this.selectedCategories = new ArrayList<>();
        }
    }


    private void inicializarVistas() {
        expandablePackagesList = findViewById(R.id.expandable_packages_list);
        btnConfirmPackages = findViewById(R.id.btn_confirm_packages);

        btnConfirmPackages.setOnClickListener(v -> confirmSelection());
    }

    // --- Lógica del ExpandableListView ---

    private void setupExpandableList() {
        // Nombres de los Grupos (Orden Fijo: Predefinidas y Personalizadas)
        List<String> groupNames = Arrays.asList(
                DataBase.getInstance().getSemanaSanta().getName(), // "Semana Santa"
                DataBase.getInstance().getNaturaleza().getName(),  // "Naturaleza"
                PACKAGE_CUSTOM_GROUP
        );

        // Cargar datos del DB
        HashMap<String, List<Categoria>> groupChildren = getPackageData();

        // Crear el adaptador, pasando la lista de categorías seleccionadas.
        adapter = new PackagesExpandableAdapter(this, groupNames, groupChildren, selectedCategories, this);
        expandablePackagesList.setAdapter(adapter);

        // Configurar listeners
        expandablePackagesList.setOnGroupClickListener(this);

        // CORRECCIÓN: Forzar la expansión del grupo "Personalizadas" al inicio
        // para que la opción "CREAR NUEVO PAQUETE" siempre esté visible.
        int customGroupPos = adapter.getGroupPosition(PACKAGE_CUSTOM_GROUP);
        if (customGroupPos != -1) {
            expandablePackagesList.expandGroup(customGroupPos);
        }
    }

    /**
     * Carga las categorías del DataBase y las prepara para el adaptador.
     */
    private HashMap<String, List<Categoria>> getPackageData() {
        DataBase db = DataBase.getInstance();
        HashMap<String, List<Categoria>> data = new HashMap<>();

        // 1. Paquetes predefinidos (gestionados directamente en el GroupView)
        data.put(db.getSemanaSanta().getName(), new ArrayList<>());
        data.put(db.getNaturaleza().getName(), new ArrayList<>());

        // 2. Paquetes personalizados (los hijos son las categorías personalizadas)
        data.put(PACKAGE_CUSTOM_GROUP, db.getCustomCategories());

        return data;
    }

    /**
     * Recarga los datos del DataBase y notifica al adaptador para refrescar la UI.
     */
    private void reloadDataAndRefreshUI() {
        // 1. Obtener la nueva estructura de datos
        HashMap<String, List<Categoria>> newChildren = getPackageData();

        // 2. Actualizar el adaptador con los nuevos datos (incluye los personalizados)
        adapter.updateData(newChildren);

        // 3. Forzar la expansión del grupo de personalizadas
        int customGroupPos = adapter.getGroupPosition(PACKAGE_CUSTOM_GROUP);
        if (customGroupPos != -1) {
            expandablePackagesList.expandGroup(customGroupPos);
        }

        // 4. Actualizar el botón de confirmación
        updateConfirmButton();
    }

    // --- Implementación de OnGroupClickListener ---

    @Override
    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
        String groupName = (String) adapter.getGroup(groupPosition);

        // Permitir que el grupo 'Personalizadas' se expanda/colapse
        // Impedir expansión de los grupos predefinidos (solo usa el CheckBox dentro)
        return groupName.equals(PACKAGE_CUSTOM_GROUP);
    }


    // --- Implementación de OnPackageActionListener (Eventos de la Lista) ---

    @Override
    public void onGroupCheckboxToggled(String groupName, boolean isChecked) {
        DataBase db = DataBase.getInstance();
        // Buscar categoría predefinida
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
        // Usamos la constante del Activity
        intent.putExtra(CreatePackageActivity.CATEGORY_TO_EDIT_KEY, category);
        createPackageLauncher.launch(intent);
    }

    @Override
    public void onDeletePackageClicked(Categoria category) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_delete_title)) // Usar String Resource
                .setMessage(getString(R.string.dialog_delete_message, category.getName())) // Usar String Resource
                .setPositiveButton(getString(R.string.dialog_delete_positive), (dialog, which) -> {
                    DataBase.getInstance().deleteCustomCategory(category);
                    selectedCategories.remove(category); // Asegurar deselección
                    Toast.makeText(this, getString(R.string.toast_package_deleted, category.getName()), Toast.LENGTH_SHORT).show();
                    reloadDataAndRefreshUI();
                })
                .setNegativeButton(getString(R.string.dialog_delete_negative), null) // Usar String Resource
                .show();
    }

    @Override
    public void onAddNewPackageClicked() {
        currentEditingCategory = null; // Indicamos que es una creación nueva
        Intent intent = new Intent(this, CreatePackageActivity.class);
        createPackageLauncher.launch(intent);
    }

    // --- Lógica del Activity Launcher (Corregida) ---

    private void setupActivityLauncher() {
        // Usamos la constante de clave definida en CreatePackageActivity
        final String RETURN_KEY = CreatePackageActivity.CATEGORY_RETURNED_KEY;

        createPackageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // 1. Recuperar la Categoria devuelta (nueva o editada)
                        Categoria returnedCategory = (Categoria) result.getData()
                                .getSerializableExtra(RETURN_KEY);

                        if (returnedCategory == null) {
                            Toast.makeText(this, getString(R.string.toast_error_retrieving_package), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // 2. Si fue una CREACIÓN (currentEditingCategory == null), la seleccionamos automáticamente.
                        if (currentEditingCategory == null) {
                            if (!selectedCategories.contains(returnedCategory)) {
                                selectedCategories.add(returnedCategory);
                            }
                            Toast.makeText(this, getString(R.string.toast_new_package_selected, returnedCategory.getName()), Toast.LENGTH_SHORT).show();
                        } else {
                            // Es una EDICIÓN, solo notificamos
                            Toast.makeText(this, getString(R.string.toast_package_edited, returnedCategory.getName()), Toast.LENGTH_SHORT).show();
                        }

                        // 3. Recargar la lista expandible
                        reloadDataAndRefreshUI();
                        currentEditingCategory = null;

                    } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                        // El usuario canceló o usó el back button.
                        Toast.makeText(this, getString(R.string.toast_creation_cancelled), Toast.LENGTH_SHORT).show();
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

        btnConfirmPackages.setText(getString(R.string.btn_continue_packages, count));

        boolean enabled = count > 0;
        btnConfirmPackages.setEnabled(enabled);
        btnConfirmPackages.setAlpha(enabled ? 1.0f : 0.5f);
    }

    /**
     * Confirma la selección y pasa a la siguiente Activity.
     */
    private void confirmSelection() {
        if (selectedCategories.isEmpty()) {
            Toast.makeText(this, getString(R.string.toast_select_at_least_one_package), Toast.LENGTH_SHORT).show();
            return;
        }

        // Pasar la lista de categorías seleccionadas de vuelta a MainActivity
        Intent intent = new Intent();
        intent.putExtra(MainActivity.KEY_SELECTED_CATEGORIES, (Serializable) selectedCategories);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}