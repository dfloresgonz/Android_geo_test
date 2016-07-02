package facilito.codigo.app.dflores.com.myapplicationcf;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import Beans.DBController;
import Beans.IncidenciaBean;
import Beans.IncidenciaImagenBean;

/**
 * Created by diego on 1/07/2016.
 */
public class Detalle_Incidencia extends AppCompatActivity {

    DBController controller = new DBController(this);
    int _idIncidenciaLocal;

    private int IMAGEN_THUMBNAIL_SIZE_DP    = 0;
    private int IMAGEN_THUMBNAIL_SIZE_PIXEL = 0;
    private float DP_WIDTH = 0;
    private static final int CANT_IMAGENES_HORIZONTAL = 4;
    private static final int CANT_IMAGENES_TOTAL      = (CANT_IMAGENES_HORIZONTAL * 2);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detalle_incidencia);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarDetaInc);
        setSupportActionBar(toolbar);

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        DP_WIDTH = displayMetrics.widthPixels / displayMetrics.density;

        IMAGEN_THUMBNAIL_SIZE_DP = (int) (DP_WIDTH - 25) / CANT_IMAGENES_HORIZONTAL;

        final float scale = getResources().getDisplayMetrics().density;
        IMAGEN_THUMBNAIL_SIZE_PIXEL = (int) (IMAGEN_THUMBNAIL_SIZE_DP * scale + 0.5f);

        if(savedInstanceState != null) {
            _idIncidenciaLocal = savedInstanceState.getParcelable("ID_INCIDENCIA");
        } else {
            Bundle b = getIntent().getExtras();
            int idIncidenciaLocal = b.getInt("ID_INCI_LOCAL");
            _idIncidenciaLocal = idIncidenciaLocal;
            Log.d("BUHOO", ":::::idIncidenciaLocal: "+idIncidenciaLocal);
        }
        IncidenciaBean incidencia = controller.getIncidenciaById(_idIncidenciaLocal);

        TextView titulo = (TextView) findViewById(R.id.deta_titulo);
        titulo.setText(incidencia.getTitulo());

        TextView descripcion = (TextView) findViewById(R.id.deta_descripcion);
        descripcion.setText(incidencia.getDescripcion());

        if(incidencia.getLstImagenes() != null) {
            for (IncidenciaImagenBean imgB : incidencia.getLstImagenes()) {
                agregarFotoUI(imgB.getCorrelativo(), __getBitmap(imgB.getRutaImagen()), imgB.getIdImagen());
            }
        }
    }

    private Bitmap __getBitmap(String filePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 8;
        Bitmap _bitmap = BitmapFactory.decodeFile(filePath, options);
        Log.d("BUHOO", "ANCHO: "+_bitmap.getWidth()+"  ALTO: "+_bitmap.getHeight());
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

    private void agregarFotoUI(int indexFoto, Bitmap bitmap, int idImagen) {
        ImageView img = new ImageView(this);
        RelativeLayout rl = (RelativeLayout) findViewById(R.id.relLayoutDetalle);

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
            params.addRule(RelativeLayout.BELOW, R.id.deta_descripcion);
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
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("ID_INCIDENCIA", _idIncidenciaLocal);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        Intent nextPage = new Intent(Detalle_Incidencia.this, Incidencia.class);
        startActivity(nextPage);
    }
}