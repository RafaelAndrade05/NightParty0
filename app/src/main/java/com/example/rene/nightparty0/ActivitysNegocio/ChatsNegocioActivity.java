package com.example.rene.nightparty0.ActivitysNegocio;

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

import com.example.rene.nightparty0.Adaptadores.AdapterChatsNegocios;
import com.example.rene.nightparty0.Adaptadores.AdapterChatsUsuarios;
import com.example.rene.nightparty0.Objetos.Chat;
import com.example.rene.nightparty0.Objetos.ChatIds;
import com.example.rene.nightparty0.Objetos.ChatVistaPrevia;
import com.example.rene.nightparty0.Objetos.Imagen;
import com.example.rene.nightparty0.Objetos.Lugar;
import com.example.rene.nightparty0.Objetos.Usuario;
import com.example.rene.nightparty0.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;

public class ChatsNegocioActivity extends AppCompatActivity {

    public static String TAG = "chatsUsuarioActivity";
    private String idLugar,nombre;
    private TextView tvSinMensajes;
    private ImageView imageViewSinMensaje;
    private RecyclerView recyclerViewChats;
    AdapterChatsNegocios adapterChatsNegocios;
    ArrayList<ChatVistaPrevia> listChats;
    ChatVistaPrevia objChatVistaPrevia;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats_negocio);

        idLugar = getIntent().getStringExtra("idLugar").toString();
        nombre = getIntent().getStringExtra("nombre").toString();

        //setup recyclerViewChats
        recyclerViewChats = (RecyclerView) findViewById(R.id.recyclerViewChatsNegocio);
        tvSinMensajes = (TextView) findViewById(R.id.tvSinMensaje);
        imageViewSinMensaje = (ImageView) findViewById(R.id.imagenSinMensaje);

        listChats = new ArrayList<>();
        recyclerViewChats.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        adapterChatsNegocios = new AdapterChatsNegocios(getApplicationContext(),listChats);
        recyclerViewChats.setAdapter(adapterChatsNegocios);

        adapterChatsNegocios.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView tvIdChat,tvIdLugar,tvIdUsuario;
                tvIdChat  = (TextView)view.findViewById(R.id.tvIdChat);
                tvIdLugar = (TextView)view.findViewById(R.id.tvIdLugarChat);
                tvIdUsuario = (TextView)view.findViewById(R.id.tvIdUsuarioChat);
                Intent i = new Intent(getApplicationContext(),ChatActivityNegocio.class);
                i.putExtra("idChat",tvIdChat.getText().toString());
                i.putExtra("idLugar",tvIdLugar.getText().toString());
                i.putExtra("nombre",nombre);
                i.putExtra("otherUser",tvIdUsuario.getText().toString());
                startActivity(i);
                //mandamos el idChat
            }
        });

        llenarRecyclerView();

    }

    String idChat;
    int position=0;
    ArrayList<String> listIdChats;

    private void llenarRecyclerView() {
        listIdChats = new ArrayList<>();

        db.collection(Lugar.COLLECTION_LUGARES).document(idLugar).collection(Lugar.COLLECTION_CHATS).get().
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
                                    Log.i(TAG,"si hay chats guardados ");
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

    private void obtenerInformacionChats(ArrayList<String> listIdChats) {
        for(int i=0; i<listIdChats.size();i++)
        {
            final String idChat = listIdChats.get(i).toString();

            db.collection(Chat.COLLECTION_CHAT).document(idChat).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                if (task.getResult().exists()) {
                                    //Recuperamos el id del Usuario que tiene el chat
                                    final ChatIds objChatIds;
                                    objChatIds = task.getResult().toObject(ChatIds.class);

                                    db.collection(Usuario.COLLECTION_USER).document(objChatIds.getIdUsuario())
                                            .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            //Obtenemos el id , nombre usario y foto del usuario
                                            final Usuario objUsuario;
                                            objUsuario = documentSnapshot.toObject(Usuario.class);


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
                                                                    objChatVistaPrevia.setNombre(objUsuario.getName());
                                                                    objChatVistaPrevia.setIdLugar(idLugar);
                                                                    objChatVistaPrevia.setUrlImagen(objUsuario.getPhotoUrl());
                                                                    objChatVistaPrevia.setMensaje(objChat.getMessage());
                                                                    objChatVistaPrevia.setFecha(objChat.getTimestamp());
                                                                    objChatVistaPrevia.setIdUsuario(objUsuario.getUid());

                                                                    listChats.add(objChatVistaPrevia);

                                                                    Collections.sort(listChats);

                                                                    adapterChatsNegocios.notifyDataSetChanged();
                                                                    //Log.i(TAG,listChats.get(position).getData());
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
