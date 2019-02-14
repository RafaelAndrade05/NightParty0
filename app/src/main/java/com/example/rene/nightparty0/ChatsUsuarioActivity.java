package com.example.rene.nightparty0;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rene.nightparty0.Adaptadores.AdapterChatsUsuarios;
import com.example.rene.nightparty0.Objetos.Chat;
import com.example.rene.nightparty0.Objetos.ChatIds;
import com.example.rene.nightparty0.Objetos.ChatVistaPrevia;
import com.example.rene.nightparty0.Objetos.Imagen;
import com.example.rene.nightparty0.Objetos.Lugar;
import com.example.rene.nightparty0.Objetos.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;

public class ChatsUsuarioActivity extends AppCompatActivity {

    public static String TAG = "chatsUsuarioActivity";
    FirebaseUser user;
    ArrayList<ChatVistaPrevia> listChats;
    ChatVistaPrevia objChatVistaPrevia;
    private RecyclerView recyclerViewChats;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private TextView tvSinMensajes;
    private ImageView imageViewSinMensaje;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats_usuario);

        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null)
        {
            finish();
            Toast.makeText(getApplicationContext(),getString(R.string.chatActivity_login_requerido),Toast.LENGTH_LONG).show();
        }

        //setup recyclerViewChats
        recyclerViewChats = (RecyclerView) findViewById(R.id.recyclerViewChats);
        tvSinMensajes = (TextView) findViewById(R.id.tvSinMensaje);
        imageViewSinMensaje = (ImageView) findViewById(R.id.imagenSinMensaje);

        listChats = new ArrayList<>();
        recyclerViewChats.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        adapter = new AdapterChatsUsuarios(getApplicationContext(),listChats);
        recyclerViewChats.setAdapter(adapter);

        adapter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView tvId = view.findViewById(R.id.tvIdLugarChat);
                Intent i = new Intent(getApplicationContext(),ChatActivityUsuario.class);
                i.putExtra("idLugar",tvId.getText());
                startActivity(i);
            }
        });

        llenarRecyclerView();

    }

    String idChat;
    int position=0;
    ArrayList<String> listIdChats;
    AdapterChatsUsuarios adapter;

    private void llenarRecyclerView()
    {
        listIdChats = new ArrayList<>();

        db.collection(Usuario.COLLECTION_USER).document(user.getUid()).collection(Usuario.COLLECTION_CHATS).get().
                addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        listChats.removeAll(listChats);
                        if(task.isSuccessful()){
                            if(!task.getResult().isEmpty()) {
                                for (final DocumentSnapshot document : task.getResult()) {
                                    //Recupera todos los id de chat donde el usuario es participe
                                    idChat = document.getString(ChatIds.CAMPO_IDCHAT);
                                    listIdChats.add(idChat);
                                }
                                obtenerInformacionChats(listIdChats);
                            }else {
                                //No hay ningun mensaje
                                tvSinMensajes.setVisibility(View.VISIBLE);
                                imageViewSinMensaje.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                });
    }

    private void obtenerInformacionChats(final ArrayList<String> listIdChats) {
        for(int i=0; i<listIdChats.size();i++)
        {
            final String idChat = listIdChats.get(i).toString();

            db.collection(Chat.COLLECTION_CHAT).document(idChat).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                if (task.getResult().exists()) {
                                    //Recuperamos el id del Lugar que tiene el chat
                                    ChatIds objChatIds;
                                    objChatIds = task.getResult().toObject(ChatIds.class);

                                    db.collection(Lugar.COLLECTION_LUGARES).document(objChatIds.getIdLugar())
                                            .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            //Obtenemos el id y nombre del lugar
                                            final Lugar objLugar;
                                            objLugar = documentSnapshot.toObject(Lugar.class);
                                            objLugar.setId(documentSnapshot.getId());

                                            db.collection(Lugar.COLLECTION_LUGARES).document(documentSnapshot.getId())
                                                    .collection(Lugar.COLLECTION_IMAGENES).limit(1).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                @Override
                                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                    if (!queryDocumentSnapshots.isEmpty()) {
                                                        //obtenemos Imagen del lugar
                                                        DocumentSnapshot documentImagenesLugar = queryDocumentSnapshots.getDocuments().get(0);
                                                        final Imagen objImagen = documentImagenesLugar.toObject(Imagen.class);

                                                        db.collection(Chat.COLLECTION_CHAT).document(idChat).
                                                                collection(Chat.COLLECTION_MENSAJES).
                                                                orderBy(Chat.CAMPO_DATE, com.google.firebase.firestore.Query.Direction.DESCENDING).
                                                                limit(1).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                            @Override
                                                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                                //Obtenemos el ultimo mensaje y la fecha
                                                                if (!queryDocumentSnapshots.isEmpty()) {
                                                                    Chat objChat;
                                                                    objChat = queryDocumentSnapshots.getDocuments().get(0).toObject(Chat.class);

                                                                    objChatVistaPrevia = new ChatVistaPrevia();

                                                                    //llenamos todo el objeto
                                                                    objChatVistaPrevia.setIdChat(idChat);
                                                                    objChatVistaPrevia.setNombre(objLugar.getNombre());
                                                                    objChatVistaPrevia.setIdLugar(objLugar.getId());
                                                                    objChatVistaPrevia.setUrlImagen(objImagen.getUrlimagen());
                                                                    objChatVistaPrevia.setMensaje(objChat.getMessage());
                                                                    objChatVistaPrevia.setFecha(objChat.getTimestamp());

                                                                    listChats.add(objChatVistaPrevia);

                                                                    Collections.sort(listChats);

                                                                    adapter.notifyDataSetChanged();
                                                                    //Log.i(TAG,listChats.get(position).getData());
                                                                }
                                                            }
                                                        });
                                                    }
                                                }
                                            });

                                        }
                                    });
                                }
                            }
                        }
                    });
        }

    }

    //reinicia el activity cuando el usuario presiona el back button
    @Override
    public void onRestart() {
        super.onRestart();
        //When BACK BUTTON is pressed, the activity on the stack is restarted
        //Do what you want on the refresh procedure here
        Log.i("ACTIVITYRELOAD","RENICIANDO");
        finish();
        startActivity(getIntent());
    }



}
