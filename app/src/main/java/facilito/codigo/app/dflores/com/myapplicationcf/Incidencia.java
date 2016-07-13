package facilito.codigo.app.dflores.com.myapplicationcf;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

    private int mInterval = 9000;
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
        List<IncidenciaBean> newListUI = controller.getAllIncidencias();
        actualizarUI(newListUI);
        if(event != null && "OK".equals(event)) {
            //
        } else {
            mHandler = new Handler();
            stopRepeatingTask();
            startRepeatingTask();
        }
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
        if(lstIncidenciasRemote != null && lstIncidenciasRemote.size() > 0) {
            for (IncidenciaBean pend : lstIncidenciasRemote) {
                _controller.insertarIncidencia(pend, 1);
            }
            lstIncidenciasRemote = _controller.getAllIncidencias();
            actualizarUI(lstIncidenciasRemote);
        }
        return;
    }

    public static void getIncidenciasInsertadasAux(List<IncidenciaBean> lstIncidenciasRemote, DBController _controller) {
        if(lstIncidenciasRemote != null && lstIncidenciasRemote.size() > 0) {
            for (IncidenciaBean pend : lstIncidenciasRemote) {
                _controller.updateSyncStatus(pend.getIdIncidenciaLocal(), pend.getIdIncidenciaRemota());
            }
            List<IncidenciaBean> newListUI = _controller.getAllIncidencias();
            actualizarUI(newListUI);
        }
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
                                    imgBean.keyName     = "img_"+pend.getIdIncidenciaLocal()+"_"+img.getCorrelativo();
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
                            controller.updateSyncStatus_PendienteSync(pend.getIdIncidenciaLocal());//ESTADO = 2, YA SE ENVIO AL SERVER Y ESTA PENDIENTE DE SINCRONIZAR
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

    public void sincronizar(DBController __controller) {
        String timeStamp = new SimpleDateFormat("dd_MM_yyyy_HH:mm:ss", Locale.getDefault()).format(new Date());
        Utiles.log("...SINCRONIZANDO... : "+timeStamp);
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
                                imgBean.keyName     = "img_"+pend.getIdIncidenciaLocal()+"_"+img.getCorrelativo();
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
                /*List<IncidenciaBean> newListUI = controller.getAllIncidencias();
                actualizarUI(newListUI);*/
                JSONObject jsonIdsLocalesSynched = __controller.getIdsRemotosIncidencias();
                Utiles.verificarIncidenciasNewRemotoServicio(jsonIdsLocalesSynched, controller, this);
            }
        } else {
            List<IncidenciaBean> newListUI = __controller.getAllIncidencias();
            actualizarUI(newListUI);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings: generarJSON_Muestras(); return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    public void generarJSON_Muestras() {
        File muestrasFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), getResources().getString(R.string.carpeta_archivo_muestras));
        if (!muestrasFolder.exists()) {
            if (!muestrasFolder.mkdirs()) {
                Log.d("BUHOO", "Hubo un error al crear el folder de muestras");
                Utiles.toast("Hubo un error al crear el folder de muestras", ctx);
            }
        }
        //
        String timeStamp = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss", Locale.getDefault()).format(new Date());
        File muestrasFileJSON = new File(muestrasFolder.getPath() + File.separator + "Muestras_" + timeStamp + ".json");
        Uri fileUri = Uri.fromFile(muestrasFileJSON);
        try {
            FileWriter fw = new FileWriter(fileUri.getPath(), false);
            ArrayList<IncidenciaBean> lstIncidencias = controller.getAllIncidencias();
            int cnt = 0;
            for (IncidenciaBean inc : lstIncidencias) {
                cnt++;
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.accumulate("id_incidencia_remoto", inc.getIdIncidenciaRemota());
                    jsonObject.accumulate("id_incidencia_local", inc.getIdIncidenciaLocal());
                    jsonObject.accumulate("titulo", inc.getTitulo());
                    jsonObject.accumulate("descripcion", inc.getDescripcion());
                    //Imagenes
                    List<IncidenciaImagenBean> lstImagenes = controller.getImagenesByIncidencia(inc.getIdIncidenciaLocal());
                    JSONArray arrayimg = new JSONArray();
                    for (IncidenciaImagenBean img : lstImagenes) {
                        String uploadImage = Utiles.__getStringImage(Utiles.__getBitmap( img.getRutaImagen() ));
                        arrayimg.put(uploadImage);
                    }
                    jsonObject.accumulate("imagenes", arrayimg);
                    //
                    fw.append(jsonObject.toString());
                    if(cnt < lstIncidencias.size()) {
                        fw.append(',');
                    }
                    fw.append('\n');
                } catch(Exception e) {
                    Utiles.printearErrores(e, "error json......-------->: ");
                }
            }
            fw.close();
            Utiles.toast("Se exportó el archivo de muestras.", ctx);
            Utiles.log("Se exportó el archivo de muestras. :::::: "+fileUri.getPath());
        } catch (Exception e) {
            Utiles.toast("Hubo un error al exportar el archivo de muestras", ctx);
            Utiles.printearErrores(e," Error al exportar muestras.");
        }
    }

    @Override
    public void onBackPressed() {
        //Include the code here
        return;
    }
}