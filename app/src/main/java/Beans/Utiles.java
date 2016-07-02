package Beans;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Base64;
import android.util.Log;
import com.google.android.gms.maps.model.LatLng;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import Interfaces.IncidenciasInterface;
import Servicios.ComboService;
import Servicios.ComunidadService;
import Servicios.ImagenService;
import Servicios.IncidenciasService;
import Servicios.InsertarIncidenciaService;
import Servicios.PublicidadService;
import facilito.codigo.app.dflores.com.myapplicationcf.R;

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
            Log.d("BUHOO", "httpResponse: "+httpResponse+"   ___stringUrl: "+stringUrl);
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

    public static String registrarIncidencia(String stringUrl, HashMap<String, String> postDataParams) {
        String response = "";
        try {
            Log.d("BUHOO", " ::::registrarIncidencia::___stringUrl: "+stringUrl);
            URL url = new URL(stringUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            if(postDataParams != null && postDataParams.size() > 0) {//SI ADJUNTO IMAGENES
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(postDataParams));

                writer.flush();
                writer.close();
                os.close();
            }

            int responseCode = conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                response = br.readLine();
            } else {
                response = "Error registrarIncidencia";
            }
        } catch(Exception e) {
            printearErrores(e, "ERROR registrarIncidencia: ");
        }
        return response;
    }

    private static String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (first) {
                first = false;
            } else {
                result.append("&");
            }
            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }
        if(!first) {//entro al loop se setea un FLAG para validar en el server
            result.append("&has_img="+params.size());
        }

        return result.toString();
    }

    public static String __getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    public static boolean checkInternet(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE );
        NetworkInfo activeNetInfoMobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo activeNetInfoWifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        boolean isConnectedMobile = activeNetInfoMobile != null && activeNetInfoMobile.isConnectedOrConnecting();
        boolean isConnectedWifi   = activeNetInfoWifi   != null && activeNetInfoWifi.isConnectedOrConnecting();
        return isConnectedMobile || isConnectedWifi;
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
        if(MapaVariables.ipServer == null) {
            MapaVariables.ipServer = controller.getCtx().getString(R.string.ip_server);
        }
        String servicio = "http://"+MapaVariables.ipServer+"/buhoo/servicio/getIncidenciasRemotosNew?objConsulta="+jsonGeneral;
        IncidenciasService incidenciasService = new IncidenciasService(controller);
        incidenciasService.incidenciasInterface = incidenciasInterface;
        incidenciasService.execute(servicio);
    }

    public static void insertarIncidenciasServicio(JSONObject jsonGeneral, DBController controller, IncidenciasInterface incidenciasInterface, List<ImagenBean> _lstImages) {
        if(MapaVariables.ipServer == null) {
            MapaVariables.ipServer = controller.getCtx().getString(R.string.ip_server);
        }
        String servicio = "http://"+MapaVariables.ipServer+"/buhoo/servicio/insertarIncidencia?objInsert="+jsonGeneral;
        InsertarIncidenciaService incidenciasService = new InsertarIncidenciaService(controller, _lstImages);
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

    public static Bitmap __getBitmap(String filePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 8;
        Bitmap _bitmap = BitmapFactory.decodeFile(filePath, options);
        Log.d("BUHOO", "ANCHO: "+_bitmap.getWidth()+"  ALTO: "+_bitmap.getHeight());
        return __compressBitmap(_bitmap);
    }

    public static Bitmap __compressBitmap(Bitmap _bitmap) {
        if(_bitmap.getWidth() > 1100 || _bitmap.getHeight() > 1100) {
            Log.d("BUHOO", "IMAGEN SUPERA LAS DIMENSIONES! REDIMENSIONANDO.......................");
            int nh = (int) ( _bitmap.getHeight() * ( 850.0 / _bitmap.getWidth()) );
            _bitmap = Bitmap.createScaledBitmap(_bitmap, 850, nh, true);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            _bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            byte[] imageInByte = out.toByteArray();
            _bitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(imageInByte));

            Log.d("BUHOO", "PESO::::: "+imageInByte.length);
        } else if(_bitmap.getWidth() <= 850 || _bitmap.getHeight() <= 850) {
            Log.d("BUHOO", " NO SE REDIMENSIONO ");
        }
        return _bitmap;
    }
}
