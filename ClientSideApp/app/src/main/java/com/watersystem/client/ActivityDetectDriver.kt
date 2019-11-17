package com.watersystem.client

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Camera
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.widget.Toast
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQueryEventListener
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.CameraPosition

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_detect_driver.*
import kotlin.concurrent.thread
import kotlin.math.absoluteValue
import kotlin.math.sqrt

class ActivityDetectDriver : AppCompatActivity(),
        OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        com.google.android.gms.location.LocationListener{


    private val stationsLocations = mutableListOf<Locations>()
    private val stationInfoList = mutableListOf<Inventory>()

    private var currentUser   = FirebaseAuth.getInstance().currentUser!!.uid

    private var x = FirebaseAuth.getInstance().currentUser!!.uid
    private lateinit var geoFire: GeoFire
    private lateinit var initGeoFire: GeoFire

    private lateinit var mDatabase : DatabaseReference
    private lateinit var mStationDatabase: DatabaseReference
    private lateinit var customerDatabase : DatabaseReference
    private lateinit var driverLocationRef: DatabaseReference


    private lateinit var mMap: GoogleMap
    private lateinit var googleApiClient: GoogleApiClient
    private var lastLocation: Location? = null
    private lateinit var cameraLatlng: LatLng
    private lateinit var locationRequest: LocationRequest

    private lateinit var customerPickUpLocation: LatLng
    private var radius = 1.0
    private var driverFound = false
    private var driverFoundID = ""

    private var bottle1Orders = 0
    private var bottle2Orders = 0
    private var bottle3Orders = 0
    private var bottle4Orders = 0
    private var bottle5Orders = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detect_driver)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        val bundle = intent.extras
        bottle1Orders = bundle.getInt("bottle1Capacity")
        bottle2Orders = bundle.getInt("bottle2Capacity")
        bottle3Orders = bundle.getInt("bottle3Capacity")
        bottle4Orders = bundle.getInt("bottle4Capacity")
        bottle5Orders = bundle.getInt("bottle5Capacity")

        mStationDatabase = FirebaseDatabase.getInstance().getReference("wms-database").child("STATION")
        mDatabase = FirebaseDatabase.getInstance().getReference("wms-database").child("USERS")
        customerDatabase = FirebaseDatabase.getInstance().getReference("wms-database").child("ORDERREQUESTS")
        driverLocationRef = FirebaseDatabase.getInstance().getReference("wms-database").child("STATIONLOCATIONS")


        getStationsInfo()


        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        Log.i("classssssssss",currentUser)
        geoFire = GeoFire(customerDatabase)
        initGeoFire = GeoFire(mDatabase)

        moveCameraToCurrentLocation()


        btn_placeOrder.setOnClickListener {
            mMap.clear()
//            geoFire.setLocation(currentUser, GeoLocation(lastLocation!!.latitude, lastLocation!!.longitude), object: GeoFire.CompletionListener{
//                override fun onComplete(key: String?, error: DatabaseError?) {
//
//                }
//
//            })

            geoFire.setLocation(currentUser, GeoLocation(mMap.cameraPosition.target.latitude, mMap.cameraPosition.target.longitude), object: GeoFire.CompletionListener{
                override fun onComplete(key: String?, error: DatabaseError?) {

                }

            })

//            customerDatabase.child(currentUser).child("location").child("0").setValue(lastLocation!!.latitude)
//            customerDatabase.child(currentUser).child("location").child("1").setValue(lastLocation!!.longitude)

//            customerPickUpLocation = LatLng(lastLocation!!.latitude, lastLocation!!.longitude)
            customerPickUpLocation = mMap.cameraPosition.target
            mMap.addMarker(MarkerOptions().position(customerPickUpLocation).title("Your Location"))

            btn_placeOrder.isEnabled = false
            btn_placeOrder.text = "finding nearest supplier.."
            runOnUiThread {
                radius=0.0
                getClosestSupplier()
            }
        }

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        buildGoogleApiClient()
    }

    @SuppressLint("MissingPermission")
    override fun onConnected(p0: Bundle?) {
        locationRequest = LocationRequest()
        locationRequest.interval = 1000
        locationRequest.fastestInterval = 1000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this)
    }

    override fun onConnectionSuspended(p0: Int) {
        Toast.makeText(this,"Connection disconnected",Toast.LENGTH_LONG).show()
    }

    override fun onLocationChanged(location: Location?) {
        lastLocation = location!!
        cameraLatlng = mMap.cameraPosition.target
        Log.i("fvbhefvhbefv",mMap.cameraPosition.target.latitude.toString())
        Log.i("fvbhefvhbefv",mMap.cameraPosition.target.longitude.toString())

//
//        val latLng = LatLng(location.latitude, location.longitude)
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
//        mMap.animateCamera(CameraUpdateFactory.zoomTo(18f))
//        mMap.addMarker(MarkerOptions().position(latLng).title("Your Location"))

        initGeoFire.setLocation(currentUser, GeoLocation(mMap.cameraPosition.target.latitude, mMap.cameraPosition.target.longitude), object: GeoFire.CompletionListener{
            override fun onComplete(key: String?, error: DatabaseError?) {

            }

        })

//        mDatabase.child(currentUser).child("location").child("0").setValue(location.latitude)
//        mDatabase.child(currentUser).child("location").child("1").setValue(location.longitude)


//        geoFire.setLocation(currentUser, GeoLocation(location.latitude, location.longitude))

    }

    private fun getClosestSupplier() {
//        Log.i("bjdsvdbdsvbj00", "sckldnsvj v")

        val geoFire1 = GeoFire(driverLocationRef)
        val geoQuery = geoFire1.queryAtLocation(GeoLocation(customerPickUpLocation.latitude,customerPickUpLocation.longitude), radius)

        Log.i("bjdsvdbdsvbj0", driverFoundID)
        geoQuery.removeAllListeners()
        geoQuery.addGeoQueryEventListener(object : GeoQueryEventListener {
            override fun onGeoQueryReady() {
                if(!driverFound){
                    radius++
                    Log.i("bjdsvdbdsvbj2", radius.toString())
                    if(radius<50) {
                        getClosestSupplier()
                    }else{
                        //calculate nearest supplier
                        showNearestSupplier()
                    }
                }
            }

            override fun onKeyEntered(key: String?, location: GeoLocation?) {
                Log.i("bjdsvdbdsvbj11", "key is: $key")
//                if(stationsLocations.size!=0){
                    if(!isKeyAlreadyPresent(key!!)){
                        stationsLocations.add(Locations(key, location!!.latitude, location.longitude))
                    }
//                }
//                if(!driverFound){
//                    driverFound = true
//                    driverFoundID = key!!
//                    val ltlng = LatLng(location!!.latitude,location.longitude)
//                    mMap.addMarker(MarkerOptions().position(ltlng).title("here is your bloody supplier"))
//                }
            }

            override fun onKeyMoved(key: String?, location: GeoLocation?) {

            }

            override fun onKeyExited(key: String?) {

            }

            override fun onGeoQueryError(error: DatabaseError?) {

            }
        })
    }

    private fun showNearestSupplier() {
        var ltLng = mMap.cameraPosition.target
        val locationDifferences = mutableListOf<Double>()

        var minIndex = 0
        var min = 100000.0
        for(loc in stationsLocations){

            //apply formula to find distance between two points
            val sum =
                    sqrt(((ltLng.latitude - loc.latitude)*(ltLng.latitude - loc.latitude))
                    + ((ltLng.longitude - loc.longitude)*(ltLng.longitude - loc.longitude)))
                    //one which has least sum will be the closest supplier

            locationDifferences.add(sum)

            Log.i("bbbbbbbbbbbbbb", sum.toString())
            if(sum<min){
                min = sum
                minIndex = locationDifferences.size-1
            }

//            if(loc.latitude<ltLng.latitude && loc.longitude<ltLng.longitude){
//                ltLng = LatLng(loc.latitude,loc.longitude)
//            }
        }

        if(!hasSupplierEnoughInventory(stationsLocations[minIndex].key)){
//            Toast.makeText(this, stationsLocations[minIndex].key+" don't has enough inventory ",Toast.LENGTH_LONG).show()
            stationsLocations.removeAt(minIndex)
            getClosestSupplier()
            return
        }
        ltLng = LatLng(stationsLocations[minIndex].latitude, stationsLocations[minIndex].longitude)
        mMap.addMarker(MarkerOptions().position(ltLng).title(stationsLocations[minIndex].key))
        btn_placeOrder.text = "SUPPLIER FOUND"
        btn_confirmSupplier.isEnabled = true
        btn_confirmSupplier.setOnClickListener {
            val intent = Intent(this, ActivityConfirmOrder::class.java)

            intent.putExtra("bottle1Capacity", bottle1Orders)
            intent.putExtra("bottle2Capacity", bottle2Orders)
            intent.putExtra("bottle3Capacity", bottle3Orders)
            intent.putExtra("bottle4Capacity", bottle4Orders)
            intent.putExtra("bottle5Capacity", bottle5Orders)
            intent.putExtra("stationID", stationsLocations[minIndex].key)

            startActivity(intent)
        }

//        if(hasSupplierEnoughInventory(stationsLocations[minIndex].key)) {
//        }else{
//            stationsLocations.removeAt(minIndex)
//            showNearestSupplier()
//        }

    }

    private fun hasSupplierEnoughInventory(minKey: String): Boolean{
        for(stationInfo in stationInfoList){
            if(minKey == stationInfo.stationID){
                return ((bottle1Orders <= stationInfo.bottle1) &&
                        (bottle2Orders <= stationInfo.bottle2) &&
                        (bottle3Orders <= stationInfo.bottle3) &&
                        (bottle4Orders <= stationInfo.bottle4) &&
                        (bottle5Orders <= stationInfo.bottle5)
                        )
            }
        }
        return false
    }

    private fun isKeyAlreadyPresent(key: String): Boolean{
        for (locKey in stationsLocations){
            if(locKey.key == key){
                return true
            }
        }
        return false
    }

    private fun buildGoogleApiClient(){
        googleApiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener({})
                .addApi(LocationServices.API)
                .build()

        googleApiClient.connect()
    }

    private fun moveCameraToCurrentLocation(){
        thread{
            while(true){
                if(lastLocation != null){
                    runOnUiThread {
                        val latLng = LatLng(lastLocation!!.latitude, lastLocation!!.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(18f))
//                        mMap.addMarker(MarkerOptions().position(latLng).title("Your Location"))
                    }
                    break
                }
            }


        }
    }

    private fun getStationsInfo(){
        mStationDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(snap: DatabaseError?) {

                Log.i("errorrrrrrrrrrr",snap!!.message)
            }

            override fun onDataChange(snap: DataSnapshot?) {


                for(h in snap!!.children){
                    Log.i("errorrrrrrrrrrr",h.key.toString())

                    stationInfoList.add(Inventory(
                            h.key.toString(),
                            h.child("INVENTORY").child("bottle1").child("quantity").value.toString().toInt(),
                            h.child("INVENTORY").child("bottle2").child("quantity").value.toString().toInt(),
                            h.child("INVENTORY").child("bottle3").child("quantity").value.toString().toInt(),
                            h.child("INVENTORY").child("bottle4").child("quantity").value.toString().toInt(),
                            h.child("INVENTORY").child("bottle5").child("quantity").value.toString().toInt()
                    ))
                }

//                stationInfoList.add(Inventory(
//                        snap.key.toString(),
//                        snap.child("inventory").child("bottle1").value.toString().toInt(),
//                        snap.child("inventory").child("bottle2").value.toString().toInt(),
//                        snap.child("inventory").child("bottle3").value.toString().toInt(),
//                        snap.child("inventory").child("bottle4").value.toString().toInt(),
//                        snap.child("inventory").child("bottle5").value.toString().toInt(),
//                        snap.child("inventory").child("bottle6").value.toString().toInt()
//                ))
            }
        })
    }

}

























