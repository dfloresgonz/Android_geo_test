package Beans;

import android.graphics.drawable.Drawable;

/**
 * Created by dflores on 29/02/2016.
 */
public class Publicidad {

    private String rutaURL;
    private Drawable imagen;

    public Publicidad(String rutaURL, Drawable imagen) {
        this.setRutaURL(rutaURL);
        this.setImagen(imagen);
    }

    public String getRutaURL() {
        return rutaURL;
    }

    public void setRutaURL(String rutaURL) {
        this.rutaURL = rutaURL;
    }

    public Drawable getImagen() {
        return imagen;
    }

    public void setImagen(Drawable imagen) {
        this.imagen = imagen;
    }
}
