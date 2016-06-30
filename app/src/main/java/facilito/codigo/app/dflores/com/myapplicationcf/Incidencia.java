package facilito.codigo.app.dflores.com.myapplicationcf;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import Adaptadores.IncidenciasAdapter;
import Beans.DBController;
import Beans.IncidenciaBean;
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
    static Context ctx;
    DBController controller = new DBController(this);

    EditText txtDescri;

    private final int REQ_CODE_SPEECH_INPUT = 100;

    private Uri fileUri;
    private String IMAGE_DIRECTORY_NAME = null;
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 200;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.incidencias);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarInc);
        setSupportActionBar(toolbar);

        IMAGE_DIRECTORY_NAME = getResources().getString(R.string.carpeta_archivos_subida);

        recycler = (RecyclerView) findViewById(R.id.lstIncidencias);
        recycler.setHasFixedSize(true);

        // Usar un administrador para LinearLayout
        lManager = new LinearLayoutManager(this);
        recycler.setLayoutManager(lManager);

        adapter = new IncidenciasAdapter(lstIncidenciasRemote);
        recycler.setAdapter(adapter);
        boolean conectado = Utiles.checkInternet(this);
        if (conectado) {
            int unsynched = controller.dbSyncCount();
            if (unsynched > 0) {
                List<IncidenciaBean> pendientes = controller.getUnsynchedIncidencias();
                if (pendientes.size() > 0) {
                    JSONObject jsonGeneral = new JSONObject();
                    for (IncidenciaBean pend : pendientes) {
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.accumulate("id_incidencia_local", pend.getIdIncidenciaLocal());
                            jsonObject.accumulate("titulo", pend.getTitulo());
                            jsonObject.accumulate("descripcion", pend.getDescripcion());
                            jsonGeneral.accumulate("objInsert", jsonObject);
                        } catch (Exception e) {
                            //...
                        }
                    }
                    Utiles.insertarIncidenciasServicio(jsonGeneral, controller, this);
                }
            } else if (unsynched == 0) {
                JSONObject jsonIdsLocalesSynched = controller.getIdsRemotosIncidencias();
                Utiles.verificarIncidenciasNewRemotoServicio(jsonIdsLocalesSynched, controller, this);
            }
        } else {
            List<IncidenciaBean> newListUI = controller.getAllIncidencias();
            actualizarUI(newListUI);
        }
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabInc);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initiatePopupWindow();
            }
        });
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    public static void actualizarUI(List<IncidenciaBean> incidencias) {
        adapter = new IncidenciasAdapter(incidencias);
        if (recycler != null) {
            recycler.setAdapter(adapter);
        }
    }

    public static void getIncidenciasRemoteAux(List<IncidenciaBean> lstIncidenciasRemote, DBController _controller) {
        for (IncidenciaBean pend : lstIncidenciasRemote) {
            _controller.insertarIncidencia(pend);
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

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Incidencia Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://facilito.codigo.app.dflores.com.myapplicationcf/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Incidencia Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://facilito.codigo.app.dflores.com.myapplicationcf/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
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
                        for (IncidenciaBean pend : pendientes) {
                            JSONObject jsonObject = new JSONObject();
                            try {
                                jsonObject.accumulate("id_incidencia_local", pend.getIdIncidenciaLocal());
                                jsonObject.accumulate("titulo", pend.getTitulo());
                                jsonObject.accumulate("descripcion", pend.getDescripcion());
                                jsonGeneral.accumulate("objInsert", jsonObject);
                            } catch (Exception e) {
                                //...
                            }
                        }
                        Utiles.insertarIncidenciasServicio(jsonGeneral, controller, this);
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

    /**
     * Showing google speech input dialog
     */
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es_ES");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(), getString(R.string.speech_not_supported), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Receiving speech input
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    //Log.d("BUHOO", "resultadooooooo: "+result.get(0));
                    String texto = result.get(0) + "";
                    txtDescri.setText(texto, TextView.BufferType.EDITABLE);
                }
                break;
            }
            case CAMERA_CAPTURE_IMAGE_REQUEST_CODE : {
                if (resultCode == Activity.RESULT_OK) {
                    displayPicture(data);
                }
            }
        }
    }

    public File getOutputMediaFileUri() throws IOException {
        // External sdcard location
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), IMAGE_DIRECTORY_NAME);
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("BUHOO", "Oops! Failed create " + IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("dd_MM_yyyy_HHmmss", Locale.getDefault()).format(new Date());
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
        fileUri = Uri.fromFile(mediaFile);
        return mediaFile;
    }

    protected void displayPicture(Intent data) {
        LayoutInflater layoutInflater = LayoutInflater.from(Incidencia.this);
        View promptView = layoutInflater.inflate(R.layout.popup_layout, null);
        /*ImageView imgPreview = (ImageView) promptView.findViewById(R.id.imgFoto);
        //imgPreview.setVisibility(View.VISIBLE);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 8;

        final Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath(), options);
        imgPreview.setImageBitmap(bitmap);*/
        //imgPreview.setImageURI(fileUri);

        //Bundle b = data.getExtras();
        //Bitmap pic = (Bitmap) data.getExtras().get("data");
        BitmapFactory.Options options = new BitmapFactory.Options();
        final Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath(), options);
        if (bitmap != null) {
            ImageView img = new ImageView(this);
            RelativeLayout rl = (RelativeLayout) promptView.findViewById(R.id.popup_element);

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

            // Add image path from drawable folder.
            img.setImageResource(R.drawable.banner_ad2);
            img.setLayoutParams(params);
            rl.addView(img);

            /*img.setLayoutParams(new RelativeLayout.LayoutParams(300, 100));
            img.setPadding(5, 200, 5, 5);
            img.setImageResource(R.drawable.banner_ad2);*/
            //img.setImageBitmap(bitmap);
        }
    }

    private void initiatePopupWindow() {
        try {
            LayoutInflater layoutInflater = LayoutInflater.from(Incidencia.this);
            View promptView = layoutInflater.inflate(R.layout.popup_layout, null);

            ctx = this;
            final EditText txtTitulo = (EditText) promptView.findViewById(R.id.titulo_incidencia);
            txtDescri = (EditText) promptView.findViewById(R.id.descripcion_incidencia);
            ImageButton btnSpeak = (ImageButton) promptView.findViewById(R.id.btnSpeak);
            ImageButton btnAbrirCamara = (ImageButton) promptView.findViewById(R.id.btnAbrirCamara);

            btnAbrirCamara.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        // Create the File wher e the photo should go
                        File photoFile = null;
                        try {
                            photoFile = getOutputMediaFileUri();
                        } catch (IOException ex) {
                            Utiles.printearErrores(ex, "PHOTO: ");
                        }
                        if (photoFile != null) {Log.d("BUHOO"," photoFilefinal: "+photoFile.getPath());
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                            startActivityForResult(takePictureIntent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
                        }
                    }
                }
            });

            btnSpeak.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    promptSpeechInput();
                }
            });

            final AlertDialog dialog = new AlertDialog.Builder(ctx)
                    .setView(promptView)
                    .setTitle("Registro de Incidencias")
                    .setPositiveButton("REGISTRAR", null)
                    .setNegativeButton("CANCELAR", null)
                    .create();

            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(final DialogInterface dialog) {
                    Button b = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                    b.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String titulo_incidencia = txtTitulo.getText().toString().trim().replaceAll("<x>", "").replaceAll(" ", "<x>");
                            String descripcion_incidencia = txtDescri.getText().toString().trim().replaceAll("<x>", "").replaceAll(" ", "<x>");

                            if (titulo_incidencia.trim().length() == 0 || descripcion_incidencia.trim().length() == 0) {
                                Toast.makeText(ctx, "Escriba el título y/o la descripción", Toast.LENGTH_LONG).show();
                                return;
                            }
                            int newId = controller.insertarIncidencia(new IncidenciaBean(0, 0, titulo_incidencia.replaceAll("<x>", " "), descripcion_incidencia.replaceAll("<x>", " "), 0));
                            boolean conectado = Utiles.checkInternet(ctx);
                            if (conectado) {
                                JSONObject jsonGeneral = new JSONObject();
                                JSONObject jsonObject = new JSONObject();
                                try {
                                    jsonObject.accumulate("id_incidencia_local", newId);
                                    jsonObject.accumulate("titulo", titulo_incidencia);
                                    jsonObject.accumulate("descripcion", descripcion_incidencia);
                                    jsonGeneral.accumulate("objInsert", jsonObject);
                                } catch (Exception e) {
                                    //...
                                }
                                Utiles.insertarIncidenciasServicio(jsonGeneral, controller, (IncidenciasInterface) ctx);
                            } else {
                                List<IncidenciaBean> newListUI = controller.getAllIncidencias();
                                actualizarUI(newListUI);
                            }
                            Toast.makeText(ctx, "Se registró la incidencia", Toast.LENGTH_LONG).show();
                            dialog.cancel();
                        }
                    });
                }
            });
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}