/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.edureport.conexion;
/**
 *
 * @author sergio
 */

import java.util.ArrayList;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class Conexion {
    
    private static final String URL = configuracion("EDUREPORT_MONGODB_URI", "mongodb://localhost:27017");
    private static final String NOMBRE_BD = configuracion("EDUREPORT_MONGODB_DATABASE", "edureport");
    
    private static MongoClient cliente;
    private static MongoDatabase baseDeDatos;

    private static String configuracion(String nombre, String valorPredeterminado) {
        String valor = System.getenv(nombre);
        return valor == null || valor.isBlank() ? valorPredeterminado : valor.trim();
    }
    
    public static MongoDatabase getBaseDeDatos() {
        //hay que proteger este if (cliente ==null) o se rompe todo
        if (cliente == null) {
            // Se reutiliza un unico cliente para que todos los DAO compartan la conexion.
            cliente = MongoClients.create(URL);
            baseDeDatos = cliente.getDatabase(NOMBRE_BD);
            //Aviso de que la conexion fue exitosa
            System.out.println("Conexion exitosa a MongoDB");
        }
        return baseDeDatos;
    }
    
    public static void cerrar() {
        if (cliente != null) {
            // MongoClient administra los recursos de red y debe cerrarse al terminar la app.
            cliente.close();
            cliente = null;
            System.out.println("Conexion cerrada");
        }
    }
}