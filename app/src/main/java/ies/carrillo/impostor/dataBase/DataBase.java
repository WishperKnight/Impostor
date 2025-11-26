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

    // --- NUEVAS CATEGORÍAS ---
    private Categoria videojuegos;
    private Categoria cineTv;
    private Categoria comida;

    // Constructor privado para forzar el uso de getInstance()
    private DataBase() {
        inicializarCategorias();
    }

    private void inicializarCategorias() {
        // --- 1. Crear el objeto Semana Santa (EXISTENTE) ---
        HashMap<String, String> palabrasSemanaSanta = new HashMap<>();
        palabrasSemanaSanta.put("Nazareno", "Capirotes");
        palabrasSemanaSanta.put("Trono", "Costaleros");
        palabrasSemanaSanta.put("Mantilla", "Velo");
        palabrasSemanaSanta.put("Capataces", "Llamador");
        palabrasSemanaSanta.put("Saeta", "Canto");
        palabrasSemanaSanta.put("Incienso", "Humo");
        palabrasSemanaSanta.put("Cruz", "Guía");
        palabrasSemanaSanta.put("Palio", "Bambalinas");
        palabrasSemanaSanta.put("Cuaresma", "Penitencia");
        palabrasSemanaSanta.put("Misterio", "Pasaje");

        palabrasSemanaSanta.put("Domingo", "Ramos");
        palabrasSemanaSanta.put("Madrugá", "Silencio");
        palabrasSemanaSanta.put("Pasos", "Flores");
        palabrasSemanaSanta.put("Marcha", "Procesional");
        palabrasSemanaSanta.put("Orfebrería", "Plata");
        palabrasSemanaSanta.put("Semana", "Mayor");
        palabrasSemanaSanta.put("Estación", "Penitencia");
        palabrasSemanaSanta.put("Carrera", "Oficial");
        palabrasSemanaSanta.put("Banda", "Música");
        palabrasSemanaSanta.put("Jesús", "Cristo");

        palabrasSemanaSanta.put("Claveles", "Flores");
        palabrasSemanaSanta.put("Manolas", "Dolorosa");
        palabrasSemanaSanta.put("Cirio", "Luz");
        palabrasSemanaSanta.put("Penitente", "Promesa");
        palabrasSemanaSanta.put("Bulla", "Gentío");
        palabrasSemanaSanta.put("Madera", "Talla");
        palabrasSemanaSanta.put("Guión", "Estandarte");
        palabrasSemanaSanta.put("Prioste", "Mayordomo"); // Se cambió "Fiscal" por "Mayordomo" para variedad
        palabrasSemanaSanta.put("Silencio", "Recogimiento");
        palabrasSemanaSanta.put("Capilla", "Iglesia");

        palabrasSemanaSanta.put("Triduo", "Pascual");
        palabrasSemanaSanta.put("Sagrario", "Monumento");
        palabrasSemanaSanta.put("Pregón", "Anuncio");
        palabrasSemanaSanta.put("Bocinas", "Heráldicas");
        palabrasSemanaSanta.put("Medalla", "Hermandad");
        palabrasSemanaSanta.put("Altar", "Culto");
        palabrasSemanaSanta.put("Fiscal", "Recorrido"); // Nuevo par
        palabrasSemanaSanta.put("Vía", "Crucis");
        palabrasSemanaSanta.put("Lágrimas", "Dolorosa");
        palabrasSemanaSanta.put("Viernes", "Santo");

        semanaSanta = new Categoria(
                "Semana Santa",
                palabrasSemanaSanta
        );

        // --- 2. Crear el objeto Naturaleza (EXISTENTE) ---
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

        // --- 3. Crear el objeto Videojuegos (NUEVA) ---
        HashMap<String, String> palabrasVideojuegos = new HashMap<>();
        palabrasVideojuegos.put("Multijugador", "Servidor");
        palabrasVideojuegos.put("Consola", "Mando");
        palabrasVideojuegos.put("Gráficos", "Renderizado");
        palabrasVideojuegos.put("Personaje", "Avatar");
        palabrasVideojuegos.put("Misión", "Objetivo");
        palabrasVideojuegos.put("Aventura", "Exploración");
        palabrasVideojuegos.put("E-Sports", "Torneo");
        palabrasVideojuegos.put("Realidad Virtual", "Gafas");
        palabrasVideojuegos.put("Indie", "Desarrollador");
        palabrasVideojuegos.put("Mapa", "Navegación");

        videojuegos = new Categoria(
                "Videojuegos",
                palabrasVideojuegos
        );

        // --- 4. Crear el objeto Cine y TV (NUEVA) ---
        HashMap<String, String> palabrasCineTv = new HashMap<>();
        palabrasCineTv.put("Director", "Cámara");
        palabrasCineTv.put("Guion", "Diálogo");
        palabrasCineTv.put("Estreno", "Cartelera");
        palabrasCineTv.put("Secuela", "Original");
        palabrasCineTv.put("Teatro", "Escenario");
        palabrasCineTv.put("Streaming", "Plataforma");
        palabrasCineTv.put("Óscar", "Estatua");
        palabrasCineTv.put("Banda Sonora", "Música");
        palabrasCineTv.put("Efectos Especiales", "CGI");
        palabrasCineTv.put("Documental", "Realidad");

        cineTv = new Categoria(
                "Cine y TV",
                palabrasCineTv
        );

        // --- 5. Crear el objeto Comida (NUEVA) ---
        HashMap<String, String> palabrasComida = new HashMap<>();
        palabrasComida.put("Chef", "Receta");
        palabrasComida.put("Ingrediente", "Sabor");
        palabrasComida.put("Vegetariano", "Verdura");
        palabrasComida.put("Especias", "Aroma");
        palabrasComida.put("Desayuno", "Mañana");
        palabrasComida.put("Postre", "Dulce");
        palabrasComida.put("Cena", "Noche");
        palabrasComida.put("Marinado", "Carne");
        palabrasComida.put("Alérgeno", "Riesgo");
        palabrasComida.put("Dieta", "Salud");

        comida = new Categoria(
                "Comida",
                palabrasComida
        );

        // --- 6. Añadir TODAS las categorías predefinidas a su lista ---
        predefinidas.add(semanaSanta);
        predefinidas.add(naturaleza);
        predefinidas.add(videojuegos); // NUEVO
        predefinidas.add(cineTv);      // NUEVO
        predefinidas.add(comida);      // NUEVO
    }

    // --- MÉTODOS DE JUGADORES (SIN CAMBIOS) ---

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

    // --- NUEVOS GETTERS ESPECÍFICOS ---

    public Categoria getVideojuegos() {
        return videojuegos;
    }

    public Categoria getCineTv() {
        return cineTv;
    }

    public Categoria getComida() {
        return comida;
    }

    // --- MÉTODOS PARA CATEGORÍAS PERSONALIZADAS MÚLTIPLES (SIN CAMBIOS) ---

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