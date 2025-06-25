package Modelo;

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
}