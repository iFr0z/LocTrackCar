package tk.ifroz.loctrackcar.ui.view.fragment

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Intent
import android.content.Intent.ACTION_SEND
import android.content.Intent.EXTRA_TEXT
import android.content.Intent.createChooser
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.content.res.Configuration.UI_MODE_NIGHT_MASK
import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.graphics.PointF
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat.checkSelfPermission
import androidx.core.app.ActivityCompat.getColor
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN
import com.google.android.material.bottomsheet.BottomSheetBehavior.from
import com.google.android.material.snackbar.Snackbar.LENGTH_LONG
import com.yandex.mapkit.Animation
import com.yandex.mapkit.Animation.Type.SMOOTH
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.RequestPointType.WAYPOINT
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.logo.Alignment
import com.yandex.mapkit.logo.HorizontalAlignment.LEFT
import com.yandex.mapkit.logo.VerticalAlignment.BOTTOM
import com.yandex.mapkit.map.CameraListener
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.CameraUpdateReason
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.map.MapType.VECTOR_MAP
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.places.PlacesFactory
import com.yandex.mapkit.places.panorama.PanoramaService
import com.yandex.mapkit.transport.TransportFactory
import com.yandex.mapkit.transport.masstransit.FitnessOptions
import com.yandex.mapkit.transport.masstransit.PedestrianRouter
import com.yandex.mapkit.transport.masstransit.Route
import com.yandex.mapkit.transport.masstransit.RouteOptions
import com.yandex.mapkit.transport.masstransit.Session.RouteListener
import com.yandex.mapkit.transport.masstransit.TimeOptions
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import com.yandex.runtime.Error
import com.yandex.runtime.image.ImageProvider.fromBitmap
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import tk.ifroz.loctrackcar.R
import tk.ifroz.loctrackcar.data.db.entity.Target
import tk.ifroz.loctrackcar.databinding.MapFragmentBinding
import tk.ifroz.loctrackcar.ui.viewmodel.CarViewModel
import tk.ifroz.loctrackcar.ui.viewmodel.GeocodeViewModel
import tk.ifroz.loctrackcar.ui.viewmodel.ReminderViewModel
import tk.ifroz.loctrackcar.ui.viewmodel.SearchPlaceViewModel
import tk.ifroz.loctrackcar.util.custom.ImageProviderCustom
import tk.ifroz.loctrackcar.util.extension.action
import tk.ifroz.loctrackcar.util.extension.bottomSheetStateCallback
import tk.ifroz.loctrackcar.util.extension.snackBarTop

@ExperimentalCoroutinesApi
class MapFragment : Fragment(), UserLocationObjectListener, CameraListener, RouteListener,
    PanoramaService.SearchListener {

    private var _binding: MapFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var checkLocationPermission: ActivityResultLauncher<Array<String>>

    private var isPermission = false
    private var isFollowUser = false
    private var isCar = false
    private var isPedestrian = false
    private var isMarker = false
    private var isReminder = false

    private var routeEnd = Point(0.0, 0.0)
    private var routeStart = Point(0.0, 0.0)

    private lateinit var userLocationLayer: UserLocationLayer

    private lateinit var carObject: MapObjectCollection
    private lateinit var carPlacemark: PlacemarkMapObject

    private lateinit var carPedestrianObject: MapObjectCollection
    private lateinit var carPedestrianRouter: PedestrianRouter

    private lateinit var markerObject: MapObjectCollection
    private lateinit var markerPlacemark: PlacemarkMapObject

    private val carViewModel: CarViewModel by activityViewModels()
    private val reminderViewModel: ReminderViewModel by activityViewModels()
    private val searchPlaceViewModel: SearchPlaceViewModel by activityViewModels()
    private val geocodeViewModel: GeocodeViewModel by activityViewModels ()

    private lateinit var customBack: OnBackPressedCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.setApiKey(mapKitApiKey)
        MapKitFactory.initialize(this.requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = MapFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        checkLocationPermission = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions[ACCESS_FINE_LOCATION] == true ||
                permissions[ACCESS_COARSE_LOCATION] == true) {
                onMapReady()
            }
        }

        userInterface(view)

        checkPermission(view)
    }

    private fun checkPermission(view: View) {
        if (checkSelfPermission(view.context, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED ||
            checkSelfPermission(view.context, ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED
        ) {
            onMapReady()
        } else {
            checkLocationPermission.launch(arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION))
        }
    }

    private fun onMapReady() {
        val mapKit = MapKitFactory.getInstance()
        userLocationLayer = mapKit.createUserLocationLayer(binding.mapView.mapWindow)
        userLocationLayer.isVisible = true
        userLocationLayer.isHeadingEnabled = false
        userLocationLayer.setObjectListener(this)

        binding.mapView.mapWindow.map.addCameraListener(this)
        binding.mapView.mapWindow.map.mapType = VECTOR_MAP
        binding.mapView.mapWindow.map.logo.setAlignment(Alignment(LEFT, BOTTOM))

        cameraPositionUser()

        isPermission = true

        val locationDetected = getString(R.string.location_detected)
        binding.coordinatorLayout.snackBarTop(locationDetected, LENGTH_LONG) {}
    }



    private fun cameraPositionUser() {
        if (userLocationLayer.cameraPosition() != null) {
            routeStart = userLocationLayer.cameraPosition()!!.target
            binding.mapView.mapWindow.map.move(
                CameraPosition(routeStart, 18f, 0f, 0f), Animation(SMOOTH, 1f), null
            )
        } else {
            binding.mapView.mapWindow.map.move(CameraPosition(Point(0.0, 0.0), 16f, 0f, 0f))
        }
    }

    override fun onCameraPositionChanged(
        map: Map, cPos: CameraPosition, cUpd: CameraUpdateReason, finish: Boolean
    ) {
        if (finish) {
            if (isFollowUser) {
                setAnchor()
            }
        } else {
            if (!isFollowUser) {
                noAnchor()
            }
        }
    }

    private fun setAnchor() {
        userLocationLayer.setAnchor(
            PointF(
                (binding.mapView.width * 0.5).toFloat(), (binding.mapView.height * 0.5).toFloat()
            ),
            PointF(
                (binding.mapView.width * 0.5).toFloat(), (binding.mapView.height * 0.83).toFloat()
            )
        )

        binding.locationFab.setImageResource(R.drawable.ic_my_location_black_24dp)

        isFollowUser = false
    }

    private fun noAnchor() {
        userLocationLayer.resetAnchor()

        binding.locationFab.setImageResource(R.drawable.ic_gps_not_fixed_black_24dp)
    }

    override fun onObjectAdded(userLocationView: UserLocationView) {
        setAnchor()

        val bitmap = view?.let {
            ImageProviderCustom(it.context, R.drawable.ic_dot_rose_24dp).image
        }
        userLocationView.apply {
            pin.setIcon(fromBitmap(bitmap))
            pin.setIconStyle(IconStyle().setFlat(true))
            arrow.setIcon(fromBitmap(bitmap))
            arrow.setIconStyle(IconStyle().setFlat(true))
            accuracyCircle.fillColor = view?.let {
                getColor(it.context, R.color.colorAccuracyCircle)
            }!!
        }
    }

    override fun onObjectRemoved(userLocationView: UserLocationView) {}

    override fun onObjectUpdated(userLocationView: UserLocationView, event: ObjectEvent) {}

    private fun userInterface(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, windowInsets ->
            val insets = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            binding.bottomSheet.translationY = (-90).toFloat()
            view.updatePadding(0, insets.top, 0, insets.bottom)
            WindowInsetsCompat.CONSUMED
        }

        when (resources.configuration.uiMode and UI_MODE_NIGHT_MASK) {
            UI_MODE_NIGHT_NO -> binding.mapView.mapWindow.map.isNightModeEnabled = false
            UI_MODE_NIGHT_YES -> binding.mapView.mapWindow.map.isNightModeEnabled = true
        }

        searchPlace()

        bottomSheetCar(view)

        binding.carFab.setOnClickListener {
            if (isPermission) {
                noAnchor()

                if (isCar) {
                    binding.mapView.mapWindow.map.move(
                        CameraPosition(routeEnd, 18f, 0f, 0f), Animation(SMOOTH, 1f), null
                    )
                } else {
                    insertCar()

                    val markerCreated = getString(R.string.marker_created)
                    binding.coordinatorLayout.snackBarTop(markerCreated, LENGTH_LONG) {}
                }

                from(binding.bottomSheet).state = STATE_EXPANDED
            } else {
                checkPermission(view)
            }
        }
        binding.locationFab.setOnClickListener {
            if (isPermission) {
                from(binding.bottomSheet).state = STATE_HIDDEN

                cameraPositionUser()

                isFollowUser = true
            } else {
                checkPermission(view)
            }
        }

        retrieveCar(view)
    }

    private fun searchPlace() {
        binding.searchPlaceFab.setOnClickListener {
            if (isMarker) {
                val markerIsAlready = getString(R.string.marker_searched_is_already)
                binding.coordinatorLayout.snackBarTop(markerIsAlready, LENGTH_LONG) {
                    val notificationChange = getString(R.string.change)
                    action(notificationChange) {
                        searchPlaceDestination()
                    }
                }
            } else {
                searchPlaceDestination()
            }
        }

        searchPlaceViewModel.searchPlaceResults.observe(viewLifecycleOwner) {
            if (isMarker) {
                markerObject.clear()

                isMarker = false
            }

            val searchPlaceTitle = getString(R.string.search_place_title)
            if (!it.isNullOrEmpty()) {
                val markerLatitude = it[0]
                val markerLongitude = it[1]
                drawMarker(markerLatitude.toDouble(), markerLongitude.toDouble())

                val searchPlaceTitleFormat =
                    getString(R.string.search_place_title_format, searchPlaceTitle)

                binding.searchPlaceFab.text = searchPlaceTitleFormat

                val markerSearched = getString(R.string.marker_searched)
                binding.coordinatorLayout.snackBarTop(markerSearched, LENGTH_LONG) {}
            } else {
                binding.searchPlaceFab.text = searchPlaceTitle
            }
        }
    }

    private fun searchPlaceDestination() {
        if (isPermission) {
            noAnchor()
        }

        findNavController().navigate(R.id.search_place_dest, null)
    }

    private fun drawMarker(markerLatitude: Double, markerLongitude: Double) {
        markerObject = binding.mapView.mapWindow.map.mapObjects.addCollection()
        val point = Point(markerLatitude, markerLongitude)
        val bitmap = view?.let {
            ImageProviderCustom(it.context, R.drawable.ic_place_black_45dp).image
        }
        markerPlacemark = markerObject.addPlacemark().apply {
            geometry = point
            setIcon(fromBitmap(bitmap))
            setIconStyle(IconStyle().setAnchor(PointF(0.5f, 1f)))
        }

        binding.mapView.mapWindow.map.move(
            CameraPosition(point, 18f, 0f, 0f), Animation(SMOOTH, 1f), null
        )

        isMarker = true
    }

    private fun bottomSheetCar(view: View) {
        from(binding.bottomSheet).state = STATE_HIDDEN

        binding.bottomSheet.bottomSheetStateCallback {
            when (it) {
                STATE_EXPANDED -> {
                    binding.carFab.hide()
                    binding.searchPlaceFab.hide()

                    customBack = requireActivity().onBackPressedDispatcher.addCallback(this) {
                        from(binding.bottomSheet).state = STATE_HIDDEN
                    }
                }
                STATE_HIDDEN -> {
                    binding.carFab.show()
                    binding.searchPlaceFab.show()

                    customBack.remove()
                }
            }
        }

        binding.pedestrianChip.setOnClickListener {
            setPedestrian()
        }

        binding.reminderChip.setOnClickListener {
            if (isReminder) {
                lifecycleScope.launch {
                    carViewModel.reminders.collect {
                        it?.let {
                            binding.coordinatorLayout.snackBarTop(it.reminder, LENGTH_LONG) {
                                val notificationChange = getString(R.string.change)
                                action(notificationChange) {
                                    findNavController().navigate(R.id.reminder_dest, null)
                                }
                            }
                        }
                    }
                }
            } else {
                findNavController().navigate(R.id.reminder_dest, null)
            }
        }

        binding.shareChip.setOnClickListener {
            val sendIntent = Intent().apply {
                action = ACTION_SEND
                putExtra(EXTRA_TEXT, "${routeEnd.latitude},${routeEnd.longitude}")
                type = "text/plain"
            }
            startActivity(createChooser(sendIntent, null))
        }

        binding.deleteChip.setOnClickListener {
            dialogMenuCar(view)
        }
    }

    private fun setPedestrian() {
        routeStart = userLocationLayer.cameraPosition()!!.target
        if (!isPedestrian) {
            drawPedestrian()
        } else {
            cameraPositionPedestrian()

            val pedestrianIsAlready = getString(R.string.pedestrian_is_already)
            binding.coordinatorLayout.snackBarTop(pedestrianIsAlready, LENGTH_LONG) {
                val pedestrianChange = getString(R.string.change)
                action(pedestrianChange) {
                    deletePedestrian()

                    drawPedestrian()
                }
            }
        }
    }

    private fun drawPedestrian() {
        carPedestrianObject = binding.mapView.mapWindow.map.mapObjects.addCollection()
        val points = ArrayList<RequestPoint>().apply {
            add(RequestPoint(routeStart, WAYPOINT, null, null))
            add(RequestPoint(routeEnd, WAYPOINT, null, null))
        }
        val avoidSteep = false
        val avoidStairs = false
        val routeOptions = RouteOptions(FitnessOptions(avoidSteep, avoidStairs))
        carPedestrianRouter = TransportFactory.getInstance().createPedestrianRouter()
        carPedestrianRouter.requestRoutes(points, TimeOptions(), routeOptions, this)

        val pedestrianComplete = getString(R.string.pedestrian_complete)
        binding.coordinatorLayout.snackBarTop(pedestrianComplete, LENGTH_LONG) {}
    }

    private fun cameraPositionPedestrian() {
        val screenCenter = Point(
            (routeStart.latitude + routeEnd.latitude) / 2,
            (routeStart.longitude + routeEnd.longitude) / 2
        )
        binding.mapView.mapWindow.map.move(
            CameraPosition(screenCenter, 16f, 0f, 0f), Animation(SMOOTH, 1f), null
        )
    }

    override fun onMasstransitRoutes(routes: List<Route>) {
        if (routes.isNotEmpty()) {
            view?.context?.let {
                carPedestrianObject.addPolyline(routes[0].geometry).apply {
                    outlineColor = getColor(it, R.color.colorWayOutline)
                    outlineWidth = 3f
                }
            }

            cameraPositionPedestrian()

            val distance = routes[0].sections[0].metadata.weight.walkingDistance.text
            val time = routes[0].sections[0].metadata.weight.time.text
            val combination = "$distance \u00B7 $time \u00B7\uD83D\uDEB6"
            binding.distanceTv.text = combination
            binding.distanceTv.visibility = VISIBLE

            isPedestrian = true
        }
    }

    override fun onMasstransitRoutesError(error: Error) {}

    private fun dialogMenuCar(view: View) {
        val menuTitle = getString(R.string.dialog_menu_title)
        val deleteReminder = getString(R.string.notification)
        val deletePedestrian = getString(R.string.pedestrian_title)
        val deleteCar = getString(R.string.dialog_menu_item_delete_car)
        val listItem = arrayOf(deleteReminder, deletePedestrian, deleteCar)
        AlertDialog.Builder(view.context).setTitle(menuTitle).setItems(listItem) { _, which ->
            when (which) {
                0 -> deleteReminder()
                1 -> deletePedestrian()
                2 -> deleteCar()
            }
        }.create().show()
    }

    private fun deleteReminder() {
        if (isReminder) {
            val notificationDeleted = getString(R.string.notification_deleted)
            binding.coordinatorLayout.snackBarTop(notificationDeleted, LENGTH_LONG) {}

            reminderViewModel.deleteScheduleNotification()
            carViewModel.deleteReminder()

            isReminder = false
        } else {
            val isNotAlready = getString(R.string.is_not_already)
            binding.coordinatorLayout.snackBarTop(isNotAlready, LENGTH_LONG) {
                val notificationInsert = getString(R.string.insert)
                action(notificationInsert) {
                    findNavController().navigate(R.id.reminder_dest, null)
                }
            }
        }
    }

    private fun deletePedestrian() {
        if (isPedestrian) {
            val pedestrianDeleted = getString(R.string.pedestrian_deleted)
            binding.coordinatorLayout.snackBarTop(pedestrianDeleted, LENGTH_LONG) {}

            carPedestrianObject.clear()

            binding.distanceTv.visibility = GONE

            isPedestrian = false
        } else {
            val isNotAlready = getString(R.string.is_not_already)
            binding.coordinatorLayout.snackBarTop(isNotAlready, LENGTH_LONG) {
                val pedestrianInsert = getString(R.string.insert)
                action(pedestrianInsert) {
                    setPedestrian()
                }
            }
        }
    }

    private fun deleteCar() {
        if (isCar) {
            deletePedestrian()

            carObject.clear()

            carViewModel.deleteTarget()
            carViewModel.deleteReminder()
            reminderViewModel.deleteScheduleNotification()
            geocodeViewModel.deleteGeocode()

            isCar = false
            isReminder = false

            from(binding.bottomSheet).state = STATE_HIDDEN

            val markerDeleted = getString(R.string.marker_deleted)
            binding.coordinatorLayout.snackBarTop(markerDeleted, LENGTH_LONG) {}
        }
    }

    private fun insertCar() {
        val cameraPositionLatitude = userLocationLayer.cameraPosition()!!.target.latitude
        val cameraPositionLongitude = userLocationLayer.cameraPosition()!!.target.longitude
        carViewModel.insertTarget(Target(cameraPositionLatitude, cameraPositionLongitude))
    }

    private fun retrieveCar(view: View) {
        lifecycleScope.launch {
            carViewModel.targets.collect {
                it?.let {
                    routeEnd = Point(it.latitude, it.longitude)
                    drawCar(routeEnd, view)
                }
            }
        }
    }

    private fun drawCar(routeEnd: Point, view: View) {
        val bitmap = ImageProviderCustom(view.context, R.drawable.ic_marker_with_outline_45dp).image
        carObject = binding.mapView.mapWindow.map.mapObjects.addCollection()
        carPlacemark = carObject.addPlacemark().apply {
            geometry = routeEnd
            setIcon(fromBitmap(bitmap))
            setIconStyle(IconStyle().setAnchor(PointF(0.5f, 1f)))
        }

        dataCar(routeEnd.latitude, routeEnd.longitude)

        isCar = true
    }

    private fun dataCar(latitude: Double, longitude: Double) {
        showPanorama(routeEnd)

        val geocode = "Широта: $latitude, Долгота: $longitude"
        geocodeViewModel.insertGeocode(geocode)
        binding.geocodeTv.text = geocode

        reminderViewModel.outputStatus.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it.isEmpty()) {
                    return@Observer
                }
                val workInfo = it[0]
                isReminder = !workInfo.state.isFinished
            }
        })
    }

    private fun showPanorama(routeEnd: Point) {
        val panoramaService = PlacesFactory.getInstance().createPanoramaService()
        panoramaService.findNearest(Point(routeEnd.latitude, routeEnd.longitude), this)
    }

    override fun onPanoramaSearchResult(panoramaId: String) {
        binding.panoramaView.player.openPanorama(panoramaId)
        binding.panoramaView.player.logo.setAlignment(Alignment(LEFT, BOTTOM))
        binding.panoramaView.setNoninteractive(true)
    }

    override fun onPanoramaSearchError(error: Error) {}

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStop() {
        binding.mapView.onStop()
        binding.panoramaView.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
        binding.panoramaView.onStart()
        MapKitFactory.getInstance().onStart()
    }

    companion object {
        const val mapKitApiKey = "bb20cb74-9351-4c60-a3c3-494214e391ac"
    }
}