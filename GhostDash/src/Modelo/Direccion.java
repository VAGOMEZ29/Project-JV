
package modelo;

import java.util.Random;

/**
 * Enumeración que define las posibles direcciones de movimiento en el juego.
 * Cada dirección tiene un cambio en las coordenadas X (dx) y Y (dy).
 */
public enum Direccion {
    ARRIBA(0, -1), // x no cambia, y disminuye
    ABAJO(0, 1), // x no cambia, y aumenta
    IZQUIERDA(-1, 0), // x disminuye, y no cambia
    DERECHA(1, 0); // x aumenta, y no cambia

    private final int dx; // Cambio en la coordenada X
    private final int dy; // Cambio en la coordenada Y

    /**
     * Constructor para la enumeración Direccion.
     * 
     * @param dx Cambio en X para esta dirección.
     * @param dy Cambio en Y para esta dirección.
     */
    Direccion(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    // --- Getters ---
    public int getDx() {
        return dx;
    }

    public int getDy() {
        return dy;
    }

    /**
     * Devuelve la dirección opuesta a la dirección actual.
     * Útil para lógica de IA de fantasmas (ej. evitar dar la vuelta
     * inmediatamente).
     * 
     * @return La dirección invertida.
     */
    public Direccion invertir() {
        return switch (this) {
            case ARRIBA -> ABAJO;
            case ABAJO -> ARRIBA;
            case IZQUIERDA -> DERECHA;
            case DERECHA -> IZQUIERDA;
        };
    }

    /**
     * Genera una dirección aleatoria de las disponibles.
     * 
     * @return Una dirección seleccionada al azar.
     */
    public static Direccion aleatoria() {
        Direccion[] direcciones = values(); // Obtiene todas las direcciones de la enum
        return direcciones[new Random().nextInt(direcciones.length)]; // Selecciona una al azar
    }
}