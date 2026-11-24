package com.mycompany.edureport.interfaz;
import com.mycompany.edureport.*;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.mycompany.edureport.dao.AlumnoDAO;
import com.mycompany.edureport.dao.AulaDAO;
import com.mycompany.edureport.dao.ReporteDAO;
import com.mycompany.edureport.modelo.Aula;

import javax.swing.*;
import java.awt.*;

/**
 * Ventana principal. Solo coordina la navegación entre las dos pantallas;
 * cada una construye su propia interfaz y carga sus datos en segundo plano:
 * <ul>
 *   <li>{@link PanelAulas}: cuadrícula de aulas.</li>
 *   <li>{@link PanelAlumnos}: lista de alumnos y detalle de reportes
 *       ({@link PanelDetalleAlumno}).</li>
 * </ul>
 * La paleta vive en {@link Tema}, la escala de colores de los reportes en {@link Severidad}
 * y las reglas de borrado en {@link ServicioAcademico}.
 */
public class Interfaz extends JFrame {

    private static final String VISTA_AULAS = "AULAS";
    private static final String VISTA_ALUMNOS = "ALUMNOS";

    // Se ejecuta una sola vez, antes de crear cualquier componente Swing.
    static {
        configurarEstilo();
    }

    /** Activa el Look and Feel moderno. Es seguro llamarlo más de una vez. */
    public static void configurarEstilo() {
        if (Tema.esOscuro()) {
            FlatDarkLaf.setup();
        } else {
            FlatLightLaf.setup();
        }
        UIManager.put("Button.arc", 12);
        UIManager.put("Component.arc", 12);
        UIManager.put("TextComponent.arc", 12);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.width", 12);
    }

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contenedor = new JPanel(cardLayout);
    private final ServicioAcademico servicio;
    private PanelAulas panelAulas;
    private PanelAlumnos panelAlumnos;
    private Aula aulaAbierta;

    public Interfaz() {
        servicio = new ServicioAcademico(new AulaDAO(), new AlumnoDAO(), new ReporteDAO());
        construirVistas();

        setTitle("EduReport");
        java.net.URL iconoUrl = getClass().getResource("/EduReportIsotipo.png");
        if (iconoUrl != null) {
            setIconImage(new ImageIcon(iconoUrl).getImage());
        }
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 650);
        setMinimumSize(new Dimension(800, 500));
        setLocationRelativeTo(null);
        getContentPane().setBackground(Tema.FONDO);
        add(contenedor);

        panelAulas.recargar();
    }

    private void abrirAula(Aula aula) {
        aulaAbierta = aula;
        panelAlumnos.mostrarAula(aula);
        cardLayout.show(contenedor, VISTA_ALUMNOS);
    }

    void alternarTema() {
        Tema.alternar();
        configurarEstilo();
        construirVistas();
        if (aulaAbierta == null) {
            panelAulas.recargar();
            cardLayout.show(contenedor, VISTA_AULAS);
        } else {
            panelAlumnos.mostrarAula(aulaAbierta);
            cardLayout.show(contenedor, VISTA_ALUMNOS);
        }
        revalidate();
        repaint();
    }

    private void construirVistas() {
        panelAulas = new PanelAulas(this, servicio, this::abrirAula);
        panelAlumnos = new PanelAlumnos(this, servicio, this::volverAAulas);
        contenedor.removeAll();
        contenedor.add(panelAulas, VISTA_AULAS);
        contenedor.add(panelAlumnos, VISTA_ALUMNOS);
    }

    /** Al volver se recargan las aulas para que los contadores reflejen los cambios. */
    private void volverAAulas() {
        aulaAbierta = null;
        panelAulas.recargar();
        cardLayout.show(contenedor, VISTA_AULAS);
    }
}
