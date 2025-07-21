// Archivo nuevo: MotorPartida.java
package controlador;

import modelo.*;
import java.util.List;

/**
 * MotorPartida contiene toda la lógica pura y las reglas del juego.
 * No guarda estado, solo recibe los elementos de la partida y los modifica
 * según las reglas de interacción (colisiones, comer puntos, etc.).
 */
public class MotorPartida {

    /**
     * Verifica y procesa todas las interacciones de Pac-Man con los elementos del
     * nivel.
     * 
     * @return true si Pac-Man ha muerto en esta verificación, false en caso
     *         contrario.
     */
    public boolean verificarInteracciones(PacMan pacman, List<Punto> puntos, List<Fantasma> fantasmas,
            List<PowerUp> powerUps, List<Fruta> frutas, SoundManager soundManager) {

        // 1. Verificar si come puntos
        puntos.removeIf(p -> {
            if (pacman.getPosicion().equals(p.getPosicion())) {
                // Aquí podrías incrementar la puntuación. Por ahora, solo el sonido.
                soundManager.reproducirComer();
                return true;
            }
            return false;
        });

        // 2. Verificar si come Power-Ups
        powerUps.removeIf(p -> {
            if (pacman.getPosicion().equals(p.getPosicion())) {
                // La lógica para activar el efecto se queda en el Controller por ahora.
                soundManager.reproducirEfecto("pacman_powerUp.wav");
                return true;
            }
            return false;
        });

        // 3. Verificar si come Frutas
        frutas.removeIf(fruta -> {
            if (pacman.getPosicion().equals(fruta.getPosicion())) {
                soundManager.reproducirComer();
                return true;
            }
            return false;
        });

        // 4. Verificar colisiones con Fantasmas
        for (Fantasma fantasma : fantasmas) {
            if (pacman.getPosicion().equals(fantasma.getPosicion())) {
                // Si el fantasma está huyendo, Pac-Man se lo come
                if (fantasma.getEstado() == EstadoFantasma.HUIDA
                        || fantasma.getEstado() == EstadoFantasma.PARPADEANDO) {
                    // Aquí podrías incrementar la puntuación por comer un fantasma.
                    fantasma.reiniciar();
                    soundManager.reproducirEfecto("pacman_comiendoFantasma.wav");
                }
                // Si el fantasma está normal, Pac-Man muere
                else if (fantasma.getEstado() == EstadoFantasma.NORMAL) {
                    soundManager.reproducirEfecto("pacman_muerto.wav");
                    return true; // ¡Señal de que Pac-Man murió!
                }
            }
        }

        return false; // No hubo colisión mortal.
    }
}