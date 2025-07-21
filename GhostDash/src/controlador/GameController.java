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

    // --- NUEVAS VARIABLES PARA LA LÓGICA DE LA FRUTA ---
    private Point posicionAparicionFruta;
    private int puntosComidosEnNivel;
    private boolean haAparecidoPrimeraFruta;
    private boolean haAparecidoSegundaFruta;

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

        // Verificamos si la fruta debe aparecer
        verificarAparicionFruta();

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
                puntosComidosEnNivel++; // Incrementamos el contador para la fruta
                soundManager.reproducirComer();
                return true;
            }
            return false;
        });
        powerUps.removeIf(p -> {
            if (pacman.getPosicion().equals(p.getPosicion())) {
                puntuacion += 50;
                puntosComidosEnNivel++; // Los Power-Ups también cuentan como puntos comidos
                activarEfecto(p.getTipo(), p.getDuracion());
                return true;
            }
            return false;
        });
        frutas.removeIf(fruta -> {
            if (pacman.getPosicion().equals(fruta.getPosicion())) {
                puntuacion += 100;
                soundManager.reproducirComer();
                return true;
            }
            return false;
        });
        return false;
    }

    // --- NUEVOS MÉTODOS PARA LA FRUTA ---

    /**
     * Comprueba si se cumplen las condiciones para que aparezca una fruta.
     */
    private void verificarAparicionFruta() {
        // Si no hay posición de fruta en este nivel, no hacemos nada.
        if (posicionAparicionFruta == null)
            return;

        // Primera fruta: aparece después de 70 puntos comidos.
        if (!haAparecidoPrimeraFruta && puntosComidosEnNivel >= 70) {
            haAparecidoPrimeraFruta = true;
            spawnFruta();
        }

        // Segunda fruta: aparece después de 170 puntos comidos.
        if (!haAparecidoSegundaFruta && puntosComidosEnNivel >= 170) {
            haAparecidoSegundaFruta = true;
            spawnFruta();
        }
    }

    /**
     * Crea una instancia de Fruta, la añade al juego y programa su desaparición.
     */
    private void spawnFruta() {
        Image imagenFruta = Nivel.cargarImagen("cherry.png"); // Podrías cambiar esto según el nivel
        Fruta nuevaFruta = new Fruta(posicionAparicionFruta, imagenFruta, 100, 9000);
        frutas.add(nuevaFruta);

        // Creamos un Timer que se ejecutará UNA VEZ después de 9 segundos.
        Timer temporizadorFruta = new Timer(9000, e -> {
            // El código dentro se ejecuta cuando el tiempo termina:
            frutas.remove(nuevaFruta); // La fruta desaparece.
        });
        temporizadorFruta.setRepeats(false);
        temporizadorFruta.start();
    }

    /**
     * Resetea el estado de la aparición de frutas. Esencial para el reinicio y paso
     * de nivel.
     */
    private void resetearEstadoFruta() {
        this.puntosComidosEnNivel = 0;
        this.haAparecidoPrimeraFruta = false;
        this.haAparecidoSegundaFruta = false;
        frutas.clear(); // Limpiamos cualquier fruta que haya quedado en pantalla
    }

    // --- MÉTODOS MODIFICADOS ---

    public void reiniciarPosiciones() {
        resetearEstadoFruta(); // Reseteamos la lógica de la fruta
        pacman.setPosicion(pacman.getPosicionInicial());
        pacman.setDireccion(Direccion.NINGUNA);
        pacman.setDireccionDeseada(Direccion.NINGUNA);
        for (Fantasma f : fantasmas)
            f.reiniciar();
        iniciarCicloIAFantasmas();
    }

    private void pasarDeNivel() {
        nivelActual++;
        cargarNivelYPrepararPartida();
    }

    private void cargarNivelYPrepararPartida() {
        cargarLaberintoYElementos();
        resetearEstadoFruta(); // Reseteamos la lógica de la fruta al cargar un nivel

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
        this.posicionAparicionFruta = nivelInfo.getPosicionFruta(); // Guardamos la posición
        if (pacman != null)
            pacman.setDireccion(Direccion.NINGUNA);
    }

    // --- El resto de tus métodos (restarVida, mover, etc.) no necesitan cambios
    // ---
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

    public GamePanel prepararJuego() {
        cargarNivelYPrepararPartida();
        vista = new GamePanel(laberinto, pacman, fantasmas, frutas, puntos, powerUps);
        vista.setGameManager(this.gameManager);
        vista.inicializarControles(pacman);
        vista.setReiniciarListener(this::reiniciarJuegoDesdeInterfaz);
        return vista;
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