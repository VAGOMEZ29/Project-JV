// Archivo: FantasmaJugador.java
// Ubicación: paquete modelo
package modelo;

import javax.swing.Timer;
import java.awt.Image;
import java.awt.Point;
import java.util.List;

/**
 * Representa un fantasma controlado por un jugador humano.
 * Su movimiento no se basa en IA, sino en la entrada del teclado.
 * Posee una habilidad especial de aceleración.
 */
public class FantasmaJugador extends Fantasma {

    private static final long COOLDOWN_HABILIDAD_MS = 10000; // 10 segundos
    private long tiempoUltimaHabilidad = 0;
    private Direccion direccionDeseada = Direccion.NINGUNA;

    public FantasmaJugador(Point posicion, Image imagen, double velocidad) {
        super(posicion, imagen, velocidad);
    }

    @Override
    public Point obtenerObjetivo(PacMan pacman, List<Fantasma> fantasmas, ModoGlobalIA modoGlobal) {
        return null; // No aplicable para el jugador
    }

    @Override
    public void decidirSiguienteDireccion(PacMan pacman, List<Fantasma> fantasmas, Laberinto laberinto,
            ModoGlobalIA modoGlobal) {
        // No hace nada. El movimiento se gestiona en GameController.
    }

    public boolean puedeUsarHabilidad() {
        return (System.currentTimeMillis() - tiempoUltimaHabilidad) >= COOLDOWN_HABILIDAD_MS;
    }

    public void activarAceleron(double multiplicador, int duracionMs) {
        if (puedeUsarHabilidad()) {
            this.tiempoUltimaHabilidad = System.currentTimeMillis();

            // ¡LÍNEA CORREGIDA!
            // Usamos el método público getVelocidadBase() en lugar de la variable privada.
            double velocidadOriginal = this.getVelocidadBase();

            setVelocidad(velocidadOriginal * multiplicador);

            new Timer(duracionMs, e -> setVelocidad(velocidadOriginal)) {
                {
                    setRepeats(false);
                }
            }.start();
        }
    }

    public void resetearCooldownHabilidad() {
        this.tiempoUltimaHabilidad = 0;
    }

    public Direccion getDireccionDeseada() {
        return direccionDeseada;
    }

    public void setDireccionDeseada(Direccion direccion) {
        this.direccionDeseada = direccion;
    }

    @Override
    public int getPrioridad() {
        return 0;
    }
}