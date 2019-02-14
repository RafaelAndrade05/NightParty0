package com.example.rene.nightparty0;

import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.rene.nightparty0.Adaptadores.ViewPagerAdapter;
import com.example.rene.nightparty0.FragmentsDetalles.FragmentDetalle;
import com.example.rene.nightparty0.FragmentsDetalles.FragmentPromociones;
import com.example.rene.nightparty0.FragmentsDetalles.FragmentUbicacion;
import com.example.rene.nightparty0.FragmentsDetalles.ViewPagerAdapterDetalle;
import com.example.rene.nightparty0.Objetos.Favorito;
import com.example.rene.nightparty0.Objetos.Imagen;
import com.example.rene.nightparty0.Objetos.Lugar;
import com.example.rene.nightparty0.Objetos.Promocion;
import com.example.rene.nightparty0.Objetos.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import javax.annotation.Nullable;

public class Detalles2Activity extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();
    FirebaseUser user;

    private TabLayout tabLayout;
    private AppBarLayout appBarLayout;
    private ViewPager viewPagerDiseño;

    private TextView tvNumImagenes,tvNombre;
    private ProgressBar progressBar;
    private String nombre,idLugar,distancia = "0.0";
    private ArrayList<String>urls;
    private String[] imagenUrls = new String[0];

    //Adaptador
    ViewPagerAdapterDetalle adapterDetalle;
    //objeto que tiene la informacion del negocio
    Lugar objLugar;
    //objeto que tiene la informacion de las promociones
    Promocion objPromocion;
    //objeto Imagen
    Imagen objImagen;
    //bundle donde se almacena el objeto
    Bundle argumentos;
    //Fragments
    FragmentDetalle fragmentDetalle;
    FragmentUbicacion fragmentUbicacion;
    FragmentPromociones fragmentPromociones;

    //ArrayList para Fragment Promociones
    private ArrayList<Uri> listUri;
    int contImagenes=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalles2);

        idLugar = getIntent().getStringExtra("id");
        nombre = getIntent().getStringExtra("nombre");
        distancia = getIntent().getStringExtra("distancia");

        setTitle(nombre);

        tabLayout = (TabLayout)findViewById(R.id.tablayoutid);
        appBarLayout = (AppBarLayout) findViewById(R.id.appbarid);
        viewPagerDiseño = (ViewPager)findViewById(R.id.viewpager_id);

        fragmentDetalle = new FragmentDetalle();
        fragmentPromociones = new FragmentPromociones();
        fragmentUbicacion = new FragmentUbicacion();

        argumentos = new Bundle();

        listUri = new ArrayList<>();

        tvNumImagenes = (TextView)findViewById(R.id.detalle_num_imagen);
        tvNombre = (TextView)findViewById(R.id.detalle_nombre);
        progressBar = (ProgressBar)findViewById(R.id.progress_bar);
        final ViewPager viewPager = findViewById(R.id.view_pager);

        llenarFragmentDetalleUbicacion();
        llenarFragmentPromociones();

        progressBar.setVisibility(View.VISIBLE);

        urls = new ArrayList();

        final CollectionReference imagenesCollectionRef = db.collection(Lugar.COLLECTION_LUGARES).document(idLugar).collection(Lugar.COLLECTION_IMAGENES);
        imagenesCollectionRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                urls.removeAll(urls);
                if (task.isSuccessful()) {
                    for (final DocumentSnapshot document : task.getResult()) {
                        storageRef.child(document.get("urlimagen").toString()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                urls.add(uri.toString());
                                Log.i("TAGIMAGEN",uri.toString());
                                imagenUrls = new String[urls.size()];
                                imagenUrls = urls.toArray(imagenUrls);
                                final ViewPagerAdapter adapter = new ViewPagerAdapter(getApplicationContext(),imagenUrls);
                                viewPager.setAdapter(adapter);
                                tvNumImagenes.setText("1"+"/"+imagenUrls.length);

                                progressBar.setVisibility(View.INVISIBLE);

                                //escuchador para el viewPager cada vez que cambia de pagina
                                viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                                    @Override
                                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                                    }

                                    @Override
                                    public void onPageSelected(int position) {
                                        tvNumImagenes.setText(String.valueOf(position+1)+"/"+imagenUrls.length);
                                    }

                                    @Override
                                    public void onPageScrollStateChanged(int state) {

                                    }
                                });
                            }
                        });
                    }

                } else {
                    Log.d("TAGIMAGEN", "Error getting documents: ", task.getException());
                }

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public void llenarFragmentDetalleUbicacion(){
        db.collection(Lugar.COLLECTION_LUGARES).document(idLugar).get().
                addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {

                    Log.i("ACTUALIZACION", "Actualizacion de datos");

                    objLugar = new Lugar();
                    objLugar = documentSnapshot.toObject(Lugar.class);
                    objLugar.setId(documentSnapshot.getId());

                    argumentos.putSerializable("objLugar", objLugar);
                    argumentos.putString("distancia", distancia);

                    fragmentDetalle.setArguments(argumentos);
                    fragmentUbicacion.setArguments(argumentos);

                    adapterDetalle = new ViewPagerAdapterDetalle(getSupportFragmentManager());

                    //Añadiendo Fragmentos
                    adapterDetalle.AddFragment(fragmentDetalle, getString(R.string.detalle_fragment_detalle), 0);
                    adapterDetalle.AddFragment(fragmentUbicacion, getString(R.string.detalle_fragment_ubicacion), 1);

                    //adapter setup
                    viewPagerDiseño.setAdapter(adapterDetalle);
                    tabLayout.setupWithViewPager(viewPagerDiseño);

                }
            }
        });
    }

    public void llenarFragmentPromociones(){
        //Llenando fragment promociones

        db.collection(Promocion.COLLECTION_PROMOCIONES).document(idLugar).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            objPromocion = new Promocion();
                            objPromocion = documentSnapshot.toObject(Promocion.class);
                            objPromocion.setIdLugar(idLugar);
                            argumentos.putSerializable("objPromocion", objPromocion);

                            //Recuperando urls de firestore
                            db.collection(Promocion.COLLECTION_PROMOCIONES).
                                    document(objPromocion.getIdLugar()).
                                    collection(Promocion.COLLECTION_IMAGENES).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(final QuerySnapshot queryDocumentSnapshots) {
                                    if(!queryDocumentSnapshots.isEmpty()){
                                        for(QueryDocumentSnapshot document : queryDocumentSnapshots){
                                            objImagen = new Imagen();
                                            objImagen = document.toObject(Imagen.class);

                                            //obteniendo Uri de Storage de las urls
                                            try{
                                                storageRef.child(objImagen.getUrlimagen()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {
                                                        listUri.add(uri);
                                                        contImagenes++;
                                                        if(queryDocumentSnapshots.size() == contImagenes){
                                                            //ultima iteraccion de las uri

                                                            argumentos.putParcelableArrayList("listUri",listUri);
                                                            fragmentPromociones.setArguments(argumentos);

                                                            adapterDetalle.AddFragment(fragmentPromociones, getString(R.string.detalle_fragment_promociones), 2);

                                                            //adapter setup
                                                            viewPagerDiseño.setAdapter(adapterDetalle);
                                                            tabLayout.setupWithViewPager(viewPagerDiseño);

                                                            contImagenes = 0;

                                                        }


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

                                    }
                                }
                            });

                        }

                    }
                });
    }

    int auxFavorito = 0; //0 es vacio y 1 es lleno

    //boton de favorito
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_favorito_lugar, menu);

        //comprueba si el lugar es favorito para el usuario
        user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null) {
            db.collection(Usuario.COLLECTION_USER).document(user.getUid()).
                    collection(Usuario.COLLECTION_FAVORITOS).document(idLugar).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        if (task.getResult().exists()) {
                            menu.findItem(R.id.favorite_button).setIcon(R.drawable.ic_favorite_fill);
                            auxFavorito = 1;
                            Log.i("FAVORITE", "Es favorito del usuario");
                        } else {
                            auxFavorito = 0;
                            Log.i("FAVORITE", "No es favorito");
                        }
                    }
                }
            });
        }
        return super.onCreateOptionsMenu(menu);
    }

    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.favorite_button) { //presiono el boton de favorito
            if(user == null) {
                //No hay ningun usuario logueado
                Toast.makeText(getApplicationContext(), R.string.detalle_añadido_fallo, Toast.LENGTH_SHORT).show();
            }else {
                //usuario logueado
                switch (auxFavorito){
                    case 0:
                        //Como esta vacio el usuario quiere añadir el lugar como favorito
                        addFavorite(idLugar,user.getUid());
                        item.setIcon(R.drawable.ic_favorite_fill);
                        break;
                    case 1:
                        //como esta lleno el usuario quiere eliminar el lugar como favorito
                        deleteFavorite(idLugar,user.getUid());
                        item.setIcon(R.drawable.ic_favorite_empty);
                        break;
                }

            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void addFavorite(String idLugar, String idUsuario) {
        Favorito objFavorito = new Favorito();
        objFavorito.setIdLugar(idLugar);

        db.collection(Usuario.COLLECTION_USER).document(idUsuario)
                .collection(Usuario.COLLECTION_FAVORITOS).document(idLugar).set(objFavorito).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    auxFavorito = 1;
                    Toast.makeText(getApplicationContext(), R.string.detalle_añadido_favorito, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void deleteFavorite(String idLugar, String idUsuario) {
        db.collection(Usuario.COLLECTION_USER).document(idUsuario)
                .collection(Usuario.COLLECTION_FAVORITOS).document(idLugar).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    auxFavorito = 0;
                    Toast.makeText(getApplicationContext(), R.string.detalle_eliminado_favorito, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
