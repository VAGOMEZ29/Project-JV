package modelo;

import javax.imageio.ImageIO;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public abstract class Fantasma extends Personaje {

    private EstadoFantasma estado = EstadoFantasma.NORMAL;
    private final Point posicionInicial;
    private static Image imagenAsustado;
    private long tiempoInicioHuida;
    private long duracionHuida;
    private final double velocidadBase;

    static {
        try {
            imagenAsustado = ImageIO.read(Fantasma.class.getResourceAsStream("/resources/imgs/scaredGhost.png"));
        } catch (Exception e) {
            System.err.println("Error cargando imagen de fantasma asustado: " + e.getMessage());
        }
    }

    public Fantasma(Point posicion, Image imagen, double velocidad) {
        super(posicion, imagen, velocidad);
        this.velocidadBase = velocidad;
        this.posicionInicial = new Point(posicion);
        this.setDireccion(Direccion.IZQUIERDA);
    }

    // En la clase Fantasma.java
    public void decidirSiguienteDireccion(PacMan pacman, List<Fantasma> fantasmas, Laberinto laberinto,
            ModoGlobalIA modoGlobal) {
        Point posActual = this.getPosicion();

        // Solo tomar decisiones cuando esté perfectamente alineado en una celda.
        if (posActual.x % 32 != 0 || posActual.y % 32 != 0) {
            return;
        }

        // Si el fantasma está huyendo, su lógica es diferente y anula todo lo demás.
        if (this.estado == EstadoFantasma.HUIDA || this.estado == EstadoFantasma.PARPADEANDO) {
            // En una intersección, elige un camino al azar para parecer perdido.
            if (laberinto.esInterseccion(posActual)) {
                calcularMejorGiroEnInterseccion(new Point(-1, -1), laberinto, true); // Target inválido para
                                                                                     // aleatoriedad
            }
            // Si no es una intersección, gestiona giros en esquinas y callejones sin
            // salida.
            else if (!laberinto.puedeMover(posActual, this.getDireccion())) {
                calcularMejorGiroEnInterseccion(new Point(-1, -1), laberinto, true);
            }
            return;
        }

        // --- Lógica para modos NORMALES (Perseguir / Dispersar) ---

        // Si el fantasma llega a una intersección O a un callejón/esquina donde no
        // puede seguir recto,
        // debe tomar una decisión.
        if (laberinto.esInterseccion(posActual) || !laberinto.puedeMover(posActual, this.getDireccion())) {
            Point objetivo = obtenerObjetivo(pacman, fantasmas, modoGlobal);
            calcularMejorGiroEnInterseccion(objetivo, laberinto, false); // No puede dar la vuelta.
        }
        // Si no se cumple la condición, significa que está en un pasillo y puede seguir
        // recto,
        // por lo que no hace falta hacer nada y mantiene su dirección actual.
    }

    private void calcularMejorGiroEnInterseccion(Point objetivo, Laberinto laberinto, boolean esHuida) {
        Point posActual = this.getPosicion();
        Direccion dirActual = this.getDireccion();
        Direccion mejorDireccion = dirActual.invertir(); // Dirección por defecto si todo falla
        double distanciaMinima = Double.MAX_VALUE;
        List<Direccion> opciones = new ArrayList<>();

        for (Direccion dir : Direccion.values()) {
            if (dir == Direccion.NINGUNA)
                continue;
            // La única dirección prohibida es la inversa, a menos que esté huyendo.
            if (!esHuida && dir == dirActual.invertir())
                continue;

            if (laberinto.puedeMover(posActual, dir)) {
                if (esHuida) {
                    opciones.add(dir); // En huida, todas las opciones válidas son candidatas
                } else {
                    Point posFutura = new Point(posActual.x + dir.getDx() * 32, posActual.y + dir.getDy() * 32);
                    double distancia = posFutura.distanceSq(objetivo);
                    if (distancia < distanciaMinima) {
                        distanciaMinima = distancia;
                        mejorDireccion = dir;
                    }
                }
            }
        }

        if (esHuida) {
            // Elige una dirección aleatoria de las disponibles para parecer errático
            if (!opciones.isEmpty()) {
                this.setDireccion(opciones.get((int) (Math.random() * opciones.size())));
            } else {
                this.setDireccion(mejorDireccion); // Fallback: si está atrapado, da la vuelta
            }
        } else {
            this.setDireccion(mejorDireccion);
        }
    }

    public abstract Point obtenerObjetivo(PacMan pacman, List<Fantasma> fantasmas, ModoGlobalIA modoGlobal);

    // En la clase: Fantasma.java

    public void reiniciar() {
        this.posicion = new Point(this.posicionInicial);
        this.estado = EstadoFantasma.NORMAL;
        this.setDireccion(Direccion.IZQUIERDA);

        // ¡LA LÍNEA QUE FALTABA!
        // Reseteamos el temporizador para que el fantasma no se mueva inmediatamente al
        // reanudarse el juego.
        resetearTemporizadorMovimiento();
    }

    public void activarHuida(long duracionMs) {
        this.estado = EstadoFantasma.HUIDA;
        this.tiempoInicioHuida = System.currentTimeMillis();
        this.duracionHuida = duracionMs;
        this.setVelocidad(velocidadBase * 0.75);
    }

    public void actualizarEstado() {
        if (estado == EstadoFantasma.HUIDA || estado == EstadoFantasma.PARPADEANDO) {
            long tiempoTranscurrido = System.currentTimeMillis() - tiempoInicioHuida;
            long tiempoRestante = duracionHuida - tiempoTranscurrido;
            if (tiempoRestante < 2000 && tiempoRestante > 0) {
                estado = EstadoFantasma.PARPADEANDO;
            } else if (tiempoRestante <= 0) {
                estado = EstadoFantasma.NORMAL;
                setVelocidad(velocidadBase);
            }
        }
    }

    public abstract int getPrioridad();

    @Override
    public void dibujar(Graphics g) {
        if (posicion == null)
            return;
        int ancho = 28, alto = 28;
        int offsetX = (32 - ancho) / 2, offsetY = (32 - alto) / 2;
        Image imgActual = this.imagen;
        if (estado == EstadoFantasma.HUIDA) {
            imgActual = imagenAsustado;
        } else if (estado == EstadoFantasma.PARPADEANDO && imagenAsustado != null) {
            imgActual = ((System.currentTimeMillis() / 250) % 2 == 0) ? imagenAsustado : this.imagen;
        }
        g.drawImage(imgActual, posicion.x + offsetX, posicion.y + offsetY, ancho, alto, null);
    }

    public EstadoFantasma getEstado() {
        return estado;
    }

    public void setEstado(EstadoFantasma estado) {
        this.estado = estado;
    }

    public Point getPosicionInicial() {
        return posicionInicial;
    }

    public Point getProximaPosicion() {
        Point proxima = new Point(this.posicion);
        proxima.translate(this.direccion.getDx() * 32, this.direccion.getDy() * 32);
        return proxima;
    }

    @Override
    public void mover(Direccion direccion) {
        int paso = 32;
        this.direccion = direccion;
        switch (direccion) {
            case ARRIBA -> posicion.translate(0, -paso);
            case ABAJO -> posicion.translate(0, paso);
            case IZQUIERDA -> posicion.translate(-paso, 0);
            case DERECHA -> posicion.translate(paso, 0);
            case NINGUNA -> {
            }
        }
    }
}