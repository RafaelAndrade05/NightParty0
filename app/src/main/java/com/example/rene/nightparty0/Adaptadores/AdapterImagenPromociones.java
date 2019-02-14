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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.rene.nightparty0.Objetos.Imagen;
import com.example.rene.nightparty0.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class AdapterImagenPromociones extends RecyclerView.Adapter<AdapterImagenPromociones.ViewHolderImagenPromo> implements View.OnClickListener {

    ArrayList<Imagen> listImagen;
    private View.OnClickListener listener;
    private Context context;

    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();


    public AdapterImagenPromociones(Context context, ArrayList<Imagen> listImagen) {
        this.listImagen = listImagen;
        this.context = context;
    }

    @NonNull
    @Override
    public AdapterImagenPromociones.ViewHolderImagenPromo onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_imagenes_promociones_negocio,parent,false);
        view.setOnClickListener(this);
        Context context = parent.getContext();
        return new ViewHolderImagenPromo(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final AdapterImagenPromociones.ViewHolderImagenPromo holder, int position) {
        holder.progressBar.setVisibility(View.VISIBLE);
        holder.tvUrlImagen.setText(listImagen.get(position).getUrlimagen());
        holder.tvid.setText(listImagen.get(position).getId());
        try{
            storageRef.child(listImagen.get(position).getUrlimagen()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(context)
                            .load(uri)
                            .apply(new RequestOptions()
                                    .centerCrop())
                            .into(holder.imagenNegocio);
                    holder.progressBar.setVisibility(View.INVISIBLE);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Log.e("IMAGE_ERROR","No se pudo cargar la imagen");
                }
            });
        }catch (Exception ex){
            Log.e("IMAGE_ERROR","excception "+ex.toString());
        }
    }

    @Override
    public int getItemCount() {
        if(listImagen != null)
            return listImagen.size();
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

    public class ViewHolderImagenPromo extends RecyclerView.ViewHolder {
        ImageView imagenNegocio;
        TextView tvid,tvUrlImagen;
        ProgressBar progressBar;
        public ViewHolderImagenPromo(View itemView) {
            super(itemView);
            imagenNegocio=(ImageView)itemView.findViewById(R.id.imagenLugar);
            tvid = (TextView)itemView.findViewById(R.id.txt_id);
            tvUrlImagen = (TextView) itemView.findViewById(R.id.txt_urlImagen);
            progressBar = (ProgressBar)itemView.findViewById(R.id.progress_bar);
        }
    }
}
