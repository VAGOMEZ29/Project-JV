package vista;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class MenuPrincipal extends JPanel {
    private JButton btnJugar, btnInstrucciones, btnSalir;
    private JComboBox<String> comboCategorias;

    public MenuPrincipal(ActionListener manejador) {
        setLayout(new GridBagLayout());
        setBackground(new Color(10, 10, 30)); // Fondo oscuro azulado
        
        // Panel central con efecto de vidrio
        JPanel centerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                
                // Fondo semi-transparente
                g2d.setColor(new Color(20, 20, 40, 200));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                
                // Borde neon
                g2d.setStroke(new BasicStroke(3));
                g2d.setColor(new Color(0, 200, 255, 100));
                g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 30, 30);
            }
        };
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));
        centerPanel.setOpaque(false);

        // Título con efecto neon
        JLabel titulo = new JLabel("GHOST DASH") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                
                // Efecto sombra neon
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
                                   RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2d.setFont(getFont());
                
                for (int i = 0; i < 5; i++) {
                    g2d.setColor(new Color(0, 200, 255, 50 - i*10));
                    g2d.drawString(getText(), i+1, i+1 + g2d.getFontMetrics().getAscent());
                }
                
                // Texto principal
                g2d.setColor(Color.WHITE);
                super.paintComponent(g2d);
            }
        };
        titulo.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 48));
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        titulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        
        centerPanel.add(titulo);

        // ComboBox estilizado
        comboCategorias = new JComboBox<>(new String[]{"CLÁSICO", "MEJORADO", "MULTIJUGADOR"});
        comboCategorias.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, 
                    int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                label.setBackground(isSelected ? new Color(50, 50, 100) : new Color(30, 30, 60));
                label.setForeground(Color.WHITE);
                label.setFont(new Font("Arial", Font.BOLD, 16));
                label.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
                return label;
            }
        });
        comboCategorias.setMaximumSize(new Dimension(250, 40));
        comboCategorias.setAlignmentX(Component.CENTER_ALIGNMENT);
        comboCategorias.setBackground(new Color(30, 30, 60));
        comboCategorias.setForeground(Color.WHITE);
        comboCategorias.setFont(new Font("Arial", Font.BOLD, 16));
        comboCategorias.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        
        centerPanel.add(comboCategorias);
        centerPanel.add(Box.createVerticalStrut(40));

        // Botones con efecto hover
        btnJugar = createHoverButton("JUGAR", new Color(255, 215, 0), manejador);
        centerPanel.add(btnJugar);
        centerPanel.add(Box.createVerticalStrut(20));

        btnInstrucciones = createHoverButton("INSTRUCCIONES", new Color(100, 200, 255), manejador);
        centerPanel.add(btnInstrucciones);
        centerPanel.add(Box.createVerticalStrut(20));

        btnSalir = createHoverButton("SALIR", new Color(255, 100, 100), manejador);
        centerPanel.add(btnSalir);

        add(centerPanel);
    }

    private JButton createHoverButton(String text, Color baseColor, ActionListener listener) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                
                // Efecto hover
                if (getModel().isRollover()) {
                    g2.setColor(baseColor.brighter());
                } else {
                    g2.setColor(baseColor);
                }
                
                // Fondo redondeado
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                
                // Texto centrado
                g2.setColor(Color.BLACK);
                g2.setFont(new Font("Arial", Font.BOLD, 18));
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(text, x, y);
            }
            
            @Override
            protected void paintBorder(Graphics g) {
                // Sin borde
            }
        };
        
        button.setContentAreaFilled(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 50, 10, 50));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.addActionListener(listener);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        return button;
    }

    // Getters 
    public JButton getBtnJugar() { return btnJugar; }
    public JButton getBtnInstrucciones() { return btnInstrucciones; }
    public JButton getBtnSalir() { return btnSalir; }
    public String getCategoriaSeleccionada() { 
        return comboCategorias.getSelectedItem().toString(); 
    }
}