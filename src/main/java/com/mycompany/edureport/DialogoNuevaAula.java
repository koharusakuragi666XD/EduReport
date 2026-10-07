/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.edureport;
/**
 *
 * @author sergio
 */
import com.mycompany.edureport.dao.AulaDAO;
import javax.swing.*;
import java.awt.*;

/**
 * Formulario modal para crear aulas.
 *
 * <p>Al guardar valida los campos mínimos, delega la persistencia al DAO y
 * ejecuta {@code alRefrescar} para que el dashboard vuelva a consultar datos.</p>
 */
public class DialogoNuevaAula extends JDialog {

    private AulaDAO aulaDAO;
    private Runnable alRefrescar;

    private JTextField txtEspecialidad;
    private JSpinner spnGrado;
    private JTextField txtGrupo;
    private JComboBox<String> cmbTurno;
    private JButton btnGuardar;
    private JButton btnCancelar;

    public DialogoNuevaAula(JFrame parent, AulaDAO aulaDAO, Runnable alRefrescar) {
        super(parent, "Nueva Aula", true);
        this.aulaDAO = aulaDAO;
        this.alRefrescar = alRefrescar;
        initComponents();
    }

    // La construcción visual está separada de guardar() para mantener aislada la lógica.
    private void initComponents() {
        setSize(380, 280);
        setLocationRelativeTo(getParent());
        setResizable(false);

        JPanel pnlPrincipal = new JPanel(new BorderLayout(10, 10));
        pnlPrincipal.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lblTitulo = new JLabel("Agregar Nueva Aula", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 14));

        JPanel pnlForm = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;

        txtEspecialidad = new JTextField(15);
        spnGrado = new JSpinner(new SpinnerNumberModel(1, 1, 6, 1));
        txtGrupo = new JTextField(5);
        cmbTurno = new JComboBox<>(new String[]{"Matutino", "Vespertino"});

        gbc.gridx = 0; gbc.gridy = 0;
        pnlForm.add(new JLabel("Especialidad:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        pnlForm.add(txtEspecialidad, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE;
        pnlForm.add(new JLabel("Grado:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        pnlForm.add(spnGrado, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
        pnlForm.add(new JLabel("Grupo:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        pnlForm.add(txtGrupo, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE;
        pnlForm.add(new JLabel("Turno:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        pnlForm.add(cmbTurno, gbc);

        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnCancelar = new JButton("Cancelar");
        btnGuardar = new JButton("Guardar");
        btnGuardar.setBackground(new Color(70, 130, 180));
        btnGuardar.setForeground(Color.WHITE);

        btnCancelar.addActionListener(e -> dispose());
        btnGuardar.addActionListener(e -> guardar());

        pnlBotones.add(btnCancelar);
        pnlBotones.add(btnGuardar);

        pnlPrincipal.add(lblTitulo, BorderLayout.NORTH);
        pnlPrincipal.add(pnlForm, BorderLayout.CENTER);
        pnlPrincipal.add(pnlBotones, BorderLayout.SOUTH);
        add(pnlPrincipal);
    }

    /** Valida la captura y comunica el resultado al resto de la aplicación. */
    private void guardar() {
        String especialidad = txtEspecialidad.getText().trim();
        String grupo = txtGrupo.getText().trim();

        if (especialidad.isEmpty() || grupo.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Especialidad y Grupo no pueden estar vacios.",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int grado = (int) spnGrado.getValue();
        String turno = (String) cmbTurno.getSelectedItem();

        aulaDAO.insertar(especialidad, grado, grupo, turno);
        JOptionPane.showMessageDialog(this, "Aula agregada correctamente.", 
            "Exito", JOptionPane.INFORMATION_MESSAGE);
        alRefrescar.run();
        dispose();
    }
}
