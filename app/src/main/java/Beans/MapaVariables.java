package Beans;

import android.app.Application;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dflores on 29/02/2016.
 */
public class MapaVariables extends Application {

    private static MapaVariables singleton;
    public static boolean enBusqueda = false;
    public static JSONArray localesJSArray;
    public static Marker markerMiPosicion;
    public static int idComunidad;
    public static String descComunidad;
    public static double distanciaPoligono = 0.0;
    public static LatLng latLonComunidad;
    //RutaService
    public static List<LatLng> lineas;
    public static Polyline polylineToAdd;

    public static MapaVariables getInstance() {
        return singleton;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;
    }
}
