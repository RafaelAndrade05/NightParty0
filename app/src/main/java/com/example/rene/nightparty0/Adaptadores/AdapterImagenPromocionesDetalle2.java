package com.example.rene.nightparty0.Adaptadores;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.rene.nightparty0.Objetos.Imagen;
import com.example.rene.nightparty0.R;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class AdapterImagenPromocionesDetalle2 extends RecyclerView.Adapter<AdapterImagenPromocionesDetalle2.ViewHolderDetalle2> implements View.OnClickListener  {

    View view;

    ArrayList<Uri> listUri;
    private View.OnClickListener listener;
    private Context context;
    private Dialog myDialog;

    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();

    public AdapterImagenPromocionesDetalle2(Context context, ArrayList<Uri> listUri) {
        this.listUri = listUri;
        this.context = context;
    }

    @NonNull
    @Override
    public AdapterImagenPromocionesDetalle2.ViewHolderDetalle2 onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_promociones_detalle2,parent,false);
        view.setOnClickListener(this);
        context = parent.getContext();

        return new ViewHolderDetalle2(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final AdapterImagenPromocionesDetalle2.ViewHolderDetalle2 holder, final int position) {
        Glide.with(context)
                .load(listUri.get(position))
                .apply(new RequestOptions()
                        .centerCrop())
                .into(holder.imageViewPromocion);

        //evento para poder hacer zoom a la imagen
        holder.imageViewPromocion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iniciarMensajePopup(listUri,position);
            }
        });


    }

    @Override
    public int getItemCount() {
        if(listUri != null){
            return listUri.size();
        }
        return 0;
    }

    public void setOnClickListener(View.OnClickListener listener){
        this.listener = listener;
    }

    @Override
    public void onClick(View view) {
        if(listener != null){
            listener.onClick(view);
        }
    }

    public void iniciarMensajePopup(ArrayList<Uri> listUri,int position){
        myDialog.setContentView(R.layout.popup_imagen_zoom);
        PhotoView photoView;
        photoView = (PhotoView) myDialog.findViewById(R.id.photoViewZoom);
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        Glide.with(context)
                .load(listUri.get(position))
                .apply(new RequestOptions()
                        .centerCrop())
                .into(photoView);

        myDialog.show();


        //evento onclick en boton cerrar
        ImageView imageViewCerrar = (ImageView)myDialog.findViewById(R.id.imageViewCerrar);
        imageViewCerrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myDialog.dismiss();
            }
        });

    }

    public class ViewHolderDetalle2 extends RecyclerView.ViewHolder {
        ImageView imageViewPromocion,imageViewCerrar;
        ProgressBar progressBar;

        public ViewHolderDetalle2(View itemView) {
            super(itemView);

            imageViewPromocion = (ImageView)itemView.findViewById(R.id.imageViewPromocion);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progress_bar);
            myDialog = new Dialog(context);
        }
    }
}
