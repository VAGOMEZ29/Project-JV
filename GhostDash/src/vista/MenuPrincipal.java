package vista;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class MenuPrincipal extends JPanel {

    private JButton btnJugar;
    private JButton btnInstrucciones;
    private JButton btnSalir;
    private JComboBox<String> comboCategorias;

    public MenuPrincipal(ActionListener manejador) {
        setPreferredSize(new Dimension(672, 704));
        setBackground(Color.BLACK);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JLabel titulo = new JLabel("GhostDash");
        titulo.setFont(new Font("Arial", Font.BOLD, 48));
        titulo.setForeground(Color.YELLOW);
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        titulo.setBorder(BorderFactory.createEmptyBorder(50, 0, 30, 0));

        add(titulo);

        comboCategorias = new JComboBox<>(new String[]{"Clásico", "Mejorado", "Multijugador"});
        comboCategorias.setMaximumSize(new Dimension(200, 30));
        comboCategorias.setAlignmentX(Component.CENTER_ALIGNMENT);
        comboCategorias.setFocusable(false);
        add(comboCategorias);
        add(Box.createVerticalStrut(30)); // Espacio

        btnJugar = crearBoton("Jugar", manejador);
        btnInstrucciones = crearBoton("Instrucciones", manejador);
        btnSalir = crearBoton("Salir", manejador);

        add(btnJugar);
        add(Box.createVerticalStrut(15));
        add(btnInstrucciones);
        add(Box.createVerticalStrut(15));
        add(btnSalir);
    }

    private JButton crearBoton(String texto, ActionListener manejador) {
        JButton boton = new JButton(texto);
        boton.setAlignmentX(Component.CENTER_ALIGNMENT);
        boton.setMaximumSize(new Dimension(200, 40));
        boton.setFocusable(false);
        boton.addActionListener(manejador);
        return boton;
    }

    public JButton getBtnJugar() {
        return btnJugar;
    }

    public JButton getBtnInstrucciones() {
        return btnInstrucciones;
    }

    public JButton getBtnSalir() {
        return btnSalir;
    }

    public String getCategoriaSeleccionada() {
        return comboCategorias.getSelectedItem().toString();
    }
}
