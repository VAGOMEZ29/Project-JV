package modelo;

import javax.imageio.ImageIO;
import java.awt.*;
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
    }

    public void decidirSiguienteDireccion(PacMan pacman, List<Fantasma> fantasmas, Laberinto laberinto,
            ModoGlobalIA modoGlobal) {
        Point posActual = this.getPosicion();
        if (posActual.x % 32 != 0 || posActual.y % 32 != 0)
            return;

        // Lógica de HUÍDA anula todo lo demás
        if (this.estado == EstadoFantasma.HUIDA || this.estado == EstadoFantasma.PARPADEANDO) {
            Point objetivo = new Point((int) (Math.random() * laberinto.getColumnas() * 32),
                    (int) (Math.random() * laberinto.getFilas() * 32));
            calcularMejorGiroEnInterseccion(objetivo, laberinto, true); // En modo huida, sí puede dar la vuelta.
            return;
        }

        // Lógica para modos NORMALES (Perseguir / Dispersar)
        if (laberinto.esInterseccion(posActual)) {
            Point objetivo = obtenerObjetivo(pacman, fantasmas, modoGlobal);
            calcularMejorGiroEnInterseccion(objetivo, laberinto, false); // En modo normal, no puede dar la vuelta.
        } else {
            // Lógica de PASILLO
            if (!laberinto.puedeMover(posActual, this.getDireccion())) {
                // Si choca, debe encontrar la única otra salida posible que no sea hacia atrás
                // (una curva).
                // Si no hay curvas, la única opción será dar la vuelta.
                for (Direccion nuevaDir : Direccion.values()) {
                    if (nuevaDir != Direccion.NINGUNA && nuevaDir != this.getDireccion().invertir()) {
                        if (laberinto.puedeMover(posActual, nuevaDir)) {
                            this.setDireccion(nuevaDir);
                            return; // Encontró la curva.
                        }
                    }
                }
                // Si el bucle no encontró una curva, es un callejón sin salida. Forzar la
                // vuelta.
                this.setDireccion(this.getDireccion().invertir());
            }
            // Si puede seguir recto, no hace nada, mantiene su dirección.
        }
    }

    private void calcularMejorGiroEnInterseccion(Point objetivo, Laberinto laberinto, boolean puedeInvertir) {
        Point posActual = this.getPosicion();
        Direccion dirActual = this.getDireccion();
        Direccion mejorDireccion = dirActual.invertir();
        double distanciaMinima = Double.MAX_VALUE;

        for (Direccion dir : Direccion.values()) {
            if (dir == Direccion.NINGUNA)
                continue;
            // La única dirección prohibida es la inversa, A MENOS que se esté en modo
            // huida.
            if (!puedeInvertir && dir == dirActual.invertir())
                continue;

            if (laberinto.puedeMover(posActual, dir)) {
                Point posFutura = new Point(posActual.x + dir.getDx() * 32, posActual.y + dir.getDy() * 32);
                double distancia = posFutura.distanceSq(objetivo);
                if (distancia < distanciaMinima) {
                    distanciaMinima = distancia;
                    mejorDireccion = dir;
                }
            }
        }
        this.setDireccion(mejorDireccion);
    }

    public abstract Point obtenerObjetivo(PacMan pacman, List<Fantasma> fantasmas, ModoGlobalIA modoGlobal);

    public void reiniciar() {
        this.posicion = new Point(this.posicionInicial);
        this.estado = EstadoFantasma.NORMAL;
        this.setDireccion(Direccion.aleatoria());
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