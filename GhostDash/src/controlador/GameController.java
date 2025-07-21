package controlador;

import modelo.*;
import vista.GamePanel;
import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.List;

public class GameController {

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
                soundManager.reproducirComer();
                return true;
            }
            return false;
        });
        powerUps.removeIf(p -> {
            if (pacman.getPosicion().equals(p.getPosicion())) {
                puntuacion += 50;
                activarEfecto(p.getTipo(), p.getDuracion());
                return true;
            }
            return false;
        });
        return false;
    }

    public void restarVida() {
        this.vidas--;
    }

    /**
     * ¡LA SOLUCIÓN! Este método ahora recarga TODO el nivel para garantizar un
     * estado limpio.
     * Es la forma más robusta de reiniciar.
     */
    public void reiniciarPosiciones() {
        cargarLaberintoYElementos();

        // Actualizamos la vista con las NUEVAS instancias de los objetos.
        vista.setPacMan(pacman);
        vista.setFantasmas(fantasmas);
        vista.setFrutas(frutas);
        vista.setPuntos(puntos);
        vista.setPowerUps(powerUps);
        vista.setLaberinto(laberinto);

        vista.inicializarControles(pacman);
        iniciarCicloIAFantasmas();
    }

    private void pasarDeNivel() {
        nivelActual++;
        reiniciarPosiciones(); // Pasar de nivel es lo mismo que reiniciar, pero con un número de nivel
                               // diferente.
    }

    // --- El resto de tus métodos (mover, preparar, etc. sin cambios) ---
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

    public GamePanel prepararJuego() {
        cargarLaberintoYElementos();
        vista = new GamePanel(laberinto, pacman, fantasmas, frutas, puntos, powerUps);
        vista.setGameManager(this.gameManager);
        vista.inicializarControles(pacman);
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