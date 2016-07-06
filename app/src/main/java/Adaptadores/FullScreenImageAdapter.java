package Adaptadores;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import Beans.TouchImageView;
import Beans.Utiles;
import facilito.codigo.app.dflores.com.myapplicationcf.FullScreenViewActivity;
import facilito.codigo.app.dflores.com.myapplicationcf.R;

/**
 * Created by diego on 2/07/2016.
 */
public class FullScreenImageAdapter extends PagerAdapter {

    private Activity _activity;
    private ArrayList<String> _imagePaths;
    private LayoutInflater inflater;
    private int _esDetalle;

    // constructor
    public FullScreenImageAdapter(Activity activity,
                                  ArrayList<String> imagePaths,
                                  int esDetalle) {
        this._activity = activity;
        this._imagePaths = imagePaths;
        this._esDetalle  = esDetalle;
    }

    @Override
    public int getCount() {
        return this._imagePaths.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((RelativeLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        TouchImageView imgDisplay;
        Button btnClose;
        Button btnBorrar;

        inflater = (LayoutInflater) _activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewLayout = inflater.inflate(R.layout.layout_fullscreen_image, container, false);

        imgDisplay = (TouchImageView) viewLayout.findViewById(R.id.imgDisplay);
        btnClose  = (Button) viewLayout.findViewById(R.id.btnClose);
        btnBorrar = (Button) viewLayout.findViewById(R.id.btnBorrar);

        if(_esDetalle == 1) {
            btnBorrar.setVisibility(View.GONE);
        } else {
            btnBorrar.setVisibility(View.VISIBLE);
            //Evento de borrar imagen
            btnBorrar.setOnClickListener(new OnImageBorrarClickListener(position));
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(_imagePaths.get(position), options);

        bitmap = Utiles.__compressBitmap(bitmap);

        imgDisplay.setImageBitmap(bitmap);

        // close button click event
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _activity.finish();
            }
        });

        ((ViewPager) container).addView(viewLayout);

        return viewLayout;
    }

    class OnImageBorrarClickListener implements View.OnClickListener {

        int _idx_imagen;

        public OnImageBorrarClickListener(int idx_imagen) {
            this._idx_imagen = idx_imagen;
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.putExtra("img_borrar_idx", _idx_imagen);
            intent.setAction("com.buhooapp.BORRAR_IMAGEN_RECEIVER");
            _activity.sendBroadcast(intent);
            _activity.finish();
        }
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((RelativeLayout) object);
    }
}