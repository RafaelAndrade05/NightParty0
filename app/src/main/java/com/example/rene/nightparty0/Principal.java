package com.example.rene.nightparty0;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.rene.nightparty0.ActivitysNegocio.AltaLugarActivity;
import com.example.rene.nightparty0.ActivitysNegocio.MisNegociosActivity;
import com.example.rene.nightparty0.Adaptadores.AdapterLugares;
import com.example.rene.nightparty0.Ayuda.SharedPreferencesManager;
import com.example.rene.nightparty0.Ayuda.Ubicacion;
import com.example.rene.nightparty0.Objetos.ChatVistaPrevia;
import com.example.rene.nightparty0.Objetos.Lugar;
import com.example.rene.nightparty0.Objetos.Usuario;
import com.facebook.login.LoginManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;


import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

public class Principal extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private ImageView imagenNavegacion;
    ArrayList<Lugar> listLugares;
    private EditText palabra;
    private ImageButton botonBuscador;
    private RecyclerView recyclerLugares;
    private ProgressBar progressBar;


    //componentes para vincular de la clase adapter datos
    TextView tvNombre, tvDescripcion, tvId,tvDistancia;
    ImageView imagen;

    // Access a Cloud Firestore instance from your Activity
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    //Ubicacion
    LocationManager locationManager;
    AlertDialog alert = null;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    Ubicacion ubicacion = new Ubicacion();
    double mLatitud = 0.0;
    double mLongitud = 0.0;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    GoogleApiClient mGoogleApiClient;
    private boolean llenadoRecyclerConMiUbicacion = true;

    private CollectionReference collectionLugares;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        palabra = (EditText) findViewById(R.id.buscador);
        botonBuscador = (ImageButton) findViewById(R.id.botonBuscador);
        recyclerLugares = (RecyclerView) findViewById(R.id.idRecycler);
        progressBar = (ProgressBar)findViewById(R.id.progress_bar);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds

        //se solicitan actualizaciones de ubicacion si fue nulo el provedor de fusion
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // encuentra la ubicacion y actualiza el recylcer
                     Log.i("LOCATION","localizacion encontrada "+ String.valueOf(location.getLatitude())+ String.valueOf(location.getLongitude()) );
                     mLatitud = location.getLatitude();
                     mLongitud = location.getLongitude();
                     llenadoRecyclerConMiUbicacion = true;
                     llenarRecyclerView();
                     progressBar.setVisibility(View.INVISIBLE);
                     mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback); //garantiza que solo se encuentre la ubicacion una vez
                }
            };
        };

        getLocationPermission();

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertNoGps();
        }

        getDeviceLocation();

        /*Linea que nos ayuda a que el background ocupe toda la pantalla incluyendo statusbar  */
       //getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        crearNavigationDrawer(user); //crea el menu segun el tipo de usuario

        botonBuscador.setOnClickListener(new View.OnClickListener() { //Evento cuando presionan el boton buscar
            @Override
            public void onClick(View view) {
                String texto = palabra.getText().toString();
                buscador(texto);
            }
        });

    }


    public void crearNavigationDrawer(FirebaseUser user){

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        imagenNavegacion = (ImageView) findViewById(R.id.botonNavegacion);
        imagenNavegacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.openDrawer(GravityCompat.START);
            }
        });

        if (user != null) { //acceso usuario cn auth
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference documentRef = db.collection(Usuario.COLLECTION_USER).document(user.getUid());
            documentRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) { //acceso de usuario con cloud firestore
                    if(documentSnapshot.exists()) {
                        Usuario usuarioFireStore = documentSnapshot.toObject(Usuario.class);
                        if (usuarioFireStore.getTypeUser().equals(Usuario.TYPE_USER_CLIENT)) {
                            Log.i("TYPEUSER", "usuario " + usuarioFireStore.getTypeUser());
                            navigationView.inflateMenu(R.menu.menu_cliente);
                            navigationView.setNavigationItemSelectedListener(Principal.this);
                            Menu nav_Menu = navigationView.getMenu();
                            nav_Menu.findItem(R.id.login).setVisible(false);
                            nav_Menu.findItem(R.id.nav_account).setVisible(true);
                            mostrarDatosUsuario(usuarioFireStore);
                        } else if (usuarioFireStore.getTypeUser().equals(Usuario.TYPE_USER_BOUSSINES)) {
                            Log.i("TYPEUSER", "usuario " + usuarioFireStore.getTypeUser());
                            navigationView.inflateMenu(R.menu.menu_negocio);
                            navigationView.setNavigationItemSelectedListener(Principal.this);
                            Menu nav_Menu = navigationView.getMenu();
                            mostrarDatosUsuario(usuarioFireStore);
                        } else if (usuarioFireStore.getTypeUser().equals(Usuario.TYPE_USER_ADMIN)) {
                            Log.i("TYPEUSER", "usuario " + usuarioFireStore.getTypeUser());
                        }
                    }
                }
            });

        } else { //ningun usuario logeado se utiliza menu cliente
            navigationView.inflateMenu(R.menu.menu_usuario_sin_login);
            navigationView.setNavigationItemSelectedListener(this);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Connect the client.
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        if (Ubicacion.mLocationPermissionsGranted && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            //se tiene accesso ala ubicacion del usuario

            Log.i("LOCATION", "SE TIENE PERMISOS DE LOCALIZACON Y SE HABILITO GPS");

            getDeviceLocation();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
            mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (alert != null)//liberar memoria del alert
        {
            alert.dismiss();
        }
    }

    private void llenarRecyclerView() {
        progressBar.setVisibility(View.VISIBLE);
        Log.i("LOCATION","LLENANDO RECYLCER VIEW");
        listLugares = new ArrayList<>();
        recyclerLugares.setLayoutManager(new LinearLayoutManager(getApplicationContext()));//forma de representar los datos
        final AdapterLugares adapter = new AdapterLugares(getApplicationContext(), listLugares);

        db.collection(Lugar.COLLECTION_LUGARES)
                .limit(15)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        listLugares.removeAll(listLugares); //Limpia la lista
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                Lugar lugar = document.toObject(Lugar.class);
                                lugar.setId(document.getId());
                                listLugares.add(lugar);
                                lugar.setDistancia(0.0);
                                //obtenemos la distancia del usuario con los lugares
                                if (Ubicacion.mLocationPermissionsGranted && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                                        && mLatitud != 0.0 && mLongitud != 0.0) {
                                    lugar.setDistancia(ubicacion.distFrom(mLatitud, mLongitud, lugar.getLatitud(), lugar.getLongitud()));
                                    Log.i("LOCATION", "Distancia en metros de " + lugar.getNombre() + " " + String.valueOf(lugar.getDistancia()));
                                }
                            }
                            //Ordena la lista segun la distancia y mostramos los lugares mas cercanos al usuario
                            if (mLatitud != 0.0 && mLongitud != 0.0)
                                Collections.sort(listLugares);
                            adapter.notifyDataSetChanged();
                            recyclerLugares.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.INVISIBLE);
                        } else {
                            Log.d("DATOS", "Error getting documents: ", task.getException());
                        }
                    }
                });

        recyclerLugares.setAdapter(adapter);//llena el recyclerView

        adapter.setOnClickListener(new View.OnClickListener() { //evento cuando presionan un Lugar
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    // versiones con android 6.0 o superior
                    //cargarAnimacion(view);
                    cargarDetalles(view);
                } else {
                    cargarDetalles(view);
                    // para versiones anteriores a android 6.0
                }
            }
        });
    }

    private void mostrarDatosUsuario(Usuario usuarioCloud) {
        String nombre = usuarioCloud.getName();
        String correo = usuarioCloud.getEmail();

        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView navUsername = (TextView) headerView.findViewById(R.id.nv_username);
        navUsername.setText(nombre);
        TextView navEmail = (TextView) headerView.findViewById(R.id.nv_email);
        navEmail.setText(correo);
        ImageView imageView = (ImageView) headerView.findViewById(R.id.nv_imageView);

        if(usuarioCloud.getTypeUser().equals(Usuario.TYPE_USER_BOUSSINES)){
            //si la cuenta es negocio podra cambiar el menu a cliente o viceversa
            final Switch switchTipoUsuario = (Switch) headerView.findViewById(R.id.switchTipoCuenta);
            //Mostrar boton switch para cambiar navigation drawer
            switchTipoUsuario.setText(getString(R.string.nav_header_businnes));
            switchTipoUsuario.setChecked(true);
            switchTipoUsuario.setVisibility(View.VISIBLE);
            switchTipoUsuario.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b) { //cuenta negocio
                        switchTipoUsuario.setText(getString(R.string.nav_header_businnes));

                        navigationView.getMenu().removeGroup(0);

                        navigationView.inflateMenu(R.menu.menu_negocio);
                        navigationView.setNavigationItemSelectedListener(Principal.this);
                        Menu nav_Menu = navigationView.getMenu();

                    } else {//cuenta cliente
                        switchTipoUsuario.setText(getString(R.string.nav_header_client));

                        navigationView.getMenu().removeGroup(0);

                        navigationView.inflateMenu(R.menu.menu_cliente);
                        navigationView.setNavigationItemSelectedListener(Principal.this);
                        Menu nav_Menu = navigationView.getMenu();
                        nav_Menu.findItem(R.id.login).setVisible(false);
                        nav_Menu.findItem(R.id.nav_account).setVisible(true);
                    }
                }
            });

        }




        if (usuarioCloud.getPhotoUrl() != null) {
            Uri fotoUrl = Uri.parse(usuarioCloud.getPhotoUrl());
            Glide.with(this)
                    .load(fotoUrl)
                    .apply(new RequestOptions()
                            .centerCrop())
                    .into(imageView);
        }


    }


    ArrayList<Lugar> listBuscada;
    private void buscador(String texto){
        progressBar.setVisibility(View.VISIBLE);
        Log.i("LOCATION","LLENANDO RECYLCER VIEW");
        listLugares = new ArrayList<>();
        recyclerLugares.setLayoutManager(new LinearLayoutManager(getApplicationContext()));//forma de representar los datos
        final AdapterLugares adapter = new AdapterLugares(getApplicationContext(), listLugares);

        //Convierte la primer letra en mayuscula
        if(!texto.isEmpty()) {
            String firsLetterUpperCase = texto.substring(0,1).toUpperCase();
            if(texto.length()>0) {
                String textoLowerCase=texto.substring(1,texto.length());
                texto = firsLetterUpperCase + textoLowerCase;
                Toast.makeText(getApplicationContext(), texto, Toast.LENGTH_SHORT).show();
            }
        }

        db.collection(Lugar.COLLECTION_LUGARES)
                .orderBy(Lugar.CAMPO_NOMBRE)
                .startAt(texto)
                .endAt(texto+"\uf8ff")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        listLugares.removeAll(listLugares); //Limpia la lista
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                Lugar lugar = document.toObject(Lugar.class);
                                lugar.setId(document.getId());
                                listLugares.add(lugar);
                                lugar.setDistancia(0.0);
                                //obtenemos la distancia del usuario con los lugares
                                if (Ubicacion.mLocationPermissionsGranted && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                                        && mLatitud != 0.0 && mLongitud != 0.0) {
                                    lugar.setDistancia(ubicacion.distFrom(mLatitud, mLongitud, lugar.getLatitud(), lugar.getLongitud()));
                                    Log.i("LOCATION", "Distancia en metros de " + lugar.getNombre() + " " + String.valueOf(lugar.getDistancia()));
                                }
                            }
                            //Ordena la lista segun la distancia y mostramos los lugares mas cercanos al usuario
                            if (mLatitud != 0.0 && mLongitud != 0.0)
                                Collections.sort(listLugares);
                            adapter.notifyDataSetChanged();
                            recyclerLugares.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.INVISIBLE);
                        } else {
                            Log.d("DATOS", "Error getting documents: ", task.getException());
                        }
                    }
                });

        recyclerLugares.setAdapter(adapter);//llena el recyclerView

        adapter.setOnClickListener(new View.OnClickListener() { //evento cuando presionan un Lugar
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    // versiones con android 6.0 o superior
                    //cargarAnimacion(view);
                    cargarDetalles(view);
                } else {
                    cargarDetalles(view);
                    // para versiones anteriores a android 6.0
                }
            }
        });


        /*
        Toast.makeText(this, "Buscando..", Toast.LENGTH_SHORT).show();
        listBuscada = new ArrayList<>();
        for (int i = 0; i < listLugares.size(); i++) {
            if (listLugares.get(i).getNombre().equals(texto)) {
                listBuscada.add(listLugares.get(i));
            }
        }
        AdapterLugares adapter = new AdapterLugares(this, listBuscada);
        adapter.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                cargarDetalles(view);
            }
        });
        recyclerLugares.setAdapter(adapter);
        */
    }

    private void cargarDetalles(View view) {
        tvId = (TextView) view.findViewById(R.id.txt_id);
        tvNombre = (TextView) view.findViewById(R.id.txt_nombre);
        tvDistancia = (TextView) view.findViewById(R.id.txt_distancia);
        Intent i = new Intent(getApplicationContext(), Detalles2Activity.class);
        i.putExtra("id", tvId.getText().toString());
        i.putExtra("nombre", tvNombre.getText().toString());
        i.putExtra("distancia",tvDistancia.getText().toString());
        startActivity(i);
    }

    public void cerrarSesion() {
        FirebaseAuth.getInstance().signOut();
        LoginManager.getInstance().logOut();
        loginActivity();
    }

    public void loginActivity() {
        Intent l = new Intent(this, LoginActivity.class);
        l.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(l);
    }


    //Metodos para el Nav Viewer
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        //menu Cliente
        if (id == R.id.login) {
            loginActivity();
        } else if (id == R.id.nav_account) {
            startActivity(new Intent(this,MiCuentaActivity.class));
        } else if (id == R.id.nav_favorites) {
            startActivity(new Intent(getApplicationContext(),FavoritosActivity.class));
        } else if (id == R.id.nav_reservation) {
            startActivity(new Intent(this,ReservacionesUsuarioActivity.class));
        }else if (id == R.id.nav_messages) {
            startActivity(new Intent(this,ChatsUsuarioActivity.class));
        } else if (id == R.id.nav_place) {
            startActivity(new Intent(getApplicationContext(),AltaLugarActivity.class));
        }else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_logout) {
            cerrarSesion();
        }
        //menu negocio
        else if(id == R.id.nav_business) {
            startActivity(new Intent(this, MisNegociosActivity.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //metodos ubicacion
    private void AlertNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("El sistema GPS esta desactivado, ¿Desea activarlo?")
                .setCancelable(false)
                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        alert = builder.create();
        alert.show();
    }

    public void getLocationPermission() { //obtiene los permisos de almacenamiento
        String[] permission = {android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this, Ubicacion.FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this, Ubicacion.COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Ubicacion.mLocationPermissionsGranted = true;
                Log.i("LOCATION", "permisos de localizacion aceptados");
            } else {
                ActivityCompat.requestPermissions(this, permission, Ubicacion.LOCATION_PERMISSION_CODE);
                Log.i("LOCATION", "permisos de localizacion denegados");
            }
        } else {
            ActivityCompat.requestPermissions(this, permission, Ubicacion.LOCATION_PERMISSION_CODE);
            Log.i("LOCATION", "permisos de localizacion denegados");
        }
    }

    @Override //comprueba los permisos
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Ubicacion.mLocationPermissionsGranted = false;

        switch (requestCode) {
            case Ubicacion.LOCATION_PERMISSION_CODE:
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        Ubicacion.mLocationPermissionsGranted = false;
                        Log.i("LOCATION", "permisos de localizacion denegados");
                        return;
                    }
                }
                Log.i("LOCATION", "permisos de localizacion aceptados");
                Ubicacion.mLocationPermissionsGranted = true;
                break;
        }
    }

    public void getDeviceLocation() {
        Log.i("LOCATION", "INTENTAR OBTENER LA LOCALIZACION DEL DISPOSITIVO");
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            if (Ubicacion.mLocationPermissionsGranted && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER )){
                //si ya tengo los permisos de localizacion

                mFusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            mLatitud = location.getLatitude();
                            mLongitud = location.getLongitude();
                            if(llenadoRecyclerConMiUbicacion) { //validacion para que entre solo una ves a llenar el recycler
                                Log.i("LOCATION", "Longitud y latitud " + String.valueOf(location.getLatitude() + " " + String.valueOf(location.getLongitude())));
                                llenarRecyclerView();
                                llenadoRecyclerConMiUbicacion = false;
                            }
                        } else {
                            //como el provedor de fusion fue nulo intentamos solicitar actualizaciones de ubicacion
                            progressBar.setVisibility(View.VISIBLE);
                            recyclerLugares.setVisibility(View.INVISIBLE);
                            Toast.makeText(getApplicationContext(),getString(R.string.principal_load_near_places),Toast.LENGTH_SHORT).show();
                            mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest,mLocationCallback, null);
                            Log.i("LOCATION", "localización nula");
                        }
                    }
                });

            }else{
                Log.i("LOCATION", "NO SE PUEDE OBTENER LA LOCALIZACION DEL DISPOSITIVO Y LLENA EL RECYCLER VIEW PREDETERMINADO");
                llenarRecyclerView();
            }
        } catch (SecurityException e) {
            Log.i("LOCATION", e.toString());
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }



}
