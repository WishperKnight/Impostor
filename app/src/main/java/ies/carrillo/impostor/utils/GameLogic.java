package ies.carrillo.impostor.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import ies.carrillo.impostor.model.Jugador;
import ies.carrillo.impostor.roles.Roles; // ⬅️ IMPORTACIÓN NECESARIA

public class GameLogic {

    /**
     * Asigna roles de 'Impostor' o 'Tripulante' aleatoriamente a la lista de jugadores.
     * @param jugadores Lista de jugadores (debe ser la lista final de la partida).
     * @param numImpostores Cantidad de impostores a asignar.
     * @return La lista de jugadores con los roles ya asignados.
     */
    public static List<Jugador> assignRoles(List<Jugador> jugadores, int numImpostores) {
        if (jugadores == null || jugadores.size() < numImpostores) {
            // Manejar error o retornar lista si no hay suficientes jugadores
            return jugadores;
        }

        // 1. Crear una lista de índices para seleccionar los impostores
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < jugadores.size(); i++) {
            indices.add(i);
            // Establecer el rol por defecto (Tripulante/CIVIL)
            jugadores.get(i).setRol(Roles.CIVIL); // ⬅️ CORRECCIÓN: Usar Roles.CIVIL
        }

        // 2. Mezclar los índices para seleccionar aleatoriamente
        // Nota: Es mejor usar un nuevo Random si se llama múltiples veces en corto tiempo
        Collections.shuffle(indices, new Random(System.currentTimeMillis()));

        // 3. Asignar el rol de 'Impostor' a los primeros 'numImpostores' índices
        for (int i = 0; i < numImpostores; i++) {
            int indiceImpostor = indices.get(i);
            jugadores.get(indiceImpostor).setRol(Roles.IMPOSTOR); // ⬅️ CORRECCIÓN: Usar Roles.IMPOSTOR
        }

        // 4. Retornar la lista con los roles asignados
        return jugadores;
    }


}