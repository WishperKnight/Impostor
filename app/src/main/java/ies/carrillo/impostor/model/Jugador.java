package ies.carrillo.impostor.model;

import java.io.Serializable;

public class Jugador implements Serializable {
    private String name;
    private String role; // Rol: Civil o Impostor
    private String colorHex; // Nuevo campo: Color en formato Hex
    private String profileImageUri = null;

    // Constructor que acepta el nombre y asigna un color por defecto
    public Jugador(String name) {
        this.name = name;
        this.role = "Civil"; // Rol por defecto
        this.colorHex = "#FF4081"; // Color Rosa/Magenta por defecto (ejemplo)
        this.profileImageUri=null;
    }

    // Constructor completo (si lo necesitas)
    public Jugador(String name, String role, String colorHex) {
        this.name = name;
        this.role = role;
        this.colorHex = colorHex;
    }

    // --- Getters y Setters ---


    public String getProfileImageUri() {
        return profileImageUri;
    }

    public void setProfileImageUri(String profileImageUri) {
        this.profileImageUri = profileImageUri;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getColorHex() { // ¡Nuevo Getter!
        return colorHex;
    }

    public void setColorHex(String colorHex) { // ¡Nuevo Setter!
        this.colorHex = colorHex;
    }
}