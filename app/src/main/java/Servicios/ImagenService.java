package Servicios;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import Beans.BeanCombo;
import Beans.GetResponse;
import Beans.Publicidad;
import Beans.Utiles;

/**
 * Created by dflores on 02/03/2016.
 */
public class ImagenService extends AsyncTask<String, Void, String> {

    public GetResponse getResponse = null;
    Drawable imagen;

    protected String doInBackground(String... urls) {
        try {
            InputStream is = (InputStream) new URL(urls[0]).getContent();
            imagen =  Drawable.createFromStream(is, "src name");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "OK";
    }

    protected void onPostExecute(String result) {
        Log.d("BUHOO", "result:::::: " + result);
        getResponse.getDataUsuarioFoto(imagen);
    }
}