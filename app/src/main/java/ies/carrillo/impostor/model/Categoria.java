package ies.carrillo.impostor.model;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects; // Importar java.util.Objects
import java.util.Random;

public class Categoria implements Serializable {
    private String name, word, clue;
    @NotNull
    private HashMap<String,String> palabras;

    public Categoria(String name,  HashMap<String, String> palabras) {
        this.name = name;
        this.word = null;
        this.clue = null;
        this.palabras = palabras;
    }

    /**
     * Selecciona una palabra y su pista de forma aleatoria del HashMap.
     */
    public void seleccionarPalabraAleatoria() {
        if (palabras.isEmpty()) {
            this.word = "ERROR";
            this.clue = "VACÍO";
            return;
        }
        // 1. Convertir las claves a una lista para acceder por índice
        ArrayList<String> keys = new ArrayList<>(palabras.keySet());

        // 2. Elegir un índice aleatorio
        Random random = new Random();
        int randomIndex = random.nextInt(keys.size());

        // 3. Obtener la palabra clave y su pista asociada
        this.word = keys.get(randomIndex);
        this.clue = palabras.get(word);
    }

    // ******************************************************
    // EQUALS Y HASHCODE (CORRECCIÓN CRÍTICA)
    // ******************************************************

    /**
     * Dos Categorías son iguales si tienen el mismo nombre.
     * Esto es crucial para la selección en Listas (e.g., selectedCategories.contains()).
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Categoria categoria = (Categoria) o;
        // Solo necesitamos que el nombre coincida para identificar la categoría
        return Objects.equals(name, categoria.name);
    }

    @Override
    public int hashCode() {
        // Generar el hashCode basado únicamente en el nombre
        return Objects.hash(name);
    }

    //******************************************************
    // GETTERS Y SETTERS
    //******************************************************


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getClue() {
        return clue;
    }

    public void setClue(String clue) {
        this.clue = clue;
    }

    public HashMap<String, String> getPalabras() {
        return palabras;
    }

    public void setPalabras(HashMap<String, String> palabras) {
        this.palabras = palabras;
    }
}