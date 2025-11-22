package ies.carrillo.impostor.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import ies.carrillo.impostor.R;
import ies.carrillo.impostor.adapters.WordPairAdapter; // Importar el nuevo adaptador
import ies.carrillo.impostor.dataBase.DataBase;
import ies.carrillo.impostor.model.Categoria;

public class CreatePackageActivity extends AppCompatActivity
        implements WordPairAdapter.OnPairActionListener { // Implementar interfaz para borrar pares

    private TextInputEditText etPackageName;
    private TextInputEditText etSecretWord;
    private TextInputEditText etImpostorClue;
    private MaterialButton btnSavePackage;
    private MaterialButton btnAddWord;
    private RecyclerView rvWordPairs;
    private TextView tvTitle;
    private TextView tvEmptyList;

    private WordPairAdapter wordPairAdapter;

    private Categoria categoryToEdit = null;
    // categoryWords es el HashMap de Palabras/Pistas que se guarda en el objeto Categoria.
    private HashMap<String, String> categoryWords;

    public static final String CATEGORY_TO_EDIT_KEY = "CATEGORY_TO_EDIT";
    public static final String CATEGORY_RETURNED_KEY = "CATEGORY_RETURNED";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_createpackage);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        inicializarVistas();
        loadPackageForEditing();
        configurarRecyclerView();
        configurarListeners();

        // Actualizar el estado inicial de la lista
        updateWordListUI();
    }

    private void inicializarVistas() {
        tvTitle = findViewById(R.id.tv_title);
        etPackageName = findViewById(R.id.et_package_name);
        etSecretWord = findViewById(R.id.et_secret_word);
        etImpostorClue = findViewById(R.id.et_impostor_clue);
        btnSavePackage = findViewById(R.id.btn_save_package);
        btnAddWord = findViewById(R.id.btn_add_word); // Nuevo botón
        rvWordPairs = findViewById(R.id.rv_word_pairs); // Nuevo RecyclerView
        tvEmptyList = findViewById(R.id.tv_empty_list); // Texto de lista vacía
    }

    private void configurarRecyclerView() {
        // Convertir el HashMap a una lista de claves (Palabras Secretas) para el adaptador
        List<String> secretWordsList = new ArrayList<>(categoryWords.keySet());

        wordPairAdapter = new WordPairAdapter(this, categoryWords, secretWordsList, this);
        rvWordPairs.setLayoutManager(new LinearLayoutManager(this));
        rvWordPairs.setAdapter(wordPairAdapter);
    }

    private void configurarListeners() {
        btnSavePackage.setOnClickListener(v -> savePackageAndFinish());
        btnAddWord.setOnClickListener(v -> addWordToPackage()); // Nuevo listener para añadir
    }

    private void loadPackageForEditing() {
        Serializable extra = getIntent().getSerializableExtra(CATEGORY_TO_EDIT_KEY);
        String titleText;

        if (extra instanceof Categoria) {
            this.categoryToEdit = (Categoria) extra;
            this.categoryWords = new HashMap<>(this.categoryToEdit.getPalabras());

            etPackageName.setText(this.categoryToEdit.getName());
            etPackageName.setEnabled(false); // No se permite cambiar el nombre al editar

            // Actualizar la interfaz de edición/creación
            btnSavePackage.setText(getString(R.string.btn_save_changes));
            titleText = getString(R.string.title_edit_package, this.categoryToEdit.getName());

            // Si hay palabras, mostrar el primer par en los campos de texto
            if (!this.categoryWords.isEmpty()) {
                String firstWord = Objects.requireNonNull(this.categoryWords.keySet().iterator().next());
                etSecretWord.setText(firstWord);
                etImpostorClue.setText(this.categoryWords.get(firstWord));
            }
        } else {
            this.categoryWords = new HashMap<>();
            btnSavePackage.setText(getString(R.string.btn_save_package));
            titleText = getString(R.string.title_create_new_package);
        }

        tvTitle.setText(titleText);
    }

    /**
     * Valida y añade el par Palabra/Pista a la lista del paquete (no guarda en DB aún).
     */
    private void addWordToPackage() {
        // 1. Obtener y limpiar los textos
        String secretWord = Objects.requireNonNull(etSecretWord.getText()).toString().trim();
        String impostorClue = Objects.requireNonNull(etImpostorClue.getText()).toString().trim();

        // Validaciones
        if (TextUtils.isEmpty(secretWord) || TextUtils.isEmpty(impostorClue)) {
            Toast.makeText(this, "Por favor, ingresa la Palabra Secreta y la Pista.", Toast.LENGTH_LONG).show();
            return;
        }
        if (secretWord.equalsIgnoreCase(impostorClue)) {
            Toast.makeText(this, "La Palabra Secreta y la Pista no pueden ser iguales.", Toast.LENGTH_LONG).show();
            return;
        }

        // 2. Añadir/Sobreescribir el par en el HashMap
        categoryWords.put(secretWord, impostorClue);

        // 3. Notificar al adaptador y limpiar campos de texto
        wordPairAdapter.updateWords(new ArrayList<>(categoryWords.keySet()));
        etSecretWord.setText("");
        etImpostorClue.setText("");

        updateWordListUI();

        Toast.makeText(this, "Par '" + secretWord + "' añadido al paquete.", Toast.LENGTH_SHORT).show();
    }

    /**
     * Actualiza la visibilidad de la lista vs. el mensaje de lista vacía.
     */
    private void updateWordListUI() {
        if (categoryWords.isEmpty()) {
            rvWordPairs.setVisibility(View.GONE);
            tvEmptyList.setVisibility(View.VISIBLE);
        } else {
            rvWordPairs.setVisibility(View.VISIBLE);
            tvEmptyList.setVisibility(View.GONE);
        }
    }

    /**
     * Implementación de la interfaz para borrar un par de palabras.
     */
    @Override
    public void onDeletePairClicked(String secretWord) {
        if (categoryWords.containsKey(secretWord)) {
            categoryWords.remove(secretWord);
            wordPairAdapter.updateWords(new ArrayList<>(categoryWords.keySet()));
            updateWordListUI();
            Toast.makeText(this, "Par '" + secretWord + "' eliminado del paquete.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Valida los campos, guarda/actualiza la categoría en la Base de Datos, devuelve el objeto y cierra la actividad.
     */
    private void savePackageAndFinish() {
        // 1. Obtener el nombre del paquete
        String packageName = Objects.requireNonNull(etPackageName.getText()).toString().trim();

        // 2. Validación final
        if (TextUtils.isEmpty(packageName)) {
            Toast.makeText(this, "El nombre del paquete no puede estar vacío.", Toast.LENGTH_LONG).show();
            return;
        }
        if (categoryWords.isEmpty()) {
            Toast.makeText(this, "El paquete debe contener al menos un par de Palabra/Pista.", Toast.LENGTH_LONG).show();
            return;
        }

        // 3. Crear o Actualizar el Objeto Categoria
        Categoria finalCategory;
        if (categoryToEdit != null) {
            finalCategory = categoryToEdit;
            finalCategory.setPalabras(categoryWords);
            Toast.makeText(this, "Paquete '" + packageName + "' actualizado correctamente.", Toast.LENGTH_SHORT).show();
        } else {
            finalCategory = new Categoria(packageName, categoryWords);
            Toast.makeText(this, "Paquete '" + packageName + "' creado correctamente.", Toast.LENGTH_SHORT).show();
        }

        // 4. GUARDAR EN LA BASE DE DATOS
        DataBase.getInstance().saveCustomCategory(finalCategory);

        // 5. Devolver el objeto guardado
        Intent resultIntent = new Intent();
        resultIntent.putExtra(CATEGORY_RETURNED_KEY, finalCategory);

        // 6. Cerrar
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }
}