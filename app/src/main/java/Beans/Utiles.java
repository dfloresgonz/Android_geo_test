package Beans;

import android.graphics.drawable.Drawable;
import android.util.Log;
import com.google.android.gms.maps.model.LatLng;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import Interfaces.IncidenciasInterface;
import Servicios.ComboService;
import Servicios.ComunidadService;
import Servicios.ImagenService;
import Servicios.IncidenciasService;
import Servicios.InsertarIncidenciaService;
import Servicios.PublicidadService;

/**
 * Created by dflores on 11/02/2016.
 */
public class Utiles {

    public static String readJSONFeed(String stringUrl) {
        StringBuilder response  = new StringBuilder();
        try {
            URL url = new URL(stringUrl);
            HttpURLConnection httpconn = (HttpURLConnection)url.openConnection();
            int httpResponse = HttpURLConnection.HTTP_UNAVAILABLE;
            try {
                httpResponse = httpconn.getResponseCode();
            } catch(Exception e) {
                httpResponse = HttpURLConnection.HTTP_UNAVAILABLE;
            }
            if (httpResponse == HttpURLConnection.HTTP_OK) {
                BufferedReader input = new BufferedReader(new InputStreamReader(httpconn.getInputStream()),8192);
                String strLine = null;
                while ((strLine = input.readLine()) != null) {
                    response.append(strLine);
                }
                input.close();
            }
        } catch(Exception e) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            Log.d("BUHOO", "errorrrr readJSONFeed: " + errors.toString());
        }
        return response.toString();
    }

    public static double round(double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }

    public static String distanciaFormat(double value) {
        String distancia = null;
        if(value < 100) {
            distancia = round(value, 1)+" metros";
        } else {
            distancia = round((value / 1000), 1)+" KM.";
        }
        return distancia;
    }

    /** POLYLINE DECODER - http://jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java **/
    public List<LatLng> decodePolyline(String encoded) {
        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;
        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((double) lat / 1E5, (double) lng / 1E5);
            poly.add(p);
        }
        return poly;
    }

    public static void invocarComboServicio(JSONObject jsonObject, GetResponse getResponse) {
        String servicio = "http://"+MapaVariables.ipServer+"/buhoo/servicio/getCombo?json="+jsonObject;
        ComboService servicioCombo = new ComboService();
        servicioCombo.getResponse = getResponse;
        servicioCombo.execute(servicio);
    }

    public static void invocarPublicidadServicio(GetResponse getResponse) {
        String servicio = "http://"+MapaVariables.ipServer+"/buhoo/servicio/getPublicidad";
        PublicidadService servicioPublicidad = new PublicidadService();
        servicioPublicidad.getResponse = getResponse;
        servicioPublicidad.execute(servicio);
    }

    public static void invocarImagenServicio(GetResponse getResponse, String imagen) {
        ImagenService servicioImagen = new ImagenService();
        servicioImagen.getResponse = getResponse;
        servicioImagen.execute(imagen);
    }

    public static void invocarComunidadServicio(GetResponse getResponse, int idUsuario, int idComu) {
        String servicio = "http://"+MapaVariables.ipServer+"/buhoo/intranet/mi_comunidad/getComunidadByPersona_Service?id_persona="+idUsuario+"&id_comunidad="+idComu;
        ComunidadService servicioComunidad = new ComunidadService();
        servicioComunidad.getResponse = getResponse;
        servicioComunidad.execute(servicio);
    }

    public static void verificarIncidenciasNewRemotoServicio(JSONObject jsonGeneral, DBController controller, IncidenciasInterface incidenciasInterface) {
        String servicio = "http://"+MapaVariables.ipServer+"/buhoo/servicio/getIncidenciasRemotosNew?objConsulta="+jsonGeneral;
        IncidenciasService incidenciasService = new IncidenciasService(controller);
        incidenciasService.incidenciasInterface = incidenciasInterface;
        incidenciasService.execute(servicio);
    }

    public static void insertarIncidenciasServicio(JSONObject jsonGeneral, DBController controller, IncidenciasInterface incidenciasInterface) {
        String servicio = "http://"+MapaVariables.ipServer+"/buhoo/servicio/insertarIncidencia?objInsert="+jsonGeneral;
        InsertarIncidenciaService incidenciasService = new InsertarIncidenciaService(controller);
        incidenciasService.incidenciasInterface = incidenciasInterface;
        incidenciasService.execute(servicio);
    }

    public static Publicidad getPublicidad(Drawable imagen) {
        for(Publicidad itm : MapaVariables.arryDraw) {
            if(imagen.equals(itm.getImagen())) {
                return itm;
            }
        }
        return null;
    }

    public static void printearErrores(Exception e, String detalle) {
        StringWriter errors = new StringWriter();
        e.printStackTrace(new PrintWriter(errors));
        Log.d("BUHOO", "ERROR "+ detalle + errors.toString());
    }
}
