package com.example.rene.nightparty0.Adaptadores;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.rene.nightparty0.Objetos.ChatVistaPrevia;
import com.example.rene.nightparty0.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;

public class AdapterChatsNegocios extends RecyclerView.Adapter<AdapterChatsNegocios.ViewHolderChatsNegocios> implements View.OnClickListener {
    ArrayList<ChatVistaPrevia> listChats;
    private View.OnClickListener listener;
    private Context context;

    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();

    public AdapterChatsNegocios(Context context,ArrayList<ChatVistaPrevia> listChats) {
        this.listChats = listChats;
        this.context = context;
    }

    @NonNull
    @Override
    public AdapterChatsNegocios.ViewHolderChatsNegocios onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_chats_usuario,parent,false);
        view.setOnClickListener(this);
        context = parent.getContext();
        return new ViewHolderChatsNegocios(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final AdapterChatsNegocios.ViewHolderChatsNegocios holder, int position) {
        Log.i("Adaptador Chats",listChats.get(position).getData());

        String nombre=listChats.get(position).getNombre();

        if(nombre != null) {
            StringTokenizer tokens = new StringTokenizer(nombre, " ");
            while (tokens.hasMoreTokens()) {
                nombre = tokens.nextToken();
                break;
            }
        }

        holder.tvIdChat.setText(listChats.get(position).getIdChat());
        holder.tvIdLugar.setText(listChats.get(position).getIdLugar());
        holder.tvNombre.setText(nombre);
        holder.tvUltimoMensaje.setText(listChats.get(position).getMensaje());
        holder.tvIdUsuario.setText(listChats.get(position).getIdUsuario());

        //recuperando imagen Usuario
        try{
            Glide.with(context)
                    .load(Uri.parse(listChats.get(position).getUrlImagen()))
                    .apply(new RequestOptions()
                            .centerCrop())
                    .into(holder.imageViewProfilePicture);

        }catch (Exception ex){
            Log.e("IMAGE_ERROR","excception "+ex.toString());
        }

        //Convirtiendo Date a String

        try{
            //Obtiene la fecha de hoy
            Date today = Calendar.getInstance().getTime();

            //Compara si la fecha es de hoy del ultimo mensaje
            //Si el mensaje es reciente pone la Hora
            DateFormat df = new SimpleDateFormat("MM/dd HH:mm a");
            String reportDate = df.format(listChats.get(position).getFecha());
            holder.tvFechaUltimoMensaje.setText(reportDate);
        }catch (Exception ex){
            Log.e("ERROR_DATE","error en la fecha");
        }
    }

    @Override
    public int getItemCount() {
        if(listChats != null){
            return listChats.size();
        }
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

    public class ViewHolderChatsNegocios extends RecyclerView.ViewHolder {
        TextView tvIdChat,tvIdLugar,tvIdUsuario,tvNombre,tvUltimoMensaje,tvFechaUltimoMensaje;
        ImageView imageViewProfilePicture;
        public ViewHolderChatsNegocios(View itemView) {
            super(itemView);

            tvIdLugar = (TextView)itemView.findViewById(R.id.tvIdLugarChat);
            tvIdUsuario = (TextView)itemView.findViewById(R.id.tvIdUsuarioChat);
            tvIdChat = (TextView) itemView.findViewById(R.id.tvIdChat);
            tvNombre = (TextView) itemView.findViewById(R.id.tvNombreUsuario);
            tvUltimoMensaje= (TextView) itemView.findViewById(R.id.tvUltimoMensaje);
            tvFechaUltimoMensaje= (TextView) itemView.findViewById(R.id.tvFechaUltimoMensaje);
            imageViewProfilePicture= (ImageView)itemView.findViewById(R.id.profile_image);
        }
    }
}
