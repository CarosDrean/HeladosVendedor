package projects.carosdrean.xyz.heladosvendedor

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.app.NotificationCompat
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.SpannableStringBuilder
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import projects.carosdrean.xyz.heladosvendedor.db.BaseDatos
import projects.carosdrean.xyz.heladosvendedor.db.DataBaseSQL
import projects.carosdrean.xyz.heladosvendedor.entidad.Identificador
import projects.carosdrean.xyz.heladosvendedor.entidad.Peticiones
import projects.carosdrean.xyz.heladosvendedor.fragments.Pedidos
import projects.carosdrean.xyz.heladosvendedor.fragments.Productos
import java.util.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    var vHome: View? = null
    var alarmManeger: AlarmManager? = null
    var lista: ArrayList<Peticiones>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab_as.setOnClickListener {
            agregarProducto()
        }

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)
        if(nav_view != null){
            onNavigationItemSelected(nav_view.menu.getItem(0))
        }
        vHome = nav_view.getHeaderView(0)

        if(detectarUsoApp == 0)dialogoBienvenida()
        if(comprobarDatosUser())dialogoBienvenida()
        datosCabecera(vHome!!)
        alarmManeger = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        lista = ArrayList()
        ids()
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_acerca_de -> {
                alertAcercaDe()
                return true
            }
            R.id.action_cambiar_contraseña ->{
                cambiarContraseña()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        var fragment: Fragment? = null
        var fragementManager = false

        when (item.itemId) {
            R.id.nav_productos -> {
                fragementManager = true
                fragment = Productos()
            }
            R.id.nav_pedidos -> {
                fragementManager = true
                fragment = Pedidos()
            }
        }

        if(fragementManager){
            supportFragmentManager.beginTransaction().replace(R.id.content_main, fragment).commit()
            item.isChecked = true
            supportActionBar?.title = item.title
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    fun notificacion(situacion: String, nombre: String, numero: String){
        val notiM = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:" + numero))
        val piDismiss = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val buider = NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(
                        resources,
                        R.mipmap.ic_launcher
                )
                )
                .setContentTitle(nombre)
                .setContentText(situacion)
                .setColor(resources.getColor(R.color.primary_material_dark))
                .setStyle(
                        NotificationCompat.BigTextStyle()
                                .bigText(situacion))
                .addAction(R.drawable.ic_call_black_24dp,
                        "Llamar", piDismiss)
                .setAutoCancel(true)
        buider.setFullScreenIntent(piDismiss, true)
        val notificacion = buider.build()
        notiM.notify(1, notificacion)
    }

    fun ids(){
        var contador = 0
        val lista = ArrayList<String>()
        val dbIds = FirebaseDatabase.getInstance().reference
                .child("Peticiones")
        dbIds.addChildEventListener(object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError?) {
            }
            override fun onChildMoved(p0: DataSnapshot?, p1: String?) {
            }
            override fun onChildChanged(p0: DataSnapshot?, p1: String?) {
            }

            override fun onChildAdded(p0: DataSnapshot?, p1: String?) {
                lista.add(p0?.key.toString())
                datos(lista[contador])
                contador++
                //Toast.makeText(context, "ver: " + p0?.key, Toast.LENGTH_LONG).show()
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
                notificacion(peticion.productos, peticion.nombre, peticion.telefono)
            }

            override fun onChildRemoved(p0: DataSnapshot?) {
            }

        })
    }

    fun agregarProducto(){
        val i = Intent(this, AgregarProducto::class.java)
        startActivity(i)
    }

    fun alertAcercaDe(){
        val builder = android.support.v7.app.AlertDialog.Builder(this)
        builder.setTitle("Acerca de Frutica")
                .setMessage("Somos una nueva empresa de la ciudad de Ica, que les ofrece un servicio de calidad unica.")
        builder.setPositiveButton("Listo"){ dialog, which ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

    fun datosCabecera(v: View){
        val fondo: ImageView = v.findViewById(R.id.fondo_cabecera)
        val perfil: CircleImageView = v.findViewById(R.id.perfil_cabecera)

        Glide.with(this).load(resources.getDrawable(R.drawable.portada)).into(fondo)
        Glide.with(this).load(resources.getDrawable(R.drawable.perfil)).into(perfil)
    }

    fun cambiarContraseña(){
        val builder = AlertDialog.Builder(this@MainActivity)
        builder.setCancelable(false)
        builder.setTitle("Administrador")
                .setCancelable(false)
        val v = layoutInflater.inflate(R.layout.cambiar_contrasena, null)
        builder.setView(v)
        builder.setPositiveButton("Aceptar"){ dialog, which ->

        }
        val dialog = builder.create()
        dialog.show()
    }

    fun dialogoBienvenida(){
        val builder = AlertDialog.Builder(this@MainActivity)
        builder.setCancelable(false)
        builder.setTitle("Bienvenido")
                .setCancelable(false)
        val v = layoutInflater.inflate(R.layout.bienvenida, null)
        val contraseña: EditText = v.findViewById(R.id.contraseña_b)
        builder.setView(v)
        builder.setPositiveButton("Aceptar"){ dialog, which ->
            if(contraseña.text.toString() != ""){
                if(contraseña.text.toString() == getString(R.string.contraseña)){
                    guardarId(UUID.randomUUID().toString())
                    dialog.dismiss()
                }else{
                    Toast.makeText(this, "¡Contraseña invalida, pongase en contacto con un administrador!", Toast.LENGTH_LONG).show()
                    System.exit(3)
                }

            }else{
                Toast.makeText(this, "¡Llene todos los campos!", Toast.LENGTH_LONG).show()
                System.exit(0)
            }

        }
        val dialog = builder.create()
        dialog.show()
    }

    fun guardarId(id: String){
        val db = BaseDatos(this)
        db.guardarId(id)
    }

    fun comprobarDatosUser() : Boolean{
        val db = DataBaseSQL(this)
        val identificador = Identificador()
        return db.obtenerRegistro(identificador).identificador == ""
    }

    private val detectarUsoApp: Int
        get() {
            val sp = getSharedPreferences("MYAPP", 0)
            val result: Int
            val currentVersionCode = BuildConfig.VERSION_CODE
            val lastVersionCode = sp.getInt("FIRSTTIMERUN", -1)
            if (lastVersionCode == -1)
                result = 0
            else
                result = if (lastVersionCode == currentVersionCode) 1 else 2
            sp.edit().putInt("FIRSTTIMERUN", currentVersionCode).apply()
            return result
        }
}
