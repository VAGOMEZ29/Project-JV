package vista;

import java.awt.*;
import javax.swing.*;

import controlador.GameController;
import controlador.GameManager;
import modelo.GuardarPartida;
import java.util.List;

public class CargarPartidaPanel extends JPanel {
    private JList<String> listaPartidas;
    private List<GuardarPartida> partidas;
    private GameManager gameManager;

    public CargarPartidaPanel(GameManager gameManager) {
        this.gameManager = gameManager;
        setLayout(new BorderLayout());
        setBackground(new Color(10, 10, 30));

        JLabel titulo = new JLabel("Cargar Partida");
        titulo.setFont(new Font("Arial", Font.BOLD, 28));
        titulo.setForeground(Color.WHITE);
        titulo.setHorizontalAlignment(SwingConstants.CENTER);
        add(titulo, BorderLayout.NORTH);

        partidas = services.GuardarPartidaCSV.cargar();

        DefaultListModel<String> modelo = new DefaultListModel<>();
        for (int i = 0; i < partidas.size(); i++) {
            GuardarPartida partida = partidas.get(i);
            modelo.addElement("Partida " + (i + 1) + ": Nivel " + partida.getNivel() +
                    ", Puntos: " + partida.getPuntos() + ", Vidas: " + partida.getVidas());
        }

        listaPartidas = new JList<>(modelo);
        listaPartidas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaPartidas.setFont(new Font("Monospaced", Font.PLAIN, 16));
        JScrollPane scrollPane = new JScrollPane(listaPartidas);
        add(scrollPane, BorderLayout.CENTER);

        JPanel botones = new JPanel();
        botones.setBackground(new Color(10,10,30));

        JButton btnCargar = new JButton("Cargar Partida");
        btnCargar.addActionListener(e-> cargarSeleccionada());
        botones.add(btnCargar);

        JButton btnVolver = new JButton("Volver");
        btnVolver.addActionListener(e -> gameManager.mostrarMenu());
        botones.add(btnVolver);

        add(botones, BorderLayout.SOUTH);

    }
    
    private void cargarSeleccionada(){
        int index = listaPartidas.getSelectedIndex();
        if (index != -1){
            GuardarPartida partidaSeleccionada = partidas.get(index);
            
            GameController gameController = new GameController(gameManager);
            gameController.setPuntuacion(partidaSeleccionada.getPuntos());
            gameController.setVidas(partidaSeleccionada.getVidas());
            gameController.setEsPartidaCargada(true);

            gameManager.setGameController(gameController);
            gameManager.iniciarJuego();
        } else {
            JOptionPane.showMessageDialog(this, "Por favor, selecciona una partida para cargar.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

}
