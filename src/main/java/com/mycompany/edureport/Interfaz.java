package com.mycompany.edureport;

import com.mycompany.edureport.dao.AlumnoDAO;
import com.mycompany.edureport.dao.AulaDAO;
import com.mycompany.edureport.dao.ReporteDAO;
import com.mycompany.edureport.modelo.Alumno;
import com.mycompany.edureport.modelo.Aula;
import com.mycompany.edureport.modelo.Reporte;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Ventana principal y coordinador de los flujos de usuario.
 *
 * <p>Mantiene el estado de navegación (aula y alumno seleccionados), escucha
 * acciones de Swing y delega persistencia a los DAO. Los diálogos reciben
 * callbacks {@code Runnable} para solicitar una recarga sin conocer esta clase.</p>
 */
public class Interfaz extends JFrame {

    private AulaDAO aulaDAO;
    private AlumnoDAO alumnoDAO;
    private ReporteDAO reporteDAO;

    private Aula aulaSeleccionada;
    private Alumno alumnoSeleccionado;

    private JPanel pnlPrincipal;
    private CardLayout cardLayout;

    private JPanel pnlDashboard;
    private JPanel pnlBotonesAulas;
    private JLabel lblTitulo;
    private JButton btnSalir;
    private JButton btnNuevaAula;
    private JButton btnImportarExcelMasivo;

    private JPanel pnlAlumnos;
    private JLabel lblNombreAula;
    private JButton btnVolver;
    private JButton btnNuevoAlumno;
    private JButton btnEliminarAlumno;
    private JButton btnExportarPDF;
    private JButton btnImportarExcel;
    private DefaultListModel<Alumno> modeloLista;
    private JList<Alumno> lstAlumnos;

    private JPanel pnlDetalle;
    private JLabel lblNombreAlumno;
    private JLabel lblContadorReportes;
    private JButton btnNuevoReporte;
    private JPanel pnlReportes;

    public Interfaz() {
        // La ventana central crea los DAO y coordina las pantallas mediante CardLayout.
        aulaDAO = new AulaDAO();
        alumnoDAO = new AlumnoDAO();
        reporteDAO = new ReporteDAO();
        initComponents();
        cargarAulas();
    }

    private void initComponents() {
        setTitle("EduReport");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 600);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        pnlPrincipal = new JPanel(cardLayout);

        construirDashboard();
        construirPanelAlumnos();

        pnlPrincipal.add(pnlDashboard, "DASHBOARD");
        pnlPrincipal.add(pnlAlumnos, "ALUMNOS");

        add(pnlPrincipal);
    }

    private void construirDashboard() {
        pnlDashboard = new JPanel(new BorderLayout());

        lblTitulo = new JLabel("EduReport - Selecciona un Aula", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        pnlBotonesAulas = new JPanel(new GridLayout(0, 3, 15, 15));
        pnlBotonesAulas.setBorder(BorderFactory.createEmptyBorder(20, 40, 40, 40));

        btnNuevaAula = new JButton("+ Nueva Aula");
        btnNuevaAula.setFont(new Font("Arial", Font.PLAIN, 13));
        btnNuevaAula.setBackground(new Color(60, 160, 80));
        btnNuevaAula.setForeground(Color.WHITE);
        btnNuevaAula.addActionListener(e -> {
            new DialogoNuevaAula(this, aulaDAO, this::cargarAulas).setVisible(true);
        });

        btnImportarExcelMasivo = new JButton("Importar Excel");
        btnImportarExcelMasivo.setFont(new Font("Arial", Font.PLAIN, 13));
        btnImportarExcelMasivo.setBackground(new Color(100, 80, 180));
        btnImportarExcelMasivo.setForeground(Color.WHITE);
        btnImportarExcelMasivo.addActionListener(e ->
                ImportadorExcel.importarAulasYAlumnos(this, aulaDAO, alumnoDAO, this::cargarAulas)
        );

        btnSalir = new JButton("Salir");
        btnSalir.setFont(new Font("Arial", Font.PLAIN, 13));
        btnSalir.setBackground(new Color(200, 60, 60));
        btnSalir.setForeground(Color.WHITE);
        btnSalir.addActionListener(e -> {
            int confirmar = JOptionPane.showConfirmDialog(this,
                    "¿Deseas salir de EduReport?", "Salir",
                    JOptionPane.YES_NO_OPTION);
            if (confirmar == JOptionPane.YES_OPTION) System.exit(0);
        });

        JPanel pnlSur = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlSur.add(btnImportarExcelMasivo);
        pnlSur.add(btnNuevaAula);
        pnlSur.add(btnSalir);

        pnlDashboard.add(lblTitulo, BorderLayout.NORTH);
        pnlDashboard.add(pnlBotonesAulas, BorderLayout.CENTER);
        pnlDashboard.add(pnlSur, BorderLayout.SOUTH);
    }

    /** Vuelve a consultar MongoDB y reconstruye los botones del dashboard. */
    private void cargarAulas() {
        List<Aula> aulas = aulaDAO.obtenerTodas();
        pnlBotonesAulas.removeAll();
        for (Aula aula : aulas) {
            JButton btn = new JButton(
                    "<html><center>" + aula.getEspecialidad() +
                            "<br>" + aula.getGrado() + "° " + aula.getGrupo() +
                            "<br>(" + aula.getTurno() + ")</center></html>"
            );
            btn.setFont(new Font("Arial", Font.PLAIN, 13));
            btn.setPreferredSize(new Dimension(180, 80));
            btn.addActionListener(e -> seleccionarAula(aula));

            JPopupMenu menu = new JPopupMenu();
            JMenuItem itemEliminar = new JMenuItem("Eliminar Aula");
            itemEliminar.setForeground(new Color(200, 60, 60));
            itemEliminar.addActionListener(e -> eliminarAula(aula));
            menu.add(itemEliminar);
            btn.setComponentPopupMenu(menu);

            pnlBotonesAulas.add(btn);
        }
        pnlBotonesAulas.revalidate();
        pnlBotonesAulas.repaint();
    }

    private void eliminarAula(Aula aula) {
        int confirmar = JOptionPane.showConfirmDialog(this,
                "¿Eliminar el aula " + aula + "?\nSe eliminarán todos sus alumnos y reportes.",
                "Confirmar eliminacion", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (confirmar != JOptionPane.YES_OPTION) return;

        // MongoDB no elimina automaticamente estas referencias: se limpian de forma manual.
        List<Alumno> alumnos = alumnoDAO.obtenerPorAula(aula.getId());
        for (Alumno alumno : alumnos) {
            reporteDAO.eliminarPorAlumno(alumno.getId());
        }
        alumnoDAO.eliminarPorAula(aula.getId());
        aulaDAO.eliminar(aula.getId());
        JOptionPane.showMessageDialog(this, "Aula eliminada correctamente.",
                "Exito", JOptionPane.INFORMATION_MESSAGE);
        cargarAulas();
    }

    private void construirPanelAlumnos() {
        pnlAlumnos = new JPanel(new BorderLayout());

        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        btnVolver = new JButton("← Volver");
        btnVolver.addActionListener(e -> {
            limpiarDetalle();
            cardLayout.show(pnlPrincipal, "DASHBOARD");
        });

        lblNombreAula = new JLabel("", SwingConstants.CENTER);
        lblNombreAula.setFont(new Font("Arial", Font.BOLD, 18));

        JPanel pnlBotonesAlumnos = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        btnNuevoAlumno = new JButton("+ Alumno");
        btnNuevoAlumno.setBackground(new Color(60, 160, 80));
        btnNuevoAlumno.setForeground(Color.WHITE);
        btnNuevoAlumno.addActionListener(e -> {
            new DialogoNuevoAlumno(this, aulaSeleccionada, alumnoDAO,
                    () -> recargarAlumnos()).setVisible(true);
        });

        btnEliminarAlumno = new JButton("Eliminar Alumno");
        btnEliminarAlumno.setBackground(new Color(200, 60, 60));
        btnEliminarAlumno.setForeground(Color.WHITE);
        btnEliminarAlumno.setEnabled(false);
        btnEliminarAlumno.addActionListener(e -> eliminarAlumno());

        btnExportarPDF = new JButton("Exportar PDF");
        btnExportarPDF.setBackground(new Color(52, 73, 94));
        btnExportarPDF.setForeground(Color.WHITE);
        btnExportarPDF.addActionListener(e -> exportarPDF());

        btnImportarExcel = new JButton("Importar Excel");
        btnImportarExcel.setBackground(new Color(100, 80, 180));
        btnImportarExcel.setForeground(Color.WHITE);
        btnImportarExcel.addActionListener(e ->
                ImportadorExcel.importarAlumnos(this, aulaSeleccionada, alumnoDAO, this::recargarAlumnos)
        );

        pnlBotonesAlumnos.add(btnImportarExcel);
        pnlBotonesAlumnos.add(btnNuevoAlumno);
        pnlBotonesAlumnos.add(btnEliminarAlumno);
        pnlBotonesAlumnos.add(btnExportarPDF);

        pnlHeader.add(btnVolver, BorderLayout.WEST);
        pnlHeader.add(lblNombreAula, BorderLayout.CENTER);
        pnlHeader.add(pnlBotonesAlumnos, BorderLayout.EAST);

        modeloLista = new DefaultListModel<>();
        lstAlumnos = new JList<>(modeloLista);
        lstAlumnos.setFont(new Font("Arial", Font.PLAIN, 14));
        lstAlumnos.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                alumnoSeleccionado = lstAlumnos.getSelectedValue();
                if (alumnoSeleccionado != null) {
                    btnEliminarAlumno.setEnabled(true);
                    mostrarDetalleAlumno(alumnoSeleccionado);
                }
            }
        });

        JScrollPane scrAlumnos = new JScrollPane(lstAlumnos);
        scrAlumnos.setPreferredSize(new Dimension(250, 0));

        construirPanelDetalle();

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT, scrAlumnos, pnlDetalle);
        splitPane.setDividerLocation(250);

        pnlAlumnos.add(pnlHeader, BorderLayout.NORTH);
        pnlAlumnos.add(splitPane, BorderLayout.CENTER);
    }

    private void eliminarAlumno() {
        if (alumnoSeleccionado == null) return;
        int confirmar = JOptionPane.showConfirmDialog(this,
                "¿Eliminar a " + alumnoSeleccionado + "?\nSe eliminarán todos sus reportes.",
                "Confirmar eliminacion", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (confirmar != JOptionPane.YES_OPTION) return;

        reporteDAO.eliminarPorAlumno(alumnoSeleccionado.getId());
        alumnoDAO.eliminar(alumnoSeleccionado.getId());
        JOptionPane.showMessageDialog(this, "Alumno eliminado correctamente.",
                "Exito", JOptionPane.INFORMATION_MESSAGE);
        alumnoSeleccionado = null;
        btnEliminarAlumno.setEnabled(false);
        limpiarDetalle();
        recargarAlumnos();
    }

    /** Actualiza la lista lateral conservando como fuente de verdad a MongoDB. */
    private void recargarAlumnos() {
        modeloLista.clear();
        List<Alumno> alumnos = alumnoDAO.obtenerPorAula(aulaSeleccionada.getId());
        for (Alumno alumno : alumnos) {
            modeloLista.addElement(alumno);
        }
    }

    private void construirPanelDetalle() {
        pnlDetalle = new JPanel(new BorderLayout());
        pnlDetalle.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel pnlDetalleHeader = new JPanel(new BorderLayout());

        lblNombreAlumno = new JLabel("Selecciona un alumno", SwingConstants.CENTER);
        lblNombreAlumno.setFont(new Font("Arial", Font.BOLD, 16));

        JPanel pnlAcciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lblContadorReportes = new JLabel("Reportes: 0");
        lblContadorReportes.setFont(new Font("Arial", Font.PLAIN, 13));

        btnNuevoReporte = new JButton("+ Nuevo Reporte");
        btnNuevoReporte.setEnabled(false);
        btnNuevoReporte.addActionListener(e -> abrirDialogoNuevoReporte());

        pnlAcciones.add(lblContadorReportes);
        pnlAcciones.add(btnNuevoReporte);

        pnlDetalleHeader.add(lblNombreAlumno, BorderLayout.NORTH);
        pnlDetalleHeader.add(pnlAcciones, BorderLayout.SOUTH);

        pnlReportes = new JPanel();
        pnlReportes.setLayout(new BoxLayout(pnlReportes, BoxLayout.Y_AXIS));
        JScrollPane scrReportes = new JScrollPane(pnlReportes);

        pnlDetalle.add(pnlDetalleHeader, BorderLayout.NORTH);
        pnlDetalle.add(scrReportes, BorderLayout.CENTER);
    }

    private void seleccionarAula(Aula aula) {
        // La seleccion actual determina que alumnos y reportes se muestran en el segundo panel.
        aulaSeleccionada = aula;
        lblNombreAula.setText(aula.toString());
        alumnoSeleccionado = null;
        btnEliminarAlumno.setEnabled(false);
        recargarAlumnos();
        limpiarDetalle();
        cardLayout.show(pnlPrincipal, "ALUMNOS");
    }

    /** Pinta las incidencias del alumno seleccionado en el panel derecho. */
    private void mostrarDetalleAlumno(Alumno alumno) {
        lblNombreAlumno.setText(alumno.getApellido() + ", " + alumno.getNombre());
        btnNuevoReporte.setEnabled(true);
        List<Reporte> reportes = reporteDAO.obtenerPorAlumno(alumno.getId());
        lblContadorReportes.setText("Reportes: " + reportes.size());
        pnlReportes.removeAll();
        if (reportes.isEmpty()) {
            JLabel lblVacio = new JLabel("Sin reportes registrados", SwingConstants.CENTER);
            lblVacio.setFont(new Font("Arial", Font.ITALIC, 14));
            pnlReportes.add(lblVacio);
        } else {
            for (Reporte r : reportes) {
                pnlReportes.add(crearCardReporte(r));
                pnlReportes.add(Box.createVerticalStrut(8));
            }
        }
        pnlReportes.revalidate();
        pnlReportes.repaint();
    }

    private void limpiarDetalle() {
        lblNombreAlumno.setText("Selecciona un alumno");
        lblContadorReportes.setText("Reportes: 0");
        btnNuevoReporte.setEnabled(false);
        pnlReportes.removeAll();
        pnlReportes.revalidate();
        pnlReportes.repaint();
    }

    /** Construye una tarjeta visual independiente para una incidencia. */
    private JPanel crearCardReporte(Reporte reporte) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        Color color;
        switch (reporte.getSeveridad()) {
            case "Grave":    color = new Color(200, 50, 50);  break;
            case "Moderado": color = new Color(200, 150, 0);  break;
            default:         color = new Color(50, 150, 50);  break;
        }

        JLabel lblTipo = new JLabel("● " + reporte.getTipo() + " - " + reporte.getSeveridad());
        lblTipo.setFont(new Font("Arial", Font.BOLD, 13));
        lblTipo.setForeground(color);

        JLabel lblFecha = new JLabel(reporte.getFecha().toString());
        lblFecha.setFont(new Font("Arial", Font.PLAIN, 11));

        JLabel lblMotivo = new JLabel("<html>" + reporte.getMotivo() + "</html>");
        lblMotivo.setFont(new Font("Arial", Font.PLAIN, 13));

        JButton btnEliminarReporte = new JButton("Eliminar");
        btnEliminarReporte.setFont(new Font("Arial", Font.PLAIN, 11));
        btnEliminarReporte.setForeground(new Color(200, 60, 60));
        btnEliminarReporte.addActionListener(e -> {
            int confirmar = JOptionPane.showConfirmDialog(this,
                    "¿Eliminar este reporte?", "Confirmar",
                    JOptionPane.YES_NO_OPTION);
            if (confirmar == JOptionPane.YES_OPTION) {
                reporteDAO.eliminar(reporte.getId());
                mostrarDetalleAlumno(alumnoSeleccionado);
            }
        });

        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.add(lblTipo, BorderLayout.WEST);
        pnlTop.add(lblFecha, BorderLayout.CENTER);
        pnlTop.add(btnEliminarReporte, BorderLayout.EAST);

        card.add(pnlTop, BorderLayout.NORTH);
        card.add(lblMotivo, BorderLayout.CENTER);
        return card;
    }

    private void abrirDialogoNuevoReporte() {
        new DialogoNuevoReporte(this, alumnoSeleccionado, reporteDAO,
                () -> mostrarDetalleAlumno(alumnoSeleccionado)).setVisible(true);
    }

    /** Coordina la exportación y convierte los errores técnicos en un mensaje visual. */
    private void exportarPDF() {
        try {
            String ruta = GeneradorPDF.generarReportePorAula(
                    aulaSeleccionada, alumnoDAO, reporteDAO
            );
            JOptionPane.showMessageDialog(this,
                    "PDF generado exitosamente en:\n" + ruta,
                    "Exportacion exitosa",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al generar el PDF:\n" + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}