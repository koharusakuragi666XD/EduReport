package com.mycompany.edureport.interfaz;
import com.mycompany.edureport.*;

import javax.swing.*;
import java.awt.Component;

/** Ejecuta trabajo pesado (MongoDB, archivos) fuera del hilo de Swing para no congelar la ventana. */
final class Tarea {

    private Tarea() { }

    /**
     * @param trabajo       se ejecuta en segundo plano
     * @param alTerminar    se ejecuta en el hilo de Swing si todo salió bien (puede ser null)
     * @param mensajeError  encabezado del mensaje si el trabajo falla
     */
    static void ejecutar(Component padre, Runnable trabajo, Runnable alTerminar, String mensajeError) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                trabajo.run();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    if (alTerminar != null) alTerminar.run();
                } catch (Exception ex) {
                    Tema.mostrarError(padre, mensajeError + ":\n" + Tema.causa(ex));
                }
            }
        }.execute();
    }
}
