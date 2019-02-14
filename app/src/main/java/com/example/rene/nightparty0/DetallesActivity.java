package com.example.rene.nightparty0;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.rene.nightparty0.Adaptadores.ViewPagerAdapter;
import com.example.rene.nightparty0.Ayuda.Ubicacion;
import com.example.rene.nightparty0.Objetos.Favorito;
import com.example.rene.nightparty0.Objetos.Imagen;
import com.example.rene.nightparty0.Objetos.Lugar;

import com.example.rene.nightparty0.Objetos.Reservacion;
import com.example.rene.nightparty0.Objetos.Usuario;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
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
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import javax.annotation.Nullable;

public class DetallesActivity extends AppCompatActivity implements OnMapReadyCallback {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private TextView tvNumImagenes,tvNombre,tvDescripcion,tvDistancia,tvDireccion,tvDisponibilidad;
    private ImageView imageViewDisponibilidad;
    private ProgressBar progressBar;
    private boolean datosCargados = false;
    private boolean imagenesCargadas = false;

    private ArrayList<String> urls;
    private String[] imagenUrls = new String[0];
    String idLugar;
    String nombre;
    String distancia;

    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();

    GoogleMapOptions options = new GoogleMapOptions();
    GoogleMap mMap;

    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalles);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //boton de retroceso al action bar

        tvNumImagenes = (TextView)findViewById(R.id.detalle_num_imagen);
        tvNombre = (TextView)findViewById(R.id.detalle_nombre);
        tvDescripcion = (TextView)findViewById(R.id.detalle_descripcion);
        tvDistancia = (TextView)findViewById(R.id.detalle_distancia_usuario);
        tvDireccion = (TextView)findViewById(R.id.detalle_direccion);
        progressBar = (ProgressBar)findViewById(R.id.progress_bar);
        imageViewDisponibilidad = (ImageView)findViewById(R.id.disponibilidad_imagen);
        tvDisponibilidad = (TextView)findViewById(R.id.disponibilidad_texto);
        final ViewPager viewPager = findViewById(R.id.view_pager);

        progressBar.setVisibility(View.VISIBLE);

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        options.liteMode(true); //Mapa en modo lite (imagen estatica de googlemaps)

        idLugar = getIntent().getStringExtra("id");
        nombre = getIntent().getStringExtra("nombre");
        distancia = getIntent().getStringExtra("distancia");

        tvNombre.setText(nombre);
        setTitle(nombre);

        final DocumentReference docRef = db.collection(Lugar.COLLECTION_LUGARES).document(idLugar);

        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Lugar lugar = documentSnapshot.toObject(Lugar.class);
                tvDescripcion.setText(lugar.getDescripcion());
                tvDireccion.setText(lugar.getDireccion());
                if(!distancia.equals("0.0")) {
                    tvDistancia.setText(distancia + " " + getString(R.string.detalle_distancia_ubicacion));
                }
                moveCamera(new LatLng(lugar.getLatitud(),lugar.getLongitud()),Ubicacion.LESS_ZOOM,lugar.getNombre());
                datosCargados = true;
                comprobarDatosImagenesCargados();
            }
        });

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
                                imagenesCargadas = true;
                                comprobarDatosImagenesCargados();

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
        db.collection(Lugar.COLLECTION_LUGARES).document(idLugar).addSnapshotListener(this,new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(documentSnapshot.exists()){
                    Lugar lugar = new Lugar();
                    lugar = documentSnapshot.toObject(Lugar.class);
                    Log.i("ACTUALIZACION",String.valueOf(lugar.getDisponibilidad()));
                    if(lugar.getDisponibilidad() == 0){ //Lugar baja disponibilidad
                        tvDisponibilidad.setText(getString(R.string.detalle_disponiblidad_baja));
                        imageViewDisponibilidad.setImageResource(R.mipmap.baja);
                        Log.i("ACTUALIZACION","Entro 0");
                    }else if(lugar.getDisponibilidad() == 1){ //lugar media disponiblidad
                        tvDisponibilidad.setText(getString(R.string.detalle_disponiblidad_media));
                        imageViewDisponibilidad.setImageResource(R.mipmap.media);
                        Log.i("ACTUALIZACION","Entro 1");
                    }else if(lugar.getDisponibilidad() == 2){ //lugar alta disponiblidad
                        tvDisponibilidad.setText(getString(R.string.detalle_disponiblidad_alta));
                        imageViewDisponibilidad.setImageResource(R.mipmap.alta);
                        Log.i("ACTUALIZACION","Entro 2");
                    }
                }
            }
        });
    }

    private void comprobarDatosImagenesCargados() {
        if(datosCargados && imagenesCargadas){
            progressBar.setVisibility(View.INVISIBLE);
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    private void moveCamera(LatLng latLng, float zoom,String title){
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoom));

        //if(!title.equals(getString(R.string.txt_mylocation))){
        MarkerOptions options = new MarkerOptions().position(latLng).title(title);
        mMap.addMarker(options);
        // }

    }

    public void onClick(View view) {
        switch (view.getId()){
            case R.id.button_reservar:
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user != null){
                    Intent i = new Intent(this,ReservacionActivity.class);
                    i.putExtra("idLugar",idLugar);
                    i.putExtra("nombreLugar",nombre);
                    startActivity(i);
                }else{
                    Toast.makeText(getApplicationContext(),getString(R.string.detalle_login_obligatorio),Toast.LENGTH_SHORT).show();
                    Intent p = new Intent(getApplicationContext(),LoginActivity.class);
                    startActivity(p);
                }
                break;
        }
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
