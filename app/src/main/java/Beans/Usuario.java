package Beans;

import java.io.Serializable;

/**
 * Created by dflores on 10/02/2016.
 */
public class Usuario implements Serializable {
    private Integer idUsuario;
    private String nombreCompleto;

    public Usuario(Integer idUsuario, String nombreCompleto) {
        this.idUsuario = idUsuario;
        this.nombreCompleto = nombreCompleto;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }
}
