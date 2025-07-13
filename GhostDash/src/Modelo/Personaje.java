package modelo;

import java.awt.Image;
import java.awt.Point;

/**
 * Clase abstracta que representa a cualquier personaje móvil en el juego
 * (Pac-Man o Fantasmas).
 * Hereda de ElementoJuego y añade lógica para velocidad, dirección y un
 * temporizador
 * de movimiento para controlar el ritmo del juego.
 */
public abstract class Personaje extends ElementoJuego {

    protected double velocidad; // Representa "movimientos por segundo"
    protected Direccion direccion;

    // --- Variables para el control de velocidad basado en tiempo ---
    private long tiempoUltimoMovimiento;
    private long retardoMovimiento; // El tiempo a esperar entre movimientos, en nanosegundos

    /**
     * Constructor para un personaje.
     * 
     * @param posicion  La posición inicial.
     * @param imagen    La imagen del personaje.
     * @param velocidad Los movimientos por segundo que realizará el personaje (ej.
     *                  2.0).
     */
    public Personaje(Point posicion, Image imagen, double velocidad) {
        super(posicion, imagen);
        this.direccion = Direccion.IZQUIERDA; // Dirección por defecto
        setVelocidad(velocidad); // Usa el setter para calcular el retardo inicial
        this.tiempoUltimoMovimiento = System.nanoTime(); // Inicia el temporizador
    }

    /**
     * Método abstracto que las subclases deben implementar para definir
     * cómo cambian su posición al moverse.
     * 
     * @param direccion La dirección en la que se debe mover.
     */
    public abstract void mover(Direccion direccion);

    /**
     * Comprueba si ha transcurrido el tiempo de retardo necesario desde el último
     * movimiento.
     * Si ha pasado, reinicia el temporizador y permite un nuevo movimiento.
     * 
     * @return true si el personaje está listo para moverse, false en caso
     *         contrario.
     */
    public boolean puedeMoverseAhora() {
        long tiempoActual = System.nanoTime();
        if (tiempoActual - tiempoUltimoMovimiento >= retardoMovimiento) {
            // Ha pasado suficiente tiempo, así que reiniciamos el temporizador para el
            // próximo ciclo.
            tiempoUltimoMovimiento = tiempoActual;
            return true;
        }
        return false;
    }

    // --- Getters y Setters ---

    public double getVelocidad() {
        return velocidad;
    }

    /**
     * Establece la velocidad del personaje en "movimientos por segundo"
     * y calcula el retardo de movimiento correspondiente en nanosegundos.
     * 
     * @param velocidad El número de casillas que el personaje debe moverse por
     *                  segundo.
     */
    public void setVelocidad(double velocidad) {
        this.velocidad = velocidad;
        if (velocidad > 0) {
            // 1 segundo (en nanosegundos) dividido por los movimientos por segundo
            // nos da cuántos nanosegundos debemos esperar por cada movimiento.
            this.retardoMovimiento = (long) (1_000_000_000 / velocidad);
        } else {
            // Si la velocidad es 0 o negativa, el retardo es prácticamente infinito,
            // por lo que el personaje no se moverá.
            this.retardoMovimiento = Long.MAX_VALUE;
        }
    }

    public Direccion getDireccion() {
        return direccion;
    }

    public void setDireccion(Direccion direccion) {
        this.direccion = direccion;
    }
}