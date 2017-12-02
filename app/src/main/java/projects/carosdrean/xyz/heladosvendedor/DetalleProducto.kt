package projects.carosdrean.xyz.heladosvendedor

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityOptionsCompat
import android.support.v7.app.AppCompatActivity
import android.transition.Explode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_detalle_producto.*
import java.io.File
import java.io.IOException
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import projects.carosdrean.xyz.heladosvendedor.db.BaseDatos


class DetalleProducto : AppCompatActivity() {

    var portada: ImageView? = null
    var descripcion: TextView? = null
    var precio: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_producto)
        setSupportActionBar(toolbar)
        fab.setOnClickListener { view ->
            editarProducto(intent.getStringExtra("ids"),
                    intent.getStringExtra("nombre"),
                    intent.getStringExtra("precio"),
                    intent.getStringExtra("descripcion"),
                    intent.getStringExtra("categoria"),
                    intent.getStringExtra("portada"))
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = intent.getStringExtra("nombre")
        inicializar()
        reemplazar()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_detalle_producto, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_eliminar -> {
                val db = BaseDatos(this)
                db.eliminarProducto(intent.getStringExtra("ids"), intent.getStringExtra("categoria"))
                db.eliminarSeleccion(intent.getStringExtra("ids"))
                finish()
                return true
            }
            android.R.id.home -> {
                finish()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    fun editarProducto(id: String, nombre: String, precio: String, descripcion: String, categoria: String, portada: String){
        val i = Intent(this, AgregarProducto::class.java)
        i.putExtra("ids", id)
        i.putExtra("categoria", categoria)
        i.putExtra("portada", portada)
        i.putExtra("nombre", nombre)
        i.putExtra("precio",precio)
        i.putExtra("descripcion", descripcion)
        startActivity(i)
        finish()
    }

    fun detalleFoto(v: View){
        val i = Intent(this, DetalleFoto::class.java)
        i.putExtra("foto", intent.getStringExtra("portada"))
        startActivity(i)
    }

    fun inicializar(){
        portada = findViewById(R.id.portada_dp)
        descripcion = findViewById(R.id.descripcion_dp)
        precio = findViewById(R.id.precio_dp)
    }

    fun reemplazar(){
        descripcion?.text = intent.getStringExtra("descripcion")
        precio?.text = intent.getStringExtra("precio")
        obtenerPortada()
    }

    fun obtenerPortada(){
        val storageReference = FirebaseStorage.getInstance().reference
        val portada = storageReference.child("Imagenes").child(intent.getStringExtra("portada"))
        try {
            val localFile = File.createTempFile("images", "jpg")
            portada.getFile(localFile).addOnSuccessListener {
                Glide.with(this).load(localFile).into(portada_dp)
                portada_dp.setOnClickListener { view ->
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
                }
            }.addOnFailureListener {
                // Handle any errors
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
