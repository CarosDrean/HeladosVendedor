package projects.carosdrean.xyz.heladosvendedor.db

import android.content.ContentValues
import android.content.Context
import com.google.firebase.database.FirebaseDatabase
import android.net.Uri
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.storage.FirebaseStorage
import projects.carosdrean.xyz.heladosvendedor.entidad.Identificador
import projects.carosdrean.xyz.heladosvendedor.entidad.Persona
import projects.carosdrean.xyz.heladosvendedor.entidad.Producto

/**
 * Created by josue on 23/11/2017.
 */
class BaseDatos (context: Context){
    val contexto: Context = context

    fun subirProducto(producto: Producto): String{
        val dbProduct = FirebaseDatabase.getInstance().reference
                .child("Productos")
                .child(producto.categoria)
        val key = dbProduct.push().key
        producto.ids = key
        dbProduct.child(key).setValue(producto)
        return key
    }

    fun subirDireccion(direccion: LatLng){
        val dbDireccion = FirebaseDatabase.getInstance().reference
                .child("Direccion")
        dbDireccion.child(obtenerId()).setValue(direccion)
    }

    fun obtenerId(): String{
        val db = DataBaseSQL(contexto)
        val identificador = Identificador()
        return db.obtenerRegistro(identificador).identificador
    }

    fun agregarSeleccion(producto: Producto, id: String){
        val dbProduct = FirebaseDatabase.getInstance().reference
                .child("Productos")
                .child("Seleccion")
        producto.ids = id
        dbProduct.child(id).setValue(producto)
    }

    fun eliminarSeleccion(id: String){
        val dbProducto = FirebaseDatabase.getInstance().reference
                .child("Productos")
                .child("Seleccion")
                .child(id)
        dbProducto.removeValue()
    }

    fun eliminarPeticion(id: String, idUser: String){
        val dbProducto = FirebaseDatabase.getInstance().reference
                .child("Peticiones")
                .child(idUser)
                .child(id)
        dbProducto.removeValue()
    }

    fun actualizarProducto(producto: Producto){
        val dbProduct = FirebaseDatabase.getInstance().reference
                .child("Productos")
                .child(producto.categoria)
                .child(producto.ids)
        dbProduct.child("nombre").setValue(producto.nombre)
        dbProduct.child("precio").setValue(producto.precio)
        dbProduct.child("descripcion").setValue(producto.descripcion)
        dbProduct.child("categoria").setValue(producto.categoria)
        dbProduct.child("portada").setValue(producto.portada)
    }

    fun eliminarProducto(id: String, categoria: String){
        val dbProducto = FirebaseDatabase.getInstance().reference
                .child("Productos")
                .child(categoria)
                .child(id)
        dbProducto.removeValue()
    }

    fun subirFoto(uri: Uri){
        val stReference = FirebaseStorage.getInstance().reference
                .child("Imagenes")
                .child(uri.lastPathSegment)
        stReference.putFile(uri).addOnSuccessListener {
            //Toast.makeText(activity, "¡La imagen se subio con exito!", Toast.LENGTH_SHORT).show()
        }
    }

    fun guardarDatos(persona: Persona){
        val dbPersona = FirebaseDatabase.getInstance().reference
                .child("Personas")
        val key = dbPersona.push().key
        persona.ids = key
        dbPersona.child(key).setValue(persona)
        guardarId(key)
    }

    fun guardarId(id: String){
        val db = DataBaseSQL(contexto)
        val content = ContentValues()
        content.put("id", id)
        content.put("ids", id)
        db.ingresarRegistro(content)
    }
}