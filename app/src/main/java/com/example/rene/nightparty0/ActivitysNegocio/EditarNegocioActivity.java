package com.example.rene.nightparty0.ActivitysNegocio;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rene.nightparty0.Adaptadores.AdapterImagenEditarNegocio;
import com.example.rene.nightparty0.Adaptadores.PlaceAutocompleteAdapter;
import com.example.rene.nightparty0.Ayuda.Ubicacion;
import com.example.rene.nightparty0.Objetos.Imagen;
import com.example.rene.nightparty0.Objetos.Lugar;
import com.example.rene.nightparty0.Objetos.Promocion;
import com.example.rene.nightparty0.Objetos.Usuario;
import com.example.rene.nightparty0.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;

public class EditarNegocioActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    public static final int EXTERNAL_STORAGE_CODE = 3543;
    private boolean mStoragePermissionsGranted = false;
    private boolean informacionGuardada = false;
    private boolean imagenesGuardadas = false;
    //componentes
    private EditText etNombre, etDescripcion;
    private ProgressBar progressBar;
    private AutoCompleteTextView etBusqueda;
    private Spinner spCategoria;
    private Button btnSeleccionImagen;
    private TextView tvNumeroImagen;
    private String idLugar;
    private Lugar mLugar;
    private GoogleMap mMap;
    private PlaceAutocompleteAdapter mPlaceAutocompleteAdapter;
    private GoogleApiClient mGoogleApiClient;
    private RecyclerView recyclerImagenes;
    private  ArrayList<Imagen> listImagenes;
    private int contadorImagenes = 0;
    ArrayList<String> filePaths = new ArrayList<>();
    int aux = 0;
    String path="";
    List<String> listUrls = new ArrayList<>();

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();
    Dialog myDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_negocio);

        if(user == null){
            finish();
        }

        etNombre = (EditText) findViewById(R.id.et_nombre);
        etDescripcion = (EditText) findViewById(R.id.et_descripcion);
        etBusqueda = (AutoCompleteTextView) findViewById(R.id.et_busqueda);
        spCategoria = (Spinner)findViewById(R.id.sp_categoria);
        progressBar = (ProgressBar)findViewById(R.id.progress_bar);
        recyclerImagenes = (RecyclerView)findViewById(R.id.recyclerViewImagenes);
        tvNumeroImagen = (TextView)findViewById(R.id.tvNumeroImagenes);
        btnSeleccionImagen = (Button)findViewById(R.id.btSeleccionImagen);
        myDialog = new Dialog(this);

        String [] opcionesSpinner = {"Antro","Bar","Merendero"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,opcionesSpinner);
        spCategoria.setAdapter(adapter);

        idLugar = getIntent().getStringExtra("idLugar");

        llenarInformacionNegocio(idLugar);
        llenarRecylcerViewImagenes();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
            }
        });

    }

    private void init() {
        Log.i("LOCATION","iniciando init");

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        etBusqueda.setOnItemClickListener(mAutoCompleteClicklistener);

        mPlaceAutocompleteAdapter = new PlaceAutocompleteAdapter(this,mGoogleApiClient,Ubicacion.LAT_LNG_BOUNDS,null);
        etBusqueda.setAdapter(mPlaceAutocompleteAdapter);
        etBusqueda.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || actionId == KeyEvent.ACTION_DOWN
                        || actionId == KeyEvent.KEYCODE_ENTER){
                    geoLocate();
                    hideKeyboard();
                }
                return false;
            }
        });
    }

    private void llenarRecylcerViewImagenes() {
        contadorImagenes=0;
        progressBar.setVisibility(View.VISIBLE);
        //recyclerImagenes.setVisibility(View.INVISIBLE);
        Log.i("IMAGENESCARGAR","LLENANDO RECYLCER VIEW");
        listImagenes = new ArrayList<>();
        recyclerImagenes.setLayoutManager(new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.HORIZONTAL, false));//forma de representar los datos
        final AdapterImagenEditarNegocio adapter = new AdapterImagenEditarNegocio(getApplicationContext(),listImagenes);
        recyclerImagenes.setAdapter(adapter);//llena el recyclerView
        db.collection(Lugar.COLLECTION_LUGARES).document(idLugar).collection(Lugar.COLLECTION_IMAGENES).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                listImagenes.removeAll(listImagenes);
                if(task.isSuccessful()){
                    for(DocumentSnapshot document : task.getResult()){
                        contadorImagenes++;
                        Imagen imagen = document.toObject(Imagen.class);
                        imagen.setId(document.getId());
                        listImagenes.add(imagen);
                        Log.i("IMAGENESCARGAR","URL"+ "=>" + imagen.getUrlimagen());
                    }
                    if(contadorImagenes == 5)
                        btnSeleccionImagen.setVisibility(View.INVISIBLE);

                    btnSeleccionImagen.setVisibility(View.VISIBLE);
                    adapter.notifyDataSetChanged();
                    recyclerImagenes.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }
        });

        adapter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView imagenEliminar = v.findViewById(R.id.imagenEliminar);
                final ProgressBar progressBarRecyler = v.findViewById(R.id.progress_bar);
                final TextView tvUrlImagen = v.findViewById(R.id.txt_urlImagen);
                final TextView tvid = v.findViewById(R.id.txt_id);
                imagenEliminar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //confirmacion de eliminacion de imagen
                        new AlertDialog.Builder(EditarNegocioActivity.this)
                                .setMessage(R.string.alertDialogMensajeImagen)
                                .setPositiveButton(R.string.alertDialogConfirmar, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        eliminarImagen(tvid.getText().toString(),tvUrlImagen.getText().toString(),progressBarRecyler);
                                    }
                                }).setNegativeButton(R.string.alertDialogCancelar, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        }).show();
                    }
                });
            }
        });
    }

    public void cargarImagen(View view) {
        if(!mStoragePermissionsGranted){
            getStoragePermission();
        }
        FilePickerBuilder.getInstance().setMaxCount(5-contadorImagenes)
                .setSelectedFiles(filePaths)
                .setActivityTheme(R.style.LibAppTheme)
                .pickPhoto(this, Ubicacion.REQUEST_CODE);
    }

    private void eliminarImagen(final String imagenId, String urlImagen, final ProgressBar progressBar) {
        progressBar.setVisibility(View.VISIBLE);
        //eliminacion de imagen en fireStorage
        storageRef.child(urlImagen).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //eliminacion por fireStore
                db.collection(Lugar.COLLECTION_LUGARES).document(idLugar).collection(Lugar.COLLECTION_IMAGENES).
                        document(imagenId).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        //eliminacion imagen fireStore
                        if(task.isSuccessful()){
                            progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(getApplicationContext(),"Imagen borrada",Toast.LENGTH_SHORT).show();
                            llenarRecylcerViewImagenes();
                        }
                    }
                });
            }
        });

    }

    private void llenarInformacionNegocio(String idLugar) {
        progressBar.setVisibility(View.VISIBLE);
        DocumentReference negocioDocument = db.collection(Lugar.COLLECTION_LUGARES).document(idLugar);
        negocioDocument.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    Log.i("NEGOCIOINFO",documentSnapshot.getId() + "=>" + documentSnapshot.getData() );
                    Lugar negocio = documentSnapshot.toObject(Lugar.class);
                    etNombre.setText(negocio.getNombre());
                    etDescripcion.setText(negocio.getDescripcion());
                    if(negocio.getCategoria().equals("Antro") ){ //selecciona antro
                        spCategoria.setSelection(0);
                    }else if(negocio.getCategoria().equals("Bar")){ //selecciona bar
                        spCategoria.setSelection(1);
                    }else if(negocio.getCategoria().equals("Merendero") ){ //selecciona merendero
                        spCategoria.setSelection(2);
                    }
                    init(); //inicializar camara googleMaps
                    moveCamera(new LatLng(negocio.getLatitud(),negocio.getLongitud() ),Ubicacion.DEFAULT_ZOOM,negocio.getNombre());
                    etBusqueda.setText(negocio.getDireccion());

                    //validacion
                    mLugar = new Lugar();
                    mLugar.setNombre(negocio.getNombre());
                    mLugar.setDireccion(negocio.getDireccion());
                    // mLugar.setAtributos(place.getAttributions().toString());
                    mLugar.setLatitud(negocio.getLatitud());
                    mLugar.setLongitud(negocio.getLongitud());
                }else{

                }
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    public void savedData(View view){
        if(!validarForm()){
            Toast.makeText(getApplicationContext(),"completa todos los campos",Toast.LENGTH_SHORT).show();
            return;}

        if(mLugar == null){
            Toast.makeText(getApplicationContext(),"Agregue una ubicación",Toast.LENGTH_SHORT).show();
            return;
        }

        String nombre = etNombre.getText().toString();
        String descripcion = etDescripcion.getText().toString();
        String categoria = spCategoria.getSelectedItem().toString();

        if(!filePaths.isEmpty()){
            iniciarMensajePopup(1);
            progressBar.setVisibility(View.VISIBLE);
            Bitmap bitmap,resizeBitmap;
            ByteArrayOutputStream baos;
            byte [] datosBytes;
            InputStream imageStream;
            UploadTask uploadTask;
            for(String imagen:filePaths){
                Uri file = Uri.fromFile(new File(imagen));
                String path = "lugares/"+idLugar+"/"+ UUID.randomUUID() + ".png";
                StorageReference mStorageRef = storageRef.child(path);
                listUrls.add(path);

                try { //Cada imagen la comprime para disminuir su tamaño
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), file);
                    resizeBitmap = Bitmap.createScaledBitmap(bitmap, 500, 500, true); //reduce el tamaño original
                    baos = new ByteArrayOutputStream();
                    resizeBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                    datosBytes = baos.toByteArray();

                    uploadTask = mStorageRef.putBytes(datosBytes);
                    uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            Log.i("COMPROBARDATOS","SUBIDA DE IMAGEN EXITOSAMENTE");

                            aux++;
                            if(aux == filePaths.size()){//entrara en la ultima pasada del foreach cuando todas las imagenes se suban correctamente
                                Log.i("COMPROBARDATOS","IMAGENES SUBIDAS CORRECTAMENTE");
                                imagenesGuardadas = true;
                                comprobarDatos();
                            }

                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }else if(filePaths.isEmpty() && contadorImagenes == 0){
            Toast.makeText(getApplicationContext(),"sube almenos una imagen de tu negocio",Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String,Object> dataToSave = new HashMap<>();
        dataToSave.put(Lugar.CAMPO_NOMBRE,nombre);
        dataToSave.put(Lugar.CAMPO_CATEGORIA,categoria);
        dataToSave.put(Lugar.CAMPO_DESCRIPCION, descripcion);
        dataToSave.put(Lugar.CAMPO_DIRECCION,mLugar.getDireccion());
        dataToSave.put(Lugar.CAMPO_LATITUD,mLugar.getLatitud());
        dataToSave.put(Lugar.CAMPO_LONGITUD,mLugar.getLongitud());


        db.collection(Lugar.COLLECTION_LUGARES).document(idLugar).update(dataToSave).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Map<String,Object> dataImagenesUrls = new HashMap<>();
                for(int x=0;x<listUrls.size();x++) {
                    dataImagenesUrls.put("urlimagen",listUrls.get(x));
                    db.collection(Lugar.COLLECTION_LUGARES).document(idLugar).
                            collection("imagenes").add(dataImagenesUrls);
                }
                //validacion en caso de que no cargo ninguna imagen desde su cel y tenia almacenada por lo menos una imagen en la bd
                if(contadorImagenes!=0 && filePaths.isEmpty())
                    imagenesGuardadas = true;
                informacionGuardada = true;
                comprobarDatos();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(),"Ocurrio un error",Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void deleteData(View view){
        //confirmacion de eliminacion de imagen
        new AlertDialog.Builder(EditarNegocioActivity.this)
                .setMessage(R.string.alertDialogMensajeBorrarNegocio)
                .setPositiveButton(R.string.alertDialogConfirmar, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        eliminarNegocio(idLugar);
                    }
                }).setNegativeButton(R.string.alertDialogCancelar, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        }).show();
    }

    int contEliminacion = 0;
    public void eliminarNegocio(final String id){

        progressBar.setVisibility(View.VISIBLE);
        iniciarMensajePopup(0);

        db.collection(Lugar.COLLECTION_LUGARES).document(id).collection(Lugar.COLLECTION_IMAGENES).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for(QueryDocumentSnapshot document : queryDocumentSnapshots){
                    Imagen imagen = document.toObject(Imagen.class);
                    storageRef.child(imagen.getUrlimagen()).delete();
                    //Borra cada documento de la subcollecion imagenes
                    db.collection(Lugar.COLLECTION_LUGARES).document(id).collection(Lugar.COLLECTION_IMAGENES)
                            .document(document.getId()).delete();
                }
                contEliminacion++;
                comprobarEliminacion();
                db.collection(Lugar.COLLECTION_LUGARES).document(id).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        contEliminacion++;
                        comprobarEliminacion();
                    }
                });
            }
        });


        db.collection(Promocion.COLLECTION_PROMOCIONES).document(id).collection(Promocion.COLLECTION_IMAGENES).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for(QueryDocumentSnapshot document: queryDocumentSnapshots){
                    Imagen imagen = document.toObject(Imagen.class);
                    storageRef.child(imagen.getUrlimagen()).delete();
                    //Borra cada documento de la subcollecion imagenes
                    db.collection(Promocion.COLLECTION_PROMOCIONES).document(id).collection(Promocion.COLLECTION_IMAGENES)
                            .document(document.getId()).delete();
                }
                db.collection(Promocion.COLLECTION_PROMOCIONES).document(id).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        contEliminacion++;
                        comprobarEliminacion();
                    }
                });
            }
        });
    }

    public void comprobarEliminacion(){
        if(contEliminacion == 3) {
            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(getApplicationContext(), R.string.editar_negocio_mensaje_eliminado, Toast.LENGTH_SHORT).show();
            Intent i = new Intent(getApplicationContext(), MisNegociosActivity.class);
            startActivity(i);
            finish(); //Elimina de la pila de actividades
        }
    }

    public void iniciarMensajePopup(int num){
        TextView tvTitulo,tvSubtitulo;
        ProgressBar progressBarPopup;
        myDialog.setContentView(R.layout.popupcargar);
        tvTitulo = (TextView)myDialog.findViewById(R.id.tvTitulo);
        tvSubtitulo = (TextView)myDialog.findViewById(R.id.tvSubtitulo);
        progressBarPopup = (ProgressBar)myDialog.findViewById(R.id.progress_bar_popup);
        if(num == 0) { //dependiendo si elimia usuario o modifica
            tvTitulo.setText(R.string.popup_cargando_eliminar_negocio);
        }else{
            tvTitulo.setText(R.string.popup_cargando_modificar_negocio);
        }
        tvSubtitulo.setText(R.string.popup_cargando_espere);
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();
    }

    public boolean validarForm(){
        boolean validado = true;
        String nombre = etNombre.getText().toString();
        String descripcion = etDescripcion.getText().toString();

        if(TextUtils.isEmpty(nombre)){
            etNombre.setError(getString(R.string.alta_fromulario_requerido));
            validado = false;
        }else{
            etNombre.setError(null);
        }

        if(TextUtils.isEmpty(descripcion)){
            etDescripcion.setError(getString(R.string.alta_fromulario_requerido));
            validado = false;
        }else{
            etDescripcion.setError(null);
        }

        return validado;

    }

    public void comprobarDatos(){
        Log.i("COMPROBARDATOS","ENTRA AL METODO COMPROBAR DATOS");
        if(informacionGuardada && imagenesGuardadas){ //ya se subio la informacion y las imagenes
            Log.i("COMPROBARDATOS","INFORMACION Y IMAGENES GUARDADAS");
            Toast.makeText(getApplicationContext(),"Tu negocio se ha modificado correctamente",Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.INVISIBLE);
            startActivity(new Intent(getApplicationContext(),MisNegociosActivity.class));

        }else{
            Log.i("COMPROBARDATOS","no comprueba datos aun");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case Ubicacion.REQUEST_CODE:
                if(resultCode == Activity.RESULT_OK && data!=null)
                {
                    //Uri de imagenes guardadas en filePaths
                    filePaths = new ArrayList<>();
                    filePaths.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA));
                    tvNumeroImagen.setText(String.valueOf(filePaths.size()) + " " + getString(R.string.promocionesDisponibilidad_num_imagenes)) ;
                }
                break;
        }

    }

    public void getStoragePermission(){
        if(ContextCompat.checkSelfPermission(this,Ubicacion.EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            mStoragePermissionsGranted = true;
            Log.i("STORAGEPERMISSION","permisos aceptados");
        }else{
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},Ubicacion.EXTERNAL_STORAGE_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Ubicacion.mLocationPermissionsGranted = false;

        switch (requestCode){
            case Ubicacion.LOCATION_PERMISSION_CODE:
                for(int i = 0 ; i <grantResults.length; i++){
                    if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                        Ubicacion.mLocationPermissionsGranted = false;
                        return;
                    }
                }
                Log.i("LOCATION","permisos aceptados");
                Ubicacion.mLocationPermissionsGranted = true;
                break;
            case EXTERNAL_STORAGE_CODE:
                for(int i = 0 ; i <grantResults.length; i++){
                    if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                        mStoragePermissionsGranted = false;
                        return;
                    }
                }
                Log.i("STORAGEPERMISSION","permisos aceptados");
                mStoragePermissionsGranted = true;
        }
    }

    private void geoLocate() {
        String busqueda = etBusqueda.getText().toString();
        Geocoder geocoder = new Geocoder(this);
        List<Address> list = new ArrayList<>();
        try{
            list = geocoder.getFromLocationName(busqueda,1);
        }catch (IOException e){

        }
        if(list.size() > 0 ){
            Address address = list.get(0);
            Log.i("LOCATION",address.toString());
            moveCamera(new LatLng(address.getLatitude(),address.getLongitude()),Ubicacion.DEFAULT_ZOOM,address.getAddressLine(0));
        }
    }

    private void moveCamera(LatLng latLng, float zoom,String title){
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoom));

        //if(!title.equals(getString(R.string.txt_mylocation))){
        MarkerOptions options = new MarkerOptions().position(latLng).title(title);
        mMap.addMarker(options);
        // }

    }

    private void hideKeyboard(){
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /*
    --------------------------------------Google places API autocomple-------------------------------
     */

    private AdapterView.OnItemClickListener mAutoCompleteClicklistener = new AdapterView.OnItemClickListener(){

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            hideKeyboard();

            Log.i("LOCATION","ENTRO EVENTO ADAPTER");
            final AutocompletePrediction item  = mPlaceAutocompleteAdapter.getItem(position);
            final String placeId  = item.getPlaceId();

            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mGoogleApiClient,placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
        }
    };

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(@NonNull PlaceBuffer places) {

            Log.i("LOCATION","ENTRO EVENTO BUFFER");
            if(!places.getStatus().isSuccess()){
                places.release();
                return;
            }
            final Place place = places.get(0);

            try{
                mLugar = new Lugar();
                mLugar.setNombre(place.getName().toString());
                mLugar.setDireccion(place.getAddress().toString());
                // mLugar.setAtributos(place.getAttributions().toString());
                mLugar.setLatitud(place.getViewport().getCenter().latitude);
                mLugar.setLongitud(place.getViewport().getCenter().longitude);
                mLugar.setNumeroTelefono(place.getPhoneNumber().toString());
                mLugar.setDireccionSitioWeb(place.getWebsiteUri());

                Log.i("LOCATION",mLugar.getNombre()+mLugar.getDireccion()+mLugar.getLatitud().toString()
                        +mLugar.getLongitud().toString());

            }catch (NullPointerException e){
                Log.i("LOCATION",e.toString());
            }

            moveCamera(new LatLng(place.getViewport().getCenter().latitude,place.getViewport().getCenter().longitude)
                    ,Ubicacion.DEFAULT_ZOOM,mLugar.getNombre());

            places.release();
        }
    };


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
