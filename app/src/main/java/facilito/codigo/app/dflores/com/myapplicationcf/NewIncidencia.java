package facilito.codigo.app.dflores.com.myapplicationcf;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import Beans.DBController;
import Beans.IncidenciaBean;
import Beans.MapaVariables;
import Beans.Utiles;
import Interfaces.IncidenciasInterface;

/**
 * Created by diego on 30/06/2016.
 */
public class NewIncidencia extends AppCompatActivity implements IncidenciasInterface{

    private final int REQ_CODE_SPEECH_INPUT = 100;
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 200;
    private int IMAGEN_THUMBNAIL_SIZE_DP    = 0;
    private int IMAGEN_THUMBNAIL_SIZE_PIXEL = 0;
    private float DP_WIDTH = 0;
    private static final int CANT_IMAGENES_HORIZONTAL = 4;
    private static final int CANT_IMAGENES_TOTAL      = (CANT_IMAGENES_HORIZONTAL * 2);

    private String IMAGE_DIRECTORY_NAME = null;
    private Uri fileUri;

    private int lastId = 1;

    EditText txtTitulo;
    EditText txtDescri;

    DBController controller = new DBController(this);
    List<Bitmap> lstBitmaps = new ArrayList<Bitmap>();
    int[] lstIds = new int[CANT_IMAGENES_TOTAL];

    static Context ctx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_incidencia);

        ctx = this;

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        //float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        DP_WIDTH = displayMetrics.widthPixels / displayMetrics.density;

        IMAGEN_THUMBNAIL_SIZE_DP = (int) (DP_WIDTH - 25) / CANT_IMAGENES_HORIZONTAL;

        final float scale = getResources().getDisplayMetrics().density;
        IMAGEN_THUMBNAIL_SIZE_PIXEL = (int) (IMAGEN_THUMBNAIL_SIZE_DP * scale + 0.5f);
        //Log.d("BUHOO", "IMAGEN_THUMBNAIL_SIZE_PIXEL: "+IMAGEN_THUMBNAIL_SIZE_PIXEL+"    IMAGEN_THUMBNAIL_SIZE_DP: "+IMAGEN_THUMBNAIL_SIZE_DP);

        //Log.d("BUHOO", "dpWidth: "+DP_WIDTH+" dpHeight: "+DP_WIDTH);

        if(savedInstanceState != null) {
            fileUri    = savedInstanceState.getParcelable("FILE_URI");
            lstBitmaps = savedInstanceState.getParcelableArrayList("LIST_BITMAPS");
            lastId     = savedInstanceState.getInt("LAST_ID");
            lstIds     = savedInstanceState.getIntArray("LIST_IDS");

            int idxArryIds = 0;
            for (Bitmap bitmap : lstBitmaps) {
                int lastIdLocal = lstIds[idxArryIds];
                if (bitmap != null) {
                    ImageView img = new ImageView(this);
                    RelativeLayout rl = (RelativeLayout) findViewById(R.id.subRelativeLayoutNewIncidencia);

                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

                    params.height = IMAGEN_THUMBNAIL_SIZE_PIXEL;
                    params.width  = IMAGEN_THUMBNAIL_SIZE_PIXEL;

                    if(idxArryIds >= CANT_IMAGENES_HORIZONTAL ) {//Estamos en la 5ta imagen
                        params.setMargins(15, 0, 0, 0);
                        params.addRule(RelativeLayout.BELOW, (idxArryIds - 2) );
                        if(idxArryIds > CANT_IMAGENES_HORIZONTAL) {
                            params.addRule(RelativeLayout.ALIGN_TOP, (lastIdLocal - 1) );
                            params.addRule(RelativeLayout.RIGHT_OF, (lastIdLocal - 1) );
                            params.addRule(RelativeLayout.END_OF, (lastIdLocal - 1) );
                        } else if( idxArryIds == CANT_IMAGENES_HORIZONTAL ) {
                            params.setMargins(15, 10, 0, 0);
                        }
                    } else {
                        params.addRule(RelativeLayout.BELOW, R.id.btnAbrirCamara);
                        if(lastIdLocal > 1) {
                            params.addRule(RelativeLayout.ALIGN_TOP, (lastIdLocal - 1) );
                            params.addRule(RelativeLayout.RIGHT_OF, (lastIdLocal - 1) );
                            params.addRule(RelativeLayout.END_OF, (lastIdLocal - 1) );
                            params.setMargins(15, 0, 0, 0);
                        } else if(lastIdLocal == 2) {
                            params.setMargins(15, 10, 0, 0);
                        }
                    }
                    img.setId(lastIdLocal);
                    img.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    img.setLayoutParams(params);
                    img.setImageBitmap(bitmap);
                    rl.addView(img);
                    lastIdLocal++;
                }
                idxArryIds++;
            }
        } else {
            //Log.d("BUHOO", "PRIMERA VEZ");
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarNewInc);
        setSupportActionBar(toolbar);

        if( MapaVariables.ipServer == null) {
            MapaVariables.ipServer = getResources().getString(R.string.ip_server);
        }

        IMAGE_DIRECTORY_NAME = getResources().getString(R.string.carpeta_archivos_subida);

        txtTitulo = (EditText) findViewById(R.id.titulo_incidencia);
        txtDescri = (EditText) findViewById(R.id.descripcion_incidencia);

        ImageButton btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);
        ImageButton btnAbrirCamara = (ImageButton) findViewById(R.id.btnAbrirCamara);

        btnSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });

        btnAbrirCamara.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(CANT_IMAGENES_TOTAL == lstBitmaps.size()) {
                    Toast.makeText(ctx, "Solo se pueden agregar 8 imágenes", Toast.LENGTH_LONG).show();
                    return;
                }
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    File photoFile = null;
                    try {
                        photoFile = getOutputMediaFileUri();
                    } catch (IOException ex) {
                        Utiles.printearErrores(ex, "PHOTO: ");
                    }
                    if (photoFile != null) {
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                        startActivityForResult(takePictureIntent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
                    }
                }
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabNewInc);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registrarIncidencia();
            }
        });
    }

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
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

    protected void displayPicture(Intent data) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 8;
        Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath(), options);

        ExifInterface ei = null;
        try {
            ei = new ExifInterface(fileUri.getPath());
        } catch(Exception e) {
            Utiles.printearErrores(e, "ExifInterface ERROR");
        }
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

        switch(orientation) {
            case ExifInterface.ORIENTATION_ROTATE_270: //8 CAMARA_FRONTAL
                bitmap = rotateImage(bitmap, -90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90: //6 CAMARA_TRASERA
                bitmap = rotateImage(bitmap, 90);
                break;
        }

        if (bitmap != null) {
            ImageView img = new ImageView(this);
            RelativeLayout rl = (RelativeLayout) findViewById(R.id.subRelativeLayoutNewIncidencia);

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

            params.height = IMAGEN_THUMBNAIL_SIZE_PIXEL;
            params.width  = IMAGEN_THUMBNAIL_SIZE_PIXEL;

            if( (lastId - 1) >= CANT_IMAGENES_HORIZONTAL ) {//Estamos en la 5ta imagen
                int idDeImgArriba = ((lastId + 1) - CANT_IMAGENES_HORIZONTAL);
                params.addRule(RelativeLayout.BELOW, idDeImgArriba);
                params.setMargins(15, 0, 0, 0);
                if( (lastId - 1) > CANT_IMAGENES_HORIZONTAL ) {
                    params.addRule(RelativeLayout.ALIGN_TOP, lastId);
                    params.addRule(RelativeLayout.RIGHT_OF, lastId);
                    params.addRule(RelativeLayout.END_OF, lastId);
                } else if( (lastId - 1) == CANT_IMAGENES_HORIZONTAL ) {
                    params.setMargins(15, 10, 0, 0);
                }
            } else {
                params.addRule(RelativeLayout.BELOW, R.id.btnAbrirCamara);
                if(lastId > 1) {
                    params.addRule(RelativeLayout.ALIGN_TOP, lastId);
                    params.addRule(RelativeLayout.RIGHT_OF, lastId);
                    params.addRule(RelativeLayout.END_OF, lastId);
                    params.setMargins(15, 0, 0, 0);
                } else if(lastId == 1) {
                    params.setMargins(15, 10, 0, 0);
                }
            }
            lastId = lastId + 1;
            lstIds[(lastId - 2)] = lastId;
            img.setId(lastId);
            img.setScaleType(ImageView.ScaleType.CENTER_CROP);
            img.setLayoutParams(params);
            img.setImageBitmap(bitmap);
            rl.addView(img);
            lstBitmaps.add(bitmap);
        }
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Bitmap retVal;

        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        retVal = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);

        return retVal;
    }

    private void registrarIncidencia() {
        String titulo_incidencia = txtTitulo.getText().toString().trim().replaceAll("<x>", "").replaceAll(" ", "<x>");
        String descripcion_incidencia = txtDescri.getText().toString().trim().replaceAll("<x>", "").replaceAll(" ", "<x>");

        if (titulo_incidencia.trim().length() == 0 || descripcion_incidencia.trim().length() == 0) {
            Toast.makeText(this, "Escriba el título y/o la descripción", Toast.LENGTH_LONG).show();
            return;
        }
        int newId = controller.insertarIncidencia(new IncidenciaBean(0, 0, titulo_incidencia.replaceAll("<x>", " "), descripcion_incidencia.replaceAll("<x>", " "), 0));
        boolean conectado = Utiles.checkInternet(this);
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
            Utiles.insertarIncidenciasServicio(jsonGeneral, controller, (IncidenciasInterface) this);
        } else {
            //List<IncidenciaBean> newListUI = controller.getAllIncidencias();
            //actualizarUI(newListUI);
        }
        Toast.makeText(this, "Se registró la incidencia", Toast.LENGTH_LONG).show();
    }

    @Override
    public Void getIncidenciasRemote(List<IncidenciaBean> lstIncidenciasRemote, DBController controller) {
        return null;
    }

    @Override
    public Void getIncidenciasInsertadas(List<IncidenciaBean> lstIncidenciasRemote, DBController controller) {
        for (IncidenciaBean pend : lstIncidenciasRemote) {
            controller.updateSyncStatus(pend.getIdIncidenciaLocal(), pend.getIdIncidenciaRemota());
        }
        return null;
    }

    @Override
    public void onBackPressed() {
        Intent nextPage = new Intent(NewIncidencia.this, Incidencia.class);
        startActivity(nextPage);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("FILE_URI", fileUri);
        outState.putParcelableArrayList("LIST_BITMAPS", (ArrayList<? extends Parcelable>) lstBitmaps);
        outState.putIntArray("LIST_IDS", lstIds);
        outState.putInt("LAST_ID", lastId);
        super.onSaveInstanceState(outState);
    }
}