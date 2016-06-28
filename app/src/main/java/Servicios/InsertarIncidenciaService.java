package Servicios;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import Beans.DBController;
import Beans.IncidenciaBean;
import Beans.Utiles;
import Interfaces.IncidenciasInterface;
import facilito.codigo.app.dflores.com.myapplicationcf.R;

/**
 * Created by diego on 27/06/2016.
 */
public class InsertarIncidenciaService extends AsyncTask<String, Void, String> {

    public IncidenciasInterface incidenciasInterface = null;
    List<IncidenciaBean> arryDraw = new ArrayList<IncidenciaBean>();
    DBController controller;

    public InsertarIncidenciaService(DBController _controller) {
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
                int idIncidenciaRemota = publ.getInt("id_incidencia");
                int idIncidencialocal  = publ.getInt("id_incidencia_local");
                String titulo          = publ.getString("titulo");
                String descripcion     = publ.getString("descripcion");
                arryDraw.add(new IncidenciaBean(idIncidencialocal, idIncidenciaRemota, titulo, descripcion, R.drawable.synched) );
            }
        } catch(Exception e) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            Log.d("BUHOO", " ERROR IMAGENES " + errors.toString());
        }
        return "InsertarIncidenciaService RPTA size: "+arryDraw.size();
    }

    @Override
    protected void onPostExecute(String message) {
        Log.d("BUHOO", " onPostExecute:: " + message);
        incidenciasInterface.getIncidenciasInsertadas(arryDraw, controller);
    }
}