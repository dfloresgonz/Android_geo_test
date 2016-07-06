package facilito.codigo.app.dflores.com.myapplicationcf;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import Adaptadores.IncidenciasAdapter;
import Beans.DBController;
import Beans.ImagenBean;
import Beans.IncidenciaBean;
import Beans.IncidenciaImagenBean;
import Beans.Utiles;
import Interfaces.IncidenciasInterface;

/**
 * Created by diego on 25/06/2016.
 */
public class Incidencia extends AppCompatActivity implements IncidenciasInterface {

    private static RecyclerView recycler;
    private static RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager lManager;
    List<IncidenciaBean> lstIncidenciasRemote = new ArrayList<IncidenciaBean>();
    DBController controller = new DBController(this);

    private String IMAGE_DIRECTORY_NAME = null;

    static Context ctx;

    private int mInterval = 5000;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.incidencias);

        ctx = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarInc);
        setSupportActionBar(toolbar);

        IMAGE_DIRECTORY_NAME = getResources().getString(R.string.carpeta_archivos_subida);

        recycler = (RecyclerView) findViewById(R.id.lstIncidencias);
        recycler.setHasFixedSize(true);

        // Usar un administrador para LinearLayout
        lManager = new LinearLayoutManager(this);
        recycler.setLayoutManager(lManager);

        adapter = new IncidenciasAdapter(lstIncidenciasRemote, ctx);
        recycler.setAdapter(adapter);

        String event = null;
        if(savedInstanceState != null) {
            event = savedInstanceState.getString("FROM_NEW_INCIDENCIA");
        } else {
            Bundle extras = getIntent().getExtras();
            if(extras != null) {
                event = extras.getString("FROM_NEW_INCIDENCIA");
            }
        }
        if(event != null && "OK".equals(event)) {
            List<IncidenciaBean> newListUI = controller.getAllIncidencias();
            actualizarUI(newListUI);
        }

        mHandler = new Handler();
        startRepeatingTask();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabInc);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToPantallaRegistro();
            }
        });
    }

    public static void actualizarUI(List<IncidenciaBean> incidencias) {
        adapter = new IncidenciasAdapter(incidencias, ctx);
        if (recycler != null) {
            recycler.setAdapter(adapter);
        }
    }

    public static void getIncidenciasRemoteAux(List<IncidenciaBean> lstIncidenciasRemote, DBController _controller) {
        for (IncidenciaBean pend : lstIncidenciasRemote) {
            _controller.insertarIncidencia(pend, 1);
        }
        lstIncidenciasRemote = _controller.getAllIncidencias();
        actualizarUI(lstIncidenciasRemote);
        return;
    }

    public static void getIncidenciasInsertadasAux(List<IncidenciaBean> lstIncidenciasRemote, DBController _controller) {
        for (IncidenciaBean pend : lstIncidenciasRemote) {
            _controller.updateSyncStatus(pend.getIdIncidenciaLocal(), pend.getIdIncidenciaRemota());
        }
        List<IncidenciaBean> newListUI = _controller.getAllIncidencias();
        actualizarUI(newListUI);
        return;
    }

    @Override
    public Void getIncidenciasRemote(List<IncidenciaBean> lstIncidenciasRemote, DBController _controller) {
        getIncidenciasRemoteAux(lstIncidenciasRemote, controller);
        return null;
    }

    @Override
    public Void getIncidenciasInsertadas(List<IncidenciaBean> lstIncidenciasRemote, DBController _controller) {
        getIncidenciasInsertadasAux(lstIncidenciasRemote, controller);
        return null;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public static class ConeccionCheck extends BroadcastReceiver implements IncidenciasInterface {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("BUHOO", " CONECTADO A LA RED :D");
            boolean conectado = Utiles.checkInternet(context);
            if (conectado) {
                DBController controller = new DBController(context);
                int unsynched = controller.dbSyncCount();
                if (unsynched > 0) {
                    List<IncidenciaBean> pendientes = controller.getUnsynchedIncidencias();
                    if (pendientes.size() > 0) {
                        JSONObject jsonGeneral = new JSONObject();
                        List<ImagenBean> lstImgs = new ArrayList<ImagenBean>();
                        for (IncidenciaBean pend : pendientes) {
                            JSONObject jsonObject = new JSONObject();
                            if(pend.getLstImagenes() != null) {
                                for (IncidenciaImagenBean img : pend.getLstImagenes()) {
                                    ImagenBean imgBean = new ImagenBean();
                                    imgBean.bitmapImage = Utiles.__getBitmap(img.getRutaImagen());
                                    imgBean.keyName     = "img_"+pend.getIdIncidenciaLocal()+imgBean.indexImagen;
                                    lstImgs.add(imgBean);
                                }
                            }
                            try {
                                jsonObject.accumulate("id_incidencia_local", pend.getIdIncidenciaLocal());
                                jsonObject.accumulate("titulo", pend.getTitulo());
                                jsonObject.accumulate("descripcion", pend.getDescripcion());
                                jsonGeneral.accumulate("objInsert", jsonObject);
                            } catch (Exception e) {
                                //...
                            }
                        }
                        Utiles.insertarIncidenciasServicio(jsonGeneral, controller, this, lstImgs);
                    }
                } else if (unsynched == 0) {
                    JSONObject jsonIdsLocalesSynched = controller.getIdsRemotosIncidencias();
                    Utiles.verificarIncidenciasNewRemotoServicio(jsonIdsLocalesSynched, controller, this);
                }
            } else {
                Log.d("BUHOO", " DESCONECTADO DE LA RED :(");
            }
        }

        @Override
        public Void getIncidenciasRemote(List<IncidenciaBean> lstIncidenciasRemote, DBController _controller) {
            getIncidenciasRemoteAux(lstIncidenciasRemote, _controller);
            return null;
        }

        @Override
        public Void getIncidenciasInsertadas(List<IncidenciaBean> lstIncidenciasRemote, DBController _controller) {
            getIncidenciasInsertadasAux(lstIncidenciasRemote, _controller);
            return null;
        }
    }

    private void goToPantallaRegistro() {
        Intent nextPage = new Intent(Incidencia.this, NewIncidencia.class);
        startActivity(nextPage);
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                sincronizar(controller); //this function can change value of mInterval.
            } finally {
                // 100% guarantee that this always happens, even if
                // your update method throws an exception
                mHandler.postDelayed(mStatusChecker, mInterval);
            }
        }
    };

    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }

    public void sincronizar(DBController __controller) {Utiles.log("...SINCRONIZANDO...");
        boolean conectado = Utiles.checkInternet(ctx);
        if (conectado) {
            int unsynched = __controller.dbSyncCount();Utiles.log("unsynched: "+unsynched);
            if (unsynched > 0) {
                List<IncidenciaBean> pendientes = __controller.getUnsynchedIncidencias();
                if (pendientes.size() > 0) {
                    JSONObject jsonGeneral = new JSONObject();
                    List<ImagenBean> lstImgs = new ArrayList<ImagenBean>();
                    for (IncidenciaBean pend : pendientes) {
                        JSONObject jsonObject = new JSONObject();
                        if(pend.getLstImagenes() != null) {
                            for (IncidenciaImagenBean img : pend.getLstImagenes()) {
                                ImagenBean imgBean = new ImagenBean();
                                imgBean.bitmapImage = Utiles.__getBitmap(img.getRutaImagen());
                                imgBean.keyName     = "img_"+pend.getIdIncidenciaLocal()+"_"+imgBean.indexImagen;
                                lstImgs.add(imgBean);
                            }
                        }
                        try {
                            jsonObject.accumulate("id_incidencia_local", pend.getIdIncidenciaLocal());
                            jsonObject.accumulate("titulo", pend.getTitulo().trim().replaceAll(" ", "<x>"));
                            jsonObject.accumulate("descripcion", pend.getDescripcion().trim().replaceAll(" ", "<x>"));
                            jsonGeneral.accumulate("objInsert", jsonObject);
                        } catch (Exception e) {
                            //...
                        }
                    }
                    Utiles.insertarIncidenciasServicio(jsonGeneral, __controller, this, lstImgs);
                }
            } else if (unsynched == 0) {
                List<IncidenciaBean> newListUI = controller.getAllIncidencias();
                actualizarUI(newListUI);

                JSONObject jsonIdsLocalesSynched = controller.getIdsRemotosIncidencias();
                Utiles.verificarIncidenciasNewRemotoServicio(jsonIdsLocalesSynched, controller, this);
            }
        } else {
            List<IncidenciaBean> newListUI = controller.getAllIncidencias();
            actualizarUI(newListUI);
        }
    }

    @Override
    public void onBackPressed() {
        //Include the code here
        return;
    }
}