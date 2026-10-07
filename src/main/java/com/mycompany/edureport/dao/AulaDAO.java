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
import com.mycompany.edureport.modelo.Aula;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;
import org.bson.types.ObjectId;

/**
 * Capa de acceso a la colección {@code aulas}.
 *
 * <p>La interfaz llama a este objeto en lugar de construir documentos MongoDB
 * directamente. Para portar el sistema, esta clase puede sustituirse por un
 * repositorio SQL o por un cliente HTTP manteniendo sus operaciones públicas.</p>
 */
public class AulaDAO {

    private MongoCollection<Document> coleccion;

    public AulaDAO() {
        MongoDatabase db = Conexion.getBaseDeDatos();
        this.coleccion = db.getCollection("aulas");
    }

    // Convierte los documentos de MongoDB en objetos Aula para la interfaz.
    public List<Aula> obtenerTodas() {
        List<Aula> aulas = new ArrayList<>();
        for (Document doc : coleccion.find()) {
            Aula aula = new Aula(
                doc.getObjectId("_id").toString(),
                doc.getString("especialidad"),
                doc.getInteger("grado"),
                doc.getString("grupo"),
                doc.getString("turno")
            );
            aulas.add(aula);
        }
        return aulas;
    }

    /** Crea un aula y deja que MongoDB genere su identificador. */
    public void insertar(String especialidad, int grado, String grupo, String turno) {
        // Las relaciones se guardan mediante ObjectId en los documentos relacionados.
        Document doc = new Document()
            .append("especialidad", especialidad)
            .append("grado", grado)
            .append("grupo", grupo)
            .append("turno", turno);
        coleccion.insertOne(doc);
    }

    /** Elimina solo el aula; la limpieza de alumnos y reportes la coordina Interfaz. */
    public void eliminar(String aulaId) {
        coleccion.deleteOne(new Document("_id", new ObjectId(aulaId)));
    }
}