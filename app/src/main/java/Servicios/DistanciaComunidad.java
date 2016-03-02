package Servicios;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;

import Beans.MapaVariables;
import Beans.Utiles;

/**
 * Created by dflores on 01/03/2016.
 */
public class DistanciaComunidad extends AsyncTask<String, Void, String> {

    Utiles utiles = new Utiles();
    static Context mContext;

    public DistanciaComunidad(Context context) {
        mContext = context;
    }

    @Override
    protected String doInBackground(String... params) {
        return utiles.readJSONFeed(params[0]);
    }

    protected void onPostExecute(String result) {
        try {
            Log.d("BUHOO", "result:::::: " + result);
            JSONObject mainResponseObject = new JSONObject(result);
            try {
                String distancia = mainResponseObject.getString("distancia");
                MapaVariables.distanciaPoligono = Double.parseDouble(distancia);
                String latitud = mainResponseObject.getString("lat_poly");
                String longitud = mainResponseObject.getString("lon_poly");
                MapaVariables.latLonComunidad = new LatLng(Double.parseDouble(latitud), Double.parseDouble(longitud));
                Intent intent = new Intent();
                intent.setAction("com.buhooapp.DISTANCIA_COMUNIDAD_RETRIEVED");
                mContext.sendBroadcast(intent);
                Log.d("BUHOO", " ---- DISTANCIA_COMUNIDAD_RETRIEVED executed: ");
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
