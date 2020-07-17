package tk.ifroz.loctrackcar.ui.view.fragment

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.content.res.Configuration.*
import android.graphics.PointF
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.FrameLayout.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
import android.widget.FrameLayout.SYSTEM_UI_FLAG_LAYOUT_STABLE
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat.checkSelfPermission
import androidx.core.app.ActivityCompat.getColor
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior.*
import com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
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
import com.yandex.mapkit.map.*
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapType.VECTOR_MAP
import com.yandex.mapkit.places.PlacesFactory
import com.yandex.mapkit.places.panorama.PanoramaService.SearchListener
import com.yandex.mapkit.transport.TransportFactory
import com.yandex.mapkit.transport.masstransit.PedestrianRouter
import com.yandex.mapkit.transport.masstransit.Route
import com.yandex.mapkit.transport.masstransit.Session.RouteListener
import com.yandex.mapkit.transport.masstransit.TimeOptions
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import com.yandex.runtime.Error
import com.yandex.runtime.image.ImageProvider.fromBitmap
import kotlinx.android.synthetic.main.map_fragment.view.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import tk.ifroz.loctrackcar.R
import tk.ifroz.loctrackcar.data.api.AddressApiBuilder.addressApiService
import tk.ifroz.loctrackcar.data.api.AddressApiHelperImpl
import tk.ifroz.loctrackcar.data.db.entity.Target
import tk.ifroz.loctrackcar.ui.intent.MainIntent
import tk.ifroz.loctrackcar.ui.viewmodel.AddressViewModel
import tk.ifroz.loctrackcar.ui.viewmodel.CarViewModel
import tk.ifroz.loctrackcar.ui.viewmodel.ReminderViewModel
import tk.ifroz.loctrackcar.ui.viewmodel.SearchPlaceViewModel
import tk.ifroz.loctrackcar.ui.viewstate.MainState
import tk.ifroz.loctrackcar.util.ViewModelFactory
import tk.ifroz.loctrackcar.util.custom.ImageProviderCustom
import tk.ifroz.loctrackcar.util.extension.action
import tk.ifroz.loctrackcar.util.extension.bottomSheetStateCallback
import tk.ifroz.loctrackcar.util.extension.snackBarTop

@ExperimentalCoroutinesApi
class MapFragment : Fragment(), UserLocationObjectListener, CameraListener, RouteListener,
    SearchListener {

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
    private val addressViewModel: AddressViewModel by activityViewModels {
        ViewModelFactory(AddressApiHelperImpl(addressApiService))
    }

    private lateinit var customBack: OnBackPressedCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        MapKitFactory.setApiKey(mapKitApiKey)
        MapKitFactory.initialize(this.requireActivity())
        PlacesFactory.initialize(this.requireActivity())
        TransportFactory.initialize(this.requireActivity())
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.map_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        userInterface(view)

        checkPermission(view)
    }

    private fun checkPermission(view: View) {
        val permissionLocation = checkSelfPermission(view.context, ACCESS_FINE_LOCATION)
        if (permissionLocation != PERMISSION_GRANTED) {
            requestPermissions(arrayOf(ACCESS_FINE_LOCATION), requestPermissionLocation)
        } else {
            onMapReady(view)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            requestPermissionLocation -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED) {
                    view?.let { view ->
                        onMapReady(view)
                    }
                }
                return
            }
        }
    }

    private fun onMapReady(view: View) {
        val mapKit = MapKitFactory.getInstance()
        userLocationLayer = mapKit.createUserLocationLayer(view.map_v.mapWindow)
        userLocationLayer.isVisible = true
        userLocationLayer.isHeadingEnabled = false
        userLocationLayer.setObjectListener(this)

        view.map_v.map.addCameraListener(this)
        view.map_v.map.mapType = VECTOR_MAP
        val mapStyle = resources.openRawResource(R.raw.map_style).bufferedReader().use {
            it.readText()
        }
        view.map_v.map.setMapStyle(mapStyle)
        view.map_v.map.logo.setAlignment(Alignment(LEFT, BOTTOM))

        cameraPositionUser()

        isPermission = true

        val locationDetected = getString(R.string.location_detected)
        view.coordinator_l.snackBarTop(locationDetected, LENGTH_SHORT) {}
    }



    private fun cameraPositionUser() {
        view?.let { view ->
            if (userLocationLayer.cameraPosition() != null) {
                routeStart = userLocationLayer.cameraPosition()!!.target
                view.map_v.map.move(
                    CameraPosition(routeStart, 18f, 0f, 0f), Animation(SMOOTH, 1f), null
                )
            } else {
                view.map_v.map.move(CameraPosition(Point(0.0, 0.0), 16f, 0f, 0f))
            }
        }
    }

    override fun onCameraPositionChanged(
        map: Map, cPos: CameraPosition, cUpd: CameraUpdateSource, finish: Boolean
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
        view?.let { view ->
            userLocationLayer.setAnchor(
                PointF((view.map_v.width * 0.5).toFloat(), (view.map_v.height * 0.5).toFloat()),
                PointF((view.map_v.width * 0.5).toFloat(), (view.map_v.height * 0.83).toFloat())
            )

            view.location_fab.setImageResource(R.drawable.ic_my_location_black_24dp)

            isFollowUser = false
        }
    }

    private fun noAnchor() {
        view?.let { view ->
            userLocationLayer.resetAnchor()

            view.location_fab.setImageResource(R.drawable.ic_gps_not_fixed_black_24dp)
        }
    }

    override fun onObjectAdded(userLocationView: UserLocationView) {
        setAnchor()

        view?.context?.let { context ->
            val bitmap = ImageProviderCustom(context, R.drawable.ic_dot_rose_24dp).image
            userLocationView.apply {
                pin.setIcon(fromBitmap(bitmap))
                pin.setIconStyle(IconStyle().setFlat(true))
                arrow.setIcon(fromBitmap(bitmap))
                arrow.setIconStyle(IconStyle().setFlat(true))
                accuracyCircle.fillColor = getColor(context, R.color.colorAccuracyCircle)
            }
        }
    }

    override fun onObjectRemoved(userLocationView: UserLocationView) {}

    override fun onObjectUpdated(userLocationView: UserLocationView, event: ObjectEvent) {}

    private fun userInterface(view: View) {
        @Suppress("DEPRECATION") view.apply {
            systemUiVisibility = SYSTEM_UI_FLAG_LAYOUT_STABLE or SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }

        when (resources.configuration.uiMode and UI_MODE_NIGHT_MASK) {
            UI_MODE_NIGHT_NO -> view.map_v.map.isNightModeEnabled = false
            UI_MODE_NIGHT_YES -> view.map_v.map.isNightModeEnabled = true
        }

        searchPlace(view)

        bottomSheetCar(view)

        view.car_fab.setOnClickListener {
            if (isPermission) {
                noAnchor()

                if (isCar) {
                    view.map_v.map.move(
                        CameraPosition(routeEnd, 18f, 0f, 0f), Animation(SMOOTH, 1f), null
                    )
                } else {
                    insertCar()

                    val markerCreated = getString(R.string.marker_created)
                    view.coordinator_l.snackBarTop(markerCreated, LENGTH_SHORT) {}
                }

                from(view.bottom_sheet).state = STATE_EXPANDED
            } else {
                checkPermission(view)
            }
        }
        view.location_fab.setOnClickListener {
            if (isPermission) {
                from(view.bottom_sheet).state = STATE_HIDDEN

                cameraPositionUser()

                isFollowUser = true
            } else {
                checkPermission(view)
            }
        }

        retrieveCar(view)
    }

    private fun searchPlace(view: View) {
        view.search_place_fab.setOnClickListener {
            if (isMarker) {
                val markerIsAlready = getString(R.string.marker_searched_is_already)
                view.coordinator_l.snackBarTop(markerIsAlready, LENGTH_SHORT) {
                    val notificationChange = getString(R.string.change)
                    action(notificationChange) {
                        searchPlaceDestination()
                    }
                }
            } else {
                searchPlaceDestination()
            }
        }

        searchPlaceViewModel.searchPlaceResults.observe(
            viewLifecycleOwner, Observer { searchPlaceResult ->
                if (isMarker) {
                    markerObject.clear()

                    isMarker = false
                }

                val searchPlaceTitle = getString(R.string.search_place_title)
                if (!searchPlaceResult.isNullOrEmpty()) {
                    val markerLatitude = searchPlaceResult[0]
                    val markerLongitude = searchPlaceResult[1]
                    drawMarker(markerLatitude.toDouble(), markerLongitude.toDouble(), view)

                    val searchPlaceTitleFormat =
                        getString(R.string.search_place_title_format, searchPlaceTitle)

                    view.search_place_fab.text = searchPlaceTitleFormat

                    val markerSearched = getString(R.string.marker_searched)
                    view.coordinator_l.snackBarTop(markerSearched, LENGTH_SHORT) {}
                } else {
                    view.search_place_fab.text = searchPlaceTitle
                }
            }
        )
    }

    private fun searchPlaceDestination() {
        if (isPermission) {
            noAnchor()
        }

        findNavController().navigate(R.id.search_place_dest, null)
    }

    private fun drawMarker(markerLatitude: Double, markerLongitude: Double, view: View) {
        markerObject = view.map_v.map.mapObjects.addCollection()
        val point = Point(markerLatitude, markerLongitude)
        val bitmap = ImageProviderCustom(view.context, R.drawable.ic_place_black_45dp).image
        markerPlacemark = markerObject.addPlacemark(point).apply {
            setIcon(fromBitmap(bitmap))
            setIconStyle(IconStyle().setAnchor(PointF(0.5f, 1f)))
        }

        view.map_v.map.move(CameraPosition(point, 18f, 0f, 0f), Animation(SMOOTH, 1f), null)

        isMarker = true
    }

    private fun bottomSheetCar(view: View) {
        from(view.bottom_sheet).state = STATE_HIDDEN

        view.bottom_sheet.bottomSheetStateCallback { state ->
            when (state) {
                STATE_EXPANDED -> {
                    view.car_fab.hide()
                    view.search_place_fab.hide()

                    customBack = requireActivity().onBackPressedDispatcher.addCallback(this) {
                        from(view.bottom_sheet).state = STATE_HIDDEN
                    }
                }
                STATE_HIDDEN -> {
                    view.car_fab.show()
                    view.search_place_fab.show()

                    customBack.remove()
                }
            }
        }

        view.pedestrian_c.setOnClickListener {
            setPedestrian(view)
        }

        view.reminder_c.setOnClickListener {
            if (isReminder) {
                lifecycleScope.launch {
                    carViewModel.reminders.collect { reminder ->
                        reminder?.let {
                            view.coordinator_l.snackBarTop(reminder.reminder, LENGTH_SHORT) {
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

        view.share_c.setOnClickListener {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "${routeEnd.latitude},${routeEnd.longitude}")
                type = "text/plain"
            }
            startActivity(Intent.createChooser(sendIntent, null))
        }

        view.delete_c.setOnClickListener {
            dialogMenuCar(view)
        }
    }

    private fun setPedestrian(view: View) {
        routeStart = userLocationLayer.cameraPosition()!!.target
        if (!isPedestrian) {
            drawPedestrian(view)
        } else {
            cameraPositionPedestrian()

            val pedestrianIsAlready = getString(R.string.pedestrian_is_already)
            view.coordinator_l.snackBarTop(pedestrianIsAlready, LENGTH_SHORT) {
                val pedestrianChange = getString(R.string.change)
                action(pedestrianChange) {
                    deletePedestrian(view)

                    drawPedestrian(view)
                }
            }
        }
    }

    private fun drawPedestrian(view: View) {
        carPedestrianObject = view.map_v.map.mapObjects.addCollection()
        val points = ArrayList<RequestPoint>().apply {
            add(RequestPoint(routeStart, WAYPOINT, null))
            add(RequestPoint(routeEnd, WAYPOINT, null))
        }
        carPedestrianRouter = TransportFactory.getInstance().createPedestrianRouter()
        carPedestrianRouter.requestRoutes(points, TimeOptions(), this)

        val pedestrianComplete = getString(R.string.pedestrian_complete)
        view.coordinator_l.snackBarTop(pedestrianComplete, LENGTH_SHORT) {}
    }

    private fun cameraPositionPedestrian() {
        view?.let { view ->
            val screenCenter = Point(
                (routeStart.latitude + routeEnd.latitude) / 2,
                (routeStart.longitude + routeEnd.longitude) / 2
            )
            view.map_v.map.move(
                CameraPosition(screenCenter, 16f, 0f, 0f), Animation(SMOOTH, 1f), null
            )
        }
    }

    override fun onMasstransitRoutes(routes: List<Route>) {
        if (routes.isNotEmpty()) {
            view?.context?.let { context ->
                carPedestrianObject.addPolyline(routes[0].geometry).apply {
                    strokeColor = getColor(context, R.color.colorDot)
                    outlineColor = getColor(context, R.color.colorWayOutline)
                    outlineWidth = 10f
                }
            }

            cameraPositionPedestrian()

            view?.let { view ->
                val distance = routes[0].sections[0].metadata.weight.walkingDistance.text
                val time = routes[0].sections[0].metadata.weight.time.text
                val combination = "$distance \u00B7 $time \u00B7\uD83D\uDEB6"
                view.distance_tv.text = combination
                view.distance_tv.visibility = VISIBLE

                isPedestrian = true
            }
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
                0 -> deleteReminder(view)
                1 -> deletePedestrian(view)
                2 -> deleteCar(view)
            }
        }.create().show()
    }

    private fun deleteReminder(view: View) {
        if (isReminder) {
            val notificationDeleted = getString(R.string.notification_deleted)
            view.coordinator_l.snackBarTop(notificationDeleted, LENGTH_SHORT) {}

            reminderViewModel.deleteScheduleNotification()
            carViewModel.deleteReminder()

            isReminder = false
        } else {
            val isNotAlready = getString(R.string.is_not_already)
            view.coordinator_l.snackBarTop(isNotAlready, LENGTH_SHORT) {
                val notificationInsert = getString(R.string.insert)
                action(notificationInsert) {
                    findNavController().navigate(R.id.reminder_dest, null)
                }
            }
        }
    }

    private fun deletePedestrian(view: View) {
        if (isPedestrian) {
            val pedestrianDeleted = getString(R.string.pedestrian_deleted)
            view.coordinator_l.snackBarTop(pedestrianDeleted, LENGTH_SHORT) {}

            carPedestrianObject.clear()

            view.distance_tv.visibility = GONE

            isPedestrian = false
        } else {
            val isNotAlready = getString(R.string.is_not_already)
            view.coordinator_l.snackBarTop(isNotAlready, LENGTH_SHORT) {
                val pedestrianInsert = getString(R.string.insert)
                action(pedestrianInsert) {
                    setPedestrian(view)
                }
            }
        }
    }

    private fun deleteCar(view: View) {
        if (isCar) {
            deletePedestrian(view)

            carObject.clear()

            carViewModel.deleteTarget()
            carViewModel.deleteReminder()
            reminderViewModel.deleteScheduleNotification()
            addressViewModel.deleteGeocode()
            addressViewModel.deleteAddressName()

            isCar = false
            isReminder = false

            from(view.bottom_sheet).state = STATE_HIDDEN

            val markerDeleted = getString(R.string.marker_deleted)
            view.coordinator_l.snackBarTop(markerDeleted, LENGTH_SHORT) {}
        }
    }

    private fun insertCar() {
        val cameraPositionLatitude = userLocationLayer.cameraPosition()!!.target.latitude
        val cameraPositionLongitude = userLocationLayer.cameraPosition()!!.target.longitude
        carViewModel.insertTarget(Target(cameraPositionLatitude, cameraPositionLongitude))
    }

    private fun retrieveCar(view: View) {
        lifecycleScope.launch {
            carViewModel.targets.collect { target ->
                target?.let {
                    routeEnd = Point(target.latitude, target.longitude)
                    drawCar(routeEnd, view)
                }
            }
        }
    }

    private fun drawCar(routeEnd: Point, view: View) {
        val bitmap = ImageProviderCustom(view.context, R.drawable.ic_marker_with_outline_45dp).image
        carObject = view.map_v.map.mapObjects.addCollection()
        carPlacemark = carObject.addPlacemark(routeEnd).apply {
            setIcon(fromBitmap(bitmap))
            setIconStyle(IconStyle().setAnchor(PointF(0.5f, 1f)))
        }

        dataCar(routeEnd.latitude, routeEnd.longitude, view)

        isCar = true
    }

    private fun dataCar(latitude: Double, longitude: Double, view: View) {
        showPanorama(routeEnd)

        lifecycleScope.launch {
            val geocode = "$longitude,$latitude"
            addressViewModel.insertGeocode(geocode)
            addressViewModel.addressIntent.send(MainIntent.FetchAddress)
        }
        lifecycleScope.launch {
            addressViewModel.state.collect {
                when (it) {
                    is MainState.Loading -> {
                        view.address_tv.visibility = GONE
                        view.progressBar.visibility = VISIBLE
                    }
                    is MainState.Success -> {
                        view.progressBar.visibility = GONE
                        renderAddress(view, it.data.toString())
                    }
                    is MainState.Error -> {
                        view.progressBar.visibility = GONE
                        renderAddress(view, it.error!!)
                    }
                }
            }
        }

        reminderViewModel.outputStatus.observe(viewLifecycleOwner, Observer { listOfWorkInfo ->
            listOfWorkInfo?.let {
                if (listOfWorkInfo.isNullOrEmpty()) {
                    return@Observer
                }
                val workInfo = listOfWorkInfo[0]
                isReminder = !workInfo.state.isFinished
            }
        })
    }

    private fun renderAddress(view: View, address: String) {
        view.address_tv.visibility = VISIBLE
        address.let { addressName ->
            addressName.let {
                view.address_tv.text = it
                addressViewModel.insertAddressName(it)
            }
        }
    }

    private fun showPanorama(routeEnd: Point) {
        val panoramaService = PlacesFactory.getInstance().createPanoramaService()
        panoramaService.findNearest(Point(routeEnd.latitude, routeEnd.longitude), this)
    }

    override fun onPanoramaSearchResult(panoramaId: String) {
        view?.let { view ->
            view.panorama_v.player.openPanorama(panoramaId)
            view.panorama_v.player.logo.setAlignment(Alignment(LEFT, BOTTOM))
            view.panorama_v.setNoninteractive(true)
        }
    }

    override fun onPanoramaSearchError(error: Error) {}

    override fun onStop() {
        view?.let { view ->
            view.map_v.onStop()
            view.panorama_v.onStop()
        }
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun onStart() {
        super.onStart()
        view?.let { view ->
            view.map_v.onStart()
            view.panorama_v.onStart()
        }
        MapKitFactory.getInstance().onStart()
    }

    companion object {
        const val mapKitApiKey = "e4b59fa0-e067-42ae-9044-5c6a038503e9"
        const val requestPermissionLocation = 1
    }
}