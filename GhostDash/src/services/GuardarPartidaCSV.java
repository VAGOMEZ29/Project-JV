package services;


import modelo.GuardarPartida;
import java.io.*;
import java.util.*;

public class GuardarPartidaCSV {
    private static final String ARCHIVO = "partidas.csv";

    public static void guardar(GuardarPartida partida) {
        try (FileWriter writer = new FileWriter(ARCHIVO, true)) {
            writer.append(partida.getNivel() + "," + partida.getPuntos() + "," + partida.getVidas() + "\n");
            System.out.println("Partida guardada en CSV.");
        } catch (IOException e) {
            System.out.println("Error al guardar: " + e.getMessage());
        }
    }

    public static List<GuardarPartida> cargar() {
        List<GuardarPartida> partidas = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(ARCHIVO))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                String[] datos = linea.split(",");
                if (datos.length == 3) {
                    int nivel = Integer.parseInt(datos[0]);
                    int puntos = Integer.parseInt(datos[1]);
                    int vidas = Integer.parseInt(datos[2]);
                    partidas.add(new GuardarPartida(nivel, puntos, vidas));
                }
            }
            System.out.println("Partidas cargadas desde CSV.");
        } catch (IOException e) {
            System.out.println("Error al leer CSV: " + e.getMessage());
        }

        return partidas;
    }

    
}
