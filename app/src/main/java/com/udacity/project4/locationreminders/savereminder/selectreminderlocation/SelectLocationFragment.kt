package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PointOfInterest
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*


private const val REQUEST_FINE_LOCATION_PERMISSION = 33
private const val REQUEST_BACKGROUND_AND_FINE_LOCATION_PERMISSION = 34
private const val LOCATION_PERMISSION_INDEX = 0
private const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1
private const val TAG = "SelectedLocFragment"
private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29


class SelectLocationFragment : BaseFragment(), OnMapReadyCallback, LocationListener {

    private lateinit var map: GoogleMap
    private val runningQOrLater =
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var locationManager: LocationManager
    private val MIN_TIME: Long = 400
    private val MIN_DISTANCE = 300f

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        locationManager =
            getSystemService(requireContext(), LocationManager::class.java) as LocationManager

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Snackbar.make(
            view, getString(R.string.selectLocationPrompt),
            Snackbar.LENGTH_LONG
        ).show()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        styleMyMap(map)
        enableMyLocation()
        onLocationSelected(map)
    }

    private fun onLocationSelected(googleMap: GoogleMap) {

        googleMap.setOnMapLongClickListener {
            map.clear()
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                it.latitude,
                it.longitude
            )
            val marker = googleMap.addMarker(
                MarkerOptions().position(it).snippet(snippet).title("dropped pin")
            )
            marker.showInfoWindow()
            Log.i(TAG, _viewModel.latitude.value.toString())
            _viewModel.reminderSelectedLocationStr.value = "no POI selected"
            _viewModel.longitude.value = it.longitude
            _viewModel.latitude.value = it.latitude
            val poiSelected = PointOfInterest(LatLng(it.latitude,it.longitude),"no POI selected","no POI selected")
            _viewModel.selectedPOI.value = poiSelected

            if (marker != null) {
                navigate()
            }
        }

        googleMap.setOnPoiClickListener {
            map.clear()
            val poiMarker = map.addMarker(
                MarkerOptions().position(it.latLng)
                    .title(it.name)
            )

            poiMarker.showInfoWindow()
            _viewModel.reminderSelectedLocationStr.value = it.name
            _viewModel.longitude.value = it.latLng.longitude
            _viewModel.latitude.value = it.latLng.latitude
            _viewModel.selectedPOI.value = it

            if (poiMarker != null) {
                navigate()
            }
        }

    }

    private fun navigate() {
        _viewModel.navigationCommand.value =
            NavigationCommand.To(
                SelectLocationFragmentDirections.actionSelectLocationFragmentToSaveReminderFragment())
    }

    @SuppressLint("MissingPermission")
    @TargetApi(29)
    private fun enableMyLocation() {
        if ((ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) === PackageManager.PERMISSION_GRANTED)) {
            map.setMyLocationEnabled(true)
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                MIN_TIME,
                MIN_DISTANCE,
                this
            )

        } else {
            val permissionsArray = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
            val reqCode = REQUEST_FINE_LOCATION_PERMISSION
            requestPermissions(
                permissionsArray,
                reqCode
            )
        }
        checkDeviceLocationTurnedOn()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (grantResults.isEmpty() || grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED ||
            requestCode == REQUEST_FINE_LOCATION_PERMISSION) {
            Snackbar.make(
                view!!,
                "You need to grant location permission all the time to use the app!",
                Snackbar.LENGTH_LONG
            )
                .setAction("Settings") {
                    startActivity(Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }.show()
            enableMyLocation()

        } else {
            enableMyLocation()
        }
    }

    override fun onLocationChanged(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15f)
        map.animateCamera(cameraUpdate)
        locationManager.removeUpdates(this)
    }

    fun styleMyMap(map: GoogleMap) {
        try {
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.map_style
                )
            )

        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "can't find style. Error: ", e)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // TODO: Change the map type based on the user's selection.
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)

    }

    private fun checkDeviceLocationTurnedOn(resolve: Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            setPriority(LocationRequest.PRIORITY_LOW_POWER)
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                try {
                    exception.startResolutionForResult(
                        requireActivity(),
                        REQUEST_TURN_DEVICE_LOCATION_ON
                    )
                } catch (sendEX: IntentSender.SendIntentException) {
                    Log.d("MainActivity", "Error getting location settings resolution" + sendEX.message)
                }
            } else {
                Snackbar.make(
                    view!!,
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationTurnedOn()
                }.show()
            }

        }

        locationSettingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful) {
                Log.i("MainActiivity", "congrats")
            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
            checkDeviceLocationTurnedOn(false)
        }
    }
}

