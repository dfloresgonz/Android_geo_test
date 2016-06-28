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

import Beans.DBController;
import Beans.GetResponse;
import Beans.IncidenciaBean;
import Beans.Publicidad;
import Beans.Utiles;
import Interfaces.IncidenciasInterface;
import facilito.codigo.app.dflores.com.myapplicationcf.R;

/**
 * Created by diego on 27/06/2016.
 */
public class IncidenciasService extends AsyncTask<String, Void, String> {

    public IncidenciasInterface incidenciasInterface = null;
    List<IncidenciaBean> arryDraw = new ArrayList<IncidenciaBean>();
    DBController controller;

    public IncidenciasService(DBController _controller) {
        controller = _controller;
    }

    @Override
    protected String doInBackground(String... params) {
        String result = Utiles.readJSONFeed(params[0]);
        try {
            JSONObject mainResponseObject = new JSONObject(result);
            JSONArray objArry = mainResponseObject.getJSONArray("incidencias");
            for (int i = 0; i < objArry.length(); ++i) {
                JSONObject publ = objArry.getJSONObject(i);
                Log.d("BUHOO", "getJSONObject____: "+objArry.getJSONObject(i));
                int idIncidenciaRemoto = publ.getInt("id_incidencia");
                String titulo          = publ.getString("titulo");
                String descripcion     = publ.getString("descripcion");
                arryDraw.add(new IncidenciaBean(0, idIncidenciaRemoto, titulo, descripcion, 1) );
            }
        } catch(Exception e) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            Log.d("BUHOO", " ERROR verificarIncidenciasNewRemotoServicio " + errors.toString());
        }
        return "incidencias size: "+arryDraw.size();
    }

    @Override
    protected void onPostExecute(String message) {
        Log.d("BUHOO", " onPostExecute verificarIncidenciasNewRemotoServicio: " + message);
        incidenciasInterface.getIncidenciasRemote(arryDraw, controller);
    }
}
