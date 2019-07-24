package com.example.rene.nightparty0.FragmentsDetalles;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.rene.nightparty0.Ayuda.Ubicacion;
import com.example.rene.nightparty0.Objetos.Lugar;
import com.example.rene.nightparty0.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class FragmentUbicacion extends android.support.v4.app.Fragment implements OnMapReadyCallback {

    View view;

    GoogleMap mMap;

    TextView tvDistancia;
    TextView tvDireccion;

    public FragmentUbicacion() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null)
                parent.removeView(view);
        }
        try {
            view = inflater.inflate(R.layout.ubicacion_fragment,container,false);

            tvDistancia = (TextView)view.findViewById(R.id.detalle_distancia_usuario);
            tvDireccion = (TextView)view.findViewById(R.id.detalle_direccion);

            GoogleMapOptions options = new GoogleMapOptions();
            options.liteMode(true); //Mapa en modo lite (imagen estatica de googlemaps)



            //obtencion de mapa
            FragmentManager manager = getFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            //new instance inserta las options en el fragment que tiene el mapa en modo lite
            SupportMapFragment fragment = SupportMapFragment.newInstance(options);
            transaction.add(R.id.mapDetalles2, fragment);
            transaction.commit();

            fragment.getMapAsync(this);


            /*
            MapFragment mapFragment = (MapFragment)getActivity().getFragmentManager().findFragmentById(R.id.mapDetalles2);
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    mMap = googleMap;
                    llenarInformacion();
                }
            });
            */

        } catch (InflateException e) {
            /* map is already there, just return view as it is */
        }

        return view;
    }

    public void llenarInformacion(){
        Log.i("FRAGMENTS", "Llenando informacion FRAGMENT UBICACION");

        Bundle args = getArguments();
        Lugar objLugar = (Lugar)args.getSerializable("objLugar");
        String distancia = args.getString("distancia");

        tvDireccion.setText(objLugar.getDireccion());

        if(!distancia.equals("0.0")) {
            tvDistancia.setText(distancia + " " + getString(R.string.detalle_distancia_ubicacion));
        }

        moveCamera(new LatLng(objLugar.getLatitud(),objLugar.getLongitud()),Ubicacion.LESS_ZOOM,objLugar.getNombre());
    }



    private void moveCamera(LatLng latLng, float zoom,String title){
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoom));

        MarkerOptions marker = new MarkerOptions().position(latLng).title(title);
        mMap.addMarker(marker);
        // Set the map type back to normal.
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        llenarInformacion();
    }
}
