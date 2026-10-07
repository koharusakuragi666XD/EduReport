/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.mycompany.edureport;
/**
 *
 * @author sergio
 */
import com.mycompany.edureport.dao.ReporteDAO;
import com.mycompany.edureport.modelo.Alumno;
import javax.swing.*;
import java.awt.*;
import java.util.Date;

/**
 * Formulario modal para registrar una incidencia del alumno seleccionado.
 *
 * <p>Los combos funcionan como un vocabulario controlado: sus textos deben
 * coincidir con los valores que interpretan la interfaz y el generador PDF.</p>
 */
public class DialogoNuevoReporte extends JDialog {

    private Alumno alumno;
    private ReporteDAO reporteDAO;
    private Runnable alRefrescar;

    private JSpinner spnFecha;
    private JComboBox<String> cmbTipo;
    private JComboBox<String> cmbSeveridad;
    private JTextArea txtMotivo;
    private JButton btnGuardar;
    private JButton btnCancelar;

    public DialogoNuevoReporte(JFrame parent, Alumno alumno, 
                                ReporteDAO reporteDAO, Runnable alRefrescar) {
        super(parent, "Nuevo Reporte", true);
        this.alumno = alumno;
        this.reporteDAO = reporteDAO;
        this.alRefrescar = alRefrescar;
        initComponents();
    }

    private void initComponents() {
        setSize(420, 400);
        setLocationRelativeTo(getParent());
        setResizable(false);

        JPanel pnlPrincipal = new JPanel(new BorderLayout(10, 10));
        pnlPrincipal.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lblTitulo = new JLabel(
            "Reporte para: " + alumno.getApellido() + ", " + alumno.getNombre(),
            SwingConstants.CENTER
        );
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 14));
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JPanel pnlForm = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;

        spnFecha = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editorFecha = new JSpinner.DateEditor(spnFecha, "dd/MM/yyyy");
        spnFecha.setEditor(editorFecha);

        cmbTipo = new JComboBox<>(new String[]{
            "Conducta", "Academico", "Asistencia"
        });

        cmbSeveridad = new JComboBox<>(new String[]{
            "Leve", "Moderado", "Grave"
        });

        txtMotivo = new JTextArea(5, 20);
        txtMotivo.setLineWrap(true);
        txtMotivo.setWrapStyleWord(true);
        JScrollPane scrMotivo = new JScrollPane(txtMotivo);

        gbc.gridx = 0; gbc.gridy = 0;
        pnlForm.add(new JLabel("Fecha:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        pnlForm.add(spnFecha, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE;
        pnlForm.add(new JLabel("Tipo:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        pnlForm.add(cmbTipo, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
        pnlForm.add(new JLabel("Severidad:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        pnlForm.add(cmbSeveridad, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        pnlForm.add(new JLabel("Motivo:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH;
        pnlForm.add(scrMotivo, gbc);

        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        btnCancelar = new JButton("Cancelar");
        btnGuardar  = new JButton("Guardar");
        btnGuardar.setBackground(new Color(70, 130, 180));
        btnGuardar.setForeground(Color.WHITE);

        btnCancelar.addActionListener(e -> dispose());
        btnGuardar.addActionListener(e -> guardarReporte());

        pnlBotones.add(btnCancelar);
        pnlBotones.add(btnGuardar);

        pnlPrincipal.add(lblTitulo,  BorderLayout.NORTH);
        pnlPrincipal.add(pnlForm,    BorderLayout.CENTER);
        pnlPrincipal.add(pnlBotones, BorderLayout.SOUTH);

        add(pnlPrincipal);
    }

    /** Comprueba el motivo, guarda la incidencia y refresca el detalle del alumno. */
    private void guardarReporte() {
        String motivo = txtMotivo.getText().trim();
        if (motivo.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "El motivo no puede estar vacío.",
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        String tipo      = (String) cmbTipo.getSelectedItem();
        String severidad = (String) cmbSeveridad.getSelectedItem();
        Date   fecha     = (Date) spnFecha.getValue();

        reporteDAO.insertar(alumno.getId(), tipo, severidad, motivo, fecha);

        JOptionPane.showMessageDialog(
            this,
            "Reporte guardado correctamente.",
            "Exito",
            JOptionPane.INFORMATION_MESSAGE
        );

        alRefrescar.run();
        dispose();
    }
}