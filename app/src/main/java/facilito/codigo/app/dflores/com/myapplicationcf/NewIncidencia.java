package facilito.codigo.app.dflores.com.myapplicationcf;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import Beans.DBController;
import Beans.ImagenBean;
import Beans.IncidenciaBean;
import Beans.IncidenciaImagenBean;
import Beans.MapaVariables;
import Beans.Utiles;

/**
 * Created by diego on 30/06/2016.
 */
public class NewIncidencia extends AppCompatActivity{

    private static final int SPEECH_INPUT_REQUEST_CODE         = 100;
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 200;
    private static final int SELECT_PHOTO_REQUEST_CODE         = 300;

    private int IMAGEN_THUMBNAIL_SIZE_DP    = 0;
    private static int IMAGEN_THUMBNAIL_SIZE_PIXEL = 0;
    private float DP_WIDTH = 0;
    private static final int CANT_IMAGENES_HORIZONTAL = 4;
    private static final int CANT_IMAGENES_TOTAL      = (CANT_IMAGENES_HORIZONTAL * 2);

    private String IMAGE_DIRECTORY_NAME = null;
    private Uri fileUri;

    private static int lastId = 1;

    EditText txtTitulo;
    EditText txtDescri;

    static RelativeLayout rl;

    private RadioGroup radioGroup;
    private int idRadioSeleccionado;
    private RadioButton radioButton;

    DBController controller = new DBController(this);
    static List<ImagenBean> lstImagenes = new ArrayList<ImagenBean>();

    static Context ctx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_incidencia);

        ctx = this;

        rl = (RelativeLayout) findViewById(R.id.subRelativeLayoutNewIncidencia);

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        DP_WIDTH = displayMetrics.widthPixels / displayMetrics.density;

        IMAGEN_THUMBNAIL_SIZE_DP = (int) (DP_WIDTH - 25) / CANT_IMAGENES_HORIZONTAL;

        final float scale = getResources().getDisplayMetrics().density;
        IMAGEN_THUMBNAIL_SIZE_PIXEL = (int) (IMAGEN_THUMBNAIL_SIZE_DP * scale + 0.5f);

        if(savedInstanceState != null) {
            fileUri    = savedInstanceState.getParcelable("FILE_URI");
            lastId     = savedInstanceState.getInt("LAST_ID");
            idRadioSeleccionado = savedInstanceState.getInt("ID_RADIO_SELECCIONADO");
            lstImagenes = savedInstanceState.getParcelableArrayList("LIST_IMAGENES");

            for (ImagenBean imgBean : lstImagenes) {
                if(imgBean.bitmapImage != null) {
                    agregarFotoUI(imgBean.indexImagen, imgBean.bitmapImage, imgBean.idImagen);
                }
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
                openDialogImagen();
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

    private void openDialogImagen() {
        if(CANT_IMAGENES_TOTAL == lstImagenes.size()) {
            Toast.makeText(ctx, "Solo se pueden agregar "+CANT_IMAGENES_TOTAL+" imágenes", Toast.LENGTH_LONG).show();
            return;
        }

        LayoutInflater layoutInflater = LayoutInflater.from(NewIncidencia.this);
        View promptView = layoutInflater.inflate(R.layout.popup_layout, null);

        radioGroup = (RadioGroup) promptView.findViewById(R.id.radioGrupo);

        //Resetear valores
        radioButton = (RadioButton) promptView.findViewById(R.id.btnCamera);
        radioButton.setChecked(false);
        radioButton = (RadioButton) promptView.findViewById(R.id.btnGaleria);
        radioButton.setChecked(false);

        idRadioSeleccionado = 0;

        AlertDialog dialog = new AlertDialog.Builder(ctx)
                .setView(promptView)
                .setTitle("Seleccionar fuente")
                .setPositiveButton("ACEPTAR", null)
                .setNegativeButton("CANCELAR", null)
                .create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(final DialogInterface dialog) {
                    Button b = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                    b.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            int selectedId = 0;
                            boolean fuenteCamara  = false;
                            boolean fuenteGaleria = false;

                            if(radioButton != null) {
                                selectedId = radioGroup.getCheckedRadioButtonId();
                                idRadioSeleccionado = selectedId;
                            } else {//PANTALLA FUE ROTADA
                                selectedId = idRadioSeleccionado;
                            }

                            //radioButton = (RadioButton) promptView.findViewById(selectedId);
                            if(selectedId == R.id.btnCamera) {
                                fuenteCamara = true;
                            } else if(selectedId == R.id.btnGaleria) {
                                fuenteGaleria = true;
                            }
                            if(!fuenteCamara && !fuenteGaleria) {
                                Toast.makeText(ctx, "Seleccione una opción", Toast.LENGTH_LONG).show();
                                return;
                            }
                            //
                            if(fuenteCamara && !fuenteGaleria) {
                                abrirCamaraParaTomarFoto();
                            } else if(fuenteGaleria && !fuenteCamara) {
                                abrirGaleriaSelectFoto();
                            }
                            dialog.cancel();
                        }
                    });
                }
            });
        dialog.show();
    }

    private void abrirCamaraParaTomarFoto() {
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

    private void abrirGaleriaSelectFoto() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, SELECT_PHOTO_REQUEST_CODE);
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
            startActivityForResult(intent, SPEECH_INPUT_REQUEST_CODE);
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
            case SPEECH_INPUT_REQUEST_CODE: {
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
            case SELECT_PHOTO_REQUEST_CODE: {
                if(resultCode == RESULT_OK) {
                    agregarImagenSelectedInGaleria(data);
                }
            }
        }
    }

    protected void displayPicture(Intent data) {
        Bitmap bitmap = Utiles.__getBitmap(fileUri.getPath());
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
            ImagenBean imagenBean = new ImagenBean();

            lastId++;
            int currentId = lastId;
            int indexFoto = currentId - 2;

            imagenBean.bitmapImage     = bitmap;
            imagenBean.idImagen        = currentId;
            imagenBean.indexImagen     = indexFoto;
            imagenBean.rutaLocalImagen = fileUri.getPath();
            imagenBean.keyName         = "img_"+indexFoto;Utiles.log("camara>>>: "+imagenBean.keyName);
            lstImagenes.add(imagenBean);

            agregarFotoUI(indexFoto, bitmap, currentId);
            Toast.makeText(ctx, "Se agregó la imagen", Toast.LENGTH_LONG).show();
        }
    }

    private void agregarImagenSelectedInGaleria(Intent data) {
        if(data == null) {
            return;
        }
        Uri selectedImage = data.getData();
        String[] filePathColumn = {MediaStore.Images.Media.DATA};

        Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String filePath = cursor.getString(columnIndex);
        cursor.close();

        Bitmap bitmap = Utiles.__getBitmap(filePath);

        if(bitmap != null) {
            ImagenBean imagenBean = new ImagenBean();
            lastId++;
            int currentId = lastId;
            int indexFoto = currentId - 2;

            imagenBean.bitmapImage     = bitmap;
            imagenBean.idImagen        = currentId;
            imagenBean.indexImagen     = indexFoto;
            imagenBean.rutaLocalImagen = filePath;
            imagenBean.keyName         = "img_"+indexFoto;Utiles.log("galeria>>>: "+imagenBean.keyName);
            lstImagenes.add(imagenBean);

            agregarFotoUI(indexFoto, bitmap, currentId);
            Toast.makeText(ctx, "Se agregó la imagen", Toast.LENGTH_LONG).show();
        }
    }

    private void agregarFotoUI(int indexFoto, Bitmap bitmap, int idImagen) {
        ImageView img = new ImageView(this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

        params.height = IMAGEN_THUMBNAIL_SIZE_PIXEL;
        params.width  = IMAGEN_THUMBNAIL_SIZE_PIXEL;
        int idFotoAnterior = indexFoto + 1;

        if(indexFoto >= CANT_IMAGENES_HORIZONTAL ) {//Estamos en la 2da fila
            params.setMargins(15, 0, 0, 0);
            params.addRule(RelativeLayout.BELOW, (indexFoto - 2) );
            if(indexFoto > CANT_IMAGENES_HORIZONTAL) {//5,6,7
                params.addRule(RelativeLayout.ALIGN_TOP, idFotoAnterior );
                params.addRule(RelativeLayout.RIGHT_OF , idFotoAnterior );
                params.addRule(RelativeLayout.END_OF   , idFotoAnterior );
            } else if( indexFoto == CANT_IMAGENES_HORIZONTAL ) {
                params.setMargins(15, 10, 0, 0);
            }
        } else {//Estamos en la 1era fila
            params.addRule(RelativeLayout.BELOW, R.id.btnAbrirCamara);
            if(indexFoto > 0) {
                params.addRule(RelativeLayout.ALIGN_TOP, idFotoAnterior );
                params.addRule(RelativeLayout.RIGHT_OF , idFotoAnterior );
                params.addRule(RelativeLayout.END_OF   , idFotoAnterior );
                params.setMargins(15, 0, 0, 0);
            } else if(indexFoto == 0) {
                params.setMargins(15, 10, 0, 0);
            }
        }
        img.setId(idImagen);
        img.setScaleType(ImageView.ScaleType.CENTER_CROP);
        img.setLayoutParams(params);
        img.setImageBitmap(bitmap);
        rl.addView(img);

        ArrayList<String> imgs = getListaImagenes();
        //
        if(indexFoto >= 1) {
            for (ImagenBean imgbean : lstImagenes) {
                ImageView _img =  (ImageView) findViewById(imgbean.idImagen);
                if(_img != null) {
                    _img.setOnClickListener(new OnImageClickListener(imgbean.indexImagen, imgs));
                }
            }
        } else if(indexFoto == 0) {
            img.setOnClickListener(new OnImageClickListener(indexFoto, imgs));
        }
    }

    static class OnImageClickListener implements View.OnClickListener {

        ArrayList<String> _imagenes;
        int _postion;

        // constructor
        public OnImageClickListener(int position, ArrayList<String> imagenes) {
            this._postion = position;
            this._imagenes = imagenes;
        }

        @Override
        public void onClick(View v) {
            Intent i = new Intent(ctx, FullScreenViewActivity.class);
            i.putExtra("position", _postion);
            i.putExtra("es_detalle", 0);
            i.putStringArrayListExtra("LIST_IMAGENES", _imagenes);
            ctx.startActivity(i);
        }
    }

    public static class ReceiverBorrarImagen extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int idx_img_borrar = intent.getIntExtra("img_borrar_idx", 0);//Recibiendo de FullScreenImageAdapter

            ImageView _img = null;
            for (ImagenBean imgbean : lstImagenes) {
                if(imgbean.indexImagen == idx_img_borrar ) {
                    _img =  (ImageView) rl.findViewById(imgbean.idImagen);
                    break;
                }
            }
            if(_img != null) {
                rl.removeView(_img);
                lstImagenes.remove(idx_img_borrar);
                lastId = lastId - 1;
                int idxAux = 0;
                Iterator<ImagenBean> iterator = lstImagenes.iterator();
                while (iterator.hasNext()) {
                    ImagenBean imgbean = (ImagenBean) iterator.next();
                    if(imgbean.indexImagen > idx_img_borrar ) {//Elemento desp del borrado, se resetean sus valores para mantener el orden
                        ImageView _imgModificar = (ImageView) rl.findViewById(imgbean.idImagen);

                        imgbean.indexImagen = imgbean.indexImagen - 1;
                        imgbean.idImagen    = imgbean.idImagen    - 1;
                        imgbean.keyName     = "img_"+idxAux;

                        ////////////////////////////////////////////////////////////////////////////
                        RelativeLayout.LayoutParams newParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                        newParams.height = IMAGEN_THUMBNAIL_SIZE_PIXEL;
                        newParams.width  = IMAGEN_THUMBNAIL_SIZE_PIXEL;

                        int idFotoAnterior = imgbean.indexImagen + 1;

                        if(imgbean.indexImagen >= CANT_IMAGENES_HORIZONTAL ) {//Estamos en la 2da fila
                            newParams.setMargins(15, 0, 0, 0);
                            newParams.addRule(RelativeLayout.BELOW, (imgbean.indexImagen - 2) );
                            if(imgbean.indexImagen > CANT_IMAGENES_HORIZONTAL) {//5,6,7
                                newParams.addRule(RelativeLayout.ALIGN_TOP, idFotoAnterior );
                                newParams.addRule(RelativeLayout.RIGHT_OF , idFotoAnterior );
                                newParams.addRule(RelativeLayout.END_OF   , idFotoAnterior );
                            } else if( imgbean.indexImagen == CANT_IMAGENES_HORIZONTAL ) {
                                newParams.setMargins(15, 10, 0, 0);
                            }
                        } else {//Estamos en la 1era fila
                            newParams.addRule(RelativeLayout.BELOW, R.id.btnAbrirCamara);
                            if(imgbean.indexImagen > 0) {
                                newParams.addRule(RelativeLayout.ALIGN_TOP, idFotoAnterior );
                                newParams.addRule(RelativeLayout.RIGHT_OF , idFotoAnterior );
                                newParams.addRule(RelativeLayout.END_OF   , idFotoAnterior );
                                newParams.setMargins(15, 0, 0, 0);
                            } else if(imgbean.indexImagen == 0) {
                                newParams.setMargins(15, 10, 0, 0);
                            }
                        }
                        _imgModificar.setId(imgbean.idImagen);
                        _imgModificar.setLayoutParams(newParams);
                        ArrayList<String> imgs = getListaImagenes();
                        _imgModificar.setOnClickListener(new OnImageClickListener(imgbean.indexImagen, imgs));
                        ////////////////////////////////////////////////////////////////////////////
                    }
                    idxAux++;
                }
            } else {
                //Utiles.log("NO SE PUDO borrar la img con indice: "+idx_img_borrar);
            }
        }
    }

    public static ArrayList<String> getListaImagenes() {
        ArrayList<String> imgs = new ArrayList<String>();
        //
        for (ImagenBean imgbean : lstImagenes) {
            imgs.add(imgbean.rutaLocalImagen);
        }
        return imgs;
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Bitmap retVal;

        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        retVal = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);

        return retVal;
    }

    private void registrarIncidencia() {
        String titulo_incidencia = txtTitulo.getText().toString().trim().replaceAll("<x>", "");
        String descripcion_incidencia = txtDescri.getText().toString().trim().replaceAll("<x>", "");

        if (titulo_incidencia.trim().length() == 0 || descripcion_incidencia.trim().length() == 0) {
            Toast.makeText(this, "Escriba el título y/o la descripción", Toast.LENGTH_LONG).show();
            return;
        }
        IncidenciaBean incidenciaBean = new IncidenciaBean(0, 0, titulo_incidencia.replaceAll("<x>", " "), descripcion_incidencia.replaceAll("<x>", " "), 0);
        if(lstImagenes.size() > 0) {
            List<IncidenciaImagenBean> imgListaIns = new ArrayList<IncidenciaImagenBean>();
            for (ImagenBean imgBean : lstImagenes) {
                IncidenciaImagenBean beanImagen = new IncidenciaImagenBean();
                beanImagen.setCorrelativo(imgBean.indexImagen);
                beanImagen.setIdImagen(imgBean.idImagen);
                beanImagen.setRutaImagen(imgBean.rutaLocalImagen);
                imgListaIns.add(beanImagen);
            }
            incidenciaBean.setLstImagenes(imgListaIns);
        }
        int newId = controller.insertarIncidencia(incidenciaBean, 0);
        Intent nextPage = new Intent(NewIncidencia.this, Incidencia.class);
        nextPage.putExtra("FROM_NEW_INCIDENCIA", "OK");

        lstImagenes = new ArrayList<ImagenBean>();
        lastId = 1;

        Utiles.toast("Se registró la incidencia", ctx);
        Utiles.log("Se registró la incidencia.......................................................");

        startActivity(nextPage);
    }

    @Override
    public void onBackPressed() {
        lstImagenes = new ArrayList<ImagenBean>();
        lastId = 1;
        Intent nextPage = new Intent(NewIncidencia.this, Incidencia.class);
        nextPage.putExtra("FROM_NEW_INCIDENCIA", "OK");
        startActivity(nextPage);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("FILE_URI", fileUri);
        outState.putInt("LAST_ID", lastId);
        outState.putInt("ID_RADIO_SELECCIONADO", idRadioSeleccionado);
        outState.putParcelableArrayList("LIST_IMAGENES", (ArrayList<? extends Parcelable>) lstImagenes);
        super.onSaveInstanceState(outState);
    }
}