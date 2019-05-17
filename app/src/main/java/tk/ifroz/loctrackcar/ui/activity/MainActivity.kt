package tk.ifroz.loctrackcar.ui.activity

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.PointF
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.*
import androidx.core.view.GravityCompat.START
import androidx.drawerlayout.widget.DrawerLayout.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders.of
import com.google.android.material.bottomsheet.BottomSheetBehavior.*
import com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener
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
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.bottom_sheet_row_distance.*
import kotlinx.android.synthetic.main.bottom_sheet_row_location.*
import kotlinx.android.synthetic.main.bottom_sheet_row_notification.*
import kotlinx.android.synthetic.main.content_main.*
import org.jetbrains.anko.*
import ru.ifr0z.core.extension.bottomSheetStateCallback
import ru.ifr0z.core.extension.onTextChanges
import ru.ifr0z.core.extension.vectorDrawableToBitmap
import ru.ifr0z.core.livedata.InternetConnectionLiveData
import tk.ifroz.loctrackcar.R
import tk.ifroz.loctrackcar.db.entity.Target
import tk.ifroz.loctrackcar.viewmodel.GeocoderViewModel
import tk.ifroz.loctrackcar.viewmodel.MarkerCarViewModel

class MainActivity : AppCompatActivity(), UserLocationObjectListener, CameraListener, RouteListener,
    SearchListener, OnNavigationItemSelectedListener {

    private var permissionLocation = false
    private var followUserLocation = false
    private var markerCar = false

    private var routeEndLocation = Point(0.0, 0.0)
    private var routeStartLocation = Point(0.0, 0.0)

    private lateinit var markerCarObject: MapObjectCollection
    private lateinit var markerCarPlacemark: PlacemarkMapObject

    private var markerCarStreet = false
    private var markerCarPanorama = false

    private lateinit var markerSearchPlaceObject: MapObjectCollection
    private lateinit var markerSearchPlacePlacemark: PlacemarkMapObject
    private var markerSearchPlace = false

    private lateinit var userLocationLayer: UserLocationLayer

    private var markerCarPolyline = false
    private lateinit var markerCarPolylineObject: MapObjectCollection
    private lateinit var markerCarPedestrianRouter: PedestrianRouter

    private lateinit var markerCarViewModel: MarkerCarViewModel
    private lateinit var geocoderViewModel: GeocoderViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        MapKitFactory.setApiKey(mapKitApiKey)
        MapKitFactory.initialize(this)
        PlacesFactory.initialize(this)
        TransportFactory.initialize(this)
        setContentView(R.layout.activity_main)
        super.onCreate(savedInstanceState)

        checkPermission()

        userInterface()
    }

    private fun checkPermission() {
        val permissionLocation = checkSelfPermission(this, ACCESS_FINE_LOCATION)
        if (permissionLocation != PERMISSION_GRANTED) {
            requestPermissions(this, arrayOf(ACCESS_FINE_LOCATION), requestPermissionLocation)
        } else {
            onMapReady()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            requestPermissionLocation -> {
                if (grantResults[0] == PERMISSION_GRANTED) {
                    onMapReady()
                }

                return
            }
        }
    }

    private fun userInterface() {
        app_bar_l.systemUiVisibility =
            SYSTEM_UI_FLAG_LAYOUT_STABLE or SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        app_bar_l.setOnApplyWindowInsetsListener { _, insets ->
            val statusBarSize = insets.systemWindowInsetTop
            app_bar_l.setPadding(0, statusBarSize, 0, 0)
            insets
        }

        setSupportActionBar(toolbar)
        initToggleToolbar()
        navigation_v.setNavigationItemSelectedListener(this)

        val mapLogoAlignment = Alignment(LEFT, BOTTOM)
        map_v.map.logo.setAlignment(mapLogoAlignment)

        searchPlace()

        bottomSheetMarkerCar()

        marker_fab.setOnClickListener {
            if (permissionLocation) {
                noAnchor()

                if (markerCar) {
                    map_v.map.move(
                        CameraPosition(routeEndLocation, 16f, 0f, 0f), Animation(SMOOTH, 1f), null
                    )
                } else {
                    insertMarkerCar()
                }

                from(bottom_sheet).state = STATE_COLLAPSED
            } else {
                checkPermission()
            }
        }
        location_fab.setOnClickListener {
            if (permissionLocation) {
                cameraUserPosition()

                followUserLocation = true
            } else {
                checkPermission()
            }
        }
        directions_walk_fab.setOnClickListener {
            drawPolyline()
        }

        retrieveMarkerCar()
    }

    private fun insertMarkerCar() {
        val cameraPositionLatitude = userLocationLayer.cameraPosition()!!.target.latitude
        val cameraPositionLongitude = userLocationLayer.cameraPosition()!!.target.longitude
        markerCarViewModel.insertTarget(Target(cameraPositionLatitude, cameraPositionLongitude))
    }

    private fun retrieveMarkerCar() {
        markerCarViewModel = of(this).get(MarkerCarViewModel::class.java)
        markerCarViewModel.targets.observe(this, Observer { target ->
            target?.let {
                routeEndLocation = Point(target.latitude, target.longitude)
                drawMarkerCar(routeEndLocation)
            }
        })
    }

    private fun drawMarkerCar(routeEndLocation: Point) {
        markerCarObject = map_v.map.mapObjects.addCollection()
        markerCarPlacemark = markerCarObject.addPlacemark(routeEndLocation)
        val bitmap = this.vectorDrawableToBitmap(R.drawable.ic_marker_black_45dp)
        markerCarPlacemark.setIcon(fromBitmap(bitmap))
        markerCarPlacemark.setIconStyle(IconStyle().setAnchor(PointF(0.5f, 1f)))

        dataMarkerCar(routeEndLocation.latitude, routeEndLocation.longitude)

        markerCar = true
    }

    private fun dataMarkerCar(latitude: Double, longitude: Double) {
        val subtitleLatitude = getString(R.string.latitude_subtitle)
        val subtitleLongitude = getString(R.string.longitude_subtitle)
        val coordinates = "$subtitleLatitude $latitude\n$subtitleLongitude $longitude"
        lat_lng_tv.text = coordinates

        InternetConnectionLiveData(this).observe(this, Observer { isConnected ->
            isConnected?.let {
                when {
                    !markerCarStreet -> {
                        geocoderViewModel = of(this).get(GeocoderViewModel::class.java)
                        val geocode = "$longitude,$latitude"
                        val params = HashMap<String, String>()
                        params["format"] = "json"
                        params["results"] = "1"
                        geocoderViewModel.getStreetName(geocode, params)
                        geocoderViewModel.geocoders.observe(this, Observer { geocoder ->
                            geocoder?.let {
                                val addressName = geocoder.response.geoObjectCollection
                                    .featureMember[0].geoObject.name

                                address_tv.text = addressName

                                markerCarStreet = true
                            }
                        })
                    }
                    !markerCarPanorama -> showPanorama(routeEndLocation)
                }
            }
        })

        markerCarViewModel.reminders.observe(this, Observer { reminder ->
            reminder?.let {
                notification_tv.text = reminder.reminder
                notification_rl.visibility = VISIBLE
            }
        })
    }

    private fun showPanorama(routeEndLocation: Point) {
        val panoramaService = PlacesFactory.getInstance().createPanoramaService()
        panoramaService.findNearest(
            Point(routeEndLocation.latitude, routeEndLocation.longitude), this
        )
    }

    override fun onPanoramaSearchResult(panoramaId: String) {
        panorama_v.player.openPanorama(panoramaId)
        val panoramaLogoAlignment = Alignment(RIGHT, TOP)
        panorama_v.player.logo.setAlignment(panoramaLogoAlignment)
        panorama_v.setNoninteractive(true)

        markerCarPanorama = true
    }

    override fun onPanoramaSearchError(error: Error) {}

    private fun onMapReady() {
        userLocationLayer = map_v.map.userLocationLayer
        userLocationLayer.isEnabled = true
        userLocationLayer.isHeadingEnabled = true
        userLocationLayer.setObjectListener(this)

        map_v.map.addCameraListener(this)

        cameraUserPosition()

        map_scale_v.metersOnly()

        permissionLocation = true
    }

    private fun cameraUserPosition() {
        if (userLocationLayer.cameraPosition() != null) {
            routeStartLocation = userLocationLayer.cameraPosition()!!.target
            map_v.map.move(
                CameraPosition(routeStartLocation, 16f, 0f, 0f), Animation(SMOOTH, 1f), null
            )
        } else {
            map_v.map.move(CameraPosition(Point(0.0, 0.0), 16f, 0f, 0f))
        }
    }

    override fun onCameraPositionChanged(
        map: Map, cPos: CameraPosition, cUpd: CameraUpdateSource, finish: Boolean
    ) {
        map_scale_v.update(map.cameraPosition.zoom, map.cameraPosition.target.latitude)

        if (finish) {
            if (followUserLocation) {
                setAnchor()
            }
        } else {
            if (!followUserLocation) {
                noAnchor()
            }
        }
    }

    private fun setAnchor() {
        userLocationLayer.setAnchor(
            PointF((map_v.width * 0.5).toFloat(), (map_v.height * 0.5).toFloat()),
            PointF((map_v.width * 0.5).toFloat(), (map_v.height * 0.83).toFloat())
        )

        location_fab.setImageResource(R.drawable.ic_my_location_black_24dp)

        followUserLocation = false
    }

    private fun noAnchor() {
        userLocationLayer.resetAnchor()

        location_fab.setImageResource(R.drawable.ic_gps_not_fixed_black_24dp)
    }

    override fun onObjectAdded(userLocationView: UserLocationView) {
        setAnchor()

        val bitmap = this.vectorDrawableToBitmap(R.drawable.ic_dot_rose_24dp)
        userLocationView.pin.setIcon(fromBitmap(bitmap))
        userLocationView.pin.setIconStyle(IconStyle().setFlat(true))
        userLocationView.arrow.setIcon(fromBitmap(bitmap))
        userLocationView.arrow.setIconStyle(IconStyle().setFlat(true))
        userLocationView.accuracyCircle.fillColor = getColor(this, R.color.colorAccuracyCircle)
    }

    override fun onObjectRemoved(userLocationView: UserLocationView) {}

    override fun onObjectUpdated(userLocationView: UserLocationView, event: ObjectEvent) {}

    private fun searchPlace() {
        search_place_et.setOnTouchListener { _, _ ->
            searchPlaceCursorOn()
            false
        }
        search_place_et.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
            if (id == IME_ACTION_SEARCH) {
                searchPlaceCursorOff()

                if (!markerSearchPlace && search_place_et.text.matches(latLngPattern.toRegex())) {
                    val arrayLatLng = search_place_et.text.split(",".toRegex())
                    val searchPlaceLatitude = arrayLatLng[0].toDouble()
                    val searchPlaceLongitude = arrayLatLng[1].toDouble()
                    drawMarkerSearchPlace(searchPlaceLatitude, searchPlaceLongitude)
                } else {
                    val requestStart = getString(R.string.search_place_request_start)
                    val requestEnd = getString(R.string.search_place_request_end)
                    toast("$requestStart '${search_place_et.text}' $requestEnd")
                }
                return@OnEditorActionListener true
            }
            true
        })
        search_place_et.onTextChanges { sequence ->
            when {
                sequence!!.isNotEmpty() -> {
                    clear_search_iv.animate().alpha(1.0f).setListener(
                        object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                clear_search_iv.visibility = VISIBLE
                                clear_search_iv.setOnClickListener {
                                    if (markerSearchPlace) {
                                        markerSearchPlaceObject.clear()

                                        markerSearchPlace = false
                                    }

                                    search_place_et.setText("")
                                }
                            }
                        }
                    )
                }
                sequence.isEmpty() -> {
                    clear_search_iv.animate().alpha(0.0f).setListener(
                        object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                clear_search_iv.visibility = INVISIBLE
                            }
                        }
                    )
                }
            }
        }
    }

    private fun drawMarkerSearchPlace(searchPlaceLatitude: Double, searchPlaceLongitude: Double) {
        markerSearchPlaceObject = map_v.map.mapObjects.addCollection()
        val point = Point(searchPlaceLatitude, searchPlaceLongitude)
        markerSearchPlacePlacemark = markerSearchPlaceObject.addPlacemark(point)
        val bitmap = this.vectorDrawableToBitmap(R.drawable.ic_place_black_45dp)
        markerSearchPlacePlacemark.setIcon(fromBitmap(bitmap))
        markerSearchPlacePlacemark.setIconStyle(IconStyle().setAnchor(PointF(0.5f, 1f)))

        map_v.map.move(CameraPosition(point, 16f, 0f, 0f), Animation(SMOOTH, 1f), null)

        markerSearchPlace = true
    }

    private fun searchPlaceCursorOn() {
        search_place_et.isCursorVisible = true

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            searchPlaceCursorOff()
        }

        drawer_l.setDrawerLockMode(LOCK_MODE_LOCKED_CLOSED)

        if (permissionLocation) {
            noAnchor()
        }
    }

    private fun searchPlaceCursorOff() {
        val inputMethodManager = this.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(search_place_et.windowToken, 0)

        search_place_et.isCursorVisible = false

        supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        initToggleToolbar()

        drawer_l.setDrawerLockMode(LOCK_MODE_UNLOCKED)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.type_map_night -> {
                if (item.isChecked) {
                    item.isChecked = false
                    map_v.map.isNightModeEnabled = false
                } else {
                    item.isChecked = true
                    map_v.map.isNightModeEnabled = true
                }
            }
            R.id.privacy_policy -> {
                val privacyPolicyUrl = getString(R.string.privacy_policy_url)
                browse(privacyPolicyUrl)
            }
        }
        drawer_l.closeDrawer(START)
        return false
    }

    private fun initToggleToolbar() {
        val toggle = ActionBarDrawerToggle(
            this, drawer_l, toolbar, R.string.app_name, R.string.app_name
        )
        drawer_l.addDrawerListener(toggle)
        toggle.syncState()
    }

    private fun bottomSheetMarkerCar() {
        from(bottom_sheet).state = STATE_HIDDEN

        bottom_sheet.bottomSheetStateCallback { state ->
            when (state) {
                STATE_COLLAPSED -> {
                    bottom_navigation_v.visibility = VISIBLE
                    bottom_navigation_v.animate().translationY(0f).alpha(1.0f)

                    marker_fab.hide()
                    location_fab.animate().translationX(200f).alpha(0.0f)
                    directions_walk_fab.show()

                    app_bar_l.animate().translationY(-200f).alpha(0.0f)

                    drawer_l.setDrawerLockMode(LOCK_MODE_LOCKED_CLOSED)
                }
                STATE_HIDDEN -> {
                    bottom_navigation_v.animate().translationY(150f).alpha(0.0f)

                    marker_fab.show()
                    location_fab.animate().translationX(0f).alpha(1.0f)
                    directions_walk_fab.hide()

                    app_bar_l.animate().translationY(0f).alpha(1.0f)

                    drawer_l.setDrawerLockMode(LOCK_MODE_UNLOCKED)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        bottom_navigation_v.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.time -> startActivity<NotificationActivity>()
                R.id.share -> share("${routeEndLocation.latitude},${routeEndLocation.longitude}")
                R.id.more -> dialogMarkerCarMenu()
            }
            true
        }
        return true
    }

    private fun drawPolyline() {
        routeStartLocation = userLocationLayer.cameraPosition()!!.target
        if (!markerCarPolyline) {
            markerCarPolylineObject = map_v.map.mapObjects.addCollection()
            val points = ArrayList<RequestPoint>()
            points.add(RequestPoint(routeStartLocation, WAYPOINT, null))
            points.add(RequestPoint(routeEndLocation, WAYPOINT, null))
            markerCarPedestrianRouter = TransportFactory.getInstance().createPedestrianRouter()
            markerCarPedestrianRouter.requestRoutes(points, TimeOptions(), this)
        } else {
            cameraPolylinePosition()
        }
    }

    private fun cameraPolylinePosition() {
        val screenCenter = Point(
            (routeStartLocation.latitude + routeEndLocation.latitude) / 2,
            (routeStartLocation.longitude + routeEndLocation.longitude) / 2
        )
        map_v.map.move(CameraPosition(screenCenter, 14f, 0f, 0f), Animation(SMOOTH, 1f), null)
    }

    override fun onMasstransitRoutes(routes: List<Route>) {
        if (routes.isNotEmpty()) {
            val polylineMapObject = markerCarPolylineObject.addPolyline(routes[0].geometry)
            polylineMapObject.strokeColor = getColor(this, R.color.colorDot)
            polylineMapObject.outlineColor = getColor(this, R.color.colorWayOutline)
            polylineMapObject.outlineWidth = 1f

            cameraPolylinePosition()

            val distance = routes[0].sections[0].metadata.weight.walkingDistance.text
            val time = routes[0].sections[0].metadata.weight.time.text
            val combination = "$distance \u00B7 $time \u00B7\uD83D\uDEB6"
            distance_tv.text = combination
            distance_rl.visibility = VISIBLE

            markerCarPolyline = true
        }
    }

    override fun onMasstransitRoutesError(error: Error) {}

    private fun dialogMarkerCarMenu() {
        val deleteMarkerCarPolyline = getString(R.string.dialog_menu_item_delete_polyline)
        val deleteMarkerCar = getString(R.string.dialog_menu_item_delete_marker_car)
        val listItem = listOf(deleteMarkerCarPolyline, deleteMarkerCar)
        val menuTitle = getString(R.string.dialog_menu_title)
        selector(menuTitle, listItem) { _, itemId ->
            when (itemId) {
                0 -> deleteMarkerCarPolyline()
                1 -> deleteMarkerCar()
            }
        }
    }

    private fun deleteMarkerCarPolyline() {
        if (markerCarPolyline) {
            markerCarPolylineObject.clear()

            distance_rl.visibility = GONE

            markerCarPolyline = false
        }
    }

    private fun deleteMarkerCar() {
        if (markerCar) {
            deleteMarkerCarPolyline()

            markerCarObject.clear()

            markerCarViewModel.deleteTarget()
            markerCarViewModel.deleteReminder()

            notification_rl.visibility = GONE

            markerCarStreet = false
            markerCarPanorama = false
            markerCar = false

            from(bottom_sheet).state = STATE_HIDDEN
        }
    }

    override fun onBackPressed() {
        val stateDrawer = drawer_l.isDrawerOpen(START)
        val stateCollapsed = from(bottom_sheet).state == STATE_COLLAPSED
        val stateExpanded = from(bottom_sheet).state == STATE_EXPANDED
        val stateCursor = search_place_et.isCursorVisible
        when {
            stateDrawer -> drawer_l.closeDrawer(START)
            stateCollapsed || stateExpanded -> from(bottom_sheet).state = STATE_HIDDEN
            stateCursor -> searchPlaceCursorOff()
            !stateCollapsed || !stateDrawer || !stateCursor -> super.onBackPressed()
        }
    }

    override fun onStop() {
        map_v.onStop()
        panorama_v.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        map_v.onStart()
        panorama_v.onStart()
    }

    companion object {
        const val mapKitApiKey = "e4b59fa0-e067-42ae-9044-5c6a038503e9"
        const val requestPermissionLocation = 1
        const val latLngPattern = "(^[-+]?(?:[1-8]?\\d(?:\\.\\d+)?|90(?:\\.0+)?))," +
                "\\s*([-+]?(?:180(?:\\.0+)?|(?:(?:1[0-7]\\d)|(?:[1-9]?\\d))(?:\\.\\d+)?))\$"
    }
}