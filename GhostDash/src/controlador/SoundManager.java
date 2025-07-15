package controlador;

import javax.sound.sampled.*;
import java.io.InputStream;
import java.io.BufferedInputStream;

/**
 * SoundManager gestiona todos los sonidos del juego:
 * música de fondo y efectos como comer puntos o perder.
 */
public class SoundManager {

    // --- Sonido de fondo ---
    private Clip musicaFondo;

    // --- Efecto controlado para comer (evita sobreposición) ---
    private Clip clipComer;
    private long ultimoSonidoComer = 0;
    private static final int COOLDOWN_COMER_MS = 250;

    /**
     * Reproduce una música de fondo desde /resources/sonidos.
     * Solo una música puede sonar a la vez.
     */
    public void reproducirMusicaFondo(String nombreArchivo) {
        detenerMusicaFondo();

        try (InputStream is = getClass().getResourceAsStream("/resources/sonidos/" + nombreArchivo)) {
            if (is == null) {
                System.err.println("🎵❌ Archivo de música no encontrado: " + nombreArchivo);
                return;
            }

            AudioInputStream audioInput = AudioSystem.getAudioInputStream(new BufferedInputStream(is));
            musicaFondo = AudioSystem.getClip();
            musicaFondo.open(audioInput);
            musicaFondo.loop(Clip.LOOP_CONTINUOUSLY); // 🔁 Repetir en bucle
            musicaFondo.start();

        } catch (Exception e) {
            System.err.println("🎵❌ Error al reproducir música de fondo: " + e.getMessage());
        }
    }

    /**
     * Detiene y cierra la música de fondo si está sonando.
     */
    public void detenerMusicaFondo() {
        if (musicaFondo != null && musicaFondo.isRunning()) {
            musicaFondo.stop();
            musicaFondo.close();
        }
    }

    /**
     * Reproduce un efecto de sonido (no se repite, suena una vez).
     * Ideal para efectos como muerte, frutas, etc.
     */
    public void reproducirEfecto(String nombreArchivo) {
        // Control especial para sonido de comer (no sobreponer)
        if ("pacman_comiendo.wav".equals(nombreArchivo)) {
            reproducirComerControlado();
            return;
        }

        try (InputStream is = getClass().getResourceAsStream("/resources/sonidos/" + nombreArchivo)) {
            if (is == null) {
                System.err.println("🔊❌ Efecto no encontrado: " + nombreArchivo);
                return;
            }

            AudioInputStream audioInput = AudioSystem.getAudioInputStream(new BufferedInputStream(is));
            Clip clip = AudioSystem.getClip();
            clip.open(audioInput);
            clip.start();

        } catch (Exception e) {
            System.err.println("🔊❌ Error al reproducir efecto: " + e.getMessage());
        }
    }

    /**
     * Reproduce el efecto de comer puntos con control de tiempo
     * para evitar que suene muchas veces seguidas.
     */
    private void reproducirComerControlado() {
        long ahora = System.currentTimeMillis();
        if (ahora - ultimoSonidoComer < COOLDOWN_COMER_MS) return;

        try {
            if (clipComer == null || !clipComer.isOpen()) {
                InputStream is = getClass().getResourceAsStream("/resources/sonidos/pacman_comiendo.wav");
                if (is == null) {
                    System.err.println("🍒❌ Sonido de comer no encontrado");
                    return;
                }

                AudioInputStream audioInput = AudioSystem.getAudioInputStream(new BufferedInputStream(is));
                clipComer = AudioSystem.getClip();
                clipComer.open(audioInput);
            }

            if (clipComer.isRunning()) return;

            clipComer.setFramePosition(0); // Reiniciar desde el principio
            clipComer.start();
            ultimoSonidoComer = ahora;

        } catch (Exception e) {
            System.err.println("🍒❌ Error al reproducir sonido de comer: " + e.getMessage());
        }
    }
    public void reproducirComer() {
        reproducirComerControlado();
    }
}
