package com.mycompany.edureport.interfaz;

import com.mycompany.edureport.dao.ReporteDAO;
import com.mycompany.edureport.modelo.Alumno;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DialogoReporteMasivo extends JDialog {

    private final List<Alumno> alumnos;
    private final ReporteDAO reporteDAO;
    private final Runnable alRefrescar;

    private final List<JCheckBox> seleccionados = new ArrayList<>();
    private JSpinner spnFecha;
    private JComboBox<String> cmbTipo;
    private JComboBox<String> cmbSeveridad;
    private JTextArea txtMotivo;

    public DialogoReporteMasivo(
            JFrame parent,
            List<Alumno> alumnos,
            ReporteDAO reporteDAO,
            Runnable alRefrescar) {

        super(parent, "Reporte grupal", true);
        this.alumnos = alumnos;
        this.reporteDAO = reporteDAO;
        this.alRefrescar = alRefrescar;

        construirInterfaz();
    }

    private void construirInterfaz() {
        setSize(520, 600);
        setLocationRelativeTo(getParent());
        setResizable(false);

        JPanel principal = new JPanel(new BorderLayout(10, 10));
        principal.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titulo = new JLabel(
                "Crear reporte para varios alumnos",
                SwingConstants.CENTER
        );
        Tema.fuente(titulo, Font.BOLD, 16f);

        JPanel panelAlumnos = new JPanel();
        panelAlumnos.setLayout(new BoxLayout(panelAlumnos, BoxLayout.Y_AXIS));
        panelAlumnos.setBorder(BorderFactory.createTitledBorder("Alumnos"));

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton btnTodos = new JButton("Seleccionar todos");
        JButton btnNinguno = new JButton("Deseleccionar todos");

        btnTodos.addActionListener(e ->
                seleccionados.forEach(c -> c.setSelected(true)));

        btnNinguno.addActionListener(e ->
                seleccionados.forEach(c -> c.setSelected(false)));

        acciones.add(btnTodos);
        acciones.add(btnNinguno);

        for (Alumno alumno : alumnos) {
            JCheckBox check = new JCheckBox(
                    alumno.getApellido() + ", " + alumno.getNombre()
            );
            check.setSelected(true);
            seleccionados.add(check);
            panelAlumnos.add(check);
        }

        JScrollPane scrollAlumnos = new JScrollPane(panelAlumnos);
        scrollAlumnos.setPreferredSize(new Dimension(460, 180));

        JPanel formulario = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        spnFecha = new JSpinner(new SpinnerDateModel());
        spnFecha.setEditor(new JSpinner.DateEditor(spnFecha, "dd/MM/yyyy"));

        cmbTipo = new JComboBox<>(new String[]{
                "Conducta",
                "Academico",
                "Asistencia"
        });

        cmbSeveridad = new JComboBox<>(new String[]{
                "Leve",
                "Moderado",
                "Grave"
        });

        txtMotivo = new JTextArea(5, 30);
        txtMotivo.setLineWrap(true);
        txtMotivo.setWrapStyleWord(true);

        agregarCampo(formulario, gbc, 0, "Fecha:", spnFecha);
        agregarCampo(formulario, gbc, 1, "Tipo:", cmbTipo);
        agregarCampo(formulario, gbc, 2, "Severidad:", cmbSeveridad);
        agregarCampo(formulario, gbc, 3, "Motivo:", new JScrollPane(txtMotivo));

        JButton btnCancelar = new JButton("Cancelar");
        JButton btnGuardar = Tema.botonPrimario("Guardar reporte");

        btnCancelar.addActionListener(e -> dispose());
        btnGuardar.addActionListener(e -> guardar());

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botones.add(btnCancelar);
        botones.add(btnGuardar);

        JPanel centro = new JPanel();
        centro.setLayout(new BoxLayout(centro, BoxLayout.Y_AXIS));
        centro.add(acciones);
        centro.add(scrollAlumnos);
        centro.add(formulario);

        principal.add(titulo, BorderLayout.NORTH);
        principal.add(centro, BorderLayout.CENTER);
        principal.add(botones, BorderLayout.SOUTH);

        add(principal);
    }

    private void agregarCampo(
            JPanel panel,
            GridBagConstraints gbc,
            int fila,
            String etiqueta,
            Component componente) {

        gbc.gridy = fila;
        gbc.gridx = 0;
        gbc.weightx = 0;
        panel.add(new JLabel(etiqueta), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(componente, gbc);
    }

    private void guardar() {
        List<Alumno> destino = new ArrayList<>();

        for (int i = 0; i < alumnos.size(); i++) {
            if (seleccionados.get(i).isSelected()) {
                destino.add(alumnos.get(i));
            }
        }

        if (destino.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Selecciona al menos un alumno.",
                    "Validación",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        String motivo = txtMotivo.getText().trim();

        if (motivo.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "El motivo no puede estar vacío.",
                    "Validación",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        String tipo = (String) cmbTipo.getSelectedItem();
        String severidad = (String) cmbSeveridad.getSelectedItem();
        Date fecha = (Date) spnFecha.getValue();

        int confirmacion = JOptionPane.showConfirmDialog(
                this,
                "Se crearán " + destino.size()
                        + " reportes, uno por cada alumno seleccionado.\n"
                        + "¿Deseas continuar?",
                "Confirmar reporte grupal",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            for (Alumno alumno : destino) {
                reporteDAO.insertar(
                        alumno.getId(),
                        tipo,
                        severidad,
                        motivo,
                        fecha
                );
            }

            JOptionPane.showMessageDialog(
                    this,
                    "Se crearon " + destino.size() + " reportes correctamente.",
                    "Operación exitosa",
                    JOptionPane.INFORMATION_MESSAGE
            );

            alRefrescar.run();
            dispose();

        } catch (RuntimeException ex) {
            Tema.mostrarError(
                    this,
                    "No se pudieron crear los reportes:\n" + Tema.causa(ex)
            );
        }
    }
}