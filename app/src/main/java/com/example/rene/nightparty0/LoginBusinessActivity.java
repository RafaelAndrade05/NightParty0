package com.example.rene.nightparty0;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.rene.nightparty0.Ayuda.SharedPreferencesManager;
import com.example.rene.nightparty0.Objetos.Usuario;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class LoginBusinessActivity extends AppCompatActivity {

    CallbackManager callbackManager;
    LoginButton loginButton;

    //Login con Facebook
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    FirebaseUser user;


    private ArrayList<String> listTokens;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_business);

        /*Linea que nos ayuda a que el background ocupe toda la pantalla incluyendo statusbar  */
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            finish();
        }

        listTokens = new ArrayList<>();
        if(SharedPreferencesManager.getInstance(getApplicationContext()).getToken() != null){
            //añade token a arrayList
            listTokens.add(SharedPreferencesManager.getInstance(getApplicationContext()).getToken());
        }


        //registro o login con facebook
        callbackManager = CallbackManager.Factory.create();
        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions("email","public_profile");

        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                facebookToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d("FACEBOOK", "facebook:onCancel");
                Toast.makeText(getApplicationContext(),"error al registrarse",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException exception) {
                Log.d("FACEBOOK", "facebook:onError", exception);
                Toast.makeText(getApplicationContext(),"error",Toast.LENGTH_SHORT).show();
            }
        });

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if(user != null){
                    //Comrpueba que exista el correo en la base de datos
                    //Si no existe el correo en firestore se agrega
                    DocumentReference docRef = db.collection(Usuario.COLLECTION_USER).document(user.getUid());
                    docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document != null && document.exists()) {
                                    Log.d("FACEBOOKAUTH", "YA ECISTE USUARIO " + document.getData());
                                    addTokenDispositivo(user.getUid()); //se logro login exitoso
                                } else { //se agrega a firestore el usuario
                                    Log.d("FACEBOOKAUTH", "CREANDO USUARIO EN FIRESTORE ", task.getException());
                                    Usuario usuario = new Usuario();
                                    usuario.setUid(user.getUid());
                                    usuario.setName(user.getDisplayName());
                                    usuario.setEmail(user.getEmail());
                                    usuario.setPhotoUrl(user.getPhotoUrl().toString());
                                    usuario.setPhoneNumber(user.getPhoneNumber());
                                    usuario.setTypeUser(usuario.TYPE_USER_BOUSSINES);
                                    usuario.setRegistrationTokens(listTokens);

                                    db.collection(Usuario.COLLECTION_USER).document(user.getUid()).set(usuario);
                                    irPantallaPrincipal(); //se logro login exitoso
                                }
                            } else {
                                Log.d("FACEBOOKAUTH", "get failed with ", task.getException());
                            }
                        }
                    });

                }
            }
        };

    }

    private void facebookToken(AccessToken accessToken) {
        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(!task.isSuccessful()){
                    Toast.makeText(getApplicationContext(),"Error firebase login",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    int aux=0;
    private void addTokenDispositivo(String uid){
        //Nuevo inicio de sesion
        db.collection(Usuario.COLLECTION_USER).document(uid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Usuario objUsuario = documentSnapshot.toObject(Usuario.class);
                String tokenDevice = SharedPreferencesManager.getInstance(getApplicationContext()).getToken();
                ArrayList<String> tokensUsuario = objUsuario.getRegistrationTokens();

                Log.i("token", "size " + tokensUsuario.size());
                for(int i=0;i<tokensUsuario.size();i++){

                    Log.i("token",tokensUsuario.get(i));

                    if (tokensUsuario.get(i).equals(tokenDevice)) {
                        //no es necesario actualizar la lista de token
                        aux=-1;
                    }

                }

                if(aux == -1) {
                    Log.i("token","No actualiza");
                    irPantallaPrincipal();
                }else {
                    Log.i("token","actualiza");
                    tokensUsuario.add(tokenDevice); //añade el nuevo token
                    db.collection(Usuario.COLLECTION_USER).document(user.getUid())
                            .update(Usuario.CAMPO_REGISTRATION_TOKENS, tokensUsuario);
                    irPantallaPrincipal();
                }

            }
        });

    }

    private void irPantallaPrincipal() {
        Intent intent = new Intent(this,Principal.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK );
        startActivity(intent); //Unica activity en ejecucion
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStart(){
        super.onStart();
        firebaseAuth.addAuthStateListener(firebaseAuthListener);
    }

    @Override
    protected void onStop(){
        super.onStop();
        firebaseAuth.removeAuthStateListener(firebaseAuthListener);
    }

    public void onClick(View view) {
        switch (view.getId()){
            case R.id.button_iniciarsesion:
                Intent p = new Intent(this,LoginEmailPasswordActivity.class);
                startActivity(p);
                break;

            case R.id.button_crear_cuenta:
                Intent i = new Intent(this, CrearCuentaNegocioActivity.class);
                startActivity(i);
                break;

            case R.id.button_cerrar:
                Intent o = new Intent(this, Principal.class);
                startActivity(o);
                break;
        }
    }
}
