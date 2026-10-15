/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.edureport;

import com.mongodb.MongoException;
import com.mycompany.edureport.conexion.Conexion;
import javax.swing.JOptionPane;
/**
 *
 * @author sergio
 */
public class EduReport {
    public static void main(String[] args) {
        // Swing debe crear y modificar sus ventanas dentro del Event Dispatch Thread.
        java.awt.EventQueue.invokeLater(() -> {
            try {
                new Interfaz().setVisible(true);
            } catch (MongoException e) {
                Conexion.cerrar();
                JOptionPane.showMessageDialog(
                    null,
                    "No se pudo conectar con MongoDB en localhost:27017.\n"
                        + "Inicia MongoDB e inténtalo de nuevo.",
                    "Error de conexión",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }
}
