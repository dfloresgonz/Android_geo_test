package facilito.codigo.app.dflores.com.myapplicationcf;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import Adaptadores.IncidenciasAdapter;
import Beans.DBController;
import Beans.IncidenciaBean;
import Beans.MapaVariables;

/**
 * Created by diego on 25/06/2016.
 */
public class Incidencia extends AppCompatActivity {

    private RecyclerView recycler;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager lManager;

    DBController controller = new DBController(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.incidencias);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarInc);
        setSupportActionBar(toolbar);

        if( MapaVariables.ipServer == null) {
            MapaVariables.ipServer = getResources().getString(R.string.ip_server);
        }

        /** LLENAR LISTA */
        List<IncidenciaBean> incidencias = controller.getAllIncidencias();
        if(incidencias.size() == 0) {
            controller.insertarIncidencia(new IncidenciaBean(0, "Problema en planta", "Hubo un derrame de minerales", 0));
            incidencias = controller.getAllIncidencias();
        }

        /*incidencias.add(new IncidenciaBean(1, "Problema en planta", "Hubo un derrame de minerales", R.drawable.synched));
        incidencias.add(new IncidenciaBean(2, "Alerta", "OJO hay personal sin casco", R.drawable.notsynched));
        incidencias.add(new IncidenciaBean(3, "Cuidado", "No hay personal de seguridad", R.drawable.notsynched));
        incidencias.add(new IncidenciaBean(4, "Mucho personal", "Hay muchas personas en el área central, supera el aforo", R.drawable.synched));
        incidencias.add(new IncidenciaBean(5, "Seguridad", "Llamar más personal de seguridad", R.drawable.synched));
        */
        // Obtener el Recycler
        recycler = (RecyclerView) findViewById(R.id.lstIncidencias);
        recycler.setHasFixedSize(true);

        // Usar un administrador para LinearLayout
        lManager = new LinearLayoutManager(this);
        recycler.setLayoutManager(lManager);

        // Crear un nuevo adaptador
        adapter = new IncidenciasAdapter(incidencias);
        recycler.setAdapter(adapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabInc);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    public static class ConeccionCheck extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE );
            NetworkInfo activeNetInfoMobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            NetworkInfo activeNetInfoWifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            boolean isConnectedMobile = activeNetInfoMobile != null && activeNetInfoMobile.isConnectedOrConnecting();
            boolean isConnectedWifi   = activeNetInfoWifi   != null && activeNetInfoWifi.isConnectedOrConnecting();
            if (isConnectedMobile || isConnectedWifi) {
                Log.d("BUHOO", " CONECTADO A LA RED");
            } else {
                Log.d("BUHOO", " DESCONECTADO DE LA RED");
            }
        }
    }
}