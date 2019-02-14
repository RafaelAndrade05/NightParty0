package com.example.rene.nightparty0.ActivitysNegocio;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rene.nightparty0.Adaptadores.PlaceAutocompleteAdapter;
import com.example.rene.nightparty0.Ayuda.Ubicacion;
import com.example.rene.nightparty0.LoginBusinessActivity;
import com.example.rene.nightparty0.Objetos.Lugar;
import com.example.rene.nightparty0.Objetos.Usuario;
import com.example.rene.nightparty0.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;

public class AltaLugarActivity extends AppCompatActivity implements OnConnectionFailedListener{
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    public static final int EXTERNAL_STORAGE_CODE = 3543;
    //componentes
    private EditText etNombre, etDescripcion;
    private ProgressBar progressBar;
    private AutoCompleteTextView etBusqueda;
    private Spinner spDescripcion;
    private TextView tvNumeroImagen;
    //Firebase
    private FirebaseFirestore mDocRef = FirebaseFirestore.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    // Create a storage reference from our app
    StorageReference storageRef = storage.getReference();
    private boolean mStoragePermissionsGranted = false;
    private GoogleMap mMap;
    private PlaceAutocompleteAdapter mPlaceAutocompleteAdapter;
    private GoogleApiClient mGoogleApiClient;
    private Lugar mLugar;
    private boolean informacionGuardada = false;
    private boolean imagenesGuardadas = false;
    FirebaseUser user;
    ArrayList<String> filePaths = new ArrayList<>();

    Dialog myDialog; //mensaje popup

    private int erroresDatos=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alta_lugar);

        user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null) {
            startActivity(new Intent(getApplicationContext(),LoginBusinessActivity.class));
        }

        etNombre = (EditText) findViewById(R.id.et_nombre);
        etDescripcion = (EditText) findViewById(R.id.et_descripcion);
        etBusqueda = (AutoCompleteTextView) findViewById(R.id.et_busqueda);
        spDescripcion = (Spinner)findViewById(R.id.sp_descripcion);
        tvNumeroImagen = (TextView)findViewById(R.id.tvNumeroImagenes);
        progressBar = (ProgressBar)findViewById(R.id.progress_bar);
        myDialog = new Dialog(this);

        String [] opcionesSpinner = {"Antro","Bar","Merendero"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,opcionesSpinner);
        spDescripcion.setAdapter(adapter);

        getStoragePermission();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                init();
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
    public void getLocationPermission(){ //obtiene los permisos de almacenamiento
        String[] permission = {android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this,Ubicacion.FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this,Ubicacion.COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                Ubicacion.mLocationPermissionsGranted = true;
                Log.i("LOCATION","permisos aceptados");
            } else{
                ActivityCompat.requestPermissions(this,permission,Ubicacion.LOCATION_PERMISSION_CODE);
            }
        }else{
            ActivityCompat.requestPermissions(this,permission,Ubicacion.LOCATION_PERMISSION_CODE);
        }
    }

    public void getStoragePermission(){
        if(ContextCompat.checkSelfPermission(this,Ubicacion.EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            mStoragePermissionsGranted = true;
            Log.i("STORAGEPERMISSION","permisos aceptados");
        }else{
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},Ubicacion.LOCATION_PERMISSION_CODE);
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
    int aux = 0;
    String path="";
    List<String> listUrls = new ArrayList<>();
    public void savedData(View view) {

        if(!validarForm()){
            Toast.makeText(getApplicationContext(),"completa todos los campos",Toast.LENGTH_SHORT).show();
            return;}

        if(mLugar == null){
            Toast.makeText(getApplicationContext(),"Agregue una ubicación",Toast.LENGTH_SHORT).show();
            return;
        }

        String nombre = etNombre.getText().toString();
        String descripcion = etDescripcion.getText().toString();
        String categoria = spDescripcion.getSelectedItem().toString();
        final String idNegocio = nombre + UUID.randomUUID();
        if(!filePaths.isEmpty()){
            iniciarMensajePopup();
            progressBar.setVisibility(View.VISIBLE);
            Bitmap bitmap,resizeBitmap;
            ByteArrayOutputStream baos;
            byte [] datosBytes;
            InputStream imageStream;
            UploadTask uploadTask;
            for(String imagen:filePaths){
                Uri file = Uri.fromFile(new File(imagen));
                path = "lugares/"+idNegocio+"/"+UUID.randomUUID() + ".png";
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
                                Map<String,Object> dataImagenesUrls = new HashMap<>();
                                for(int x=0;x<listUrls.size();x++) {
                                    dataImagenesUrls.put("urlimagen",listUrls.get(x));
                                    mDocRef.collection(Lugar.COLLECTION_LUGARES).document(idNegocio).
                                            collection(Lugar.COLLECTION_IMAGENES).add(dataImagenesUrls);
                                }
                                imagenesGuardadas = true;
                                comprobarDatos();
                            }

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        //si hay un error al subir imagenes a storage
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            erroresDatos++;
                            comprobarDatos();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }else{
            Toast.makeText(getApplicationContext(),"sube almenos una imagen de tu negocio",Toast.LENGTH_SHORT).show();
            return;
        }


        Map<String,Object> dataToSave = new HashMap<>();
        dataToSave.put(Lugar.CAMPO_NOMBRE,nombre);
        Log.i("ALTALUGAR",nombre);
        dataToSave.put(Lugar.CAMPO_CATEGORIA,categoria);
        Log.i("ALTALUGAR",categoria);
        dataToSave.put(Lugar.CAMPO_DESCRIPCION, descripcion);
        Log.i("ALTALUGAR",descripcion);
        dataToSave.put(Lugar.CAMPO_DIRECCION,mLugar.getDireccion());
        Log.i("ALTALUGAR",mLugar.getDireccion());
        dataToSave.put(Lugar.CAMPO_LATITUD,mLugar.getLatitud());
        dataToSave.put(Lugar.CAMPO_LONGITUD,mLugar.getLongitud());
        dataToSave.put(Lugar.CAMPO_NUM_RATING,0);
        dataToSave.put(Lugar.CAMPO_UID,user.getUid());
        dataToSave.put(Lugar.CAMPO_DISPONIBILIDAD,0);
        dataToSave.put(Lugar.CAMPO_DATE,Calendar.getInstance().getTime());
        Log.i("ALTALUGAR",String.valueOf(mLugar.getNumRatings()));
        dataToSave.put(Lugar.CAMPO_PROMEDIO_RATING,mLugar.getNumRatings());

        mDocRef.collection(Lugar.COLLECTION_LUGARES).document(idNegocio).set(dataToSave).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i("COMPROBARDATOS","INFORMACION GUARDADA");
                informacionGuardada = true;
                comprobarDatos();
                etNombre.setText("");
                etDescripcion.setText("");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                erroresDatos++;
                comprobarDatos();
            }
        });

    }

    public void iniciarMensajePopup(){
        TextView tvTitulo,tvSubtitulo;
        ProgressBar progressBarPopup;
        myDialog.setContentView(R.layout.popupcargar);
        tvTitulo = (TextView)myDialog.findViewById(R.id.tvTitulo);
        tvSubtitulo = (TextView)myDialog.findViewById(R.id.tvSubtitulo);
        progressBarPopup = (ProgressBar)myDialog.findViewById(R.id.progress_bar_popup);
        tvTitulo.setText(R.string.popup_cargando_creando_negocio);
        tvSubtitulo.setText(R.string.popup_cargando_espere);
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();
    }

    public void comprobarDatos(){
        Log.i("COMPROBARDATOS","ENTRA AL METODO COMPROBAR DATOS");
        if(informacionGuardada && imagenesGuardadas && erroresDatos == 0){ //ya se subio la informacion y las imagenes
            Log.i("COMPROBARDATOS","INFORMACION Y IMAGENES GUARDADAS");
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection(Usuario.COLLECTION_USER).document(user.getUid())
                    .update(Usuario.CAMPO_TYPE_USER,Usuario.TYPE_USER_BOUSSINES).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(getApplicationContext(),getString(R.string.alta_exitoso_guardar),Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.INVISIBLE);
                    myDialog.dismiss();
                    startActivity(new Intent(getApplicationContext(),MisNegociosActivity.class));
                    finish(); //Elimina de la pila de actividades
                }
            });

        }

        if(erroresDatos != 0){
            Toast.makeText(getApplicationContext(),getString(R.string.alta_error_guardar),Toast.LENGTH_SHORT).show();
        }

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

    public void cargarImagen(View view) {
        /*Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        i.setType("image/");
        startActivityForResult(Intent.createChooser(i,"Seleccione la aplicacion"),10);*/

        if(!mStoragePermissionsGranted){
            getStoragePermission();
        }
        FilePickerBuilder.getInstance().setMaxCount(5)
                .setSelectedFiles(filePaths)
                .setActivityTheme(R.style.LibAppTheme)
                .pickPhoto(this,Ubicacion.REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case Ubicacion.REQUEST_CODE:
                if(resultCode==Activity.RESULT_OK && data!=null)
                {
                    //Uri de imagenes guardadas en filePaths
                    filePaths = new ArrayList<>();
                    filePaths.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA));
                    tvNumeroImagen.setText(String.valueOf(filePaths.size()) + " " + getString(R.string.promocionesDisponibilidad_num_imagenes)) ;
                }
                break;
        }

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



}
