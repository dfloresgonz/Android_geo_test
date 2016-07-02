package Adaptadores;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import Beans.IncidenciaBean;
import facilito.codigo.app.dflores.com.myapplicationcf.Detalle_Incidencia;
import facilito.codigo.app.dflores.com.myapplicationcf.NewIncidencia;
import facilito.codigo.app.dflores.com.myapplicationcf.R;

/**
 * Created by diego on 25/06/2016.
 */
public class IncidenciasAdapter extends RecyclerView.Adapter<IncidenciasAdapter.IncidenciasViewHolder>{

    private List<IncidenciaBean> incidencias = new ArrayList<IncidenciaBean>();
    static Context mContext;

    @Override
    public IncidenciasViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.incidencia_card, viewGroup, false);
        return new IncidenciasViewHolder(v);
    }

    @Override
    public void onBindViewHolder(IncidenciasViewHolder viewHolder, int i) {
        viewHolder.imagenSynch.setImageResource(incidencias.get(i).getEstadoSync());
        viewHolder.titulo.setText(incidencias.get(i).getTitulo());
        viewHolder.descripcion.setText(incidencias.get(i).getDescripcion());
        viewHolder.idIncidenciaLocal = incidencias.get(i).getIdIncidenciaLocal();
        viewHolder.idIncidenciaRemote = incidencias.get(i).getIdIncidenciaRemota();
    }

    public IncidenciasAdapter(List<IncidenciaBean> items, Context context) {
        this.incidencias = items;
        mContext = context;
    }

    @Override
    public int getItemCount() {
        return incidencias.size();
    }

    public static class IncidenciasViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        // Campos respectivos de un item
        public ImageView imagenSynch;
        public TextView titulo;
        public TextView descripcion;
        public int idIncidenciaLocal;
        public int idIncidenciaRemote;

        public IncidenciasViewHolder(View v) {
            super(v);
            imagenSynch = (ImageView) v.findViewById(R.id.imagen);
            titulo = (TextView) v.findViewById(R.id.titulo);
            descripcion = (TextView) v.findViewById(R.id.descripcion);

            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Intent nextPage = new Intent(mContext, Detalle_Incidencia.class);
            nextPage.putExtra("ID_INCI_LOCAL", idIncidenciaLocal);
            mContext.startActivity(nextPage);
        }
    }
}
//