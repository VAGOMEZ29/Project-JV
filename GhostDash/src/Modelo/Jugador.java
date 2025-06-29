package Modelo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Jugador {
    private String nombreUsuario;
    private int puntajeMaximo;
    private int nivelesSuperados;
    private long tiempoJugado; // en segundos
    private int enemigosVencidos;
    private LocalDateTime fechaUltimoIngreso;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public Jugador() {
        this.fechaUltimoIngreso = LocalDateTime.now();
    }

    public Jugador(String nombreUsuario) {
        this();
        this.nombreUsuario = nombreUsuario;
    }

    // Métodos de acceso
    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public int getPuntajeMaximo() {
        return puntajeMaximo;
    }

    public int getNivelesSuperados() {
        return nivelesSuperados;
    }

    public long getTiempoJugado() {
        return tiempoJugado;
    }

    public int getEnemigosVencidos() {
        return enemigosVencidos;
    }

    public LocalDateTime getFechaUltimoIngreso() {
        return fechaUltimoIngreso;
    }

    public String getFechaUltimoIngresoFormateada() {
        if (fechaUltimoIngreso == null)
            return "Nunca";
        return fechaUltimoIngreso.format(DATE_FORMATTER);
    }

    // Métodos de modificación
    public void registrar(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
        this.fechaUltimoIngreso = LocalDateTime.now();
    }

    public void iniciarSesion() {
        this.fechaUltimoIngreso = LocalDateTime.now();
    }

    public void actualizarEstadisticas(int puntuacion, int nivel) {
        if (puntuacion > puntajeMaximo) {
            puntajeMaximo = puntuacion;
        }
        if (nivel > nivelesSuperados) {
            nivelesSuperados = nivel;
        }
    }

    public void incrementarEnemigosVencidos(int cantidad) {
        this.enemigosVencidos += cantidad;
    }

    public void agregarTiempoJugado(long segundos) {
        this.tiempoJugado += segundos;
    }

    @Override
    public String toString() {
        return String.format("Jugador: %s | Puntaje Máximo: %d | Niveles: %d | Último ingreso: %s",
                nombreUsuario, puntajeMaximo, nivelesSuperados, getFechaUltimoIngresoFormateada());
    }
}