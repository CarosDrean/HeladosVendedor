package projects.carosdrean.xyz.heladosvendedor

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.transition.Explode
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_detalle_pedido.*
import kotlinx.android.synthetic.main.content_detalle_pedido.*
import projects.carosdrean.xyz.heladosvendedor.adapters.AdapterDetallePedido
import projects.carosdrean.xyz.heladosvendedor.db.BaseDatos
import projects.carosdrean.xyz.heladosvendedor.entidad.Pedido
import projects.carosdrean.xyz.heladosvendedor.entidad.Producto
import java.io.File
import java.io.IOException

class DetallePedido : AppCompatActivity() {

    var lista: ArrayList<Pedido>? = null
    var listaPeticiones: RecyclerView? = null
    var adapter: AdapterDetallePedido? = null
    var contexto: Context? = null
    var llamar: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_pedido)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = intent.getStringExtra("nombre")
        inicializar()
        val llm = LinearLayoutManager(this)
        llm.orientation = LinearLayoutManager.VERTICAL
        listaPeticiones!!.setHasFixedSize(true)
        listaPeticiones!!.layoutManager = llm
        contenedor_llamar.setOnClickListener { llamar() }
        direccion()
        ver_direccion.setOnClickListener {
            //verUbicacion()
            Toast.makeText(this, "¡No disponible!", Toast.LENGTH_SHORT).show()
        }
        fab_detalle_pedido.setOnClickListener {
            eliminarPeticion(intent.getStringExtra("id"), intent.getStringExtra("idUser"))
            Toast.makeText(this, "Petición cumplida.", Toast.LENGTH_SHORT).show()
            onBackPressed()
        }
        obtenerPortada()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        val ids = intent.getStringArrayListExtra("ids")
        for (i in ids){
            datos(i, intent.getStringExtra("idUser"))
        }
        inicializarAdaptador()
    }

    fun eliminarPeticion(id: String, idUser: String){
        val db = BaseDatos(contexto!!)
        db.eliminarPeticion(id, idUser)
    }

    fun verUbicacion(){
        val builder = AlertDialog.Builder(this)
        val inflater: LayoutInflater = layoutInflater
        val v: View = inflater.inflate(R.layout.ver_ubicacion, null)
        builder.setView(v)
                .setTitle("Ubicación de comprador")
                .setPositiveButton("Aceptar"){ dialog, which ->

                }
        val alert = builder.create()
        alert.show()
    }

    fun direccion(){
        val db = BaseDatos(contexto!!)
        val bundle: Bundle = intent.getParcelableExtra("bundle")
        db.subirDireccion(bundle.getParcelable("ubicacion"))
    }

    fun inicializar(){
        listaPeticiones = findViewById(R.id.pedidos)
        llamar = findViewById(R.id.llamar_detalle_pedido)
        llamar?.text = intent.getStringExtra("telefono")
        contexto = this
        lista = ArrayList()
    }

    fun inicializarAdaptador() {
        adapter = AdapterDetallePedido(lista!!, contexto!!)
        listaPeticiones?.adapter = adapter
    }

    fun llamar(){
        val i = Intent(Intent.ACTION_CALL, Uri.parse("tel:" + intent.getStringExtra("telefono")))
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
            return
        }
        startActivity(i)
    }

    fun obtenerPortada(){
        val storageReference = FirebaseStorage.getInstance().reference
        val portada = storageReference.child("Imagenes").child(intent.getStringExtra("portada"))
        try {
            val localFile = File.createTempFile("images", "jpg")
            portada.getFile(localFile).addOnSuccessListener {
                Glide.with(this).load(localFile).into(portada_pedido)
                /*portada_pedido.setOnClickListener { view ->
                    val i = Intent(this, DetalleFoto::class.java)
                    i.putExtra("foto", intent.extras.getString("portada"))
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                        val explode = Explode()
                        explode.duration = 300
                        window.exitTransition = explode
                        startActivity(i, ActivityOptionsCompat.makeSceneTransitionAnimation(
                                this, view, getString(R.string.trancicionFoto)).toBundle()
                        )
                    }else{
                        startActivity(i)
                    }
                }*/
            }.addOnFailureListener {
                // Handle any errors
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun datos(id: String, idUser: String){
        lista?.clear()
        var contador = 0
        val dbPeticion = FirebaseDatabase.getInstance().reference
                .child("Pedidos")
                .child(idUser)
                .child(id)
        dbPeticion.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(p0: DataSnapshot?) {
                val pedido = Pedido(p0?.child("ids")?.value.toString(),
                        p0?.child("portada")?.value.toString(),
                        p0?.child("nombre")?.value.toString(),
                        p0?.child("precio")?.value.toString(),
                        p0?.child("cantidad")?.value.toString())
                lista!!.add(pedido)
                //Toast.makeText(contexto, "ver" + contador, Toast.LENGTH_SHORT).show()
                contador++
            }

        })

    }
}
