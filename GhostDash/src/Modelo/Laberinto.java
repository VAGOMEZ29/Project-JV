package modelo;

import java.awt.Point;

public class Laberinto {
    private int numeroNivel;
    private int dificultad;
    private char[][] diseno;

    public int getNumeroNivel() {
        return numeroNivel;
    }

    public int getDificultad() {
        return dificultad;
    }

    public char[][] getDiseno() {
        return diseno;
    }

    public void setDiseno(char[][] diseno) {
        this.diseno = diseno;
    }

    public boolean cargarNivel(int numero) {
        this.numeroNivel = numero;
        // Carga datos del nivel
        return true;
    }

    public boolean estaCompletado() {
        // Verifica si el nivel se completó
        return false;
    }

    public boolean puedeMover(Point posicion, Direccion direccion) {
        int paso = 32; // El tamaño de cada celda
        int nuevaX = posicion.x + direccion.getDx() * paso;
        int nuevaY = posicion.y + direccion.getDy() * paso;

        int columnas = diseno[0].length;
        int filas = diseno.length;

        // Permite el movimiento a través del túnel horizontal
        if ((direccion == Direccion.IZQUIERDA && posicion.x <= 0) ||
                (direccion == Direccion.DERECHA && posicion.x >= (columnas - 1) * paso)) {
            return true;
        }

        int col = nuevaX / paso;
        int fila = nuevaY / paso;

        if (fila < 0 || fila >= filas || col < 0 || col >= columnas)
            return false;

        return diseno[fila][col] != 'X';
    }
}