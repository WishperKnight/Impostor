package ies.carrillo.impostor.dataBase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ies.carrillo.impostor.model.Categoria;
import ies.carrillo.impostor.model.Jugador;

/**
 * Singleton para gestionar los datos persistentes de la aplicación:
 * Jugadores, Categorías Predefinidas y Categorías Personalizadas.
 */
public class DataBase {

    // --- PATRÓN SINGLETON ---
    private static DataBase instance;

    public static DataBase getInstance() {
        if (instance == null) {
            instance = new DataBase();
        }
        return instance;
    }
    // ------------------------

    // --- Datos Principales ---
    private final ArrayList<Jugador> jugadores = new ArrayList<>();
    private final ArrayList<Categoria> predefinidas = new ArrayList<>();
    // Lista mutable para las categorías personalizadas creadas por el usuario.
    private final List<Categoria> customCategories = new ArrayList<>();

    private Categoria semanaSanta;
    private Categoria naturaleza;

    // Constructor privado para forzar el uso de getInstance()
    private DataBase() {
        inicializarCategorias();
    }

    private void inicializarCategorias() {
        // --- 1. Crear el objeto Semana Santa ---
        HashMap<String, String> palabrasSemanaSanta = new HashMap<>();
        palabrasSemanaSanta.put("Nazareno", "Capirotes");
        palabrasSemanaSanta.put("Trono", "Costaleros");
        palabrasSemanaSanta.put("Mantilla", "Velo");
        palabrasSemanaSanta.put("Capataces", "Llamador");
        palabrasSemanaSanta.put("Saeta", "Canto");
        palabrasSemanaSanta.put("Incienso", "Cera");
        palabrasSemanaSanta.put("Cruz", "Guía");
        palabrasSemanaSanta.put("Palio", "Bambalinas");
        palabrasSemanaSanta.put("Cuaresma", "Penitencia");
        palabrasSemanaSanta.put("Misterio", "Pasaje");

        semanaSanta = new Categoria(
                "Semana Santa",
                palabrasSemanaSanta
        );

        // --- 2. Crear el objeto Naturaleza ---
        HashMap<String, String> palabrasNaturaleza = new HashMap<>();
        palabrasNaturaleza.put("Fotosíntesis", "Clorofila");
        palabrasNaturaleza.put("Ecosistema", "Habitat");
        palabrasNaturaleza.put("Lluvia", "Nubes");
        palabrasNaturaleza.put("Árbol", "Raíces");
        palabrasNaturaleza.put("Biodiversidad", "Vida");
        palabrasNaturaleza.put("Atmósfera", "Aire");
        palabrasNaturaleza.put("Océano", "Agua");
        palabrasNaturaleza.put("Volcán", "Magma");
        palabrasNaturaleza.put("Glaciar", "Hielo");
        palabrasNaturaleza.put("Desierto", "Arena");

        naturaleza = new Categoria(
                "Naturaleza",
                palabrasNaturaleza
        );

        // --- 3. Añadir las categorías predefinidas a su lista ---
        predefinidas.add(semanaSanta);
        predefinidas.add(naturaleza);
    }

    // --- MÉTODOS DE JUGADORES ---

    public ArrayList<Jugador> getJugadores() {
        return jugadores;
    }

    public void setJugadores(ArrayList<Jugador> nuevosJugadores) {
        this.jugadores.clear();
        this.jugadores.addAll(nuevosJugadores);
    }

    // --- MÉTODOS DE CATEGORÍAS PREDEFINIDAS ---

    public ArrayList<Categoria> getCategorias() {
        return predefinidas;
    }

    public Categoria getSemanaSanta() {
        return semanaSanta;
    }

    public Categoria getNaturaleza() {
        return naturaleza;
    }

    // --- MÉTODOS PARA CATEGORÍAS PERSONALIZADAS MÚLTIPLES ---

    public List<Categoria> getCustomCategories() {
        return customCategories;
    }

    /**
     * Guarda o actualiza una categoría personalizada.
     */
    public void saveCustomCategory(Categoria categoria) {
        // Eliminar la versión antigua si existe (comparando por nombre gracias a equals/hashCode)
        customCategories.remove(categoria);

        // Añadir el nuevo o el actualizado (la instancia actual)
        customCategories.add(categoria);
    }

    /**
     * Elimina una categoría personalizada de la lista.
     */
    public void deleteCustomCategory(Categoria category) {
        customCategories.remove(category);
    }
}