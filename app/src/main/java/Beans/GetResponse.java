package Beans;

import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dflores on 02/03/2016.
 */
public interface GetResponse {
    public Void getDataCombo(ArrayList<String> comusList,
                             ArrayList<BeanCombo> comunidadesBeanCombo,
                             String tipoCombo);

    public Void getDataPublicidad(List<Publicidad> arryDraw);

    public Void getDataUsuarioFoto(Drawable imagen);
}
