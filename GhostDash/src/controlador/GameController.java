package controlador;

import modelo.*;
import vista.GamePanel;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * GameController es el orquestador principal de la partida.
 * Gestiona el estado, el flujo del juego, la lógica de los personajes y la IA,
 * comunicándose con el GameManager para eventos críticos como la muerte de
 * Pac-Man.
 */
public class GameController {

    // ================================================================================
    // SECCIÓN: VARIABLES DE ESTADO Y MANAGERS
    // ================================================================================

    // --- Elementos del Juego y Vista ---
    private GamePanel vista;
    private Laberinto laberinto;
    private PacMan pacman;
    private List<Fantasma> fantasmas = Collections.emptyList();
    private List<Punto> puntos = Collections.emptyList();
    private List<PowerUp> powerUps = Collections.emptyList();
    private List<Fruta> frutas = Collections.emptyList();

    // --- Estado de la Partida ---
    private int puntuacion = 0;
    private int vidas = 3;
    private int nivelActual = 1;
    private CategoriaJuego categoria = CategoriaJuego.CLASICO;
    private boolean pacmanInvencible = false;

    // --- Lógica de la Fruta ---
    private Point posicionAparicionFruta;
    private int puntosComidosEnNivel;
    private boolean haAparecidoPrimeraFruta;
    private boolean haAparecidoSegundaFruta;

    // --- Lógica de la IA ---
    private ModoGlobalIA modoActualIA;
    private Timer cicloIATimer;
    private final int[] duracionesCiclo = { 7000, 20000, 7000, 20000, 5000, 20000, 5000 };
    private int indiceCicloIA = 0;
    private FantasmaJugador fantasmaJugador;

    // --- Managers Externos ---
    private final GameManager gameManager;
    private final SoundManager soundManager = new SoundManager();

    /**
     * Constructor de la clase.
     * 
     * @param manager Una referencia al GameManager principal para la comunicación.
     */
    public GameController(GameManager manager) {
        this.gameManager = manager;
    }

    // ================================================================================
    // SECCIÓN: BUCLE PRINCIPAL DEL JUEGO
    // ================================================================================

    /**
     * Es el método principal del bucle de juego, llamado repetidamente por el
     * GameManager.
     * Organiza el orden de los turnos de movimiento y las verificaciones de reglas.
     */
    public void actualizarJuego() {
        if (isGameOver() || isGameWon())
            return;

        moverPacMan();
        if (procesarInteracciones())
            return; // Si Pac-Man muere, detenemos el fotograma.

        moverFantasmas();
        if (procesarInteracciones())
            return; // Verificamos de nuevo por si un fantasma se movió sobre Pac-Man.

        verificarAparicionFruta();

        if (puntos.isEmpty() && powerUps.isEmpty() && nivelActual < 3) {
            pasarDeNivel();
        }
    }

    // ================================================================================
    // SECCIÓN: LÓGICA DE MOVIMIENTO DE PERSONAJES
    // ================================================================================

    /**
     * Gestiona el movimiento de Pac-Man basándose en la entrada del usuario y las
     * reglas del laberinto.
     */
    private void moverPacMan() {
        if (pacman.puedeMoverseAhora()) {
            Point pos = pacman.getPosicion();
            Direccion dirDeseada = pacman.getDireccionDeseada();
            if (dirDeseada != Direccion.NINGUNA && laberinto.puedeMover(pos, dirDeseada)) {
                pacman.setDireccion(dirDeseada);
            }
            if (laberinto.puedeMover(pos, pacman.getDireccion())) {
                pacman.mover(pacman.getDireccion());
                aplicarEfectoTunel(pacman);
            }
        }
    }

    /**
     * Orquesta el movimiento de todos los fantasmas, tanto los controlados por IA
     * como por el jugador.
     */
    private void moverFantasmas() {
        // Movimiento del Fantasma Jugador (si existe)
        if (fantasmaJugador != null && fantasmaJugador.puedeMoverseAhora()) {
            Point pos = fantasmaJugador.getPosicion();
            Direccion dirDeseada = fantasmaJugador.getDireccionDeseada();
            if (dirDeseada != Direccion.NINGUNA && laberinto.puedeMover(pos, dirDeseada)) {
                fantasmaJugador.setDireccion(dirDeseada);
            }
            if (laberinto.puedeMover(pos, fantasmaJugador.getDireccion())) {
                fantasmaJugador.mover(fantasmaJugador.getDireccion());
                aplicarEfectoTunel(fantasmaJugador);
            }
        }

        // Movimiento de los Fantasmas IA
        for (Fantasma fantasma : fantasmas) {
            if (fantasma instanceof FantasmaJugador)
                continue; // Saltamos al fantasma del jugador

            fantasma.actualizarEstado();
            fantasma.decidirSiguienteDireccion(pacman, fantasmas, laberinto, modoActualIA);

            if (fantasma.puedeMoverseAhora()) {
                boolean cederElPaso = false;
                Point proximaPos = fantasma.getProximaPosicion();
                for (Fantasma otroFantasma : fantasmas) {
                    if (fantasma == otroFantasma)
                        continue;
                    if (proximaPos.equals(otroFantasma.getPosicion())
                            && otroFantasma.getProximaPosicion().equals(fantasma.getPosicion())) {
                        if (fantasma.getPrioridad() > otroFantasma.getPrioridad())
                            cederElPaso = true;
                    } else if (proximaPos.equals(otroFantasma.getProximaPosicion())) {
                        if (fantasma.getPrioridad() > otroFantasma.getPrioridad())
                            cederElPaso = true;
                    }
                }
                if (!cederElPaso && laberinto.puedeMover(fantasma.getPosicion(), fantasma.getDireccion())) {
                    fantasma.mover(fantasma.getDireccion());
                    aplicarEfectoTunel(fantasma);
                }
            }
        }
    }

    /**
     * Aplica la lógica del túnel para que un personaje aparezca en el lado opuesto
     * del mapa.
     * 
     * @param personaje El personaje (Pac-Man o Fantasma) al que aplicar el efecto.
     */
    private void aplicarEfectoTunel(Personaje personaje) {
        int paso = 32;
        int columnas = laberinto.getColumnas();
        int maxX = (columnas - 1) * paso;
        Point pos = personaje.getPosicion();
        if (pos.x < 0)
            personaje.setPosicion(new Point(maxX, pos.y));
        else if (pos.x > maxX)
            personaje.setPosicion(new Point(0, pos.y));
    }

    // ================================================================================
    // SECCIÓN: LÓGICA DE INTERACCIONES Y REGLAS
    // ================================================================================

    /**
     * Verifica todas las posibles interacciones (comer puntos, frutas, power-ups,
     * colisiones).
     * Si detecta una muerte, notifica al GameManager.
     * 
     * @return true si Pac-Man murió durante la interacción, false en caso
     *         contrario.
     */
    private boolean procesarInteracciones() {
        for (Fantasma fantasma : fantasmas) {
            if (pacman.getPosicion().equals(fantasma.getPosicion())) {
                if (pacmanInvencible) {
                    fantasma.reiniciar();
                    puntuacion += 200;
                    soundManager.reproducirEfecto("pacman_comiendoFantasma.wav");
                } else if (fantasma.getEstado() == EstadoFantasma.NORMAL) {
                    gameManager.notificarMuerteDePacman();
                    return true;
                }
            }
        }
        puntos.removeIf(p -> {
            if (pacman.getPosicion().equals(p.getPosicion())) {
                puntuacion += 10;
                puntosComidosEnNivel++;
                soundManager.reproducirComer();
                return true;
            }
            return false;
        });
        powerUps.removeIf(p -> {
            if (pacman.getPosicion().equals(p.getPosicion())) {
                puntuacion += 50;
                puntosComidosEnNivel++;
                activarEfecto(p.getTipo(), p.getDuracion());
                return true;
            }
            return false;
        });
        frutas.removeIf(fruta -> {
            boolean comida = false;
            if (pacman.getPosicion().equals(fruta.getPosicion())) {
                puntuacion += 100;
                soundManager.reproducirComer();
                comida = true;
            } else if (categoria == CategoriaJuego.MULTIJUGADOR && fantasmaJugador != null
                    && fantasmaJugador.getPosicion().equals(fruta.getPosicion())) {
                fantasmaJugador.resetearCooldownHabilidad();
                soundManager.reproducirEfecto("pacman_powerUp.wav");
                comida = true;
            }
            return comida;
        });
        return false;
    }

    /**
     * Activa el efecto de un Power-Up, como la invencibilidad.
     * 
     * @param tipo     El tipo de efecto a activar.
     * @param duracion La duración del efecto en milisegundos.
     */
    private void activarEfecto(TipoPowerUp tipo, int duracion) {
        pacmanInvencible = true;
        for (Fantasma fantasma : fantasmas) {
            fantasma.activarHuida(duracion);
        }
        new Timer(duracion, e -> pacmanInvencible = false) {
            {
                setRepeats(false);
            }
        }.start();
    }

    // ================================================================================
    // SECCIÓN: GESTIÓN DE LA LÓGICA DE LA FRUTA
    // ================================================================================

    private void verificarAparicionFruta() {
        if (posicionAparicionFruta == null)
            return;
        if (!haAparecidoPrimeraFruta && puntosComidosEnNivel >= 70) {
            haAparecidoPrimeraFruta = true;
            spawnFruta();
        }
        if (!haAparecidoSegundaFruta && puntosComidosEnNivel >= 170) {
            haAparecidoSegundaFruta = true;
            spawnFruta();
        }
    }

    private void spawnFruta() {
        Image imagenFruta = Nivel.cargarImagen("cherry.png");
        Fruta nuevaFruta = new Fruta(posicionAparicionFruta, imagenFruta, 100, 9000);
        frutas.add(nuevaFruta);
        Timer temporizadorFruta = new Timer(9000, e -> frutas.remove(nuevaFruta));
        temporizadorFruta.setRepeats(false);
        temporizadorFruta.start();
    }

    private void resetearEstadoFrutaCompleto() {
        this.puntosComidosEnNivel = 0;
        this.haAparecidoPrimeraFruta = false;
        this.haAparecidoSegundaFruta = false;
        frutas.clear();
    }

    // ================================================================================
    // SECCIÓN: GESTIÓN DEL CICLO DE VIDA DE LA PARTIDA
    // ================================================================================

    public GamePanel prepararJuego() {
        nivelActual = 1;
        puntuacion = 0;
        vidas = 3;

        cargarLaberintoYElementos();
        resetearEstadoFrutaCompleto();

        vista = new GamePanel(laberinto, pacman, fantasmas, frutas, puntos, powerUps, this.categoria);
        vista.setGameManager(this.gameManager);
        vista.inicializarControles(pacman, this.fantasmaJugador);
        vista.setReiniciarListener(this::reiniciarJuegoDesdeInterfaz);
        iniciarCicloIAFantasmas();
        return vista;
    }

    private void cargarLaberintoYElementos() {
        NivelInfo nivelInfo = Nivel.cargarNivel(nivelActual, categoria);
        if (nivelInfo == null)
            return;
        this.laberinto = nivelInfo.getLaberinto();
        this.pacman = nivelInfo.getPacman();
        this.fantasmas = nivelInfo.getFantasmas();
        this.frutas = nivelInfo.getFrutas();
        this.puntos = nivelInfo.getPuntos();
        this.powerUps = nivelInfo.getPowerUps();
        this.posicionAparicionFruta = nivelInfo.getPosicionFruta();

        this.fantasmaJugador = null;
        if (categoria == CategoriaJuego.MULTIJUGADOR) {
            for (Fantasma f : fantasmas) {
                if (f instanceof FantasmaJugador) {
                    this.fantasmaJugador = (FantasmaJugador) f;
                    break;
                }
            }
        }

        if (pacman != null)
            pacman.setDireccion(Direccion.NINGUNA);
    }

    public void reiniciarPosiciones() {
        List<Punto> puntosActuales = new ArrayList<>(this.puntos);
        List<PowerUp> powerUpsActuales = new ArrayList<>(this.powerUps);
        int puntosComidosGuardados = this.puntosComidosEnNivel;
        boolean fruta1Aparecida = this.haAparecidoPrimeraFruta;
        boolean fruta2Aparecida = this.haAparecidoSegundaFruta;

        cargarLaberintoYElementos();

        this.puntos = puntosActuales;
        this.powerUps = powerUpsActuales;
        this.puntosComidosEnNivel = puntosComidosGuardados;
        this.haAparecidoPrimeraFruta = fruta1Aparecida;
        this.haAparecidoSegundaFruta = fruta2Aparecida;
        this.frutas.clear();

        vista.setPacMan(pacman);
        vista.setFantasmas(fantasmas);
        vista.setFrutas(frutas);
        vista.setPuntos(puntos);
        vista.setPowerUps(powerUps);
        vista.setLaberinto(laberinto);

        vista.inicializarControles(pacman, this.fantasmaJugador);

        iniciarCicloIAFantasmas();
    }

    private void pasarDeNivel() {
        nivelActual++;
        cargarLaberintoYElementos();
        resetearEstadoFrutaCompleto();

        vista.setPacMan(pacman);
        vista.setFantasmas(fantasmas);
        vista.setFrutas(frutas);
        vista.setPuntos(puntos);
        vista.setPowerUps(powerUps);
        vista.setLaberinto(laberinto);
        vista.inicializarControles(pacman, this.fantasmaJugador);

        iniciarCicloIAFantasmas();
    }

    private void reiniciarJuegoDesdeInterfaz() {
        gameManager.mostrarMenu();
    }

    /**
     * Reinicia el nivel actual a su estado inicial.
     * Mantiene las vidas y la puntuación, pero resetea las posiciones y la fruta.
     */
    public void reiniciarNivel() {
        // La lógica de reiniciar posiciones ya hace un "reinicio duro" que es
        // perfecto para reiniciar el nivel desde cero.
        reiniciarPosiciones();
    }

    // ================================================================================
    // SECCIÓN: GESTIÓN DE LA IA
    // ================================================================================

    private void iniciarCicloIAFantasmas() {
        if (categoria == CategoriaJuego.MULTIJUGADOR) {
            if (cicloIATimer != null)
                cicloIATimer.stop();
            modoActualIA = ModoGlobalIA.PERSEGUIR;
            return;
        }
        indiceCicloIA = 0;
        if (cicloIATimer != null)
            cicloIATimer.stop();
        modoActualIA = ModoGlobalIA.DISPERSAR;
        cicloIATimer = new Timer(duracionesCiclo[indiceCicloIA], e -> cambiarModoIA());
        cicloIATimer.setRepeats(false);
        cicloIATimer.start();
    }

    private void cambiarModoIA() {
        indiceCicloIA++;
        if (indiceCicloIA >= duracionesCiclo.length) {
            modoActualIA = ModoGlobalIA.PERSEGUIR;
            if (cicloIATimer != null)
                cicloIATimer.stop();
            return;
        }
        modoActualIA = (modoActualIA == ModoGlobalIA.PERSEGUIR) ? ModoGlobalIA.DISPERSAR : ModoGlobalIA.PERSEGUIR;
        cicloIATimer.setInitialDelay(duracionesCiclo[indiceCicloIA]);
        cicloIATimer.restart();
    }

    // ================================================================================
    // SECCIÓN: GETTERS Y SETTERS (PÚBLICOS)
    // ================================================================================

    public void restarVida() {
        this.vidas--;
    }

    public int getPuntuacion() {
        return puntuacion;
    }

    public int getVidas() {
        return vidas;
    }

    public boolean isGameOver() {
        return vidas <= 0;
    }

    public boolean isGameWon() {
        return (puntos.isEmpty() && powerUps.isEmpty() && nivelActual >= 3);
    }

    public CategoriaJuego getCategoria() {
        return this.categoria;
    }

    public void setCategoria(CategoriaJuego categoria) {
        this.categoria = categoria;
    }

}