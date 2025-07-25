package modelo;
import java.io.*;

public class GuardarPartida implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int nivel;
    private int puntos;
    private int vidas;

    public GuardarPartida(int nivel, int puntos, int vidas) {
        this.nivel = nivel;
        this.puntos = puntos;
        this.vidas = vidas;
    }
    public int getNivel() {return nivel;}
    public int getPuntos() {return puntos;}
    public int getVidas() {return vidas;}

    @Override
    public String toString() {
        return "Nivel: "+ nivel + ", Puntos: " + puntos + ", Vidas: " + vidas;
    }
}
