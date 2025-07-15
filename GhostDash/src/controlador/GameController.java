package controlador;

import vista.GamePanel;
import modelo.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GameController {

    private GamePanel vista;
    private Laberinto laberinto;
    private PacMan pacman;
    private List<Fantasma> fantasmas;
    private List<Fruta> frutas;
    private List<Punto> puntos;
    private List<PowerUp> powerUps;

    private int puntuacion = 0;
    private int vidas = 3;
    private int nivelActual = 1;
    private boolean pacmanInvencible = false;
    private boolean fantasmasCongelados = false;
    private Jugador jugador;
    private Timer juegoTimer;

    private CategoriaJuego categoria = CategoriaJuego.CLASICO;
    private final SoundManager soundManager = new SoundManager();

    public void setCategoria(CategoriaJuego categoria) {
        this.categoria = categoria;
    }

    public GamePanel prepararJuego() {
        if (jugador == null) {
            jugador = new Jugador("Temporal", "tempUser", "1234", 18, true);
        }
        cargarLaberintoYElementos();
        vista = new GamePanel(laberinto, pacman, fantasmas, frutas, puntos, powerUps);
        vista.inicializarControles(pacman);
        juegoTimer = new Timer(150, e -> actualizarJuego());
        juegoTimer.start();
        vista.setReiniciarListener(this::reiniciarJuegoDesdeInterfaz);
        return vista;
    }

    private void cargarLaberintoYElementos() {
        NivelInfo nivelInfo = Nivel.cargarNivel(nivelActual, categoria);

        this.laberinto = nivelInfo.getLaberinto();
        this.pacman = nivelInfo.getPacman();
        this.fantasmas = nivelInfo.getFantasmas();
        this.frutas = nivelInfo.getFrutas();
        this.puntos = nivelInfo.getPuntos();
        this.powerUps = nivelInfo.getPowerUps();

        if (pacman != null) pacman.setDireccion(Direccion.NINGUNA);
    }

    public void actualizarJuego() {
        if (vidas <= 0) {
            juegoTimer.stop();
            vista.actualizarEstado(puntuacion, vidas, true, false);
            vista.mostrarBotonReiniciar();
            return;
        }

        if (puntos.isEmpty()) {
            pasarDeNivel();
            return;
        }

        if (pacman.puedeMoverseAhora() && laberinto.puedeMover(pacman.getPosicion(), pacman.getDireccion())) {
            pacman.mover(pacman.getDireccion());
            aplicarEfectoTunel(pacman);
        }

        for (Fantasma fantasma : fantasmas) {
            fantasma.actualizarEstado();
            if (!fantasmasCongelados && fantasma.puedeMoverseAhora()) {
                fantasma.actualizarMovimiento(pacman, fantasmas, laberinto);
                aplicarEfectoTunel(fantasma);
            }
        }

        verificarPuntos();
        if (categoria == CategoriaJuego.CLASICO_MEJORADO) {
            verificarPowerUps();
        }
        verificarFrutas();
        verificarColisiones();

        vista.actualizarEstado(puntuacion, vidas, false, puntos.isEmpty());
    }

    private void reiniciarJuegoDesdeInterfaz() {
        puntuacion = 0;
        vidas = 3;
        nivelActual = 1;

        cargarLaberintoYElementos();

        pacman.setPosicion(buscarPosicionInicialPacman());
        pacman.setDireccion(Direccion.NINGUNA);
        for (Fantasma fantasma : fantasmas) reiniciarFantasma(fantasma);

        vista.setPacMan(pacman);
        vista.setFantasmas(fantasmas);
        vista.setFrutas(frutas);
        vista.setPowerUps(powerUps);
        vista.setPuntos(puntos);
        vista.setLaberinto(laberinto);
        vista.actualizarEstado(puntuacion, vidas, false, false);
        vista.inicializarControles(pacman);

        if (juegoTimer != null) juegoTimer.start();
    }

    private void pasarDeNivel() {
        if (nivelActual >= 3) {
            juegoTimer.stop();
            vista.actualizarEstado(puntuacion, vidas, false, true);
            vista.mostrarBotonReiniciar();
            return;
        }

        nivelActual++;
        cargarLaberintoYElementos();

        pacman.setPosicion(buscarPosicionInicialPacman());
        pacman.setDireccion(Direccion.NINGUNA);

        for (Fantasma fantasma : fantasmas) reiniciarFantasma(fantasma);

        vista.setPacMan(pacman);
        vista.setFantasmas(fantasmas);
        vista.setFrutas(frutas);
        vista.setPowerUps(powerUps);
        vista.setPuntos(puntos);
        vista.setLaberinto(laberinto);
        vista.actualizarEstado(puntuacion, vidas, false, false);
        vista.inicializarControles(pacman);
    }

    private void verificarPuntos() {
        // Se reemplazó el bucle for-each por un Iterator para evitar ConcurrentModificationException.
        // Esto ocurre porque no se puede modificar una lista mientras se recorre con for-each.
        // Con Iterator y su método remove(), se puede eliminar de forma segura.
        synchronized (puntos) {
            Iterator<Punto> iter = puntos.iterator();
            while (iter.hasNext()) {
                Punto p = iter.next();
                if (pacman.getPosicion().equals(p.getPosicion())) {
                    puntuacion += p.getValor();
                    soundManager.reproducirComer();
                    iter.remove();
                }
            }
        }
    }

    private void verificarPowerUps() {
        List<PowerUp> recogidos = new ArrayList<>();
        for (PowerUp p : powerUps) {
            if (pacman.getPosicion().equals(p.getPosicion())) {
                activarEfecto(p.getTipo(), p.getDuracion());
                puntuacion += 50;
                recogidos.add(p);
                soundManager.reproducirEfecto("pacman_powerUp.wav");
            }
        }
        powerUps.removeAll(recogidos);
    }

    private void activarEfecto(TipoPowerUp tipo, int duracion) {
        switch (tipo) {
            case INVENCIBILIDAD -> {
                pacmanInvencible = true;
                for (Fantasma fantasma : fantasmas) fantasma.activarHuida(duracion);
                Timer timer = new Timer(duracion, e -> pacmanInvencible = false);
                timer.setRepeats(false);
                timer.start();
            }
            case CONGELAR_ENEMIGOS -> {
                fantasmasCongelados = true;
                Timer timer = new Timer(duracion, e -> fantasmasCongelados = false);
                timer.setRepeats(false);
                timer.start();
            }
            case VELOCIDAD -> {
                double originalSpeed = pacman.getVelocidad();
                pacman.setVelocidad(originalSpeed * 2);
                Timer timer = new Timer(duracion, e -> pacman.setVelocidad(originalSpeed));
                timer.setRepeats(false);
                timer.start();
            }
            case DOBLE_PUNTOS -> System.out.println("PowerUp DOBLE_PUNTOS aún no implementado.");
        }
    }

    private void verificarFrutas() {
        List<Fruta> comidas = new ArrayList<>();
        for (Fruta fruta : frutas) {
            if (pacman.getPosicion().equals(fruta.getPosicion())) {
                puntuacion += fruta.getValor();
                soundManager.reproducirComer();
                comidas.add(fruta);
            }
        }
        frutas.removeAll(comidas);
    }

    private void verificarColisiones() {
        if (vidas <= 0) return;

        for (Fantasma fantasma : fantasmas) {
            if (pacman.getPosicion().equals(fantasma.getPosicion())) {
                if (pacmanInvencible) {
                    puntuacion += 200;
                    reiniciarFantasma(fantasma);
                    soundManager.reproducirEfecto("pacman_comiendoFantasma.wav");
                } else {
                    vidas--;
                    pacman.morir();
                    soundManager.reproducirEfecto("pacman_muerto.wav");
                    if (vidas <= 0) {
                        juegoTimer.stop();
                        vista.actualizarEstado(puntuacion, vidas, true, false);
                        vista.mostrarBotonReiniciar();
                    } else {
                        reiniciarNivel();
                    }
                    break;
                }
            }
        }
    }

    private void reiniciarFantasma(Fantasma fantasma) {
        fantasma.setPosicion(fantasma.getPosicionInicial());
        fantasma.setDireccion(Direccion.aleatoria());
    }

    private Point buscarPosicionInicialPacman() {
        for (int f = 0; f < laberinto.getFilas(); f++) {
            for (int c = 0; c < laberinto.getColumnas(); c++) {
                if (laberinto.getDiseno()[f][c] == 'P') {
                    return new Point(c * 32, f * 32);
                }
            }
        }
        return new Point(0, 0);
    }

    private void reiniciarNivel() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        pacman.setPosicion(buscarPosicionInicialPacman());
        pacman.setDireccion(Direccion.NINGUNA);
        for (Fantasma fantasma : fantasmas) reiniciarFantasma(fantasma);

        pacmanInvencible = false;
        vista.actualizarEstado(puntuacion, vidas, false, false);
    }

    private void aplicarEfectoTunel(Personaje personaje) {
        int paso = 32;
        int columnas = laberinto.getDiseno()[0].length;
        int maxX = (columnas - 1) * paso;
        Point pos = personaje.getPosicion();

        if (pos.x < -paso / 2) {
            personaje.setPosicion(new Point(maxX, pos.y));
        } else if (pos.x > maxX + paso / 2) {
            personaje.setPosicion(new Point(0, pos.y));
        }
    }

    private Image cargarImagen(String nombreArchivo) {
        try {
            InputStream is = getClass().getResourceAsStream("/resources/imgs/" + nombreArchivo);
            if (is != null) return ImageIO.read(is);
            File archivo = new File("src/resources/imgs/" + nombreArchivo);
            if (archivo.exists()) return ImageIO.read(archivo);
            System.err.println("Imagen no encontrada: " + nombreArchivo);
        } catch (Exception e) {
            System.err.println("Error al cargar la imagen '" + nombreArchivo + "': " + e.getMessage());
        }
        return null;
    }
}
        