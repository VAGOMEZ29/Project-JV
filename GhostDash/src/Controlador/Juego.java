package Controlador;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import Modelo.Jugador;
import Modelo.Partida;
import Modelo.Ranking;

public class Juego {
    private List<Jugador> jugadores = new ArrayList<>();
    private Ranking ranking = new Ranking();
    private Partida partidaActual;
    private Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        Juego juego = new Juego();
        juego.mostrarMenuPrincipal();
    }

    public void mostrarMenuPrincipal() {
        while (true) {
            System.out.println("=== GHOSTDASH ===");
            System.out.println("1. Registrar jugador");
            System.out.println("2. Iniciar partida");
            System.out.println("3. Ver ranking");
            System.out.println("4. Salir");
            System.out.print("Seleccione: ");
            int opcion = scanner.nextInt();
            scanner.nextLine();
            switch (opcion) {
                case 1 -> registrarJugador();
                case 2 -> iniciarPartida();
                case 3 -> verRanking();
                case 4 -> {
                    System.out.println("Adiós");
                    return;
                }
                default -> System.out.println("Opción inválida");
            }
        }
    }

    public void registrarJugador() {
        System.out.print("Nombre de usuario: ");
        String nombre = scanner.nextLine();
        Jugador j = new Jugador();
        j.registrar(nombre);
        j.iniciarSesion();
        jugadores.add(j);
    }

    public void iniciarPartida() {
        if (jugadores.isEmpty()) {
            System.out.println("Debe registrar jugadores antes.");
            return;
        }
        System.out.println("Seleccione jugador:");
        for (int i = 0; i < jugadores.size(); i++) {
            System.out.println((i + 1) + ". " + jugadores.get(i).getNombreUsuario());
        }
        int sel = scanner.nextInt();
        Jugador elegido = jugadores.get(sel - 1);
        elegido.iniciarSesion();

        partidaActual = new Partida();
        partidaActual.iniciar();

        System.out.println("Partida iniciada para " + elegido.getNombreUsuario());
        System.out.println("Nivel: " + partidaActual.getNivelActual());

        while (partidaActual.getVidas() > 0) {
            System.out.print("Puntos ganados (0 para perder vida): ");
            int pts = scanner.nextInt();
            if (pts == 0) {
                partidaActual.reiniciarCombo();
                System.out.println("¡Vida perdida!");
            } else {
                partidaActual.incrementarPuntuacion(pts);
                System.out.println("Puntaje actual: " + partidaActual.getPuntuacion());
            }
        }

        partidaActual.finalizar();
        elegido.actualizarEstadisticas(partidaActual.getPuntuacion(), partidaActual.getNivelActual());
        ranking.agregarPuntaje(elegido, partidaActual.getPuntuacion());
        System.out.println("Juego terminado. Puntaje: " + partidaActual.getPuntuacion());
    }

    public void verRanking() {
        System.out.println("=== RANKING ===");
        var top = ranking.obtenerMejoresPuntajes();
        for (int i = 0; i < top.size(); i++) {
            var entry = top.get(i);
            System.out.println((i + 1) + ". " + entry.getKey().getNombreUsuario() + ": " + entry.getValue() + " pts");
        }
    }
}
