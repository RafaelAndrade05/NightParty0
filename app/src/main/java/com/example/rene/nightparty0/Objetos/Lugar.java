package com.example.rene.nightparty0.Objetos;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.Date;

public class Lugar implements Comparable<Lugar>,Serializable {

    final public static String NIGTHPARTY_REFERENCE = "nightParty";
    final public static String COLLECTION_LUGARES = "lugares";
    final public static String COLLECTION_IMAGENES = "imagenes";
    public static final String COLLECTION_CHATS = "chats";
    public static final String CAMPO_ID = "id";
    public static final String CAMPO_NOMBRE = "nombre";
    public static final String CAMPO_CATEGORIA = "categoria";
    public static final String CAMPO_UBICACION = "ubicacion";
    public static final String CAMPO_DESCRIPCION = "descripcion";
    public static final String CAMPO_NUM_RATING = "numRatings";
    public static final String CAMPO_PROMEDIO_RATING = "promedioRating";
    public static final String CAMPO_DIRECCION = "direccion";
    public static final String CAMPO_TELEFONO = "numeroTelefono";
    public static final String CAMPO_DIRECCIONWEB = "direccionSitioWeb";
    public static final String CAMPO_LATITUD = "latitud";
    public static final String CAMPO_LONGITUD = "longitud";
    public static final String CAMPO_UID = "uid";
    public static final String CAMPO_ATRIBUTOS = "atributos";
    public static final String CAMPO_DISPONIBILIDAD = "disponibilidad";
    public static final String CAMPO_DATE = "timestamp";

    private String id;
    private String nombre;
    private String categoria;
    private String descripcion;
    private String ubicacion;
    private int disponibilidad; //0=baja , 1=Media , 2=Alta
    private int numRatings;
    private double promedioRating;
    private String direccion;
    private String numeroTelefono;
    private Uri direccionSitioWeb;
    private Double latitud;
    private Double longitud;
    private String uid;
    private String atributos;
    private Double distancia; //nos ayudara a almacenar la distancia que tiene el usuario con el lugar
    private Date timestamp;

    public Lugar() {

    }

    public Lugar(String id, String nombre, String categoria, String descripcion, String ubicacion, int disponibilidad, int numRatings, double promedioRating, String direccion, String numeroTelefono, Uri direccionSitioWeb, Double latitud, Double longitud, String uid, String atributos, Double distancia, Date timestamp) {
        this.id = id;
        this.nombre = nombre;
        this.categoria = categoria;
        this.descripcion = descripcion;
        this.ubicacion = ubicacion;
        this.disponibilidad = disponibilidad;
        this.numRatings = numRatings;
        this.promedioRating = promedioRating;
        this.direccion = direccion;
        this.numeroTelefono = numeroTelefono;
        this.direccionSitioWeb = direccionSitioWeb;
        this.latitud = latitud;
        this.longitud = longitud;
        this.uid = uid;
        this.atributos = atributos;
        this.distancia = distancia;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public int getDisponibilidad() {
        return disponibilidad;
    }

    public void setDisponibilidad(int disponibilidad) {
        this.disponibilidad = disponibilidad;
    }

    public int getNumRatings() {
        return numRatings;
    }

    public void setNumRatings(int numRatings) {
        this.numRatings = numRatings;
    }

    public double getPromedioRating() {
        return promedioRating;
    }

    public void setPromedioRating(double promedioRating) {
        this.promedioRating = promedioRating;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getNumeroTelefono() {
        return numeroTelefono;
    }

    public void setNumeroTelefono(String numeroTelefono) {
        this.numeroTelefono = numeroTelefono;
    }

    public Uri getDireccionSitioWeb() {
        return direccionSitioWeb;
    }

    public void setDireccionSitioWeb(Uri direccionSitioWeb) {
        this.direccionSitioWeb = direccionSitioWeb;
    }

    public Double getLatitud() {
        return latitud;
    }

    public void setLatitud(Double latitud) {
        this.latitud = latitud;
    }

    public Double getLongitud() {
        return longitud;
    }

    public void setLongitud(Double longitud) {
        this.longitud = longitud;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getAtributos() {
        return atributos;
    }

    public void setAtributos(String atributos) {
        this.atributos = atributos;
    }

    public Double getDistancia() {
        return distancia;
    }

    public void setDistancia(Double distancia) {
        this.distancia = distancia;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    //metodo para ordenar por distancia
    @Override
    public int compareTo(@NonNull Lugar lugar) {
        if(lugar.getDistancia()>distancia){
            return -1;
        }else if(lugar.getDistancia()>distancia){
            return 0;
        }else{
            return 1;
        }
    }
}