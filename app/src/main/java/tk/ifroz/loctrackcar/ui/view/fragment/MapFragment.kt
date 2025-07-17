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
import android.view.ViewGroup.MarginLayoutParams
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat.checkSelfPermission
import androidx.core.app.ActivityCompat.getColor
import androidx.core.view.ViewCompat
import androidx.core.view.ViewGroupCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
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
import com.yandex.mapkit.layers.GeoObjectTapEvent
import com.yandex.mapkit.layers.GeoObjectTapListener
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.logo.Alignment
import com.yandex.mapkit.logo.HorizontalAlignment.LEFT
import com.yandex.mapkit.logo.VerticalAlignment.BOTTOM
import com.yandex.mapkit.map.CameraListener
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.CameraUpdateReason
import com.yandex.mapkit.map.GeoObjectSelectionMetadata
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.map.MapType.VECTOR_MAP
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.places.PlacesFactory
import com.yandex.mapkit.places.panorama.PanoramaService
import com.yandex.mapkit.search.Address
import com.yandex.mapkit.search.Response
import com.yandex.mapkit.search.SearchFactory
import com.yandex.mapkit.search.SearchManager
import com.yandex.mapkit.search.SearchManagerType
import com.yandex.mapkit.search.SearchOptions
import com.yandex.mapkit.search.Session
import com.yandex.mapkit.search.Session.SearchListener
import com.yandex.mapkit.search.ToponymObjectMetadata
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
import androidx.core.graphics.toColorInt
import androidx.core.view.insets.GradientProtection
import androidx.core.view.insets.ProtectionLayout
import com.yandex.mapkit.map.LineStyle

@ExperimentalCoroutinesApi
class MapFragment : Fragment(), UserLocationObjectListener, CameraListener, RouteListener,
    PanoramaService.SearchListener, SearchListener, InputListener, GeoObjectTapListener {

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

    private var cameraListener: CameraListener? = null
    private var searchListener: SearchListener? = null
    private lateinit var searchManager: SearchManager
    private lateinit var searchSession: Session
    private var inputListener: InputListener? = null
    private var geoObjectTapListener: GeoObjectTapListener? = null
    private var routeListener: RouteListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            checkSelfPermission(view.context, ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED) {
            onMapReady()
        } else {
            checkLocationPermission.launch(arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION))
        }
    }

    private fun onMapReady() {
        val mapKit = MapKitFactory.getInstance()
        userLocationLayer = mapKit.createUserLocationLayer(binding.mapView.mapWindow)
        userLocationLayer.isVisible = true
        userLocationLayer.isHeadingModeActive = false
        userLocationLayer.setObjectListener(this)

        searchManager = SearchFactory.getInstance().createSearchManager(SearchManagerType.ONLINE)

        routeListener = this
        inputListener = this
        searchListener = this
        binding.mapView.mapWindow.map.addInputListener(inputListener!!)
        geoObjectTapListener = this
        binding.mapView.mapWindow.map.addTapListener(geoObjectTapListener!!)
        cameraListener = this
        binding.mapView.mapWindow.map.addCameraListener(cameraListener!!)


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
            ImageProviderCustom(it.context, R.drawable.ic_dot_blue_24dp).image
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
        binding.protectionLayout.setProtections(
            listOf(
                GradientProtection(WindowInsetsCompat.Side.TOP,"#60000000".toColorInt())
            )
        )
        ViewGroupCompat.installCompatInsetsDispatch(binding.coordinatorLayout)
        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomSheet) { v, insets ->
            val systemBarsInsets = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            v.updatePadding(
                bottom = systemBarsInsets.bottom
            )
            WindowInsetsCompat.CONSUMED
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.searchPlaceFab) { v, insets ->
            val systemBarsInsets = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            v.updateLayoutParams<MarginLayoutParams> {
                bottomMargin = systemBarsInsets.bottom
            }
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
        // Инициализация коллекции объектов карты
        val mapObjects = binding.mapView.mapWindow.map.mapObjects
        carPedestrianObject = mapObjects.addCollection()

        // Создание точек маршрута
        val routePoints = createRoutePoints(routeStart, routeEnd)

        // Настройка опций маршрута
        val routeOptions = configureRouteOptions()

        // Создание и настройка маршрутизатора
        carPedestrianRouter = createPedestrianRouter()

        // Запрос маршрута
        requestPedestrianRoute(routePoints, routeOptions)

        // Отображение уведомления
        showPedestrianCompleteMessage()
    }

    private fun createRoutePoints(start: Point, end: Point): List<RequestPoint> {
        return listOf(
            RequestPoint(start, WAYPOINT, null, null, null),
            RequestPoint(end, WAYPOINT, null, null, null)
        )
    }

    private fun configureRouteOptions(): RouteOptions {
        val fitnessOptions = FitnessOptions(false, false)
        return RouteOptions(fitnessOptions)
    }

    private fun createPedestrianRouter(): PedestrianRouter {
        return TransportFactory.getInstance().createPedestrianRouter()
    }

    private fun requestPedestrianRoute(points: List<RequestPoint>, routeOptions: RouteOptions) {
        lifecycleScope.launch {
            carPedestrianRouter.requestRoutes(points, TimeOptions(), routeOptions, routeListener!!)
        }
    }

    private fun showPedestrianCompleteMessage() {
        val message = getString(R.string.pedestrian_complete)
        binding.coordinatorLayout.snackBarTop(message, LENGTH_LONG) {}
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
            carPedestrianObject.addPolyline(routes[0].geometry).apply {
                setStrokeColor("#2196F3".toColorInt())
                style = LineStyle().apply {
                    strokeWidth = 5f
                    outlineWidth = 3f
                    outlineColor = "#1976D2".toColorInt()
                }
            }

            cameraPositionPedestrian()

            val distance = routes[0].sections[0].metadata.weight.walkingDistance.text
            val time = routes[0].sections[0].metadata.weight.time.text
            val combination = "$distance \u00B7 $time \u00B7\uD83D\uDEB6"
            binding.distanceTv.apply {
                text = combination
                visibility = VISIBLE
            }

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

    override fun onSearchResponse(response: Response) {
        val street = response.collection.children.firstOrNull()?.obj
            ?.metadataContainer
            ?.getItem(ToponymObjectMetadata::class.java)
            ?.address
            ?.components
            ?.firstOrNull { it.kinds.contains(Address.Component.Kind.STREET)}
            ?.name ?: "Информация об улице не найдена"

        val house = response.collection.children.firstOrNull()?.obj
            ?.metadataContainer
            ?.getItem(ToponymObjectMetadata::class.java)
            ?.address
            ?.components
            ?.firstOrNull { it.kinds.contains(Address.Component.Kind.HOUSE)}
            ?.name ?: "Информация об доме не найдена"

        Toast.makeText(context, "$street, $house", Toast.LENGTH_LONG).show()
    }

    override fun onSearchError(p0: Error) {}

    override fun onMapTap(p0: Map, point: Point) {
        lifecycleScope.launch {
            searchSession = searchManager.submit(point, 20, SearchOptions(), searchListener!!)
        }
    }

    override fun onMapLongTap(p0: Map, p1: Point) {}

    override fun onObjectTap(goTapEvent: GeoObjectTapEvent): Boolean {
        val point = goTapEvent.geoObject.geometry.firstOrNull()?.point ?: return true
        binding.mapView.mapWindow.map.cameraPosition.run {
            binding.mapView.mapWindow.map.move(CameraPosition(point, zoom, azimuth, tilt))
            if (!isFollowUser) {
                noAnchor()
            }
        }

        val selectionMetadata = goTapEvent.geoObject.metadataContainer.getItem(
            GeoObjectSelectionMetadata::class.java
        )
        binding.mapView.mapWindow.map.selectGeoObject(selectionMetadata)

        return false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchListener = null
        binding.mapView.mapWindow.map.removeCameraListener(cameraListener!!)
        cameraListener = null
        binding.mapView.mapWindow.map.removeInputListener(inputListener!!)
        inputListener = null
        binding.mapView.mapWindow.map.removeTapListener(geoObjectTapListener!!)
        geoObjectTapListener = null
        routeListener = null
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        binding.mapView.onStart()
        binding.panoramaView.onStart()
    }

    override fun onStop() {
        binding.mapView.onStop()
        binding.panoramaView.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }
}