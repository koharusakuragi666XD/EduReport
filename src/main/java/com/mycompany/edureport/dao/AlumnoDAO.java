/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.edureport.dao;
/**
 *
 * @author sergio
 */
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mycompany.edureport.conexion.Conexion;
import com.mycompany.edureport.modelo.Alumno;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;
import org.bson.types.ObjectId;

/**
 * Capa de persistencia de alumnos.
 *
 * <p>La relación con un aula se representa con el campo {@code aula_id}.
 * MongoDB no aplica aquí una cascada automática, por lo que las eliminaciones
 * relacionadas deben coordinarse desde la capa de aplicación.</p>
 */
public class AlumnoDAO {

    private MongoCollection<Document> coleccion;

    public AlumnoDAO() {
        MongoDatabase db = Conexion.getBaseDeDatos();
        this.coleccion = db.getCollection("alumnos");
    }

    // aula_id funciona como referencia a la coleccion de aulas.
    public List<Alumno> obtenerPorAula(String aulaId) {
        List<Alumno> alumnos = new ArrayList<>();
        Document filtro = new Document("aula_id", new ObjectId(aulaId));
        for (Document doc : coleccion.find(filtro)) {
            Alumno alumno = new Alumno(
                doc.getObjectId("_id").toString(),
                doc.getString("nombre"),
                doc.getString("apellido"),
                doc.getInteger("edad"),
                doc.getObjectId("aula_id").toString()
            );
            alumnos.add(alumno);
        }
        return alumnos;
    }

    /** Inserta un alumno vinculado al aula indicada. */
    public void insertar(String nombre, String apellido, int edad, String aulaId) {
        Document doc = new Document()
            .append("nombre", nombre)
            .append("apellido", apellido)
            .append("edad", edad)
            .append("aula_id", new ObjectId(aulaId));
        coleccion.insertOne(doc);
    }

    /** Elimina al alumno, pero no sus reportes; el llamador debe eliminarlos antes. */
    public void eliminar(String alumnoId) {
        coleccion.deleteOne(new Document("_id", new ObjectId(alumnoId)));
    }

    public void eliminarPorAula(String aulaId) {
        coleccion.deleteMany(new Document("aula_id", new ObjectId(aulaId)));
    }
}
