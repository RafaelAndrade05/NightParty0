package com.example.rene.nightparty0;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.rene.nightparty0.Objetos.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

public class MiCuentaActivity extends AppCompatActivity {

    FirebaseUser user;
    private EditText etNombre,etCorreo,etTelefono;
    private CardView cardView;
    private ImageButton imagenPerfil;
    private ProgressBar progressBar,progressBarInicio;
    private Uri path;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private UserProfileChangeRequest profilesUpdates;

    byte[] datosBytes; //para almacenar la imagen que se muestra en ImageButton

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mi_cuenta);

        user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null)
            finish();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //boton de retroceso al action bar

        etNombre = (EditText)findViewById(R.id.etNombre);
        etCorreo = (EditText)findViewById(R.id.etCorreo);
        etTelefono = (EditText)findViewById(R.id.etTelefono);
        imagenPerfil = (ImageButton)findViewById(R.id.imagenPerfil);
        progressBar = (ProgressBar)findViewById(R.id.progress_bar);
        progressBarInicio = (ProgressBar)findViewById(R.id.progress_barInicio);
        cardView = (CardView)findViewById(R.id.cardView);

        cardView.setVisibility(View.INVISIBLE);
        progressBarInicio.setVisibility(View.VISIBLE);

        DocumentReference documentUsuario = db.collection(Usuario.COLLECTION_USER).document(user.getUid());
        documentUsuario.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot document) {
                if(document != null){
                    Usuario usuario = document.toObject(Usuario.class);
                    usuario.setName(user.getDisplayName());
                    etNombre.setText(usuario.getName());
                    etCorreo.setText(usuario.getEmail());
                    etTelefono.setText(usuario.getPhoneNumber());
                    if(usuario.getPhotoUrl()!= null) {
                        Uri fotoUrl = Uri.parse(usuario.getPhotoUrl());
                        Glide.with(getApplicationContext())
                                .load(fotoUrl)
                                .apply(new RequestOptions()
                                        .centerCrop())
                                .into(imagenPerfil);
                    }
                    progressBarInicio.setVisibility(View.INVISIBLE);
                    cardView.setVisibility(View.VISIBLE);
                    Log.i("USERUPDATE",usuario.getName() + usuario.getPhotoUrl() + usuario.getEmail() + usuario.getPhoneNumber());
                }
            }
        });

    }

    public void onClick(View view) {
        switch (view.getId()){
            case R.id.imagenPerfil:
                cargarImagen();
                break;
            case R.id.btnActualizar:
                progressBar.setVisibility(View.VISIBLE);

                if(path != null) { //si el usuario agrego imagen actualizamos desde subir imagen
                    progressBar.setVisibility(View.VISIBLE);
                    Log.i("USERUPDATE","ACTUALIZAR USUARIO CON IMAGEN");
                    actualizarDatosConImagen();
                }else { //si el usuario no agrego ninguna imagen actualizamos desde actualizar datos
                    progressBar.setVisibility(View.VISIBLE);
                    Log.i("USERUPDATE","ACTUALIZAR USUARIO SIN IMAGEN");
                    actualizarDatosSinImagen();
                }

                break;
        }
    }



    public void actualizarDatosConImagen(){
        if(path != null) { //validacion que el usuario cargo nueva imagen
            //metodo para covertir la imagen del ImageButton a Bytes[]

            try {
                // Create a reference to the file to delete
                if (user.getPhotoUrl() != null) { //Ya almaceno una vez la foto y debemos eliminarla para actualizar su foto nueva
                    StorageReference imagenRef = storageRef.child("usuarios/" + user.getUid() + ".png");
                    imagenRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.i("IMAGES", "Imagen anterior eliminada");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.i("IMAGES", "Error al borrar Imagen");
                        }
                    });
                }
            }catch (Exception ex){}


            final String referenciaStorage = "usuarios" + "/"+ user.getUid() + ".png"; //referencia donde se guardara la imagen en firestorage
            StorageReference imgRef = storage.getReference(referenciaStorage);
            UploadTask uploadTask = imgRef.putBytes(datosBytes); //sube imagen a Firestorage
            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    try{
                        Log.i("IMAGES","Subida exitosa de imagen");
                        //subida de imagen exitosa ahora cargamos la url que genera FireStorage para guardarla al usuario
                        storageRef.child(referenciaStorage).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) { //se guarda la url al usuario
                                Log.i("IMAGES",uri.toString());
                                actualizarUsuarioUrl(uri);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                Log.e("IMAGES","No se pudo cargar url de firestoage");
                            }
                        });
                    }catch (Exception ex){
                        Log.e("IMAGE_ERROR","excception "+ex.toString());
                    }

                    progressBar.setVisibility(View.INVISIBLE);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.i("IMAGES","Subida de imagen fallida");
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(),getString(R.string.micuenta_error_subir_imagen),Toast.LENGTH_SHORT).show();
                }
            });


        }
    }

    public void actualizarUsuarioUrl(Uri uri){
        //Creo un objeto usuario como ayuda para actualizar datos
        Usuario usuario = new Usuario();
        usuario.setName(etNombre.getText().toString());
        usuario.setEmail(etCorreo.getText().toString());
        usuario.setPhoneNumber(etTelefono.getText().toString());
        usuario.setPhotoUrl(uri.toString());

        profilesUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(usuario.getName())
                .setPhotoUri(uri)
                .build();

        user.updateProfile(profilesUpdates).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i("USERUPDATE", "Datos actualizados en auth");
            }
        });
        user.updateEmail(etCorreo.getText().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i("USERUPDATE", "correo actualizado en auth");
            }
        });

        //Actualizamos los datos de usuario en FireStore
        DocumentReference documentUsuario = db.collection(Usuario.COLLECTION_USER).document(user.getUid());
        documentUsuario.update(
                Usuario.CAMPO_NAME, usuario.getName(),
                Usuario.CAMPO_EMAIL, usuario.getEmail(),
                Usuario.CAMPO_PHONE_NUMBER, usuario.getPhoneNumber(),
                Usuario.CAMPO_PHOTO_URL, usuario.getPhotoUrl()
        ).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i("USER UPDATE", "datos en firestore actualizados");
                Toast.makeText(getApplicationContext(), getString(R.string.micuenta_actualiza_correctamente), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    public void actualizarDatosSinImagen(){
        //Creo un objeto usuario como ayuda para actualizar datos
        Usuario usuario = new Usuario();
        usuario.setName(etNombre.getText().toString());
        usuario.setEmail(etCorreo.getText().toString());
        usuario.setPhoneNumber(etTelefono.getText().toString());

        profilesUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(usuario.getName())
                .build();

        user.updateProfile(profilesUpdates).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i("USERUPDATE", "Datos actualizados en auth");
            }
        });
        user.updateEmail(etCorreo.getText().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i("USERUPDATE", "correo actualizado en auth");
            }
        });

        //Actualizamos los datos de usuario en FireStore
        DocumentReference documentUsuario = db.collection(Usuario.COLLECTION_USER).document(user.getUid());
        documentUsuario.update(
                Usuario.CAMPO_NAME, usuario.getName(),
                Usuario.CAMPO_EMAIL, usuario.getEmail(),
                Usuario.CAMPO_PHONE_NUMBER, usuario.getPhoneNumber(),
                Usuario.CAMPO_PHOTO_URL, usuario.getPhotoUrl()
        ).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i("USER UPDATE", "datos en firestore actualizados");
                Toast.makeText(getApplicationContext(), getString(R.string.micuenta_actualiza_correctamente), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    public void cargarImagen(){
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        i.setType("image/");
        startActivityForResult(Intent.createChooser(i,"Seleccione la aplicacion"),10); //entra al onActivityResult
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){ //muestra la imagen cargada
            path = data.getData();
            imagenPerfil.setImageURI(path);

            imagenPerfil.setDrawingCacheEnabled(true);
            imagenPerfil.buildDrawingCache();
            //Bitmap bitmap = imagenPerfil.getDrawingCache();
            Bitmap bitmap = ((BitmapDrawable) imagenPerfil.getDrawable()).getBitmap();
            Bitmap resizeBitmap = Bitmap.createScaledBitmap(bitmap, 350, 350, true); //reduce el tamaño original
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            resizeBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            imagenPerfil.setDrawingCacheEnabled(false);
            datosBytes = baos.toByteArray();
        }
    }
}
