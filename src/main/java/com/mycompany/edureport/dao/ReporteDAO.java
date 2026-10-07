/*2
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
import com.mycompany.edureport.modelo.Reporte;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.bson.Document;
import org.bson.types.ObjectId;

/**
 * Capa de persistencia de reportes disciplinarios.
 *
 * <p>Cada reporte apunta a un alumno mediante {@code alumno_id}. Los métodos
 * con "Ordenados" aplican orden descendente por fecha y se usan al construir
 * documentos PDF.</p>
 */
public class ReporteDAO {

    private MongoCollection<Document> coleccion;

    public ReporteDAO() {
        MongoDatabase db = Conexion.getBaseDeDatos();
        this.coleccion = db.getCollection("reportes");
    }

    public List<Reporte> obtenerPorAlumno(String alumnoId) {
        List<Reporte> reportes = new ArrayList<>();
        Document filtro = new Document("alumno_id", new ObjectId(alumnoId));
        for (Document doc : coleccion.find(filtro)) {
            Reporte reporte = new Reporte(
                doc.getObjectId("_id").toString(),
                doc.getObjectId("alumno_id").toString(),
                doc.getString("tipo"),
                doc.getString("severidad"),
                doc.getString("motivo"),
                doc.getDate("fecha")
            );
            reportes.add(reporte);
        }
        return reportes;
    }

    /** Registra una incidencia asociada al alumno indicado. */
    public void insertar(String alumnoId, String tipo, String severidad, String motivo, Date fecha) {
        Document doc = new Document()
            .append("alumno_id", new ObjectId(alumnoId))
            .append("tipo", tipo)
            .append("severidad", severidad)
            .append("motivo", motivo)
            .append("fecha", fecha);
        coleccion.insertOne(doc);
    }

    /** Devuelve el total de incidencias, sin cargar todos los documentos. */
    public int contarPorAlumno(String alumnoId) {
        Document filtro = new Document("alumno_id", new ObjectId(alumnoId));
        return (int) coleccion.countDocuments(filtro);
    }

    public void eliminar(String reporteId) {
        coleccion.deleteOne(new Document("_id", new ObjectId(reporteId)));
    }

    public void eliminarPorAlumno(String alumnoId) {
        coleccion.deleteMany(new Document("alumno_id", new ObjectId(alumnoId)));
    }

    public List<Reporte> obtenerPorAlumnoOrdenados(String alumnoId) {
        // El PDF necesita mostrar primero los reportes mas recientes.
        List<Reporte> reportes = new ArrayList<>();
        Document filtro = new Document("alumno_id", new ObjectId(alumnoId));
        Document orden = new Document("fecha", -1);
        for (Document doc : coleccion.find(filtro).sort(orden)) {
            Reporte reporte = new Reporte(
                    doc.getObjectId("_id").toString(),
                    doc.getObjectId("alumno_id").toString(),
                    doc.getString("tipo"),
                    doc.getString("severidad"),
                    doc.getString("motivo"),
                    doc.getDate("fecha")
            );
            reportes.add(reporte);
        }
        return reportes;
    }
}