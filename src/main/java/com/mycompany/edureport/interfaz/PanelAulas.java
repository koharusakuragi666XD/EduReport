package com.mycompany.edureport.interfaz;
import com.mycompany.edureport.*;

import com.mycompany.edureport.ServicioAcademico.ResumenAula;
import com.mycompany.edureport.modelo.Aula;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.function.Consumer;

/** Pantalla inicial: cuadrícula de aulas. Una sola acción primaria: "Nueva aula". */
class PanelAulas extends JPanel {

    private static final int COLUMNAS = 3;
    private static final int RADIO_TARJETA = 16;

    private final Interfaz ventana;
    private final ServicioAcademico servicio;
    private final Consumer<Aula> alAbrirAula;

    private final JPanel cuadricula = new JPanel(new GridLayout(0, COLUMNAS, 16, 16));
    private final JLabel lblSubtitulo = new JLabel(" ");
    private int version;

    PanelAulas(Interfaz ventana, ServicioAcademico servicio, Consumer<Aula> alAbrirAula) {
        super(new BorderLayout());
        this.ventana = ventana;
        this.servicio = servicio;
        this.alAbrirAula = alAbrirAula;
        setBackground(Tema.FONDO);
        add(crearEncabezado(), BorderLayout.NORTH);
        add(crearZonaScroll(), BorderLayout.CENTER);
    }

    // =====================================================================
    //  Construcción
    // =====================================================================

    private JPanel crearEncabezado() {
        JPanel encabezado = Tema.encabezado(22, 32);

        JLabel titulo = cargarLogo("/EduReportLogo.png", 220, 119);
        Tema.fuente(lblSubtitulo, Font.PLAIN, 14f);
        lblSubtitulo.setForeground(Tema.TEXTO_SUAVE);

        JPanel textos = new JPanel(new BorderLayout(0, 4));
        textos.setOpaque(false);
        textos.add(titulo, BorderLayout.NORTH);
        textos.add(lblSubtitulo, BorderLayout.CENTER);

        JButton btnAyuda = Tema.botonIcono(Iconos.ayuda(), "Formato del Excel a importar", 44);
        btnAyuda.addActionListener(e ->
                new DialogoAyudaExcel(ventana, DialogoAyudaExcel.PESTANA_MASIVO).setVisible(true));

        JButton btnTema = Tema.botonIcono(Tema.esOscuro() ? Iconos.sol() : Iconos.luna(),
                Tema.nombreModo(), 44);
        btnTema.addActionListener(e -> ventana.alternarTema());

        JButton btnCreditos = Tema.botonSecundario("Créditos");
        btnCreditos.addActionListener(e ->
                new DialogoCreditos(ventana).setVisible(true));

        JButton btnImportar = Tema.botonSecundario("Importar Excel");
        btnImportar.addActionListener(e ->
                ImportadorExcel.importarAulasYAlumnos(ventana, servicio.aulaDAO(),
                        servicio.alumnoDAO(), this::recargar));

        JButton btnNueva = Tema.botonPrimario("Nueva aula");
        btnNueva.setIcon(Iconos.mas());
        btnNueva.addActionListener(e ->
                new DialogoNuevaAula(ventana, servicio.aulaDAO(), this::recargar).setVisible(true));

        encabezado.add(textos, BorderLayout.WEST);
        encabezado.add(Tema.centrado(Tema.fila(
                10, btnTema, btnAyuda, btnCreditos, btnImportar, btnNueva)), BorderLayout.EAST);
        return encabezado;
    }

    private JLabel cargarLogo(String recurso, int ancho, int alto) {
        java.net.URL url = getClass().getResource(recurso);
        if (url == null) {
            return new JLabel("EduReport");
        }

        Image imagen = new ImageIcon(url).getImage()
                .getScaledInstance(ancho, alto, Image.SCALE_SMOOTH);
        JLabel logo = new JLabel(new ImageIcon(imagen));
        logo.setOpaque(false);
        return logo;
    }

    private JScrollPane crearZonaScroll() {
        cuadricula.setOpaque(false);

        // La cuadrícula va en NORTH para que cada fila conserve su altura fija.
        PanelScrollable contenido = new PanelScrollable(new BorderLayout());
        contenido.setBackground(Tema.FONDO);
        contenido.setBorder(BorderFactory.createEmptyBorder(32, 32, 32, 32));
        contenido.add(cuadricula, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(contenido);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Tema.FONDO);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(24);
        return scroll;
    }

    // =====================================================================
    //  Datos
    // =====================================================================

    /** Vuelve a consultar la base en segundo plano y reconstruye las tarjetas. */
    void recargar() {
        final int miVersion = ++version;
        lblSubtitulo.setText("Cargando aulas…");
        new SwingWorker<List<ResumenAula>, Void>() {
            @Override
            protected List<ResumenAula> doInBackground() {
                return servicio.resumenAulas();
            }

            @Override
            protected void done() {
                if (miVersion != version) return; // llegó una recarga más reciente
                try {
                    pintar(get());
                } catch (Exception ex) {
                    lblSubtitulo.setText("No se pudieron cargar las aulas");
                    Tema.mostrarError(ventana, "No se pudieron cargar las aulas:\n" + Tema.causa(ex));
                }
            }
        }.execute();
    }

    private void pintar(List<ResumenAula> resumenes) {
        cuadricula.removeAll();
        for (ResumenAula r : resumenes) {
            cuadricula.add(new TarjetaAula(r));
        }
        lblSubtitulo.setText(resumenes.isEmpty()
                ? "Aún no hay aulas. Crea una con \"Nueva aula\" o importa un Excel."
                : "Selecciona un aula para ver a sus alumnos");
        cuadricula.revalidate();
        cuadricula.repaint();
    }

    private void eliminarAula(Aula aula) {
        boolean confirmado = Tema.confirmarEliminacion(ventana, "Eliminar aula",
                "¿Eliminar el aula " + aula + "?\nSe eliminarán todos sus alumnos y reportes.");
        if (!confirmado) return;
        Tarea.ejecutar(ventana, () -> servicio.eliminarAula(aula), this::recargar,
                "No se pudo eliminar el aula");
    }

    // =====================================================================
    //  Tarjeta de aula
    // =====================================================================

    /** Tarjeta clicable (también con Enter/Espacio) con menú "⋮" y clic derecho para eliminar. */
    private class TarjetaAula extends JPanel {

        private boolean hover;

        TarjetaAula(ResumenAula resumen) {
            super(new BorderLayout(0, 10));
            final Aula aula = resumen.aula;
            final String especialidad = String.valueOf(aula.getEspecialidad());
            final String detalle = aula.getGrado() + "° " + aula.getGrupo() + " · " + aula.getTurno();

            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 14));
            setPreferredSize(new Dimension(280, 124));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFocusable(true);
            getAccessibleContext().setAccessibleName("Abrir " + especialidad + " " + detalle);

            // ---- Fila superior: textos + menú ----
            JLabel lblEspecialidad = new JLabel(especialidad);
            Tema.fuente(lblEspecialidad, Font.BOLD, 16f);
            lblEspecialidad.setForeground(Tema.TEXTO);
            JLabel lblDetalle = new JLabel(detalle);
            Tema.fuente(lblDetalle, Font.PLAIN, 13f);
            lblDetalle.setForeground(Tema.TEXTO_SUAVE);

            JPanel textos = new JPanel();
            textos.setOpaque(false);
            textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
            textos.add(lblEspecialidad);
            textos.add(Box.createVerticalStrut(2));
            textos.add(lblDetalle);

            JPopupMenu menu = new JPopupMenu();
            JMenuItem itemEliminar = new JMenuItem("Eliminar aula");
            itemEliminar.setForeground(Tema.PELIGRO);
            itemEliminar.addActionListener(e -> eliminarAula(aula));
            menu.add(itemEliminar);
            setComponentPopupMenu(menu);

            JButton btnMas = Tema.botonIcono(Iconos.puntos(), "Opciones del aula", 32);
            btnMas.addActionListener(e -> menu.show(btnMas, 0, btnMas.getHeight()));
            JPanel zonaMenu = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            zonaMenu.setOpaque(false);
            zonaMenu.add(btnMas);

            JPanel arriba = new JPanel(new BorderLayout());
            arriba.setOpaque(false);
            arriba.add(textos, BorderLayout.CENTER);
            arriba.add(zonaMenu, BorderLayout.EAST);

            // ---- Fila inferior: alumnos + graves ----
            JLabel lblAlumnos = new JLabel(Tema.plural(resumen.alumnos, "alumno", "alumnos"));
            Tema.fuente(lblAlumnos, Font.PLAIN, 13f);
            lblAlumnos.setForeground(Tema.TEXTO_SUAVE);

            JPanel abajo = new JPanel(new BorderLayout());
            abajo.setOpaque(false);
            abajo.add(lblAlumnos, BorderLayout.WEST);
            abajo.add(Pildora.graves(resumen.graves), BorderLayout.EAST);

            add(arriba, BorderLayout.NORTH);
            add(abajo, BorderLayout.SOUTH);

            // ---- Interacción ----
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { cambiarHover(true); }

                @Override public void mouseExited(MouseEvent e) {
                    // Al entrar al botón "⋮" también sale de la tarjeta, pero sigue dentro de ella.
                    if (!new Rectangle(getSize()).contains(e.getPoint())) cambiarHover(false);
                }

                @Override public void mouseClicked(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e)) alAbrirAula.accept(aula);
                }
            });
            getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "abrir");
            getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("SPACE"), "abrir");
            getActionMap().put("abrir", new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) { alAbrirAula.accept(aula); }
            });
            addFocusListener(new FocusAdapter() {
                @Override public void focusGained(FocusEvent e) { repaint(); }
                @Override public void focusLost(FocusEvent e)   { repaint(); }
            });
        }

        private void cambiarHover(boolean valor) {
            hover = valor;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth() - 1;
            int h = getHeight() - 1;
            g2.setColor(hover ? Tema.HOVER_TARJETA : Tema.SUPERFICIE);
            g2.fillRoundRect(0, 0, w, h, RADIO_TARJETA, RADIO_TARJETA);
            g2.setColor(hasFocus() ? Tema.PRIMARIO : Tema.BORDE);
            g2.setStroke(new BasicStroke(hasFocus() ? 2f : 1f));
            g2.drawRoundRect(0, 0, w, h, RADIO_TARJETA, RADIO_TARJETA);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
