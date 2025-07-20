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
    private List<Fruta> frutas = Collections.emptyList();
    private List<Punto> puntos = Collections.emptyList();
    private List<PowerUp> powerUps = Collections.emptyList();
    private int puntuacion = 0;
    private int vidas = 3;
    private int nivelActual = 1;
    private boolean pacmanInvencible = false;
    private boolean fantasmasCongelados = false;
    private CategoriaJuego categoria = CategoriaJuego.CLASICO;
    private final SoundManager soundManager = new SoundManager();
    private final GameManager gameManager;

    private ModoGlobalIA modoActualIA;
    private Timer cicloIATimer;
    private final int[] duracionesCiclo = { 7000, 20000, 7000, 20000, 5000, 20000, 5000 };
    private int indiceCicloIA = 0;

    public GameController(GameManager manager) {
        this.gameManager = manager;
    }

    // --- Métodos de Consulta para el GameManager ---
    public boolean isGameOver() {
        return vidas <= 0;
    }

    public boolean isGameWon() {
        return puntos.isEmpty() && nivelActual >= 3;
    }

    public int getPuntuacion() {
        return puntuacion;
    }

    public int getVidas() {
        return vidas;
    }

    public void setCategoria(CategoriaJuego categoria) {
        this.categoria = categoria;
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

    private void iniciarCicloIAFantasmas() {
        indiceCicloIA = 0;
        if (cicloIATimer != null)
            cicloIATimer.stop();
        modoActualIA = ModoGlobalIA.DISPERSAR;
        System.out.println("IA GLOBAL: " + modoActualIA);
        cicloIATimer = new Timer(duracionesCiclo[indiceCicloIA], e -> cambiarModoIA());
        cicloIATimer.setRepeats(false);
        cicloIATimer.start();
    }

    private void cambiarModoIA() {
        indiceCicloIA++;
        if (indiceCicloIA >= duracionesCiclo.length) {
            modoActualIA = ModoGlobalIA.PERSEGUIR;
            System.out.println("IA GLOBAL: PERSEGUIR (Permanente)");
            if (cicloIATimer != null)
                cicloIATimer.stop();
            return;
        }
        modoActualIA = (modoActualIA == ModoGlobalIA.PERSEGUIR) ? ModoGlobalIA.DISPERSAR : ModoGlobalIA.PERSEGUIR;
        System.out.println("IA GLOBAL: " + modoActualIA);
        cicloIATimer.setInitialDelay(duracionesCiclo[indiceCicloIA]);
        cicloIATimer.restart();
    }

    private void cargarLaberintoYElementos() {
        NivelInfo nivelInfo = Nivel.cargarNivel(nivelActual, categoria);
        if (nivelInfo == null) {
            System.err.println("No se pudo cargar el nivel " + nivelActual);
            return;
        }
        this.laberinto = nivelInfo.getLaberinto();
        this.pacman = nivelInfo.getPacman();
        this.fantasmas = nivelInfo.getFantasmas();
        this.frutas = nivelInfo.getFrutas();
        this.puntos = nivelInfo.getPuntos();
        this.powerUps = nivelInfo.getPowerUps();
        if (pacman != null)
            pacman.setDireccion(Direccion.NINGUNA);
    }

    public void actualizarJuego() {
        if (puntos.isEmpty() && nivelActual < 3) {
            pasarDeNivel();
            return;
        }

        if (pacman.puedeMoverseAhora()) {
            Point pos = pacman.getPosicion();
            Direccion dirActual = pacman.getDireccion();
            Direccion dirDeseada = pacman.getDireccionDeseada();
            if (dirDeseada != Direccion.NINGUNA && dirDeseada != dirActual && laberinto.puedeMover(pos, dirDeseada)) {
                pacman.setDireccion(dirDeseada);
                dirActual = dirDeseada;
            }
            if (laberinto.puedeMover(pos, dirActual)) {
                pacman.mover(dirActual);
                aplicarEfectoTunel(pacman);
            }
        }

        for (Fantasma fantasma : fantasmas) {
            fantasma.actualizarEstado();
            if (!fantasmasCongelados && fantasma.puedeMoverseAhora()) {
                fantasma.decidirSiguienteDireccion(pacman, fantasmas, laberinto, modoActualIA);
                if (laberinto.puedeMover(fantasma.getPosicion(), fantasma.getDireccion())) {
                    fantasma.mover(fantasma.getDireccion());
                    aplicarEfectoTunel(fantasma);
                }
            }
        }

        verificarPuntos();
        if (categoria == CategoriaJuego.CLASICO_MEJORADO)
            verificarPowerUps();
        verificarFrutas();
        verificarColisiones();
    }

    private void reiniciarJuegoDesdeInterfaz() {
        puntuacion = 0;
        vidas = 3;
        nivelActual = 1;
        if (cicloIATimer != null)
            cicloIATimer.stop();
        gameManager.mostrarMenu(); // Vuelve al menú principal al reiniciar.
    }

    private void pasarDeNivel() {
        nivelActual++;
        cargarLaberintoYElementos();
        iniciarCicloIAFantasmas();
    }

    private void verificarPuntos() {
        puntos.removeIf(p -> {
            if (pacman.getPosicion().equals(p.getPosicion())) {
                puntuacion += p.getValor();
                soundManager.reproducirComer();
                return true;
            }
            return false;
        });
    }

    private void verificarPowerUps() {
        powerUps.removeIf(p -> {
            if (pacman.getPosicion().equals(p.getPosicion())) {
                activarEfecto(p.getTipo(), p.getDuracion());
                puntuacion += 50;
                soundManager.reproducirEfecto("pacman_powerUp.wav");
                return true;
            }
            return false;
        });
    }

    private void activarEfecto(TipoPowerUp tipo, int duracion) {
        switch (tipo) {
            case INVENCIBILIDAD -> {
                pacmanInvencible = true;
                for (Fantasma fantasma : fantasmas)
                    fantasma.activarHuida(duracion);
                new Timer(duracion, e -> pacmanInvencible = false) {
                    {
                        setRepeats(false);
                    }
                }.start();
            }
            case CONGELAR_ENEMIGOS -> {
                fantasmasCongelados = true;
                new Timer(duracion, e -> fantasmasCongelados = false) {
                    {
                        setRepeats(false);
                    }
                }.start();
            }
            case VELOCIDAD -> {
                double originalSpeed = pacman.getVelocidad();
                pacman.setVelocidad(originalSpeed * 2);
                new Timer(duracion, e -> pacman.setVelocidad(originalSpeed)) {
                    {
                        setRepeats(false);
                    }
                }.start();
            }
            case DOBLE_PUNTOS -> System.out.println("PowerUp DOBLE_PUNTOS aún no implementado.");
        }
    }

    private void verificarFrutas() {
        frutas.removeIf(fruta -> {
            if (pacman.getPosicion().equals(fruta.getPosicion())) {
                puntuacion += fruta.getValor();
                soundManager.reproducirComer();
                return true;
            }
            return false;
        });
    }

    private void verificarColisiones() {
        if (isGameOver())
            return;
        for (Fantasma fantasma : fantasmas) {
            if (pacman.getPosicion().equals(fantasma.getPosicion())) {
                if (fantasma.getEstado() == EstadoFantasma.HUIDA
                        || fantasma.getEstado() == EstadoFantasma.PARPADEANDO) {
                    puntuacion += 200;
                    fantasma.reiniciar();
                    soundManager.reproducirEfecto("pacman_comiendoFantasma.wav");
                } else if (fantasma.getEstado() == EstadoFantasma.NORMAL) {
                    vidas--;
                    soundManager.reproducirEfecto("pacman_muerto.wav");
                    if (!isGameOver()) {
                        reiniciarPosiciones();
                    }
                }
            }
        }
    }

    private void reiniciarPosiciones() {
        pacman.setPosicion(pacman.getPosicionInicial());
        for (Fantasma f : fantasmas)
            f.reiniciar();
        iniciarCicloIAFantasmas();
    }

    private void aplicarEfectoTunel(Personaje personaje) {
        int paso = 32;
        int columnas = laberinto.getColumnas();
        int maxX = (columnas - 1) * paso;
        Point pos = personaje.getPosicion();
        if (pos.x < 0) {
            personaje.setPosicion(new Point(maxX, pos.y));
        } else if (pos.x > maxX) {
            personaje.setPosicion(new Point(0, pos.y));
        }
    }
}