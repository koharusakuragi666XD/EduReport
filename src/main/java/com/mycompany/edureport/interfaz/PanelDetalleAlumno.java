package com.mycompany.edureport.interfaz;
import com.mycompany.edureport.*;

import com.mycompany.edureport.modelo.Alumno;
import com.mycompany.edureport.modelo.Reporte;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel derecho: reportes del alumno seleccionado, con filtros por severidad.
 * Tiene dos vistas: un estado vacío ("Selecciona un alumno") y el contenido.
 */
class PanelDetalleAlumno extends JPanel {

    private static final String VACIO = "VACIO";
    private static final String CONTENIDO = "CONTENIDO";
    /** null = todos. El orden coincide con los chips. */
    private static final Severidad[] FILTROS = {null, Severidad.GRAVE, Severidad.MODERADO, Severidad.LEVE};

    private final Interfaz ventana;
    private final ServicioAcademico servicio;
    private final Runnable alCambiarDatos;

    private final CardLayout vistas = new CardLayout();
    private final JLabel lblNombre = new JLabel(" ");
    private final JLabel lblResumen = new JLabel(" ");
    private final JButton btnNuevo = Tema.botonPrimario("Nuevo reporte");
    private final JButton btnEliminar = Tema.botonPeligro("Eliminar alumno");
    private final JToggleButton[] chips = new JToggleButton[FILTROS.length];
    private final PanelScrollable pnlReportes = new PanelScrollable(null);
    private JScrollPane scroll;

    private Alumno alumno;
    private List<Reporte> reportes = new ArrayList<>();
    private Severidad filtro;
    private boolean cargando;
    private int version;
    /** Ancho con el que se pintó el texto de los motivos (para re-ajustar al redimensionar). */
    private int anchoTexto = 400;

    /** @param alCambiarDatos se invoca cuando cambian los reportes o se elimina al alumno */
    PanelDetalleAlumno(Interfaz ventana, ServicioAcademico servicio, Runnable alCambiarDatos) {
        this.ventana = ventana;
        this.servicio = servicio;
        this.alCambiarDatos = alCambiarDatos;
        setLayout(vistas);
        setBackground(Tema.FONDO);
        add(crearVacio(), VACIO);
        add(crearContenido(), CONTENIDO);
        limpiar();
    }

    // =====================================================================
    //  Construcción
    // =====================================================================

    private JPanel crearVacio() {
        JLabel mensaje = new JLabel("Selecciona un alumno de la lista para ver sus reportes");
        Tema.fuente(mensaje, Font.PLAIN, 14f);
        mensaje.setForeground(Tema.TEXTO_SUAVE);
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Tema.FONDO);
        panel.add(mensaje);
        return panel;
    }

    private JPanel crearContenido() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(Tema.FONDO);
        panel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        // ---- Nombre, resumen y acciones ----
        Tema.fuente(lblNombre, Font.BOLD, 22f);
        lblNombre.setForeground(Tema.PRIMARIO);
        Tema.fuente(lblResumen, Font.PLAIN, 13f);
        lblResumen.setForeground(Tema.TEXTO_SUAVE);

        JPanel textos = new JPanel(new BorderLayout(0, 2));
        textos.setOpaque(false);
        textos.add(lblNombre, BorderLayout.NORTH);
        textos.add(lblResumen, BorderLayout.CENTER);

        btnNuevo.setIcon(Iconos.mas());
        btnNuevo.addActionListener(e ->
                new DialogoNuevoReporte(ventana, alumno, servicio.reporteDAO(), this::alModificar)
                        .setVisible(true));
        btnEliminar.addActionListener(e -> eliminarAlumno());

        JPanel fila1 = new JPanel(new BorderLayout(16, 0));
        fila1.setOpaque(false);
        fila1.add(textos, BorderLayout.WEST);
        fila1.add(Tema.centrado(Tema.fila(8, btnEliminar, btnNuevo)), BorderLayout.EAST);

        // ---- Filtros por severidad ----
        ButtonGroup grupo = new ButtonGroup();
        for (int i = 0; i < FILTROS.length; i++) {
            chips[i] = Tema.chip(FILTROS[i] == null ? "Todos" : FILTROS[i].etiqueta());
            grupo.add(chips[i]);
            final Severidad elegido = FILTROS[i];
            chips[i].addActionListener(e -> {
                filtro = elegido;
                pintar();
            });
        }
        chips[0].setSelected(true);

        JPanel norte = new JPanel(new BorderLayout(0, 16));
        norte.setOpaque(false);
        norte.add(fila1, BorderLayout.NORTH);
        norte.add(Tema.fila(8, chips), BorderLayout.CENTER);

        // ---- Reportes ----
        pnlReportes.setLayout(new BoxLayout(pnlReportes, BoxLayout.Y_AXIS));
        pnlReportes.setBackground(Tema.FONDO);
        pnlReportes.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 8));

        scroll = new JScrollPane(pnlReportes);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Tema.FONDO);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(20);

        // Al cambiar mucho el ancho, se vuelve a ajustar el salto de línea de los motivos.
        javax.swing.Timer reajuste = new javax.swing.Timer(200, e -> {
            if (alumno != null && !cargando
                    && Math.abs(calcularAnchoTexto() - anchoTexto) > 40) {
                pintar();
            }
        });
        reajuste.setRepeats(false);
        scroll.addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                reajuste.restart();
            }
        });

        panel.add(norte, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // =====================================================================
    //  API para PanelAlumnos
    // =====================================================================

    /** Alumno que se está mostrando, o null si no hay ninguno. */
    Alumno alumnoActual() {
        return alumno;
    }

    /** Muestra al alumno y carga sus reportes en segundo plano. */
    void mostrar(Alumno nuevo) {
        alumno = nuevo;
        reportes = new ArrayList<>();
        cargando = true;
        filtro = null;
        chips[0].setSelected(true);
        lblNombre.setText(nuevo.getApellido() + ", " + nuevo.getNombre());
        lblResumen.setText("Cargando reportes…");
        btnNuevo.setEnabled(true);
        btnEliminar.setEnabled(true);
        actualizarChips();
        pintar();
        vistas.show(this, CONTENIDO);
        cargarReportes();
    }

    /** Vuelve al estado vacío y cancela cualquier carga pendiente. */
    void limpiar() {
        version++;
        alumno = null;
        reportes = new ArrayList<>();
        cargando = false;
        btnNuevo.setEnabled(false);
        btnEliminar.setEnabled(false);
        vistas.show(this, VACIO);
    }

    // =====================================================================
    //  Datos
    // =====================================================================

    private void cargarReportes() {
        final Alumno actual = alumno;
        if (actual == null) return;
        final int miVersion = ++version;
        new SwingWorker<List<Reporte>, Void>() {
            @Override
            protected List<Reporte> doInBackground() {
                return servicio.reporteDAO().obtenerPorAlumno(actual.getId());
            }

            @Override
            protected void done() {
                if (miVersion != version) return;
                cargando = false;
                try {
                    reportes = get();
                    lblResumen.setText(textoResumen());
                    actualizarChips();
                } catch (Exception ex) {
                    lblResumen.setText("No se pudieron cargar los reportes");
                    Tema.mostrarError(ventana, "No se pudieron cargar los reportes:\n" + Tema.causa(ex));
                }
                pintar();
            }
        }.execute();
    }

    /** Tras crear o borrar un reporte: recarga el detalle y avisa para actualizar los contadores. */
    private void alModificar() {
        cargarReportes();
        alCambiarDatos.run();
    }

    private void eliminarReporte(Reporte reporte) {
        boolean confirmado = Tema.confirmarEliminacion(ventana, "Eliminar reporte",
                "¿Eliminar este reporte?\nEsta acción no se puede deshacer.");
        if (!confirmado) return;
        Tarea.ejecutar(ventana, () -> servicio.reporteDAO().eliminar(reporte.getId()),
                this::alModificar, "No se pudo eliminar el reporte");
    }

    private void eliminarAlumno() {
        final Alumno eliminado = alumno;
        if (eliminado == null) return;
        boolean confirmado = Tema.confirmarEliminacion(ventana, "Eliminar alumno",
                "¿Eliminar a " + eliminado.getApellido() + ", " + eliminado.getNombre()
                        + "?\nSe eliminarán todos sus reportes.");
        if (!confirmado) return;
        Tarea.ejecutar(ventana, () -> servicio.eliminarAlumno(eliminado), () -> {
            limpiar();
            alCambiarDatos.run();
        }, "No se pudo eliminar al alumno");
    }

    // =====================================================================
    //  Pintado
    // =====================================================================

    private String textoResumen() {
        if (reportes.isEmpty()) return "Sin reportes";
        int graves = contar(Severidad.GRAVE);
        String texto = Tema.plural(reportes.size(), "reporte", "reportes");
        return graves > 0 ? texto + " · " + Tema.plural(graves, "grave", "graves") : texto;
    }

    private int contar(Severidad severidad) {
        int n = 0;
        for (Reporte r : reportes) {
            if (Severidad.desde(r.getSeveridad()) == severidad) n++;
        }
        return n;
    }

    private void actualizarChips() {
        chips[0].setText("Todos · " + reportes.size());
        for (int i = 1; i < FILTROS.length; i++) {
            chips[i].setText(FILTROS[i].etiqueta() + " · " + contar(FILTROS[i]));
        }
    }

    private List<Reporte> visibles() {
        if (filtro == null) return reportes;
        List<Reporte> resultado = new ArrayList<>();
        for (Reporte r : reportes) {
            if (Severidad.desde(r.getSeveridad()) == filtro) resultado.add(r);
        }
        return resultado;
    }

    /** Dibuja las tarjetas con los reportes ya cargados (sin volver a consultar la base). */
    private void pintar() {
        anchoTexto = calcularAnchoTexto();
        pnlReportes.removeAll();
        if (!cargando) {
            List<Reporte> lista = visibles();
            if (lista.isEmpty()) {
                String mensaje = reportes.isEmpty()
                        ? "Este alumno no tiene reportes registrados"
                        : "No hay reportes con severidad " + filtro.etiqueta().toLowerCase();
                JLabel vacio = new JLabel(mensaje);
                Tema.fuente(vacio, Font.PLAIN, 14f);
                vacio.setForeground(Tema.TEXTO_SUAVE);
                vacio.setBorder(BorderFactory.createEmptyBorder(40, 0, 0, 0));
                vacio.setAlignmentX(Component.CENTER_ALIGNMENT);
                pnlReportes.add(vacio);
            } else {
                for (Reporte r : lista) {
                    pnlReportes.add(new TarjetaReporte(r, anchoTexto));
                    pnlReportes.add(Box.createVerticalStrut(12));
                }
            }
        }
        pnlReportes.revalidate();
        pnlReportes.repaint();
    }

    /** Ancho disponible para el texto del motivo dentro de una tarjeta. */
    private int calcularAnchoTexto() {
        return Math.max(260, scroll.getViewport().getWidth() - 90);
    }

    /** Fecha como dd/MM/yyyy, sea cual sea el tipo con que la guarde el modelo. */
    private static String formatearFecha(Object fecha) {
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        if (fecha instanceof LocalDate) return ((LocalDate) fecha).format(formato);
        if (fecha instanceof LocalDateTime) return ((LocalDateTime) fecha).format(formato);
        if (fecha instanceof java.util.Date) {
            return new SimpleDateFormat("dd/MM/yyyy").format((java.util.Date) fecha);
        }
        String texto = String.valueOf(fecha);
        if (texto.matches("\\d{4}-\\d{2}-\\d{2}.*")) {
            try {
                return LocalDate.parse(texto.substring(0, 10)).format(formato);
            } catch (Exception ignorada) {
                // Si no se puede interpretar, se muestra tal cual.
            }
        }
        return texto;
    }

    // =====================================================================
    //  Tarjeta de reporte
    // =====================================================================

    /** Una incidencia: píldora de severidad, tipo, fecha, menú "⋮" y el motivo completo. */
    private class TarjetaReporte extends JPanel {

        private static final int RADIO = 14;

        TarjetaReporte(Reporte reporte, int ancho) {
            super(new BorderLayout(0, 6));
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 12));

            JLabel lblTipo = new JLabel(String.valueOf(reporte.getTipo()));
            Tema.fuente(lblTipo, Font.PLAIN, 13f);
            lblTipo.setForeground(Tema.TEXTO_SUAVE);

            JLabel lblFecha = new JLabel(formatearFecha(reporte.getFecha()));
            Tema.fuente(lblFecha, Font.PLAIN, 13f);
            lblFecha.setForeground(Tema.TEXTO_SUAVE);

            JPopupMenu menu = new JPopupMenu();
            JMenuItem itemEliminar = new JMenuItem("Eliminar reporte");
            itemEliminar.setForeground(Tema.PELIGRO);
            itemEliminar.addActionListener(e -> eliminarReporte(reporte));
            menu.add(itemEliminar);
            setComponentPopupMenu(menu);

            JButton btnMas = Tema.botonIcono(Iconos.puntos(), "Opciones del reporte", 32);
            btnMas.addActionListener(e -> menu.show(btnMas, 0, btnMas.getHeight()));

            JPanel arriba = new JPanel();
            arriba.setOpaque(false);
            arriba.setLayout(new BoxLayout(arriba, BoxLayout.X_AXIS));
            arriba.add(Pildora.severidad(Severidad.desde(reporte.getSeveridad())));
            arriba.add(Box.createHorizontalStrut(10));
            arriba.add(lblTipo);
            arriba.add(Box.createHorizontalGlue());
            arriba.add(lblFecha);
            arriba.add(Box.createHorizontalStrut(6));
            arriba.add(btnMas);

            JLabel lblMotivo = new JLabel("<html><body style='width:" + ancho + "px'>"
                    + Tema.esc(reporte.getMotivo()) + "</body></html>");
            Tema.fuente(lblMotivo, Font.PLAIN, 14f);
            lblMotivo.setForeground(Tema.TEXTO);

            add(arriba, BorderLayout.NORTH);
            add(lblMotivo, BorderLayout.CENTER);
        }

        /** Altura = la que necesite el contenido (nada de cortar motivos largos). */
        @Override
        public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Tema.SUPERFICIE);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, RADIO, RADIO);
            g2.setColor(Tema.BORDE);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, RADIO, RADIO);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
