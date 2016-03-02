package facilito.codigo.app.dflores.com.myapplicationcf;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import Beans.MapaVariables;
import Servicios.Locales;

/**
 * Created by dflores on 29/02/2016.
 */
public class ReceiverDrawer extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String server = "buhooweb.com";
        String servicioLocales = "http://"+server+"/buhoo//intranet/mi_comunidad/getPuntosBusqueda_Service";
        Log.d("BUHOO", "Recibido!!! " + MapaVariables.enBusqueda);
        MapaVariables.localesJSArray = null;
        //new Locales(Mapa.this).execute(servicioLocales);
    }
}