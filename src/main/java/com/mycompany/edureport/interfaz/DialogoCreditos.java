package com.mycompany.edureport.interfaz;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.net.URI;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class DialogoCreditos extends JDialog {

    private static final String TWITTER_URL = "https://x.com/Snufmin44";
    private static final String GITHUB_URL = "https://github.com/koharusakuragi666XD/EduReport";

    public DialogoCreditos(JFrame parent) {
        super(parent, "Créditos", true);
        setResizable(false);
        setLayout(new BorderLayout(12, 12));

        JPanel contenido = new JPanel();
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        contenido.setOpaque(false);
        contenido.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JLabel titulo = cargarLogo("/EduReportLogo.png", 220, 119);
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel desarrollador = crearLinea("Desarrollado por: Sergio Said López Calvario");
        JLabel artista = crearLinea("Logotipo e isotipo por: Constanza Ureste Osorio");

        JButton btnTwitter = Tema.botonSecundario("Twitter del artista");
        btnTwitter.setIcon(cargarIcono("/logo-white.png", "/logo-black.png"));
        btnTwitter.setHorizontalTextPosition(JButton.RIGHT);
        btnTwitter.setIconTextGap(10);
        btnTwitter.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnTwitter.addActionListener(e -> abrirEnNavegador(TWITTER_URL));

        JLabel mensajeRepo = crearLinea("Visita el repositorio del proyecto:");

        JButton btnGithub = Tema.botonSecundario("GitHub");
        btnGithub.setIcon(cargarIcono(
                "/GitHub_Invertocat_White.png",
                "/GitHub_Invertocat_Black.png"));
        btnGithub.setHorizontalTextPosition(JButton.RIGHT);
        btnGithub.setIconTextGap(10);
        btnGithub.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnGithub.addActionListener(e -> abrirEnNavegador(GITHUB_URL));

        contenido.add(titulo);
        contenido.add(Box.createVerticalStrut(16));
        contenido.add(desarrollador);
        contenido.add(Box.createVerticalStrut(6));
        contenido.add(artista);
        contenido.add(Box.createVerticalStrut(18));
        contenido.add(btnTwitter);
        contenido.add(Box.createVerticalStrut(18));
        contenido.add(mensajeRepo);
        contenido.add(Box.createVerticalStrut(8));
        contenido.add(btnGithub);

        JButton cerrar = Tema.botonPrimario("Cerrar");
        cerrar.addActionListener(e -> dispose());

        JPanel pie = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pie.setOpaque(false);
        pie.setBorder(BorderFactory.createEmptyBorder(0, 12, 12, 12));
        pie.add(cerrar);

        add(contenido, BorderLayout.CENTER);
        add(pie, BorderLayout.SOUTH);

        pack();
        setMinimumSize(new Dimension(420, 320));
        setLocationRelativeTo(parent);
    }

    private JLabel crearLinea(String texto) {
        JLabel etiqueta = new JLabel(texto);
        Tema.fuente(etiqueta, Font.PLAIN, 14f);
        etiqueta.setForeground(Tema.TEXTO);
        etiqueta.setAlignmentX(Component.CENTER_ALIGNMENT);
        return etiqueta;
    }

    private JLabel cargarLogo(String recurso, int ancho, int alto) {
        java.net.URL url = getClass().getResource(recurso);
        if (url == null) {
            JLabel etiqueta = new JLabel("EduReport");
            Tema.fuente(etiqueta, Font.BOLD, 22f);
            etiqueta.setForeground(Tema.PRIMARIO);
            return etiqueta;
        }

        Image imagen = new ImageIcon(url).getImage()
                .getScaledInstance(ancho, alto, Image.SCALE_SMOOTH);
        return new JLabel(new ImageIcon(imagen));
    }

    private static ImageIcon cargarIcono(String recursoOscuro, String recursoClaro) {
        String recurso = Tema.esOscuro() ? recursoOscuro : recursoClaro;

        java.net.URL url = DialogoCreditos.class.getResource(recurso);
        if (url == null) {
            return null;
        }

        ImageIcon icono = new ImageIcon(url);
        Image imagen = icono.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
        return new ImageIcon(imagen);
    }

    private static void abrirEnNavegador(String url) {
        try {
            if (Desktop.isDesktopSupported()
                    && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                JOptionPane.showMessageDialog(
                        null,
                        "No se pudo abrir el navegador.\nVisita:\n" + url,
                        "Enlace",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    null,
                    "No se pudo abrir el enlace:\n" + url,
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
