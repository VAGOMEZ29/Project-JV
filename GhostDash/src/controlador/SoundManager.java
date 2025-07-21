package controlador;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * SoundManager gestiona todos los sonidos del juego: música de fondo y efectos.
 * Los efectos de sonido se precargan al inicio para una reproducción
 * instantánea
 * y un rendimiento óptimo, evitando la carga de archivos durante el juego.
 */
public class SoundManager {

    // Contendrá la música de fondo que esté sonando actualmente.
    private Clip musicaFondo;

    // Un mapa para almacenar todos los efectos de sonido precargados.
    // La clave es el nombre del archivo (ej: "pacman_muerto.wav")
    // El valor es el objeto Clip ya listo para ser reproducido.
    private final Map<String, Clip> efectosCargados;

    // --- Variables para el control del sonido de comer ---
    private long ultimoSonidoComer = 0;
    private static final int COOLDOWN_COMER_MS = 80; // Reducido para ser más responsivo

    /**
     * Constructor de SoundManager.
     * Al crearse, inicializa el mapa y llama al método para precargar
     * todos los efectos de sonido necesarios para el juego.
     */
    public SoundManager() {
        this.efectosCargados = new HashMap<>();
        precargarSonidos();
    }

    /**
     * Carga todos los archivos de efectos de sonido desde la carpeta de recursos
     * y los almacena en el mapa `efectosCargados` como objetos Clip.
     * Este método se llama una sola vez.
     */
    private void precargarSonidos() {
        System.out.println("🎵 Pre-cargando efectos de sonido...");
        cargarEfecto("pacman_comiendo.wav");
        cargarEfecto("pacman_muerto.wav");
        cargarEfecto("pacman_powerUp.wav");
        cargarEfecto("pacman_comiendoFantasma.wav");
        System.out.println("👍 Efectos de sonido cargados.");
    }

    /**
     * Método de utilidad interna para cargar un único archivo de sonido.
     *
     * @param nombreArchivo El nombre del fichero .wav en /resources/sonidos/
     */
    private void cargarEfecto(String nombreArchivo) {
        try (InputStream is = getClass().getResourceAsStream("/resources/sonidos/" + nombreArchivo)) {
            if (is == null) {
                System.err.println("🔊❌ Archivo de efecto no encontrado: " + nombreArchivo);
                return;
            }

            // Usamos un buffer para mejorar el rendimiento de la lectura del stream
            AudioInputStream audioInput = AudioSystem.getAudioInputStream(new BufferedInputStream(is));
            Clip clip = AudioSystem.getClip();
            clip.open(audioInput);

            // Guardamos el clip ya cargado en nuestro mapa
            efectosCargados.put(nombreArchivo, clip);

        } catch (UnsupportedAudioFileException | LineUnavailableException | java.io.IOException e) {
            System.err.println("🔊❌ Error al pre-cargar efecto '" + nombreArchivo + "': " + e.getMessage());
        }
    }

    /**
     * Reproduce una música de fondo desde /resources/sonidos.
     * La música se reproduce en bucle continuo. Si ya hay una sonando, se detiene.
     *
     * @param nombreArchivo El nombre del fichero .wav a reproducir.
     */
    public void reproducirMusicaFondo(String nombreArchivo) {
        // Detiene cualquier música que esté sonando antes de iniciar la nueva.
        detenerMusicaFondo();

        try (InputStream is = getClass().getResourceAsStream("/resources/sonidos/" + nombreArchivo)) {
            if (is == null) {
                System.err.println("🎵❌ Archivo de música no encontrado: " + nombreArchivo);
                return;
            }

            AudioInputStream audioInput = AudioSystem.getAudioInputStream(new BufferedInputStream(is));
            musicaFondo = AudioSystem.getClip();
            musicaFondo.open(audioInput);
            musicaFondo.loop(Clip.LOOP_CONTINUOUSLY); // Repetir en bucle 🔁
            musicaFondo.start();

        } catch (Exception e) {
            System.err.println("🎵❌ Error al reproducir música de fondo: " + e.getMessage());
        }
    }

    /**
     * Detiene y libera los recursos de la música de fondo si está sonando.
     */
    public void detenerMusicaFondo() {
        if (musicaFondo != null && musicaFondo.isRunning()) {
            musicaFondo.stop();
            musicaFondo.close();
        }
    }

    /**
     * Reproduce un efecto de sonido que ya ha sido precargado.
     *
     * @param nombreArchivo El nombre del efecto a reproducir (debe coincidir con el
     *                      precargado).
     */
    public void reproducirEfecto(String nombreArchivo) {
        Clip clip = efectosCargados.get(nombreArchivo);

        if (clip != null) {
            // Si el sonido ya se está reproduciendo, lo detenemos primero.
            // Esto es útil para sonidos que pueden ser llamados muy rápido.
            if (clip.isRunning()) {
                clip.stop();
            }
            // Rebobinamos el sonido al principio para que siempre suene completo.
            clip.setFramePosition(0);
            clip.start();
        } else {
            System.err.println("🔊❌ Se intentó reproducir un efecto no cargado: " + nombreArchivo);
        }
    }

    /**
     * Wrapper público para reproducir el sonido de comer.
     * Llama al método controlado para evitar la sobreposición de sonidos.
     */
    public void reproducirComer() {
        reproducirComerControlado();
    }

    /**
     * Reproduce el efecto de comer puntos, pero solo si ha pasado un
     * tiempo mínimo (cooldown) desde la última vez que sonó.
     * Esto evita que el sonido se sature y suene como una "ametralladora".
     */
    private void reproducirComerControlado() {
        long ahora = System.currentTimeMillis();
        // Si no ha pasado suficiente tiempo desde la última vez, no hacemos nada.
        if (ahora - ultimoSonidoComer < COOLDOWN_COMER_MS) {
            return;
        }

        Clip clipComer = efectosCargados.get("pacman_comiendo.wav");
        if (clipComer != null) {
            // No es necesario detenerlo porque el cooldown ya evita que se solape.
            clipComer.setFramePosition(0);
            clipComer.start();
            // Actualizamos la marca de tiempo de la última reproducción.
            ultimoSonidoComer = ahora;
        }
    }
}