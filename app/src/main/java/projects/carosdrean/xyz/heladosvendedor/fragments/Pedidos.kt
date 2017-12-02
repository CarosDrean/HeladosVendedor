package projects.carosdrean.xyz.heladosvendedor.fragments


import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.*

import projects.carosdrean.xyz.heladosvendedor.R
import projects.carosdrean.xyz.heladosvendedor.adapters.AdapterPedido
import projects.carosdrean.xyz.heladosvendedor.entidad.Peticiones
import java.util.*
import kotlin.collections.ArrayList


/**
 * A simple [Fragment] subclass.
 */
class Pedidos : Fragment() {

    var lista: ArrayList<Peticiones>? = null
    var listaPeticiones: RecyclerView? = null
    var adapter: AdapterPedido? = null
    var contexto: Context? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val v = inflater!!.inflate(R.layout.fragment_pedidos, container, false)
        inicializar(v)
        val llm = LinearLayoutManager(context)
        llm.orientation = LinearLayoutManager.VERTICAL
        listaPeticiones!!.setHasFixedSize(true)
        listaPeticiones!!.layoutManager = llm
        return v
    }

    override fun onResume() {
        super.onResume()
        ids()
    }

    fun inicializar(v: View){
        listaPeticiones = v.findViewById(R.id.reciclador_pedidos)
        contexto = context
        lista = ArrayList()
    }

    fun inicializarAdaptador() {
        adapter = AdapterPedido(lista!!, contexto!!)
        listaPeticiones?.adapter = adapter
    }

    fun ids(){
        var contador = 0
        val lista = ArrayList<String>()
        val dbIds = FirebaseDatabase.getInstance().reference
                .child("Peticiones")
        dbIds.addChildEventListener(object : ChildEventListener{
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onChildMoved(p0: DataSnapshot?, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot?, p1: String?) {
            }

            override fun onChildAdded(p0: DataSnapshot?, p1: String?) {
                lista.add(p0?.key.toString())
                //Toast.makeText(context, "ver" + p0?.key, Toast.LENGTH_LONG).show()
                datos(lista[contador])
                contador++
            }

            override fun onChildRemoved(p0: DataSnapshot?) {
            }

        })
    }

    fun datos(id: String){
        lista?.clear()
        val dbPeticion = FirebaseDatabase.getInstance().reference
                .child("Peticiones")
                .child(id)

        dbPeticion.addChildEventListener(object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError?) {
            }

            override fun onChildMoved(p0: DataSnapshot?, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot?, p1: String?) {
            }

            override fun onChildAdded(p0: DataSnapshot?, p1: String?) {
                val peticion = Peticiones(p0?.child("id")?.value.toString(),
                        id,
                        p0?.child("nombre")?.value.toString(),
                        p0?.child("telefono")?.value.toString(),
                        LatLng(p0?.child("direccion")?.child("latitude")?.value.toString().toDouble(),
                                p0?.child("direccion")?.child("longitude")?.value.toString().toDouble()),
                        p0?.child("ids")?.getValue(object : GenericTypeIndicator<ArrayList<String>>(){})!!,
                        p0?.child("portada")?.value.toString(),
                        p0?.child("productos")?.value.toString())
                lista!!.add(peticion)
                inicializarAdaptador()
            }

            override fun onChildRemoved(p0: DataSnapshot?) {
            }

        })
    }
}// Required empty public constructor
