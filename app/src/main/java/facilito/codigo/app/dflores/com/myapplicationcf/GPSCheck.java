package facilito.codigo.app.dflores.com.myapplicationcf;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.util.Log;

/**
 * Created by dflores on 22/02/2016.
 */
public class GPSCheck extends BroadcastReceiver{


    @Override
    public void onReceive(Context context, Intent intent) {
        LocationManager locationManager = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            //Log.d("BUHOO", " HABILITADO !!!!!!!!!!!!!!!!!!!");
        } else {
            //Log.d("BUHOO", " DESHABILITADO !!!!!!!!!!!!!!!!!!!");
            //Toast.makeText(context, "Please switch on the GPS", Toast.LENGTH_LONG).show();
        }
    }
}