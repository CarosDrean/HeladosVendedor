package projects.carosdrean.xyz.heladosvendedor.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import projects.carosdrean.xyz.heladosvendedor.R
import projects.carosdrean.xyz.heladosvendedor.db.BaseDatos
import projects.carosdrean.xyz.heladosvendedor.db.DataBaseSQL
import projects.carosdrean.xyz.heladosvendedor.entidad.Identificador

/**
 * Created by josue on 28/11/2017.
 */
class Ubicacion : SupportMapFragment(), OnMapReadyCallback, GoogleMap.OnMarkerDragListener {
    private var mMap: GoogleMap? = null
    var marcador: Marker? = null
    var ubicacion: LatLng = LatLng(-14.063585, -75.729179)

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = super.onCreateView(inflater, container, savedInstanceState)

        getMapAsync(this)
        //ubicacion = obtenerUbicacion()
        return rootView
    }

    override fun onMapReady(map: GoogleMap) {
        mMap = map
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        map.isMyLocationEnabled = true
        agregarMarcador(ubicacion)
        map.setOnMarkerDragListener(this)

    }

    private fun agregarMarcador(ub: LatLng) {
        val miUbicacion = CameraUpdateFactory.newLatLngZoom(ub, 16f)
        marcador = mMap!!.addMarker(MarkerOptions()
                .position(ub)
                .title("Mover para seleccionar")
                .draggable(false)
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_casa))
        )
        mMap!!.animateCamera(miUbicacion)
    }

    override fun onMarkerDragStart(marker: Marker) {

    }

    override fun onMarkerDrag(marker: Marker) {

    }

    override fun onMarkerDragEnd(marker: Marker) {

    }

    fun obtenerId(): String{
        val db = DataBaseSQL(context)
        val identificador = Identificador()
        return db.obtenerRegistro(identificador).identificador
    }

    fun obtenerUbicacion(): LatLng{
        var ub: LatLng? = null
        val dbPeticion = FirebaseDatabase.getInstance().reference
                .child("Direccion")
                .child(obtenerId())
        dbPeticion.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(p0: DataSnapshot?) {
                ub = LatLng(p0?.child("latitude")?.value.toString().toDouble(),
                        p0?.child("longitude")?.value.toString().toDouble())
            }

        })
        return ub!!
    }

}