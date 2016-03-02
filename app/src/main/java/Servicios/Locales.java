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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import Beans.BeanCombo;
import Beans.MapaVariables;
import Beans.Utiles;

/**
 * Created by dflores on 29/02/2016.
 */
public class Locales extends AsyncTask<String, Void, String> {

    Utiles utiles = new Utiles();
    static Context mContext;

    public Locales(Context context) {
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
                String error = mainResponseObject.getString("error");
                if ("0".equals(error)) {
                    MapaVariables.localesJSArray = mainResponseObject.getJSONArray("locales");
                    Intent intent = new Intent();
                    intent.setAction("com.buhooapp.LOCALES_RETRIEVED");
                    mContext.sendBroadcast(intent);
                    Log.d("BUHOO", " ---- LOCALES_RETRIEVED executed: ");
                } else {
                    Log.d("BUHOO", " ---- error inesperdo: ");
                }
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
