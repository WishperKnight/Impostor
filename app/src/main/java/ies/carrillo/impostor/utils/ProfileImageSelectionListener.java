
package ies.carrillo.impostor.utils; // Ajusta el paquete si es necesario

public interface ProfileImageSelectionListener {

    /**
     * Se llama cuando el usuario selecciona una imagen de perfil.
     *
     * @param drawableId El ID del recurso drawable (R.drawable.xxx) seleccionado.
     */
    void onProfileImageSelected(int drawableId);
}