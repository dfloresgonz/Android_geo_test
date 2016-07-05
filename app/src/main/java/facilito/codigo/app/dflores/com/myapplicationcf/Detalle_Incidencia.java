package facilito.codigo.app.dflores.com.myapplicationcf;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import Beans.DBController;
import Beans.IncidenciaBean;
import Beans.IncidenciaImagenBean;
import Beans.Utiles;

/**
 * Created by diego on 1/07/2016.
 */
public class Detalle_Incidencia extends AppCompatActivity {

    DBController controller = new DBController(this);
    int _idIncidenciaLocal;

    Context ctx;

    private int IMAGEN_THUMBNAIL_SIZE_DP    = 0;
    private int IMAGEN_THUMBNAIL_SIZE_PIXEL = 0;
    private float DP_WIDTH = 0;
    private static final int CANT_IMAGENES_HORIZONTAL = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detalle_incidencia);

        ctx = this;

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
            Log.d("BUHOO", "________idIncidenciaLocal: "+idIncidenciaLocal);
        }
        IncidenciaBean incidencia = controller.getIncidenciaById(_idIncidenciaLocal);

        TextView titulo = (TextView) findViewById(R.id.deta_titulo);
        titulo.setText(incidencia.getTitulo());

        TextView descripcion = (TextView) findViewById(R.id.deta_descripcion);
        descripcion.setText(incidencia.getDescripcion());

        Log.d("BUHOO", "::::incidencia:::: "+incidencia.toString());

        if(incidencia.getLstImagenes() != null) {
            ArrayList<String> imgs = new ArrayList<String>();//incidencia.getLstImagenes()
            for (IncidenciaImagenBean imgB : incidencia.getLstImagenes()) {
                imgs.add(imgB.getRutaImagen());
            }
            for (IncidenciaImagenBean imgB : incidencia.getLstImagenes()) { Utiles.log("imgB.getRutaImagen():::::::: "+imgB.getRutaImagen());
                agregarFotoUI(imgB.getCorrelativo(), Utiles.__getBitmap(imgB.getRutaImagen()), imgB.getIdImagen(), imgs);
            }
        }
    }

    private void agregarFotoUI(int indexFoto, Bitmap bitmap, int idImagen, ArrayList<String> imgs) {
        ImageButton img = new ImageButton(Detalle_Incidencia.this);
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
        img.setOnClickListener(new OnImageClickListener(indexFoto, imgs));
        //img.setOnClickListener(viewOnClickListener);
        /*img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("BUHOO", "__1__click imagen: "+Build.VERSION.SDK_INT);
                ImageButton imagen = (ImageButton) findViewById(v.getId());
                Intent i = new Intent(ctx, FullScreenViewActivity.class);
                i.putExtra("position", 0);
                startActivity(i);
            }
        });*/
    }

    class OnImageClickListener implements View.OnClickListener {

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
            i.putStringArrayListExtra("LIST_IMAGENES", _imagenes);
            startActivity(i);
        }

    }

    /*View.OnClickListener viewOnClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            Log.d("BUHOO", "click imagen: "+Build.VERSION.SDK_INT);
            int myId = v.getId();

            Toast.makeText(Detalle_Incidencia.this,
                    "ID: " + String.valueOf(myId) + " clicked",
                    Toast.LENGTH_LONG).show();
        }};*/

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