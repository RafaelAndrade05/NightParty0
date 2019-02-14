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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.rene.nightparty0.Objetos.Lugar;
import com.example.rene.nightparty0.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class AdapterLugares extends RecyclerView.Adapter<AdapterLugares.ViewHolderLugares> implements View.OnClickListener{

    ArrayList<Lugar> listLugares;
    private View.OnClickListener listener;
    private Context context;

    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public AdapterLugares(Context context , ArrayList<Lugar> listLugares) {
        this.context = context;
        this.listLugares = listLugares;
    }

    @Override
    public ViewHolderLugares onCreateViewHolder(ViewGroup parent, int viewType) { //se pasa el layout list_lugares
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_lugares,parent,false);
        view.setOnClickListener(this);
        Context context = parent.getContext();
        return new ViewHolderLugares(view);
    }

    DecimalFormat df = new DecimalFormat("#.0");

    @Override
    public void onBindViewHolder(final ViewHolderLugares holder, final int position) { //Aqui se llenan los datos
        holder.tvId.setText(listLugares.get(position).getId());
        holder.tvNombre.setText(listLugares.get(position).getNombre());
        holder.tvCategoria.setText(listLugares.get(position).getCategoria());
        holder.tvDescripcion.setText(listLugares.get(position).getDescripcion());
        holder.tvUbicacion.setText(listLugares.get(position).getUbicacion());

        if(listLugares.get(position).getDistancia() != 0.0){
            holder.tvDistancia.setText(String.valueOf(df.format(listLugares.get(position).getDistancia()/1000.00))+" Km" );
            holder.tvDistancia.setVisibility(View.VISIBLE);
        }else{
            holder.tvDistancia.setVisibility(View.INVISIBLE);
        }

        try{
            CollectionReference imagenesCollectionRef = db.collection(Lugar.COLLECTION_LUGARES).document(listLugares.get(position).getId()).collection(Lugar.COLLECTION_IMAGENES);
            imagenesCollectionRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if(task.isSuccessful()){
                        for (final DocumentSnapshot document : task.getResult()) {
                            storageRef.child(document.get("urlimagen").toString()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Glide.with(context)
                                            .load(uri)
                                            .apply(new RequestOptions()
                                                    .centerCrop())
                                            .into(holder.imagen);
                                }
                            });
                            break;//solo carga la primer imagen
                        }
                    }
                }
            });
        }catch (Exception ex){
            Log.e("IMAGE_ERROR","excception "+ex.toString());
        }


    }

    @Override
    public int getItemCount() {

        if(listLugares != null)
            return listLugares.size();

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

    public class ViewHolderLugares extends RecyclerView.ViewHolder { //Se declaran los componentes de la vista
        TextView tvNombre,tvDescripcion,tvId,tvCategoria,tvUbicacion,tvDistancia;
        ImageView imagen;

        public ViewHolderLugares(View itemView) {
            super(itemView);
            tvId = (TextView)itemView.findViewById(R.id.txt_id);
            tvNombre = (TextView)itemView.findViewById(R.id.txt_nombre);
            tvDescripcion = (TextView)itemView.findViewById(R.id.txt_descripcion);
            tvCategoria = (TextView)itemView.findViewById(R.id.txt_categoria);
            tvUbicacion = (TextView)itemView.findViewById(R.id.txt_ubicacion);
            imagen = (ImageView)itemView.findViewById(R.id.imagenLugar);
            tvDistancia = (TextView)itemView.findViewById(R.id.txt_distancia);
        }
    }
}
