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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class AdapterReservacionesUsuarios extends RecyclerView.Adapter<AdapterReservacionesUsuarios.ViewHolderReservacionesUsuarios> implements View.OnClickListener {
    ArrayList<Reservacion> listReservaciones;
    Context context;
    private View.OnClickListener listener;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public AdapterReservacionesUsuarios(Context context,ArrayList<Reservacion> listReservaciones) {
        this.listReservaciones = listReservaciones;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolderReservacionesUsuarios onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view  = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_reservaciones_usuario,parent,false);
        view.setOnClickListener(this);
        Context context = parent.getContext();
        return new ViewHolderReservacionesUsuarios(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderReservacionesUsuarios holder, final int position) {
        holder.tvNombre.setText(listReservaciones.get(position).getNombreLugar());
        holder.tvFecha.setText(listReservaciones.get(position).getDia());
        holder.tvPersonas.setText(String.valueOf(listReservaciones.get(position).getNumPersonas()));
        holder.tvHora.setText(listReservaciones.get(position).getHora());
        holder.tvEstatus.setText(listReservaciones.get(position).getEstatusReservacion());
        holder.tvIdReservacion.setText(listReservaciones.get(position).getId());
        holder.tvIdLugar.setText(listReservaciones.get(position).getLugarId());

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

    public class ViewHolderReservacionesUsuarios extends RecyclerView.ViewHolder {
        TextView tvNombre,tvFecha,tvPersonas,tvHora,tvEstatus, tvIdReservacion,tvIdLugar;
        public ViewHolderReservacionesUsuarios(View itemView) {
            super(itemView);
            tvNombre = (TextView)itemView.findViewById(R.id.tvNombre2);
            tvFecha = (TextView)itemView.findViewById(R.id.tvFecha2);
            tvPersonas = (TextView)itemView.findViewById(R.id.tvPersonas2);
            tvHora = (TextView)itemView.findViewById(R.id.tvHora2);
            tvEstatus = (TextView)itemView.findViewById(R.id.tvEstatus);
            tvIdReservacion = (TextView)itemView.findViewById(R.id.tvIdReservacion);
            tvIdLugar = (TextView)itemView.findViewById(R.id.tvIdLugar);

        }
    }
}
