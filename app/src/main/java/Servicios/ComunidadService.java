package Servicios;

import android.os.AsyncTask;
import android.util.Log;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;
import org.json.JSONObject;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import Beans.GetResponse;
import Beans.Utiles;

/**
 * Created by dflores on 02/03/2016.
 */
public class ComunidadService extends AsyncTask<String, Void, String> {

    public GetResponse getResponse = null;
    PolygonOptions comunidad = new PolygonOptions();

    protected String doInBackground(String... urls) {
        return Utiles.readJSONFeed(urls[0]);
    }

    protected void onPostExecute(String result) {
        try {
            Log.d("BUHOO", "result:::::: " + result);
            JSONObject mainResponseObject = new JSONObject(result);
            try {
                String error = mainResponseObject.getString("error");
                if("0".equals(error)) {
                    JSONObject polyObj = new JSONObject(mainResponseObject.getString("poligono"));
                    String puntos = polyObj.getString("puntos");
                    List<String> puntosList = Arrays.asList(puntos.split(","));
                    for(Iterator it = puntosList.iterator(); it.hasNext(); ) {
                        String str = (String) it.next();
                        String[] latlon = str.split(" ");
                        double lati  = Double.parseDouble(latlon[0].replaceAll("\"", ""));
                        double longi = Double.parseDouble(latlon[1].replaceAll("\"", ""));
                        comunidad.add(new LatLng(lati, longi));
                    }
                    Log.d("BUHOO"," onPostExecute cantidad latlongs: "+comunidad.getPoints().size());
                    getResponse.getDataComunidad(comunidad, polyObj.getInt("id_comunidad"), polyObj.getString("desc_comunidad"));
                    //onMapReady(mMap);
                } else {
                    Log.d("CREATION", " ---- error inesperdo: " );
                }
            } catch (Exception e) {
                e.printStackTrace();
                StringWriter errors = new StringWriter();
                e.printStackTrace(new PrintWriter(errors));
                Log.d("CREATION", "tratando el JSON: "+errors.toString());
            }
        } catch (Exception e) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            Log.d("CREATION", "errorrrr onPostExecute: "+errors.toString());
        }
    }
}
