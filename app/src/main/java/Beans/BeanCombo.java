package Beans;

/**
 * Created by dflores on 11/02/2016.
 */
public class BeanCombo {

    private int idCombo;
    private String descCombo;

    public BeanCombo(int idCombo, String descCombo) {
        this.setIdCombo(idCombo);
        this.setDescCombo(descCombo);
    }


    public int getIdCombo() {
        return idCombo;
    }

    public void setIdCombo(int idCombo) {
        this.idCombo = idCombo;
    }

    public String getDescCombo() {
        return descCombo;
    }

    public void setDescCombo(String descCombo) {
        this.descCombo = descCombo;
    }
}
