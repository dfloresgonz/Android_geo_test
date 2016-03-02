package Servicios;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import Beans.MapaVariables;
import Beans.Utiles;

/**
 * Created by dflores on 01/03/2016.
 */
public class RutaService extends AsyncTask<String, Void, String> {

    Utiles utiles = new Utiles();
    static Context mContext;

    public RutaService(Context context) {
        mContext = context;
    }

    @Override
    protected String doInBackground(String... params) {
        return utiles.readJSONFeed(params[0]);
    }

    protected void onPostExecute(String result) {
        try {
            Log.d("BUHOO", "result:::::: " + result);
            try {
                JSONObject resultado = new JSONObject(result);
                JSONArray routes = resultado.getJSONArray("routes");
                long distanceForSegment = routes.getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("distance").getInt("value");
                JSONArray steps = routes.getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONArray("steps");
                List<LatLng> lines = new ArrayList<LatLng>();
                for(int i=0; i < steps.length(); i++) {
                    String polyline = steps.getJSONObject(i).getJSONObject("polyline").getString("points");
                    for(LatLng p : utiles.decodePolyline(polyline)) {
                        lines.add(p);
                    }
                }
                MapaVariables.lineas = lines;
                Intent intent = new Intent();
                intent.setAction("com.buhooapp.RUTA_RETRIEVED");
                mContext.sendBroadcast(intent);
            } catch (Exception e) {
                e.printStackTrace();
                StringWriter errors = new StringWriter();
                e.printStackTrace(new PrintWriter(errors));
                Log.d("CREATION", "tratando el JSON: " + errors.toString());
            }
        } catch (Exception e) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            Log.d("CREATION", "errorrrr onPostExecute: " + errors.toString());
        }
    }
}
