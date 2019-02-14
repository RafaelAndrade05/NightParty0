package com.example.rene.nightparty0.Adaptadores;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.rene.nightparty0.Objetos.Reservacion;
import com.example.rene.nightparty0.R;

import java.util.ArrayList;

public class AdapterReservacionesNegocios extends RecyclerView.Adapter<AdapterReservacionesNegocios.ViewHolderReservacionesNegocios> implements View.OnClickListener {
    ArrayList<Reservacion> listReservaciones;
    private View.OnClickListener listener;
    private Context context;

    public AdapterReservacionesNegocios(Context context ,ArrayList<Reservacion> listReservaciones) {
        this.listReservaciones = listReservaciones;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolderReservacionesNegocios onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view  = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_reservaciones_negocio,parent,false);
        view.setOnClickListener(this);
        Context context = parent.getContext();
        return new ViewHolderReservacionesNegocios(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderReservacionesNegocios holder, int position) {
        holder.tvFecha.setText(listReservaciones.get(position).getDia());
        holder.tvPersonas.setText(String.valueOf(listReservaciones.get(position).getNumPersonas()));
        holder.tvHora.setText(listReservaciones.get(position).getHora());
        holder.tvEstatus.setText(listReservaciones.get(position).getEstatusReservacion());
        holder.tvId.setText(listReservaciones.get(position).getId());
    }

    @Override
    public int getItemCount() {
        if(listReservaciones != null)
            return listReservaciones.size();
        return 0;
    }
    public void setOnClickListener(View.OnClickListener listener){
        this.listener = listener;
    }
    @Override
    public void onClick(View v) {
        if(listener != null){
            listener.onClick(v);
        }
    }

    public class ViewHolderReservacionesNegocios extends RecyclerView.ViewHolder {
        TextView tvFecha,tvPersonas,tvHora,tvEstatus,tvId;
        public ViewHolderReservacionesNegocios(View itemView) {
            super(itemView);
            tvFecha = (TextView)itemView.findViewById(R.id.tvFecha2);
            tvPersonas = (TextView)itemView.findViewById(R.id.tvPersonas2);
            tvHora = (TextView)itemView.findViewById(R.id.tvHora2);
            tvEstatus = (TextView)itemView.findViewById(R.id.tvEstatus);
            tvId = (TextView)itemView.findViewById(R.id.tvIdReservacion);
        }
    }
}
