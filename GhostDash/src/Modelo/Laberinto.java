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

    public int getFilas() {
        return diseno.length;
    }

    public int getColumnas() {
        return diseno[0].length;
    }

    public boolean puedeMover(Point posicion, Direccion direccion) {
        int paso = 32;
        int nuevaX = posicion.x + direccion.getDx() * paso;
        int nuevaY = posicion.y + direccion.getDy() * paso;
        int columnas = diseno[0].length;
        int filas = diseno.length;

        if ((direccion == Direccion.IZQUIERDA && posicion.x <= 0) ||
                (direccion == Direccion.DERECHA && posicion.x >= (columnas - 1) * paso)) {
            return true;
        }

        int col = nuevaX / paso;
        int fila = nuevaY / paso;

        if (fila < 0 || fila >= filas || col < 0 || col >= columnas) {
            return false;
        }
        return diseno[fila][col] != 'X';
    }

    public boolean esInterseccion(Point posicion) {
        if (posicion.x % 32 != 0 || posicion.y % 32 != 0) {
            return false;
        }
        int contadorSalidas = 0;
        for (Direccion dir : Direccion.values()) {
            if (dir != Direccion.NINGUNA) {
                if (puedeMover(posicion, dir)) {
                    contadorSalidas++;
                }
            }
        }
        return contadorSalidas > 2;
    }
}