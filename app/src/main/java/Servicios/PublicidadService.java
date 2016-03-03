package Servicios;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import Beans.GetResponse;
import Beans.Publicidad;
import Beans.Utiles;

/**
 * Created by dflores on 02/03/2016.
 */
public class PublicidadService extends AsyncTask<String, Void, String> {

    public GetResponse getResponse = null;
    List<Publicidad> arryDraw = new ArrayList<Publicidad>();

    @Override
    protected String doInBackground(String[] params) {
        String result = Utiles.readJSONFeed(params[0]);
        try {
            JSONObject mainResponseObject = new JSONObject(result);
            JSONArray objArry = mainResponseObject.getJSONArray("publicidad");
            for (int i = 0; i < objArry.length(); ++i) {
                JSONObject publ = objArry.getJSONObject(i);
                String linkBanner = publ.getString("link");//Log.d("BUHOO", "IMG:::::::: "+linkBanner);
                InputStream is = (InputStream) new URL(linkBanner).getContent();
                arryDraw.add(new Publicidad(linkBanner, Drawable.createFromStream(is, "src name")) );
            }
        } catch(Exception e) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            Log.d("BUHOO", " ERROR IMAGENES " + errors.toString());
        }
        return "some message size: "+arryDraw.size();
    }

    @Override
    protected void onPostExecute(String message) {
        Log.d("BUHOO", " respondio!! " + message);
        getResponse.getDataPublicidad(arryDraw);
    }
}
