package Beans;

import java.util.List;

/**
 * Created by diego on 25/06/2016.
 */
public class IncidenciaBean {

    private int idIncidenciaLocal;
    private int idIncidenciaRemota;
    private String titulo;
    private String descripcion;
    private int estadoSync;
    private List<IncidenciaImagenBean> lstImagenes;

    public IncidenciaBean(int idIncidenciaLocal, int idIncidenciaRemota, String titulo, String descripcion, int estadoSync) {
        this.setIdIncidenciaLocal(idIncidenciaLocal);
        this.setIdIncidenciaRemota(idIncidenciaRemota);
        this.setTitulo(titulo);
        this.setDescripcion(descripcion);
        this.setEstadoSync(estadoSync);
    }

    public int getIdIncidenciaLocal() {
        return idIncidenciaLocal;
    }

    public void setIdIncidenciaLocal(int idIncidenciaLocal) {
        this.idIncidenciaLocal = idIncidenciaLocal;
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

    public int getIdIncidenciaRemota() {
        return idIncidenciaRemota;
    }

    public void setIdIncidenciaRemota(int idIncidenciaRemota) {
        this.idIncidenciaRemota = idIncidenciaRemota;
    }

    public String toString() {
        return "ID local: "+getIdIncidenciaLocal()+"\n"+
               "ID remoto: "+getIdIncidenciaRemota()+"\n"+
                "Titulo: "+getTitulo()+"\n"+
                "Desc: "+getDescripcion()+"\n"+
                "Estado: "+getEstadoSync()+"\n"+
                "lstImagenes: "+( getLstImagenes() != null ? getLstImagenes().size() : "0 imagenes" )
                ;
    }

    public List<IncidenciaImagenBean> getLstImagenes() {
        return lstImagenes;
    }

    public void setLstImagenes(List<IncidenciaImagenBean> lstImagenes) {
        this.lstImagenes = lstImagenes;
    }
}