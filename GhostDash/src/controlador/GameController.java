package controlador;

import modelo.*;
import vista.GamePanel;
import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.List;

public class GameController {

    // --- Variables de la clase (sin cambios) ---
    private GamePanel vista;
    private Laberinto laberinto;
    private PacMan pacman;
    private List<Fantasma> fantasmas = Collections.emptyList();
    private List<Punto> puntos = Collections.emptyList();
    private List<PowerUp> powerUps = Collections.emptyList();
    private List<Fruta> frutas = Collections.emptyList();
    private int puntuacion = 0;
    private int vidas = 3;
    private int nivelActual = 1;
    private CategoriaJuego categoria = CategoriaJuego.CLASICO;
    private boolean pacmanInvencible = false;
    private final GameManager gameManager;
    private final SoundManager soundManager = new SoundManager();
    private ModoGlobalIA modoActualIA;
    private Timer cicloIATimer;
    private final int[] duracionesCiclo = { 7000, 20000, 7000, 20000, 5000, 20000, 5000 };
    private int indiceCicloIA = 0;

    public GameController(GameManager manager) {
        this.gameManager = manager;
    }

    public void actualizarJuego() {
        if (isGameOver() || (puntos.isEmpty() && nivelActual >= 3)) {
            return;
        }

        moverPacMan();
        if (procesarInteracciones())
            return;

        moverFantasmas();
        if (procesarInteracciones())
            return;

        if (puntos.isEmpty() && nivelActual < 3) {
            pasarDeNivel();
        }
    }

    // --- CAMBIO #1: NUEVO MÉTODO PARA LA CARGA COMPLETA ---
    /**
     * Carga un nivel desde archivo y prepara una nueva partida.
     * Esto se usa al iniciar el juego y al pasar de nivel.
     */
    private void cargarNivelYPrepararPartida() {
        cargarLaberintoYElementos();

        // Si la vista ya existe, actualiza sus referencias. Si no, prepararJuego() la
        // creará.
        if (vista != null) {
            vista.setPacMan(pacman);
            vista.setFantasmas(fantasmas);
            vista.setFrutas(frutas);
            vista.setPuntos(puntos);
            vista.setPowerUps(powerUps);
            vista.setLaberinto(laberinto);
            vista.inicializarControles(pacman);
        }

        iniciarCicloIAFantasmas();
    }

    // --- CAMBIO #2: `reiniciarPosiciones` ahora es mucho más simple ---
    /**
     * Reinicia SOLO las posiciones de los personajes.
     * Ya no recarga todo el nivel, por lo que los puntos comidos no reaparecerán.
     */
    public void reiniciarPosiciones() {
        // Reposiciona a Pac-Man y resetea su movimiento.
        pacman.setPosicion(pacman.getPosicionInicial());
        pacman.setDireccion(Direccion.NINGUNA);
        pacman.setDireccionDeseada(Direccion.NINGUNA);

        // Reposiciona a los fantasmas.
        for (Fantasma f : fantasmas) {
            f.reiniciar();
        }

        // Reinicia el ciclo de la IA.
        iniciarCicloIAFantasmas();
    }

    // --- CAMBIO #3: `pasarDeNivel` ahora usa el nuevo método ---
    private void pasarDeNivel() {
        nivelActual++;
        // Al pasar de nivel, sí queremos una recarga completa.
        cargarNivelYPrepararPartida();
    }

    // --- CAMBIO #4: `prepararJuego` ahora usa el nuevo método ---
    public GamePanel prepararJuego() {
        // Carga el nivel 1 por primera vez.
        cargarNivelYPrepararPartida();

        // Crea la vista por primera vez.
        vista = new GamePanel(laberinto, pacman, fantasmas, frutas, puntos, powerUps);
        vista.setGameManager(this.gameManager);
        vista.inicializarControles(pacman);
        vista.setReiniciarListener(this::reiniciarJuegoDesdeInterfaz);

        return vista;
    }

    private boolean procesarInteracciones() {
        // 1. Colisión con fantasmas (la más importante, va primero)
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

        // 2. Comer puntos
        puntos.removeIf(p -> {
            if (pacman.getPosicion().equals(p.getPosicion())) {
                puntuacion += 10;
                soundManager.reproducirComer();
                return true;
            }
            return false;
        });

        // 3. Comer Power-Ups
        powerUps.removeIf(p -> {
            if (pacman.getPosicion().equals(p.getPosicion())) {
                puntuacion += 50;
                activarEfecto(p.getTipo(), p.getDuracion());
                return true;
            }
            return false;
        });

        // 4. Comer Frutas (¡LA PARTE NUEVA!)
        frutas.removeIf(fruta -> {
            if (pacman.getPosicion().equals(fruta.getPosicion())) {
                puntuacion += 100; // Puedes ajustar este valor
                soundManager.reproducirComer();
                return true;
            }
            return false;
        });

        return false; // No hubo muerte
    }

    public void restarVida() {
        this.vidas--;
    }

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

    private void moverFantasmas() {
        for (Fantasma fantasma : fantasmas) {
            fantasma.actualizarEstado();
            fantasma.decidirSiguienteDireccion(pacman, fantasmas, laberinto, modoActualIA);
        }
        for (Fantasma fantasmaActual : fantasmas) {
            if (fantasmaActual.puedeMoverseAhora()) {
                boolean cederElPaso = false;
                Point proximaPos = fantasmaActual.getProximaPosicion();
                for (Fantasma otroFantasma : fantasmas) {
                    if (fantasmaActual == otroFantasma)
                        continue;
                    if (proximaPos.equals(otroFantasma.getPosicion())
                            && otroFantasma.getProximaPosicion().equals(fantasmaActual.getPosicion())) {
                        if (fantasmaActual.getPrioridad() > otroFantasma.getPrioridad())
                            cederElPaso = true;
                    } else if (proximaPos.equals(otroFantasma.getProximaPosicion())) {
                        if (fantasmaActual.getPrioridad() > otroFantasma.getPrioridad())
                            cederElPaso = true;
                    }
                }
                if (!cederElPaso && laberinto.puedeMover(fantasmaActual.getPosicion(), fantasmaActual.getDireccion())) {
                    fantasmaActual.mover(fantasmaActual.getDireccion());
                    aplicarEfectoTunel(fantasmaActual);
                }
            }
        }
    }

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

    private void reiniciarJuegoDesdeInterfaz() {
        puntuacion = 0;
        vidas = 3;
        nivelActual = 1;
        if (cicloIATimer != null)
            cicloIATimer.stop();
        gameManager.mostrarMenu();
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
        if (pacman != null)
            pacman.setDireccion(Direccion.NINGUNA);
    }

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

    private void iniciarCicloIAFantasmas() {
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
        return puntos.isEmpty() && nivelActual >= 3;
    }

    public void setCategoria(CategoriaJuego categoria) {
        this.categoria = categoria;
    }
}