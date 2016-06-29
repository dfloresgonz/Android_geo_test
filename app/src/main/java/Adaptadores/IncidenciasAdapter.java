package Adaptadores;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import Beans.IncidenciaBean;
import facilito.codigo.app.dflores.com.myapplicationcf.R;

/**
 * Created by diego on 25/06/2016.
 */
public class IncidenciasAdapter extends RecyclerView.Adapter<IncidenciasAdapter.IncidenciasViewHolder>{
    private List<IncidenciaBean> incidencias = new ArrayList<IncidenciaBean>();

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
    }

    public IncidenciasAdapter(List<IncidenciaBean> items) {
        this.incidencias = items;
    }

    @Override
    public int getItemCount() {
        return incidencias.size();
    }

    public static class IncidenciasViewHolder extends RecyclerView.ViewHolder {
        // Campos respectivos de un item
        public ImageView imagenSynch;
        public TextView titulo;
        public TextView descripcion;

        public IncidenciasViewHolder(View v) {
            super(v);
            imagenSynch = (ImageView) v.findViewById(R.id.imagen);
            titulo = (TextView) v.findViewById(R.id.titulo);
            descripcion = (TextView) v.findViewById(R.id.descripcion);
        }
    }
}
//