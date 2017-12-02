package projects.carosdrean.xyz.heladosvendedor

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SwitchCompat
import android.text.Editable
import android.text.SpannableStringBuilder
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView

import kotlinx.android.synthetic.main.activity_agregar_producto.*
import projects.carosdrean.xyz.heladosvendedor.db.BaseDatos
import projects.carosdrean.xyz.heladosvendedor.entidad.Producto
import java.io.File
import java.io.IOException

class AgregarProducto : AppCompatActivity() {

    val SELECT_PICTURE = 1

    private var nombrePortada: String? = "default"
    private var actualizarPortada = false

    private var portada: CircleImageView? = null
    private var nombre: EditText? = null
    private var precio: EditText? = null
    private var descripcion: EditText? = null
    private var helado_pequeno: RadioButton? = null
    private var helado_grande: RadioButton? = null
    private var seleccion: SwitchCompat? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar_producto)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val actionBar = supportActionBar
        actionBar?.setHomeAsUpIndicator(R.mipmap.ic_close)
        inicializar()
        if(intent.getStringExtra("ids") != null){
            actionBar?.title = "Editar Producto"
            llenarDatos()
        }

        subirDatos()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val db = BaseDatos(this)
        if(resultCode == RESULT_OK && requestCode == SELECT_PICTURE){
            val uri: Uri = data!!.data
            Glide.with(this).load(uri).into(portada!!)
            db.subirFoto(uri)
            actualizarPortada = true
            nombrePortada = uri.lastPathSegment
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item?.itemId
        if(id == android.R.id.home){
            onBackPressed()
            return true
        }else{
            return super.onOptionsItemSelected(item)
        }
    }

    fun inicializar(){
        portada = findViewById(R.id.foto_producto_a)
        nombre = findViewById(R.id.nombre_prodcuto_a)
        precio = findViewById(R.id.precio_producto_a)
        descripcion = findViewById(R.id.descripcion_producto_a)
        helado_pequeno = findViewById(R.id.helado_pequeno)
        helado_grande = findViewById(R.id.helado_grande)
        seleccion = findViewById(R.id.nuestra_seleccion)
    }

    fun llenarDatos(){
        nombre?.text = SpannableStringBuilder(intent.getStringExtra("nombre"))
        precio?.text = SpannableStringBuilder(intent.getStringExtra("precio"))
        descripcion?.text = SpannableStringBuilder(intent.getStringExtra("descripcion"))

        if(intent.getStringExtra("categoria") == "Grande"){
            helado_grande?.isChecked = true
            helado_pequeno?.isChecked = false
        }else{
            helado_grande?.isChecked = false
            helado_pequeno?.isChecked = true
        }
        if(!actualizarPortada){
            cargarImagen(intent.getStringExtra("portada"))
        }
    }

    fun cargarImagen(nombre: String){
        val storageReference = FirebaseStorage.getInstance().reference
        val portadaa = storageReference.child("Imagenes").child(nombre)
        try {
            val localFile = File.createTempFile("images", "jpg")
            portadaa.getFile(localFile).addOnSuccessListener { Glide.with(this).load(localFile).into(portada) }.addOnFailureListener {
                // Handle any errors
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun lanzarGalerias(v: View){
        val i = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        i.type = "image/*"
        startActivityForResult(Intent.createChooser(i, "Seleccionar imagen"), SELECT_PICTURE)
    }

    fun subirDatos(){
        val fab: FloatingActionButton = findViewById(R.id.fab_a)
        fab.setOnClickListener {
            if(!nombre?.text.toString().equals("") || !precio?.text.toString().equals("") || !descripcion?.text.toString().equals("")){
                val db = BaseDatos(this)
                var categoria = "Grande"
                if(helado_pequeno?.isChecked!!){
                    categoria = "Pequeño"
                }
                var idGuardado = ""
                var id = "id"
                val estado = intent.getStringExtra("ids") != null
                if(estado){
                    id = intent.getStringExtra("ids")
                    if(!actualizarPortada) nombrePortada = intent.getStringExtra("portada")
                }
                val producto = Producto(id ,nombrePortada!!, nombre?.text.toString(), precio?.text.toString(), descripcion?.text.toString(), categoria)

                if(estado){
                    val categoriaEx = intent.getStringExtra("categoria")
                    if(categoria != categoriaEx) {
                        db.eliminarProducto(id, categoriaEx)
                        eliminarSeleccion(id)
                    }
                    db.actualizarProducto(producto)
                    if(seleccion?.isChecked!!){
                        agregarSeleccion(producto, producto.ids)
                    }else{
                        eliminarSeleccion(producto.ids)
                    }
                }else{
                    idGuardado = db.subirProducto(producto)
                    if(seleccion?.isChecked!!){
                        agregarSeleccion(producto, idGuardado)
                    }
                }
                onBackPressed()
            } else{
                Toast.makeText(this, "¡Por favor llena todos los campos!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun agregarSeleccion(producto: Producto, id: String){
        val db = BaseDatos(this)
        db.agregarSeleccion(producto, id)
    }

    fun eliminarSeleccion(id:String){
        val db = BaseDatos(this)
        db.eliminarSeleccion(id)
    }
}
