package projects.carosdrean.xyz.heladosvendedor.adapters

import android.app.Activity
import android.content.Context
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
import projects.carosdrean.xyz.heladosvendedor.R
import projects.carosdrean.xyz.heladosvendedor.entidad.Pedido
import java.io.File
import java.io.IOException

/**
 * Created by josue on 27/11/2017.
 */
class AdapterDetallePedido(private val items: List<Pedido>, private val activity: Context) : RecyclerView.Adapter<AdapterDetallePedido.ViewHolder>() {
    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent?.context)
                .inflate(R.layout.item_pedidos, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        val storageReference = FirebaseStorage.getInstance().reference
        val pedido = items[position]
        holder?.llamar?.visibility = View.INVISIBLE
        holder?.nombre?.text = pedido.nombre
        holder?.productos?.text = "Cantidad: " + pedido.cantidad + " - Precio Total: S/." + pedido.cantidad.toInt() * pedido.precio.toDouble()
        val portada = storageReference.child("Imagenes").child(pedido.portada)
        try {
            val localFile = File.createTempFile("images", "jpg")
            portada.getFile(localFile).addOnSuccessListener { Glide.with(activity.applicationContext).load(localFile).into(holder?.perfil) }.addOnFailureListener {
                // Handle any errors
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
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