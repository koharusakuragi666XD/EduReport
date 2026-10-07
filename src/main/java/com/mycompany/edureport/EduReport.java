/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.edureport;
/**
 *
 * @author sergio
 */
public class EduReport {
    public static void main(String[] args) {
        // Swing debe crear y modificar sus ventanas dentro del Event Dispatch Thread.
        java.awt.EventQueue.invokeLater(() -> {
            new Interfaz().setVisible(true);
        });
    }
}
