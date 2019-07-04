package tk.ifroz.loctrackcar.ui.fragment

import android.content.res.Configuration.*
import android.graphics.PointF
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import androidx.core.app.ActivityCompat.getColor
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders.of
import androidx.work.Data
import androidx.work.WorkInfo
import com.google.android.material.bottomsheet.BottomSheetBehavior.*
import com.yandex.mapkit.Animation
import com.yandex.mapkit.Animation.Type.SMOOTH
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.RequestPointType.WAYPOINT
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.logo.Alignment
import com.yandex.mapkit.logo.HorizontalAlignment.LEFT
import com.yandex.mapkit.logo.HorizontalAlignment.RIGHT
import com.yandex.mapkit.logo.VerticalAlignment.BOTTOM
import com.yandex.mapkit.logo.VerticalAlignment.TOP
import com.yandex.mapkit.map.*
import com.yandex.mapkit.map.Map
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
import kotlinx.android.synthetic.main.app_bar_main.view.*
import kotlinx.android.synthetic.main.bottom_sheet_backend.view.*
import kotlinx.android.synthetic.main.bottom_sheet_frontend.view.*
import kotlinx.android.synthetic.main.bottom_sheet_navigation.*
import kotlinx.android.synthetic.main.bottom_sheet_row_distance.view.*
import kotlinx.android.synthetic.main.bottom_sheet_row_location.view.*
import kotlinx.android.synthetic.main.bottom_sheet_row_reminder.view.*
import org.jetbrains.anko.configuration
import org.jetbrains.anko.selector
import org.jetbrains.anko.share
import ru.ifr0z.core.custom.ImageProviderCustom
import ru.ifr0z.core.extension.bottomSheetStateCallback
import ru.ifr0z.core.interfaces.OnBackClick
import ru.ifr0z.core.livedata.ConnectivityLiveData
import tk.ifroz.loctrackcar.R
import tk.ifroz.loctrackcar.db.entity.Target
import tk.ifroz.loctrackcar.viewmodel.CarViewModel
import tk.ifroz.loctrackcar.viewmodel.GeocoderViewModel
import tk.ifroz.loctrackcar.viewmodel.ReminderViewModel
import tk.ifroz.loctrackcar.viewmodel.SearchPlaceViewModel
import tk.ifroz.loctrackcar.work.GeocoderWork.Companion.FORMAT_DATA
import tk.ifroz.loctrackcar.work.GeocoderWork.Companion.GEOCODE_DATA
import tk.ifroz.loctrackcar.work.GeocoderWork.Companion.OUTPUT_DATA
import tk.ifroz.loctrackcar.work.GeocoderWork.Companion.RESULTS_DATA

class MapFragment : Fragment(), UserLocationObjectListener, CameraListener, RouteListener,
    SearchListener, OnBackClick {

    private var isFollowUser = false
    private var isCar = false
    private var isPanorama = false
    private var isPedestrian = false
    private var isMarker = false

    private var routeEnd = Point(0.0, 0.0)
    private var routeStart = Point(0.0, 0.0)

    private lateinit var userLocationLayer: UserLocationLayer

    private lateinit var carObject: MapObjectCollection
    private lateinit var carPlacemark: PlacemarkMapObject

    private lateinit var carPedestrianObject: MapObjectCollection
    private lateinit var carPedestrianRouter: PedestrianRouter

    private lateinit var markerObject: MapObjectCollection
    private lateinit var markerPlacemark: PlacemarkMapObject

    private lateinit var carViewModel: CarViewModel
    private lateinit var geocoderViewModel: GeocoderViewModel
    private lateinit var reminderViewModel: ReminderViewModel
    private lateinit var searchPlaceViewModel: SearchPlaceViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        MapKitFactory.setApiKey(mapKitApiKey)
        MapKitFactory.initialize(this.activity!!)
        PlacesFactory.initialize(this.activity!!)
        TransportFactory.initialize(this.activity!!)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        onMapReady(view)
    }

    private fun onMapReady(view: View) {
        userLocationLayer = view.map_v.map.userLocationLayer
        userLocationLayer.isEnabled = true
        userLocationLayer.isHeadingEnabled = false
        userLocationLayer.setObjectListener(this)

        view.map_v.map.addCameraListener(this)
        view.map_v.map.logo.setAlignment(Alignment(LEFT, BOTTOM))

        cameraPositionUser()

        userInterface(view)
    }

    private fun cameraPositionUser() {
        view?.let { view ->
            if (userLocationLayer.cameraPosition() != null) {
                routeStart = userLocationLayer.cameraPosition()!!.target
                view.map_v.map.move(
                    CameraPosition(routeStart, 16f, 0f, 0f), Animation(SMOOTH, 1f), null
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
            userLocationView.pin.setIcon(fromBitmap(bitmap))
            userLocationView.pin.setIconStyle(IconStyle().setFlat(true))
            userLocationView.arrow.setIcon(fromBitmap(bitmap))
            userLocationView.arrow.setIconStyle(IconStyle().setFlat(true))
            userLocationView.accuracyCircle.fillColor =
                getColor(context, R.color.colorAccuracyCircle)
        }
    }

    override fun onObjectRemoved(userLocationView: UserLocationView) {}

    override fun onObjectUpdated(userLocationView: UserLocationView, event: ObjectEvent) {}

    private fun userInterface(view: View) {
        view.app_bar_l.systemUiVisibility =
            SYSTEM_UI_FLAG_LAYOUT_STABLE or SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        view.app_bar_l.setOnApplyWindowInsetsListener { _, insets ->
            val statusBarSize = insets.systemWindowInsetTop
            view.app_bar_l.setPadding(0, statusBarSize, 0, 0)
            insets
        }

        when (view.context.configuration.uiMode and UI_MODE_NIGHT_MASK) {
            UI_MODE_NIGHT_NO -> view.map_v.map.isNightModeEnabled = false
            UI_MODE_NIGHT_YES -> view.map_v.map.isNightModeEnabled = true
        }

        searchPlace(view)

        bottomSheetCar(view)

        view.car_fab.setOnClickListener {
            noAnchor()

            if (isCar) {
                view.map_v.map.move(
                    CameraPosition(routeEnd, 16f, 0f, 0f), Animation(SMOOTH, 1f), null
                )
            } else {
                insertCar()
            }

            from(view.bottom_sheet).state = STATE_EXPANDED
        }
        view.location_fab.setOnClickListener {
            from(view.bottom_sheet).state = STATE_HIDDEN

            cameraPositionUser()

            isFollowUser = true
        }

        retrieveCar(view)
    }

    private fun searchPlace(view: View) {
        view.search_place_fab.setOnClickListener {
            noAnchor()

            val searchPlaceFragment = SearchPlaceFragment.newInstance()
            searchPlaceFragment.show(this.activity!!.supportFragmentManager, searchPlaceFragments)
        }

        searchPlaceViewModel = of(this.activity!!).get(SearchPlaceViewModel::class.java)
        searchPlaceViewModel.searchPlaceResult.observe(this, Observer { searchPlaceResult ->
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
            } else {
                view.search_place_fab.text = searchPlaceTitle
            }
        })
    }

    private fun drawMarker(markerLatitude: Double, markerLongitude: Double, view: View) {
        markerObject = view.map_v.map.mapObjects.addCollection()
        val point = Point(markerLatitude, markerLongitude)
        markerPlacemark = markerObject.addPlacemark(point)
        val bitmap = ImageProviderCustom(view.context, R.drawable.ic_place_black_45dp).image
        markerPlacemark.setIcon(fromBitmap(bitmap))
        markerPlacemark.setIconStyle(IconStyle().setAnchor(PointF(0.5f, 1f)))

        view.map_v.map.move(CameraPosition(point, 16f, 0f, 0f), Animation(SMOOTH, 1f), null)

        isMarker = true
    }

    private fun bottomSheetCar(view: View) {
        from(view.bottom_sheet).state = STATE_HIDDEN

        view.bottom_sheet.bottomSheetStateCallback { state ->
            when (state) {
                STATE_EXPANDED -> {
                    view.car_fab.hide()
                    view.search_place_fab.hide()
                }
                STATE_HIDDEN -> {
                    view.car_fab.show()
                    view.search_place_fab.show()
                }
            }
        }

        pedestrian_c.setOnClickListener {
            drawPedestrian(view)
        }
        reminder_c.setOnClickListener {
            val reminderFragment = ReminderFragment.newInstance()
            reminderFragment.show(this.activity!!.supportFragmentManager, reminderFragments)
        }
        share_c.setOnClickListener {
            view.context.share("${routeEnd.latitude},${routeEnd.longitude}")
        }
        delete_c.setOnClickListener {
            dialogMenuCar(view)
        }
    }

    private fun drawPedestrian(view: View) {
        routeStart = userLocationLayer.cameraPosition()!!.target
        if (!isPedestrian) {
            carPedestrianObject = view.map_v.map.mapObjects.addCollection()
            val points = ArrayList<RequestPoint>()
            points.add(RequestPoint(routeStart, WAYPOINT, null))
            points.add(RequestPoint(routeEnd, WAYPOINT, null))
            carPedestrianRouter = TransportFactory.getInstance().createPedestrianRouter()
            carPedestrianRouter.requestRoutes(points, TimeOptions(), this)
        } else {
            cameraPositionPedestrian()
        }
    }

    private fun cameraPositionPedestrian() {
        view?.let { view ->
            val screenCenter = Point(
                (routeStart.latitude + routeEnd.latitude) / 2,
                (routeStart.longitude + routeEnd.longitude) / 2
            )
            view.map_v.map.move(
                CameraPosition(screenCenter, 14f, 0f, 0f), Animation(SMOOTH, 1f), null
            )
        }
    }

    override fun onMasstransitRoutes(routes: List<Route>) {
        if (routes.isNotEmpty()) {
            view?.context?.let { context ->
                val pedestrianMapObject = carPedestrianObject.addPolyline(routes[0].geometry)
                pedestrianMapObject.strokeColor = getColor(context, R.color.colorDot)
                pedestrianMapObject.outlineColor = getColor(context, R.color.colorWayOutline)
                pedestrianMapObject.outlineWidth = 1f
            }

            cameraPositionPedestrian()

            view?.let { view ->
                val distance = routes[0].sections[0].metadata.weight.walkingDistance.text
                val time = routes[0].sections[0].metadata.weight.time.text
                val combination = "$distance \u00B7 $time \u00B7\uD83D\uDEB6"
                view.distance_tv.text = combination
                view.distance_cv.visibility = VISIBLE

                isPedestrian = true
            }
        }
    }

    override fun onMasstransitRoutesError(error: Error) {}

    private fun dialogMenuCar(view: View) {
        val deleteReminder = getString(R.string.notification_title)
        val deletePedestrian = getString(R.string.dialog_menu_item_delete_pedestrian)
        val deleteCar = getString(R.string.dialog_menu_item_delete_car)
        val menuTitle = getString(R.string.dialog_menu_title)
        val listItem = listOf(deleteReminder, deletePedestrian, deleteCar)
        view.context.selector(menuTitle, listItem) { _, itemId ->
            when (itemId) {
                0 -> reminderViewModel.cancel()
                1 -> deletePedestrian(view)
                2 -> deleteCar(view)
            }
        }
    }

    private fun deletePedestrian(view: View) {
        if (isPedestrian) {
            carPedestrianObject.clear()

            view.distance_cv.visibility = GONE

            isPedestrian = false
        }
    }

    private fun deleteCar(view: View) {
        if (isCar) {
            deletePedestrian(view)

            carObject.clear()

            carViewModel.deleteTarget()
            carViewModel.deleteReminder()
            geocoderViewModel.cancel()
            reminderViewModel.cancel()

            isPanorama = false
            isCar = false

            from(view.bottom_sheet).state = STATE_HIDDEN
        }
    }

    private fun insertCar() {
        val cameraPositionLatitude = userLocationLayer.cameraPosition()!!.target.latitude
        val cameraPositionLongitude = userLocationLayer.cameraPosition()!!.target.longitude
        carViewModel.insertTarget(Target(cameraPositionLatitude, cameraPositionLongitude))
    }

    private fun retrieveCar(view: View) {
        carViewModel = of(this.activity!!).get(CarViewModel::class.java)
        carViewModel.targets.observe(this, Observer { target ->
            target?.let {
                routeEnd = Point(target.latitude, target.longitude)
                drawCar(routeEnd, view)
            }
        })
    }

    private fun drawCar(routeEnd: Point, view: View) {
        carObject = view.map_v.map.mapObjects.addCollection()
        carPlacemark = carObject.addPlacemark(routeEnd)
        val bitmap = ImageProviderCustom(view.context, R.drawable.ic_marker_black_45dp).image
        carPlacemark.setIcon(fromBitmap(bitmap))
        carPlacemark.setIconStyle(IconStyle().setAnchor(PointF(0.5f, 1f)))

        dataCar(routeEnd.latitude, routeEnd.longitude, view)

        isCar = true
    }

    private fun dataCar(latitude: Double, longitude: Double, view: View) {
        val subtitleLatitude = getString(R.string.latitude_subtitle)
        val subtitleLongitude = getString(R.string.longitude_subtitle)
        val coordinates = "$subtitleLatitude $latitude\n$subtitleLongitude $longitude"
        view.coordinates_tv.text = coordinates

        ConnectivityLiveData(view.context).observe(this, Observer { isNetworkAvailable ->
            if (isNetworkAvailable && !isPanorama) {
                showPanorama(routeEnd)
            }
        })

        geocoderViewModel = of(this.activity!!).get(GeocoderViewModel::class.java)
        val geocode = "$longitude,$latitude"
        val format = "json"
        val results = "1"
        val data = Data.Builder().putString(GEOCODE_DATA, geocode).putString(FORMAT_DATA, format)
            .putString(RESULTS_DATA, results).build()

        geocoderViewModel.getStreetName(data)
        geocoderViewModel.outputStatus.observe(this, Observer<List<WorkInfo>> { listOfWorkInfo ->
            listOfWorkInfo?.let {
                if (listOfWorkInfo.isNullOrEmpty()) {
                    return@Observer
                }
                val workInfo = listOfWorkInfo[0]
                if (workInfo.state.isFinished) {
                    val addressName = workInfo.outputData.getString(OUTPUT_DATA)
                    view.address_tv.text = addressName
                }
            }
        })

        carViewModel.reminders.observe(this, Observer { reminder ->
            reminder?.let {
                view.reminder_tv.text = reminder.reminder
            }
        })

        reminderViewModel = of(this.activity!!).get(ReminderViewModel::class.java)
        reminderViewModel.outputStatus.observe(this, Observer<List<WorkInfo>> { listOfWorkInfo ->
            listOfWorkInfo?.let {
                if (listOfWorkInfo.isNullOrEmpty()) {
                    return@Observer
                }
                val workInfo = listOfWorkInfo[0]
                if (workInfo.state.isFinished) {
                    view.reminder_cv.visibility = GONE
                } else {
                    view.reminder_cv.visibility = VISIBLE
                }
            }
        })
    }

    private fun showPanorama(routeEnd: Point) {
        val panoramaService = PlacesFactory.getInstance().createPanoramaService()
        panoramaService.findNearest(Point(routeEnd.latitude, routeEnd.longitude), this)
    }

    override fun onPanoramaSearchResult(panoramaId: String) {
        view?.let { view ->
            view.panorama_v.player.openPanorama(panoramaId)
            view.panorama_v.player.logo.setAlignment(Alignment(RIGHT, TOP))
            view.panorama_v.setNoninteractive(true)

            isPanorama = true
        }
    }

    override fun onPanoramaSearchError(error: Error) {}

    override fun onBackClick(): Boolean {
        val stateExpanded = from(view!!.bottom_sheet).state == STATE_EXPANDED
        return when {
            stateExpanded -> {
                from(view!!.bottom_sheet).state = STATE_HIDDEN
                true
            }
            else -> false
        }
    }

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
        fun newInstance() = MapFragment()
        const val mapKitApiKey = "e4b59fa0-e067-42ae-9044-5c6a038503e9"
        const val searchPlaceFragments = "search_place_fragment"
        const val reminderFragments = "reminder_fragment"
    }
}
