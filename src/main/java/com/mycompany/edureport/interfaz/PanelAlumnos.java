package com.mycompany.edureport.interfaz;
import com.mycompany.edureport.*;

import com.mycompany.edureport.ServicioAcademico.ResumenAlumno;
import com.mycompany.edureport.modelo.Alumno;
import com.mycompany.edureport.modelo.Aula;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** Pantalla de un aula: lista de alumnos con búsqueda a la izquierda y sus reportes a la derecha. */
class PanelAlumnos extends JPanel {

    private final Interfaz ventana;
    private final ServicioAcademico servicio;
    private final Runnable alVolver;

    private final JLabel lblTitulo = new JLabel(" ");
    private final JLabel lblSubtitulo = new JLabel(" ");
    private final JTextField txtBuscar = Tema.campoBusqueda("Buscar alumno");
    private final DefaultListModel<ResumenAlumno> modelo = new DefaultListModel<>();
    private final JList<ResumenAlumno> lista = new JList<>(modelo);
    private final PanelDetalleAlumno detalle;
    private JButton btnPdf;

    private Aula aula;
    private List<ResumenAlumno> todos = new ArrayList<>();
    private boolean ignorarSeleccion;
    private int version;

    PanelAlumnos(Interfaz ventana, ServicioAcademico servicio, Runnable alVolver) {
        super(new BorderLayout());
        this.ventana = ventana;
        this.servicio = servicio;
        this.alVolver = alVolver;
        setBackground(Tema.FONDO);

        detalle = new PanelDetalleAlumno(ventana, servicio, this::recargarAlumnos);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, crearPanelLista(), detalle);
        split.setDividerLocation(300);
        split.setContinuousLayout(true);
        split.setBorder(null);

        add(crearEncabezado(), BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);
    }

    // =====================================================================
    //  Construcción
    // =====================================================================

    private JPanel crearEncabezado() {
        JPanel encabezado = Tema.encabezado(14, 24);

        JButton btnVolver = Tema.botonSecundario("Aulas");
        btnVolver.setIcon(Iconos.atras());
        btnVolver.addActionListener(e -> alVolver.run());

        Tema.fuente(lblTitulo, Font.BOLD, 20f);
        lblTitulo.setForeground(Tema.PRIMARIO);
        Tema.fuente(lblSubtitulo, Font.PLAIN, 13f);
        lblSubtitulo.setForeground(Tema.TEXTO_SUAVE);
        JPanel textos = new JPanel(new BorderLayout(0, 2));
        textos.setOpaque(false);
        textos.add(lblTitulo, BorderLayout.NORTH);
        textos.add(lblSubtitulo, BorderLayout.CENTER);

        // Importar y exportar afectan a toda el aula, por eso viven en su encabezado.
        JButton btnAyuda = Tema.botonIcono(Iconos.ayuda(), "Formato del Excel a importar", 44);
        btnAyuda.addActionListener(e ->
                new DialogoAyudaExcel(ventana, DialogoAyudaExcel.PESTANA_ALUMNOS).setVisible(true));

        JButton btnTema = Tema.botonIcono(Tema.esOscuro() ? Iconos.sol() : Iconos.luna(),
                Tema.nombreModo(), 44);
        btnTema.addActionListener(e -> ventana.alternarTema());

        JButton btnImportar = Tema.botonSecundario("Importar Excel");
        btnImportar.addActionListener(e ->
                ImportadorExcel.importarAlumnos(ventana, aula, servicio.alumnoDAO(), this::recargarAlumnos));

        btnPdf = Tema.botonSecundario("Exportar PDF");
        btnPdf.addActionListener(e -> exportarPDF());

        encabezado.add(Tema.centrado(Tema.fila(16, btnVolver, textos)), BorderLayout.WEST);
        encabezado.add(Tema.centrado(Tema.fila(10, btnTema, btnAyuda, btnImportar, btnPdf)), BorderLayout.EAST);
        return encabezado;
    }

    private JPanel crearPanelLista() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(Tema.SUPERFICIE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, Tema.BORDE),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));
        panel.setPreferredSize(new Dimension(300, 0));
        panel.setMinimumSize(new Dimension(240, 0));

        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { aplicarFiltro(); }
            @Override public void removeUpdate(DocumentEvent e)  { aplicarFiltro(); }
            @Override public void changedUpdate(DocumentEvent e) { aplicarFiltro(); }
        });

        JButton btnAgregar = Tema.botonSecundarioIcono(Iconos.mas(), "Agregar alumno");
        btnAgregar.addActionListener(e ->
                new DialogoNuevoAlumno(ventana, aula, servicio.alumnoDAO(), this::recargarAlumnos)
                        .setVisible(true));

        JPanel fila = new JPanel(new BorderLayout(8, 0));
        fila.setOpaque(false);
        fila.add(txtBuscar, BorderLayout.CENTER);
        fila.add(btnAgregar, BorderLayout.EAST);

        lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lista.setFixedCellHeight(46);
        lista.setBackground(Tema.SUPERFICIE);
        lista.setCellRenderer(new FilaAlumno());
        lista.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || ignorarSeleccion) return;
            ResumenAlumno elegido = lista.getSelectedValue();
            // Sin selección el detalle se limpia: así "Nuevo reporte" nunca recibe un alumno nulo.
            if (elegido == null) detalle.limpiar();
            else detalle.mostrar(elegido.alumno);
        });

        JScrollPane scroll = new JScrollPane(lista);
        scroll.setBorder(null);

        panel.add(fila, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // =====================================================================
    //  Navegación y datos
    // =====================================================================

    /** Prepara la pantalla para un aula nueva y carga sus alumnos. */
    void mostrarAula(Aula nueva) {
        version++; // descarta cargas pendientes del aula anterior
        aula = nueva;
        todos = new ArrayList<>();
        detalle.limpiar();
        ignorarSeleccion = true;
        modelo.clear();
        ignorarSeleccion = false;
        txtBuscar.setText("");
        lblTitulo.setText(nueva.getEspecialidad() + " · " + nueva.getGrado() + "° " + nueva.getGrupo());
        lblSubtitulo.setText("Cargando alumnos…");
        recargarAlumnos();
    }

    /** Consulta de nuevo los alumnos (y sus contadores) sin perder la selección actual. */
    void recargarAlumnos() {
        if (aula == null) return;
        final int miVersion = ++version;
        final Aula aulaActual = aula;
        new SwingWorker<List<ResumenAlumno>, Void>() {
            @Override
            protected List<ResumenAlumno> doInBackground() {
                return servicio.resumenAlumnos(aulaActual);
            }

            @Override
            protected void done() {
                if (miVersion != version) return;
                try {
                    todos = get();
                    lblSubtitulo.setText("Turno " + String.valueOf(aulaActual.getTurno()).toLowerCase()
                            + " · " + Tema.plural(todos.size(), "alumno", "alumnos"));
                    Alumno actual = detalle.alumnoActual();
                    if (actual != null && !existe(actual)) detalle.limpiar();
                    aplicarFiltro();
                } catch (Exception ex) {
                    lblSubtitulo.setText("No se pudieron cargar los alumnos");
                    Tema.mostrarError(ventana, "No se pudieron cargar los alumnos:\n" + Tema.causa(ex));
                }
            }
        }.execute();
    }

    private boolean existe(Alumno alumno) {
        String id = String.valueOf(alumno.getId());
        for (ResumenAlumno r : todos) {
            if (id.equals(String.valueOf(r.alumno.getId()))) return true;
        }
        return false;
    }

    /**
     * Reconstruye la lista según el texto de búsqueda. Mientras se reconstruye no se avisa
     * al detalle: si el alumno abierto queda fuera del filtro, sigue visible a la derecha.
     */
    private void aplicarFiltro() {
        String consulta = Tema.normalizar(txtBuscar.getText().trim());
        Alumno abierto = detalle.alumnoActual();
        String idAbierto = abierto == null ? null : String.valueOf(abierto.getId());

        ignorarSeleccion = true;
        try {
            modelo.clear();
            int indice = -1;
            for (ResumenAlumno r : todos) {
                String nombre = Tema.normalizar(r.alumno.getApellido() + " " + r.alumno.getNombre());
                if (!consulta.isEmpty() && !nombre.contains(consulta)) continue;
                modelo.addElement(r);
                if (idAbierto != null && idAbierto.equals(String.valueOf(r.alumno.getId()))) {
                    indice = modelo.size() - 1;
                }
            }
            if (indice >= 0) {
                lista.setSelectedIndex(indice);
                lista.ensureIndexIsVisible(indice);
            } else {
                lista.clearSelection();
            }
        } finally {
            ignorarSeleccion = false;
        }
    }

    // =====================================================================
    //  Exportar PDF
    // =====================================================================

    /** Genera el PDF en segundo plano y ofrece abrirlo al terminar. */
    private void exportarPDF() {
        final Aula aulaExportada = aula;
        btnPdf.setEnabled(false);
        btnPdf.setText("Generando…");
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return GeneradorPDF.generarReportePorAula(
                        aulaExportada, servicio.alumnoDAO(), servicio.reporteDAO());
            }

            @Override
            protected void done() {
                btnPdf.setEnabled(true);
                btnPdf.setText("Exportar PDF");
                try {
                    mostrarPdfGenerado(get());
                } catch (Exception ex) {
                    Tema.mostrarError(ventana, "Error al generar el PDF:\n" + Tema.causa(ex));
                }
            }
        }.execute();
    }

    private void mostrarPdfGenerado(String ruta) {
        Object[] opciones = {"Abrir PDF", "Cerrar"};
        int r = JOptionPane.showOptionDialog(ventana,
                "PDF generado correctamente en:\n" + ruta, "Exportación exitosa",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                null, opciones, opciones[0]);
        if (r != 0) return;
        try {
            if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(new File(ruta));
        } catch (Exception ex) {
            Tema.mostrarError(ventana, "No se pudo abrir el PDF:\n" + Tema.causa(ex));
        }
    }

    // =====================================================================
    //  Fila de la lista
    // =====================================================================

    /** "Apellido, Nombre" con el contador de reportes (rojo si hay alguno grave). */
    private static class FilaAlumno extends JPanel implements ListCellRenderer<ResumenAlumno> {

        private final JLabel nombre = new JLabel();

        FilaAlumno() {
            super(new BorderLayout(8, 0));
            setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
            nombre.setForeground(Tema.TEXTO);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends ResumenAlumno> lista,
                                                      ResumenAlumno valor, int indice,
                                                      boolean seleccionado, boolean foco) {
            removeAll();
            nombre.setText(valor.alumno.getApellido() + ", " + valor.alumno.getNombre());
            Tema.fuente(nombre, seleccionado ? Font.BOLD : Font.PLAIN, 14f);
            setBackground(seleccionado ? Tema.SELECCION : Tema.SUPERFICIE);
            add(nombre, BorderLayout.CENTER);
            add(Tema.centrado(Pildora.contador(valor.reportes, valor.graves > 0)), BorderLayout.EAST);
            return this;
        }
    }
}
