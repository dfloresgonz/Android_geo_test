package Beans;

import java.util.ArrayList;

/**
 * Created by dflores on 02/03/2016.
 */
public interface GetResponse {
    public Void getData(ArrayList<String> comusList,
                        ArrayList<BeanCombo> comunidadesBeanCombo,
                        String tipoCombo);
}
