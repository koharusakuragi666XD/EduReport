package com.mycompany.edureport;
import com.mycompany.edureport.interfaz.Tema;

import com.mycompany.edureport.dao.AlumnoDAO;
import com.mycompany.edureport.dao.AulaDAO;
import com.mycompany.edureport.dao.ReporteDAO;
import com.mycompany.edureport.modelo.Alumno;
import com.mycompany.edureport.modelo.Aula;
import com.mycompany.edureport.modelo.Reporte;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Reglas de negocio que antes vivían dentro de la ventana: resúmenes para las tarjetas y
 * borrados en cascada (MongoDB no elimina las referencias automáticamente).
 *
 * <p>Todos los métodos hacen consultas a la base: llámalos desde un SwingWorker, no desde el
 * hilo de Swing.</p>
 */
public class ServicioAcademico {

    /** Un aula con sus totales, para pintar la tarjeta. */
    public static final class ResumenAula {
        public final Aula aula;
        public final int alumnos;
        public final int graves;

        ResumenAula(Aula aula, int alumnos, int graves) {
            this.aula = aula;
            this.alumnos = alumnos;
            this.graves = graves;
        }
    }

    /** Un alumno con el número de reportes, para pintar la lista. */
    public static final class ResumenAlumno {
        public final Alumno alumno;
        public final int reportes;
        public final int graves;

        ResumenAlumno(Alumno alumno, int reportes, int graves) {
            this.alumno = alumno;
            this.reportes = reportes;
            this.graves = graves;
        }
    }

    private final AulaDAO aulaDAO;
    private final AlumnoDAO alumnoDAO;
    private final ReporteDAO reporteDAO;

    public ServicioAcademico(AulaDAO aulaDAO, AlumnoDAO alumnoDAO, ReporteDAO reporteDAO) {
        this.aulaDAO = aulaDAO;
        this.alumnoDAO = alumnoDAO;
        this.reporteDAO = reporteDAO;
    }

    public AulaDAO aulaDAO()       { return aulaDAO; }
    public AlumnoDAO alumnoDAO()   { return alumnoDAO; }
    public ReporteDAO reporteDAO() { return reporteDAO; }

    /**
     * Todas las aulas con su número de alumnos y de reportes graves.
     * Hace una consulta por alumno; si la base crece, conviene añadir a los DAO un método
     * que cuente directamente por aula.
     */
    public List<ResumenAula> resumenAulas() {
        List<ResumenAula> resultado = new ArrayList<>();
        for (Aula aula : aulaDAO.obtenerTodas()) {
            List<Alumno> alumnos = alumnoDAO.obtenerPorAula(aula.getId());
            int graves = 0;
            for (Alumno alumno : alumnos) {
                graves += contarGraves(reporteDAO.obtenerPorAlumno(alumno.getId()));
            }
            resultado.add(new ResumenAula(aula, alumnos.size(), graves));
        }
        return resultado;
    }

    /** Alumnos del aula ordenados por apellido y nombre, con su número de reportes. */
    public List<ResumenAlumno> resumenAlumnos(Aula aula) {
        List<ResumenAlumno> resultado = new ArrayList<>();
        for (Alumno alumno : alumnoDAO.obtenerPorAula(aula.getId())) {
            List<Reporte> reportes = reporteDAO.obtenerPorAlumno(alumno.getId());
            resultado.add(new ResumenAlumno(alumno, reportes.size(), contarGraves(reportes)));
        }
        resultado.sort(Comparator.comparing(
                (ResumenAlumno r) -> Tema.normalizar(r.alumno.getApellido() + " " + r.alumno.getNombre())));
        return resultado;
    }

    /** Elimina el aula junto con sus alumnos y los reportes de cada uno. */
    public void eliminarAula(Aula aula) {
        for (Alumno alumno : alumnoDAO.obtenerPorAula(aula.getId())) {
            reporteDAO.eliminarPorAlumno(alumno.getId());
        }
        alumnoDAO.eliminarPorAula(aula.getId());
        aulaDAO.eliminar(aula.getId());
    }

    /** Elimina al alumno junto con todos sus reportes. */
    public void eliminarAlumno(Alumno alumno) {
        reporteDAO.eliminarPorAlumno(alumno.getId());
        alumnoDAO.eliminar(alumno.getId());
    }

    private static int contarGraves(List<Reporte> reportes) {
        int graves = 0;
        for (Reporte r : reportes) {
            if (Severidad.desde(r.getSeveridad()) == Severidad.GRAVE) graves++;
        }
        return graves;
    }
}
