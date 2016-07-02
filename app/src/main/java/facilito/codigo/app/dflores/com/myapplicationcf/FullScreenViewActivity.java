package facilito.codigo.app.dflores.com.myapplicationcf;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import java.util.ArrayList;
import Adaptadores.FullScreenImageAdapter;

/**
 * Created by diego on 2/07/2016.
 */
public class FullScreenViewActivity extends Activity {

    //private Utils utils;
    private FullScreenImageAdapter adapter;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_view);

        viewPager = (ViewPager) findViewById(R.id.pager);

        //utils = new Utils(getApplicationContext());

        Intent i = getIntent();
        int position = i.getIntExtra("position", 0);
        ArrayList<String> _imgs = i.getStringArrayListExtra("LIST_IMAGENES");

        adapter = new FullScreenImageAdapter(FullScreenViewActivity.this, _imgs/*utils.getFilePaths()*/);

        viewPager.setAdapter(adapter);

        // displaying selected image first
        viewPager.setCurrentItem(position);
    }
}