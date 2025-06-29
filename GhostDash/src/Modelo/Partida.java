package Modelo;

public class Partida {
    private int puntuacion;
    private int vidas;
    private EstadoPartida estado;
    private int combos;
    private int nivelActual = 1;

    public EstadoPartida getEstado() {
        return estado;
    }

    public void iniciar() {
        vidas = 3;
        puntuacion = 0;
        combos = 0;
        estado = EstadoPartida.EN_CURSO;
    }

    public void finalizar() {
        estado = EstadoPartida.FINALIZADA;
    }

    public void actualizarEstado() {
        // Actualizar estado de la partida
    }

    public void incrementarPuntuacion(int puntos) {
        puntuacion += puntos;
        combos++;
        if (combos % 5 == 0) {
            puntuacion += 50; // Bonificación por combo
        }
    }

    public void reiniciarCombo() {
        combos = 0;
    }

    public void avanzarNivel() {
        nivelActual++;
    }

    public int getPuntuacion() {
        return puntuacion;
    }

    public int getNivelActual() {
        return nivelActual;
    }

    public int getVidas() {
        return vidas;
    }

    public void perderVida() {
        vidas--;
    }
}