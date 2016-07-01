package Beans;

/**
 * Created by diego on 1/07/2016.
 */
public class IncidenciaImagenBean {

    private int idIncidenciaLocal;
    private int idIncidenciaRemota;
    private int correlativo;
    private String rutaImagen;
    private int idImagen;

    public IncidenciaImagenBean() {

    }

    public IncidenciaImagenBean(int idIncidenciaLocal,
                                int idIncidenciaRemota,
                                int correlativo,
                                String rutaImagen,
                                int idImagen) {
        this.setIdIncidenciaLocal(idIncidenciaLocal);
        this.setIdIncidenciaRemota(idIncidenciaRemota);
        this.setCorrelativo(correlativo);
        this.setRutaImagen(rutaImagen);
        this.setIdImagen(idImagen);
    }

    public int getIdIncidenciaLocal() {
        return idIncidenciaLocal;
    }

    public void setIdIncidenciaLocal(int idIncidenciaLocal) {
        this.idIncidenciaLocal = idIncidenciaLocal;
    }

    public int getIdIncidenciaRemota() {
        return idIncidenciaRemota;
    }

    public void setIdIncidenciaRemota(int getIdIncidenciaRemota) {
        this.idIncidenciaRemota = getIdIncidenciaRemota;
    }

    public int getCorrelativo() {
        return correlativo;
    }

    public void setCorrelativo(int correlativo) {
        this.correlativo = correlativo;
    }

    public String getRutaImagen() {
        return rutaImagen;
    }

    public void setRutaImagen(String rutaImagen) {
        this.rutaImagen = rutaImagen;
    }

    public int getIdImagen() {
        return idImagen;
    }

    public void setIdImagen(int idImagen) {
        this.idImagen = idImagen;
    }
}