package Beans;

/**
 * Created by diego on 25/06/2016.
 */
public class IncidenciaBean {

    private int idIncidencia;
    private String titulo;
    private String descripcion;
    private int estadoSync;

    public IncidenciaBean(int idIncidencia, String titulo, String descripcion, int estadoSync) {
        this.setIdIncidencia(idIncidencia);
        this.setTitulo(titulo);
        this.setDescripcion(descripcion);
        this.setEstadoSync(estadoSync);
    }

    public int getIdIncidencia() {
        return idIncidencia;
    }

    public void setIdIncidencia(int idIncidencia) {
        this.idIncidencia = idIncidencia;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getEstadoSync() {
        return estadoSync;
    }

    public void setEstadoSync(int estadoSync) {
        this.estadoSync = estadoSync;
    }
}