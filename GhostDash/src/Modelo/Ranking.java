package modelo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Ranking {
    private Map<Jugador, Integer> puntajes = new HashMap<>();

    public void agregarPuntaje(Jugador jugador, int puntaje) {
        puntajes.put(jugador, Math.max(puntajes.getOrDefault(jugador, 0), puntaje));
    }

    public List<Map.Entry<Jugador, Integer>> obtenerMejoresPuntajes() {
        return puntajes.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))
                .toList();
    }
}
