package com.example.rene.nightparty0.ActivitysNegocio;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rene.nightparty0.Adaptadores.AdapterImagenEditarNegocio;
import com.example.rene.nightparty0.Adaptadores.AdapterImagenPromociones;
import com.example.rene.nightparty0.Ayuda.Ubicacion;
import com.example.rene.nightparty0.Objetos.Imagen;
import com.example.rene.nightparty0.Objetos.Lugar;
import com.example.rene.nightparty0.Objetos.Promocion;
import com.example.rene.nightparty0.R;
import com.google.android.gms.tasks.OnCompleteListener;
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

import org.w3c.dom.Text;

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

public class PromocionesDisponibilidadActivity extends AppCompatActivity {


    private boolean mStoragePermissionsGranted = false;
    private int contadorImagenes = 0;

    String idLugar;
    String [] opcionesDisponiblidad = {"Baja","Media","Alta"};
    ArrayList<String> filePaths = new ArrayList<>();

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();


    private Spinner spinnerDisponiblidad;
    private RecyclerView recyclerViewImagenes;
    private EditText etDescripcionPromociones;
    private ProgressBar progressBarPromociones;
    private ArrayList<Imagen> listImagenes;
    private Button btnSeleccionImagen;
    private TextView tvNumeroImagen;

    private Dialog myDialog;
    private int aux = 0;
    private int aux2 = 0;
    List<String> listUrls = new ArrayList<>();

    private boolean imagenesGuardadas = false;
    private boolean datosGuardaddos = false;
    private boolean urlsGuardaddos = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promociones_disponibilidad);

        if(user == null){
            finish();
        }


        idLugar = getIntent().getStringExtra("idLugar");

        progressBarPromociones = (ProgressBar)findViewById(R.id.progress_bar_promociones);
        btnSeleccionImagen = (Button)findViewById(R.id.btSeleccionImagen);
        tvNumeroImagen = (TextView)findViewById(R.id.tvNumeroImagenes);
        myDialog = new Dialog(this);

        //seccion Disponibilidad
        spinnerDisponiblidad = (Spinner)findViewById(R.id.spinnerDisponiblidad);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),R.layout.support_simple_spinner_dropdown_item,opcionesDisponiblidad);
        spinnerDisponiblidad.setAdapter(adapter);

        //seccion Promociones
        recyclerViewImagenes = (RecyclerView)findViewById(R.id.recyclerViewImagenesPromociones);
        etDescripcionPromociones = (EditText)findViewById(R.id.etDescripcionPromocion);

        llenarRecylcerViewImagenes();
        llenarInformacion();
    }

    public void onClick(View view){

        switch (view.getId()){
            case R.id.btnGuardarDisponibilidad:
                Log.i("IDLUGAR",idLugar);
                int eleccion = spinnerDisponiblidad.getSelectedItemPosition();
                Log.i("position",String.valueOf(eleccion));
                actualizarDisponiblidad(eleccion);
                break;
            case R.id.btnGuardarPromocion:
                guardarPromocion();
                break;
        }

    }

    private void guardarPromocion() {

        //Guarda los datos
        String descripcion = etDescripcionPromociones.getText().toString();
        if(TextUtils.isEmpty(descripcion)) {
            descripcion = "";
        }

        //Guarda el comentario este vacio o no
        Map<String, Object> dataToSave = new HashMap<>();
        dataToSave.put(Promocion.CAMPO_DESCRIPCION, descripcion);
        //sube la descripcion si hubiera
        db.collection(Promocion.COLLECTION_PROMOCIONES).document(idLugar).set(dataToSave)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            datosGuardaddos = true;
                            comprobarDatos();
                        }
                    }
                });

        //comprueba que haya una imagen para almacenar como promocion
        if(!filePaths.isEmpty()){
            iniciarMensajePopup();
            Bitmap bitmap,resizeBitmap;
            ByteArrayOutputStream baos;
            byte [] datosBytes;
            InputStream imageStream;
            UploadTask uploadTask;
            for(String imagen:filePaths){
                Uri file = Uri.fromFile(new File(imagen));
                String path = Promocion.COLLECTION_PROMOCIONES+"/"+idLugar+"/"+ UUID.randomUUID() + ".png";
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
                                aux = 0;
                            }

                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        }else if(filePaths.isEmpty() && contadorImagenes == 0) {
            //Toast.makeText(getApplicationContext(), R.string.promocionesDisponibilidad_imagen_requerida, Toast.LENGTH_SHORT).show();
            return;
        }

        //guardar las url de imagenes en la firestore
        Map<String,Object> dataImagenesUrls = new HashMap<>();
        for(int x=0;x<listUrls.size();x++) {
            dataImagenesUrls.put("urlimagen",listUrls.get(x));
            db.collection(Promocion.COLLECTION_PROMOCIONES).document(idLugar).
                    collection(Promocion.COLLECTION_IMAGENES).add(dataImagenesUrls).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                @Override
                public void onComplete(@NonNull Task<DocumentReference> task) {
                    Log.i("COMPROBARDATOS","SUBIDA DE URL FIRESTORE");

                    aux2++;
                    if(aux2 == listUrls.size()){//entrara en la ultima pasada del foreach cuando todas las imagenes se suban correctamente
                        Log.i("COMPROBARDATOS","URLS SUBIDAS CORRECTAMENTE");
                        urlsGuardaddos = true;
                        comprobarDatos();
                        aux2 = 0;
                    }
                }
            });
        }


    }

    public void comprobarDatos(){

        if(datosGuardaddos && urlsGuardaddos && imagenesGuardadas) {
            Toast.makeText(getApplicationContext(), R.string.promocionesDisponibilidad_guardado_promo, Toast.LENGTH_SHORT).show();
            myDialog.dismiss();

            //Recargar activity
            finish();
            startActivity(getIntent());
        }else if(datosGuardaddos && filePaths.isEmpty()){
            Toast.makeText(getApplicationContext(), R.string.promocionesDisponibilidad_guardado_promo, Toast.LENGTH_SHORT).show();
            //Recargar activity
            finish();
            startActivity(getIntent());
        }

    }

    public void iniciarMensajePopup(){
        TextView tvTitulo,tvSubtitulo;
        ProgressBar progressBarPopup;
        myDialog.setContentView(R.layout.popupcargar);
        tvTitulo = (TextView)myDialog.findViewById(R.id.tvTitulo);
        tvSubtitulo = (TextView)myDialog.findViewById(R.id.tvSubtitulo);
        progressBarPopup = (ProgressBar)myDialog.findViewById(R.id.progress_bar_popup);
        tvTitulo.setText(R.string.popup_guardando_promociones);
        tvSubtitulo.setText(R.string.popup_cargando_espere);
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();
    }


    private void llenarInformacion() {
        db.collection(Promocion.COLLECTION_PROMOCIONES).document(idLugar).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    Promocion objPromocion = new Promocion();
                    objPromocion = documentSnapshot.toObject(Promocion.class);
                    etDescripcionPromociones.setText(objPromocion.getDescripcion());
                }
            }
        });

    }



    private void llenarRecylcerViewImagenes() {
        contadorImagenes=0;
        progressBarPromociones.setVisibility(View.VISIBLE);
        //recyclerImagenes.setVisibility(View.INVISIBLE);
        Log.i("IMAGENESCARGAR","LLENANDO RECYLCER VIEW IMAGENES");
        listImagenes = new ArrayList<>();
        recyclerViewImagenes.setLayoutManager(new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.HORIZONTAL, false));//forma de representar los datos
        final AdapterImagenPromociones adapter = new AdapterImagenPromociones(getApplicationContext(),listImagenes);
        recyclerViewImagenes.setAdapter(adapter);//llena el recyclerView
        db.collection(Promocion.COLLECTION_PROMOCIONES).document(idLugar).collection(Promocion.COLLECTION_IMAGENES).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                listImagenes.removeAll(listImagenes);
                if(task.isSuccessful()){
                    if(! task.getResult().isEmpty()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            contadorImagenes++;
                            Imagen imagen = document.toObject(Imagen.class);
                            imagen.setId(document.getId());
                            listImagenes.add(imagen);
                            Log.i("IMAGENESCARGAR", "URL" + "=>" + imagen.getUrlimagen());
                        }
                        if (contadorImagenes == 5)
                            btnSeleccionImagen.setVisibility(View.INVISIBLE);

                        btnSeleccionImagen.setVisibility(View.VISIBLE);
                        adapter.notifyDataSetChanged();
                        recyclerViewImagenes.setVisibility(View.VISIBLE);
                        progressBarPromociones.setVisibility(View.INVISIBLE);
                    }else{
                        progressBarPromociones.setVisibility(View.INVISIBLE);
                    }
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
                        new AlertDialog.Builder(PromocionesDisponibilidadActivity.this)
                                .setMessage(R.string.alertDialogMensajeImagen)
                                .setPositiveButton(R.string.alertDialogConfirmar, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        eliminarImagen(tvid.getText().toString(),tvUrlImagen.getText().toString(),progressBarRecyler);
                                    }
                                }).setNegativeButton(R.string.alertDialogCancelar, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
                    }
                });
            }
        });
    }

    //metodos seccion Disponibilidad

    private void actualizarDisponiblidad(int eleccion) {
        db.collection(Lugar.COLLECTION_LUGARES).document(idLugar).update(Lugar.CAMPO_DISPONIBILIDAD,eleccion)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getApplicationContext(), "Se actualizo la disponibilidad", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    //metodos seccion Promociones

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
                db.collection(Promocion.COLLECTION_PROMOCIONES).document(idLugar).collection(Promocion.COLLECTION_IMAGENES).
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

    //metodo para guardar las uri de las imagenes seleccionadas
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case Ubicacion.REQUEST_CODE:
                if(resultCode== Activity.RESULT_OK && data!=null)
                {
                    //Uri de imagenes guardadas en filePaths
                    filePaths = new ArrayList<>();
                    filePaths.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA));
                    tvNumeroImagen.setText(String.valueOf(filePaths.size()) + " " + getString(R.string.promocionesDisponibilidad_num_imagenes)) ;
                }
                break;
        }
    }


    //metodos para comprobar los permisos de almacenamiento
    public void getStoragePermission(){
        if(ContextCompat.checkSelfPermission(this,Ubicacion.EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            mStoragePermissionsGranted = true;
            Log.i("STORAGEPERMISSION","permisos aceptados");
        }else{
            Log.i("STORAGEPERMISSION","permisos aun no se aceptan");
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},Ubicacion.EXTERNAL_STORAGE_CODE);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case Ubicacion.EXTERNAL_STORAGE_CODE:
                for(int i = 0 ; i <grantResults.length; i++){
                    if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                        mStoragePermissionsGranted = false;
                        Log.i("STORAGEPERMISSION","permisos rechazados");
                        return;
                    }
                }
                Log.i("STORAGEPERMISSION","permisos aceptados");
                mStoragePermissionsGranted = true;
        }
    }


}
