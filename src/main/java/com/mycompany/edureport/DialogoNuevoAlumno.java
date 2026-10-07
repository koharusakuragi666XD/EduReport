/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.edureport;
/**
 *
 * @author sergio
 */
import com.mycompany.edureport.dao.AlumnoDAO;
import com.mycompany.edureport.modelo.Aula;
import javax.swing.*;
import java.awt.*;

/**
 * Formulario modal para agregar un alumno al aula que estaba seleccionada.
 */
public class DialogoNuevoAlumno extends JDialog {

    private AlumnoDAO alumnoDAO;
    private Aula aula;
    private Runnable alRefrescar;

    private JTextField txtNombre;
    private JTextField txtApellido;
    private JSpinner spnEdad;
    private JButton btnGuardar;
    private JButton btnCancelar;

    public DialogoNuevoAlumno(JFrame parent, Aula aula, 
                               AlumnoDAO alumnoDAO, Runnable alRefrescar) {
        super(parent, "Nuevo Alumno", true);
        this.aula = aula;
        this.alumnoDAO = alumnoDAO;
        this.alRefrescar = alRefrescar;
        initComponents();
    }

    private void initComponents() {
        setSize(360, 250);
        setLocationRelativeTo(getParent());
        setResizable(false);

        JPanel pnlPrincipal = new JPanel(new BorderLayout(10, 10));
        pnlPrincipal.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lblTitulo = new JLabel("Nuevo alumno en: " + aula.toString(), 
            SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 13));

        JPanel pnlForm = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;

        txtNombre = new JTextField(15);
        txtApellido = new JTextField(15);
        spnEdad = new JSpinner(new SpinnerNumberModel(14, 10, 25, 1));

        gbc.gridx = 0; gbc.gridy = 0;
        pnlForm.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        pnlForm.add(txtNombre, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE;
        pnlForm.add(new JLabel("Apellido:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        pnlForm.add(txtApellido, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
        pnlForm.add(new JLabel("Edad:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        pnlForm.add(spnEdad, gbc);

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

    // El aula se recibe desde Interfaz; por eso el usuario no puede asignar el alumno a otra aula aquí.
    private void guardar() {
        String nombre = txtNombre.getText().trim();
        String apellido = txtApellido.getText().trim();

        if (nombre.isEmpty() || apellido.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Nombre y Apellido no pueden estar vacios.",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int edad = (int) spnEdad.getValue();
        alumnoDAO.insertar(nombre, apellido, edad, aula.getId());
        JOptionPane.showMessageDialog(this, "Alumno agregado correctamente.",
            "Exito", JOptionPane.INFORMATION_MESSAGE);
        alRefrescar.run();
        dispose();
    }
}
