package Servicios;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import Beans.DBController;
import Beans.ImagenBean;
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
    List<ImagenBean> lstImages;

    public InsertarIncidenciaService(DBController _controller, List<ImagenBean> _lstImages) {
        controller = _controller;
        lstImages    = _lstImages;
    }

    @Override
    protected String doInBackground(String... params) {
        HashMap<String, String> postDataParams = new HashMap<>();
        if(lstImages != null) {
            int key = 0;
            for (ImagenBean img : lstImages) {
                String uploadImage = Utiles.__getStringImage(img.bitmapImage);
                postDataParams.put("imagen_"+key, uploadImage);
                key++;
            }
        }
        String result = Utiles.registrarIncidencia(params[0], postDataParams);
        try {
            Log.d("BUHOO","......resultLength:: "+result.length()+"   result: "+result);
            if(result != null && result.length() > 0) {
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
            }
        } catch(Exception e) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            Log.d("BUHOO", " ERROR InsertarIncidenciaService " + errors.toString());
        }
        return "InsertarIncidenciaService RPTA size: "+arryDraw.size();
    }

    @Override
    protected void onPostExecute(String message) {
        Log.d("BUHOO", " onPostExecute:: " + message);
        incidenciasInterface.getIncidenciasInsertadas(arryDraw, controller);
    }
}