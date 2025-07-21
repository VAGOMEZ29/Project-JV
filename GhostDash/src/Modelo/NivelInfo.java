package modelo;

import java.awt.Point;
import java.util.List;

/**
 * Clase contenedora de información del nivel actual.
 * Esta clase fue creada para agrupar todos los elementos necesarios para
 * cargar un nivel completo de forma ordenada y estructurada.
 * En lugar de devolver cada objeto (laberinto, PacMan, fantasmas, etc.)
 * por separado, ahora se encapsulan dentro de una sola instancia de
 * `NivelInfo`.
 */
public class NivelInfo {

    private Laberinto laberinto;
    private PacMan pacman;
    private List<Fantasma> fantasmas;
    private List<Fruta> frutas;

    private List<Punto> puntos;
    private List<PowerUp> powerUps;

    private Point posicionFruta; // ¡NUEVO CAMPO!

    // Recibe todos los elementos del nivel como parámetros y los almacena.
    public NivelInfo(Laberinto laberinto, PacMan pacman,
            List<Fantasma> fantasmas, List<Fruta> frutas,
            List<Punto> puntos, List<PowerUp> powerUps, Point posicionFruta) {
        this.laberinto = laberinto;
        this.pacman = pacman;
        this.fantasmas = fantasmas;
        this.frutas = frutas;
        this.puntos = puntos;
        this.powerUps = powerUps;
        this.posicionFruta = posicionFruta;
    }

    // Métodos getters para acceder a cada elemento del nivel:

    public Point getPosicionFruta() {
        return posicionFruta;
    }

    public Laberinto getLaberinto() {
        return laberinto;
    }

    public PacMan getPacman() {
        return pacman;
    }

    public List<Fantasma> getFantasmas() {
        return fantasmas;
    }

    public List<Fruta> getFrutas() {
        return frutas;
    }

    public List<Punto> getPuntos() {
        return puntos;
    }

    public List<PowerUp> getPowerUps() {
        return powerUps;
    }
}