package projects.carosdrean.xyz.heladosvendedor.adapters

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import projects.carosdrean.xyz.heladosvendedor.DetallePedido
import projects.carosdrean.xyz.heladosvendedor.R
import projects.carosdrean.xyz.heladosvendedor.entidad.Peticiones
import java.io.File
import java.io.IOException

/**
 * Created by josue on 27/11/2017.
 */
class AdapterPedido (private val items: List<Peticiones>, private val activity: Context) : RecyclerView.Adapter<AdapterPedido.ViewHolder>(){
    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        val storageReference = FirebaseStorage.getInstance().reference
        val producto = items[position]
        holder?.nombre?.text = producto.nombre
        holder?.productos?.text = producto.productos
        val portada = storageReference.child("Imagenes").child(producto.portada)
        try {
            val localFile = File.createTempFile("images", "jpg")
            portada.getFile(localFile).addOnSuccessListener { Glide.with(activity.applicationContext).load(localFile).into(holder?.perfil) }.addOnFailureListener {
                // Handle any errors
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        holder?.llamar?.setOnClickListener {
            val i = Intent(Intent.ACTION_CALL, Uri.parse("tel:" + producto.telefono))
            if(ActivityCompat.checkSelfPermission(activity, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
                return@setOnClickListener
            }
            activity.startActivity(i)
        }

        holder?.contenedor?.setOnClickListener{
            val args = Bundle()
            args.putParcelable("ubicacion", producto.direccion)
            val i = Intent(activity, DetallePedido::class.java)
            i.putExtra("id", producto.id)
            i.putExtra("idUser", producto.idUser)
            i.putExtra("bundle", args)
            i.putExtra("nombre", producto.nombre)
            i.putExtra("telefono", producto.telefono)
            i.putExtra("portada", producto.portada)
            i.putExtra("productos", producto.productos)
            i.putExtra("direccion", producto.direccion)
            i.putExtra("ids", producto.ids)
            activity.startActivity(i)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent?.context)
                .inflate(R.layout.item_pedidos, parent, false)
        return ViewHolder(v)
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        // Campos respectivos de un item
        var nombre: TextView
        var productos: TextView
        var perfil: CircleImageView
        var llamar: ImageView
        var contenedor: CardView

        init {
            contenedor = v.findViewById(R.id.contenedor_pedido)
            nombre = v.findViewById(R.id.nombre_pedido)
            productos = v.findViewById(R.id.productos_pedido)
            perfil = v.findViewById(R.id.iv_pedido)
            llamar = v.findViewById(R.id.llamar_pediodo)
        }
    }
}