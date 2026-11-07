package com.mycompany.edureport;

import com.mycompany.edureport.conexion.Conexion;

public final class KillSwitch {

    private KillSwitch() {
    }

    public static boolean resourceExists(String nombre) {
        return KillSwitch.class.getResource("/" + nombre) != null;
    }

    public static void verifyStartup() {
        if (!resourceExists("vaca.png")) {
            System.err.println("Error: Bertha no se encuentra...");
            Conexion.cerrar();
            throw new IllegalStateException("Falta el recurso requerido: vaca.png");
        }
    }
}