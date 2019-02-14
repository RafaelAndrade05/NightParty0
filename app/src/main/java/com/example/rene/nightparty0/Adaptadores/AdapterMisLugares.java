package com.example.rene.nightparty0.Adaptadores;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.rene.nightparty0.ActivitysNegocio.ChatsNegocioActivity;
import com.example.rene.nightparty0.ActivitysNegocio.EditarNegocioActivity;
import com.example.rene.nightparty0.ActivitysNegocio.PromocionesDisponibilidadActivity;
import com.example.rene.nightparty0.ActivitysNegocio.ReservacionesNegocioActivity;
import com.example.rene.nightparty0.DetallesActivity;
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

import java.util.ArrayList;

public class AdapterMisLugares extends RecyclerView.Adapter<AdapterMisLugares.ViewHolderMisLugares> implements View.OnClickListener {

    ArrayList<Lugar> listLugares;
    private View.OnClickListener listener;
    private Context context;

    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();

    FirebaseFirestore db = FirebaseFirestore.getInstance();


    public AdapterMisLugares(Context context ,ArrayList<Lugar> listLugares) {
        this.listLugares = listLugares;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolderMisLugares onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view  = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_mis_negocios,parent,false);
        view.setOnClickListener(this);
        Context context = parent.getContext();
        return new ViewHolderMisLugares(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolderMisLugares holder, final int position) {
        holder.tvNombre.setText(listLugares.get(position).getNombre());
        holder.tvid.setText(listLugares.get(position).getId());
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //creating a popup menu
                PopupMenu popup = new PopupMenu(context, holder.buttonViewOption);
                //inflating menu from xml resource
                popup.inflate(R.menu.popup_menu_negocio);
                //adding click listener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.menu1:
                                Intent p = new Intent(context, ReservacionesNegocioActivity.class);
                                p.putExtra("idLugar",listLugares.get(position).getId());
                                p.putExtra("nombre",listLugares.get(position).getNombre());
                                p.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(p);
                                break;
                            case R.id.menu2:
                                Intent o = new Intent(context, PromocionesDisponibilidadActivity.class);
                                o.putExtra("idLugar",listLugares.get(position).getId());
                                o.putExtra("nombre",listLugares.get(position).getNombre());
                                o.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(o);
                                break;
                            case R.id.menu3:
                                Intent q = new Intent(context, EditarNegocioActivity.class);
                                q.putExtra("idLugar",listLugares.get(position).getId());
                                q.putExtra("nombre",listLugares.get(position).getNombre());
                                q.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(q);
                                break;
                            case R.id.menu4:
                                Intent i = new Intent(context, DetallesActivity.class);
                                i.putExtra("id", holder.tvid.getText().toString());
                                i.putExtra("nombre", holder.tvNombre.getText().toString());
                                i.putExtra("distancia","0.0");
                                //ayuda a que no se detenga la aplicacion en versiones lolipop por el cache
                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(i);
                                break;
                            case R.id.menu5:
                                Intent l = new Intent(context, ChatsNegocioActivity.class);
                                l.putExtra("idLugar",listLugares.get(position).getId());
                                l.putExtra("nombre",listLugares.get(position).getNombre());
                                //ayuda a que no se detenga la aplicacion en versiones lolipop por el cache
                                l.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(l);
                                break;
                        }
                        return false;
                    }
                });
                //displaying the popup
                popup.show();
            }
        });

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
                                            .into(holder.imageView);
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
    public void onClick(View v) {
        if(listener != null){
            listener.onClick(v);
        }
    }

    public class ViewHolderMisLugares extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView tvNombre,tvid,buttonViewOption;

        public ViewHolderMisLugares(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imagenLugar);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvid = itemView.findViewById(R.id.tvId);
            buttonViewOption = (TextView) itemView.findViewById(R.id.textViewOptions);
        }
    }


}
