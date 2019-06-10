package tk.ifroz.loctrackcar.ui.activity

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.PointF
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.*
import androidx.core.view.GravityCompat.START
import androidx.drawerlayout.widget.DrawerLayout.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders.of
import androidx.work.Data
import androidx.work.WorkInfo
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
import org.jetbrains.anko.browse
import org.jetbrains.anko.selector
import org.jetbrains.anko.share
import org.jetbrains.anko.startActivity
import ru.ifr0z.core.custom.ImageProviderCustom
import ru.ifr0z.core.extension.bottomSheetStateCallback
import ru.ifr0z.core.extension.onEditorAction
import ru.ifr0z.core.extension.onTextChanges
import ru.ifr0z.core.livedata.ConnectivityLiveData
import tk.ifroz.loctrackcar.R
import tk.ifroz.loctrackcar.db.entity.Target
import tk.ifroz.loctrackcar.ui.work.GeocoderWork.Companion.FORMAT_DATA
import tk.ifroz.loctrackcar.ui.work.GeocoderWork.Companion.GEOCODE_DATA
import tk.ifroz.loctrackcar.ui.work.GeocoderWork.Companion.OUTPUT_DATA
import tk.ifroz.loctrackcar.ui.work.GeocoderWork.Companion.RESULTS_DATA
import tk.ifroz.loctrackcar.viewmodel.CarViewModel
import tk.ifroz.loctrackcar.viewmodel.GeocoderViewModel
import tk.ifroz.loctrackcar.viewmodel.NotificationViewModel

class MainActivity : AppCompatActivity(), UserLocationObjectListener, CameraListener, RouteListener,
    SearchListener, OnNavigationItemSelectedListener {

    private var isPermission = false
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
    private lateinit var notificationViewModel: NotificationViewModel

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
                if (grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED) {
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

        bottomSheetCar()

        car_fab.setOnClickListener {
            if (isPermission) {
                noAnchor()

                if (isCar) {
                    map_v.map.move(
                        CameraPosition(routeEnd, 16f, 0f, 0f), Animation(SMOOTH, 1f), null
                    )
                } else {
                    insertCar()
                }

                from(bottom_sheet).state = STATE_COLLAPSED
            } else {
                checkPermission()
            }
        }
        location_fab.setOnClickListener {
            if (isPermission) {
                cameraPositionUser()

                isFollowUser = true
            } else {
                checkPermission()
            }
        }
        walk_fab.setOnClickListener {
            drawPedestrian()
        }

        retrieveCar()
    }

    private fun insertCar() {
        val cameraPositionLatitude = userLocationLayer.cameraPosition()!!.target.latitude
        val cameraPositionLongitude = userLocationLayer.cameraPosition()!!.target.longitude
        carViewModel.insertTarget(Target(cameraPositionLatitude, cameraPositionLongitude))
    }

    private fun retrieveCar() {
        carViewModel = of(this).get(CarViewModel::class.java)
        carViewModel.targets.observe(this, Observer { target ->
            target?.let {
                routeEnd = Point(target.latitude, target.longitude)
                drawCar(routeEnd)
            }
        })
    }

    private fun drawCar(routeEnd: Point) {
        carObject = map_v.map.mapObjects.addCollection()
        carPlacemark = carObject.addPlacemark(routeEnd)
        val bitmap = ImageProviderCustom(this, R.drawable.ic_marker_black_45dp).image
        carPlacemark.setIcon(fromBitmap(bitmap))
        carPlacemark.setIconStyle(IconStyle().setAnchor(PointF(0.5f, 1f)))

        dataCar(routeEnd.latitude, routeEnd.longitude)

        isCar = true
    }

    private fun dataCar(latitude: Double, longitude: Double) {
        val subtitleLatitude = getString(R.string.latitude_subtitle)
        val subtitleLongitude = getString(R.string.longitude_subtitle)
        val coordinates = "$subtitleLatitude $latitude\n$subtitleLongitude $longitude"
        coordinates_tv.text = coordinates

        ConnectivityLiveData(this).observe(this, Observer { isNetworkAvailable ->
            if (isNetworkAvailable && !isPanorama) {
                showPanorama(routeEnd)
            }
        })

        geocoderViewModel = of(this).get(GeocoderViewModel::class.java)
        val geocode = "$longitude,$latitude"
        val format = "json"
        val results = "1"
        val data = Data.Builder().putString(GEOCODE_DATA, geocode).putString(FORMAT_DATA, format)
            .putString(RESULTS_DATA, results).build()

        geocoderViewModel.getStreetName(data)
        geocoderViewModel.outputStatus.observe(
            this, Observer<List<WorkInfo>> { listOfWorkInfo ->
                listOfWorkInfo?.let {
                    if (listOfWorkInfo.isNullOrEmpty()) {
                        return@Observer
                    }
                    val workInfo = listOfWorkInfo[0]
                    if (workInfo.state.isFinished) {
                        val addressName = workInfo.outputData.getString(OUTPUT_DATA)
                        address_tv.text = addressName
                    }
                }
            }
        )

        carViewModel.reminders.observe(this, Observer { reminder ->
            reminder?.let {
                notification_tv.text = reminder.reminder

                from(bottom_sheet).state = STATE_EXPANDED
            }
        })

        notificationViewModel = of(this).get(NotificationViewModel::class.java)
        notificationViewModel.outputStatus.observe(
            this, Observer<List<WorkInfo>> { listOfWorkInfo ->
                listOfWorkInfo?.let {
                    if (listOfWorkInfo.isNullOrEmpty()) {
                        return@Observer
                    }
                    val workInfo = listOfWorkInfo[0]
                    if (workInfo.state.isFinished) {
                        notification_rl.visibility = GONE
                    } else {
                        notification_rl.visibility = VISIBLE
                    }
                }
            }
        )
    }

    private fun showPanorama(routeEnd: Point) {
        val panoramaService = PlacesFactory.getInstance().createPanoramaService()
        panoramaService.findNearest(Point(routeEnd.latitude, routeEnd.longitude), this)
    }

    override fun onPanoramaSearchResult(panoramaId: String) {
        panorama_v.player.openPanorama(panoramaId)
        panorama_v.player.logo.setAlignment(Alignment(RIGHT, TOP))
        panorama_v.setNoninteractive(true)

        isPanorama = true
    }

    override fun onPanoramaSearchError(error: Error) {}

    private fun onMapReady() {
        userLocationLayer = map_v.map.userLocationLayer
        userLocationLayer.isEnabled = true
        userLocationLayer.isHeadingEnabled = true
        userLocationLayer.setObjectListener(this)

        map_v.map.addCameraListener(this)

        cameraPositionUser()

        map_scale_v.metersOnly()

        isPermission = true
    }

    private fun cameraPositionUser() {
        if (userLocationLayer.cameraPosition() != null) {
            routeStart = userLocationLayer.cameraPosition()!!.target
            map_v.map.move(CameraPosition(routeStart, 16f, 0f, 0f), Animation(SMOOTH, 1f), null)
        } else {
            map_v.map.move(CameraPosition(Point(0.0, 0.0), 16f, 0f, 0f))
        }
    }

    override fun onCameraPositionChanged(
        map: Map, cPos: CameraPosition, cUpd: CameraUpdateSource, finish: Boolean
    ) {
        map_scale_v.update(map.cameraPosition.zoom, map.cameraPosition.target.latitude)

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
            PointF((map_v.width * 0.5).toFloat(), (map_v.height * 0.5).toFloat()),
            PointF((map_v.width * 0.5).toFloat(), (map_v.height * 0.83).toFloat())
        )

        location_fab.setImageResource(R.drawable.ic_my_location_black_24dp)

        isFollowUser = false
    }

    private fun noAnchor() {
        userLocationLayer.resetAnchor()

        location_fab.setImageResource(R.drawable.ic_gps_not_fixed_black_24dp)
    }

    override fun onObjectAdded(userLocationView: UserLocationView) {
        setAnchor()

        val bitmap = ImageProviderCustom(this, R.drawable.ic_dot_rose_24dp).image
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

        val requestStart = getString(R.string.search_place_request_start)
        val requestEnd = getString(R.string.search_place_request_end)
        search_place_et.onEditorAction(
            IME_ACTION_SEARCH, coordinatesPattern, requestStart, requestEnd
        ) { arrayLatLng ->
            searchPlaceCursorOff()

            if (!isMarker) {
                val markerLatitude = arrayLatLng!![0].toDouble()
                val markerLongitude = arrayLatLng[1].toDouble()
                drawMarker(markerLatitude, markerLongitude)
            }
        }

        search_place_et.onTextChanges(clear_search_iv)
        clear_search_iv.setOnClickListener {
            if (isMarker) {
                markerObject.clear()

                isMarker = false
            }

            search_place_et.text.clear()
        }
    }

    private fun drawMarker(markerLatitude: Double, markerLongitude: Double) {
        markerObject = map_v.map.mapObjects.addCollection()
        val point = Point(markerLatitude, markerLongitude)
        markerPlacemark = markerObject.addPlacemark(point)
        val bitmap = ImageProviderCustom(this, R.drawable.ic_place_black_45dp).image
        markerPlacemark.setIcon(fromBitmap(bitmap))
        markerPlacemark.setIconStyle(IconStyle().setAnchor(PointF(0.5f, 1f)))

        map_v.map.move(CameraPosition(point, 16f, 0f, 0f), Animation(SMOOTH, 1f), null)

        isMarker = true
    }

    private fun searchPlaceCursorOn() {
        search_place_et.isCursorVisible = true

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            searchPlaceCursorOff()
        }

        drawer_l.setDrawerLockMode(LOCK_MODE_LOCKED_CLOSED)

        if (isPermission) {
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

    private fun bottomSheetCar() {
        from(bottom_sheet).state = STATE_HIDDEN

        bottom_sheet.bottomSheetStateCallback { state ->
            when (state) {
                STATE_COLLAPSED -> {
                    bottom_navigation_v.visibility = VISIBLE
                    bottom_navigation_v.animate().translationY(0f).alpha(1.0f)

                    car_fab.hide()
                    location_fab.animate().translationX(200f).alpha(0.0f)
                    walk_fab.show()

                    app_bar_l.animate().translationY(-200f).alpha(0.0f)

                    drawer_l.setDrawerLockMode(LOCK_MODE_LOCKED_CLOSED)
                }
                STATE_HIDDEN -> {
                    bottom_navigation_v.animate().translationY(150f).alpha(0.0f)

                    car_fab.show()
                    location_fab.animate().translationX(0f).alpha(1.0f)
                    walk_fab.hide()

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
                R.id.share -> share("${routeEnd.latitude},${routeEnd.longitude}")
                R.id.more -> dialogMenuCar()
            }
            true
        }
        return true
    }

    private fun drawPedestrian() {
        routeStart = userLocationLayer.cameraPosition()!!.target
        if (!isPedestrian) {
            carPedestrianObject = map_v.map.mapObjects.addCollection()
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
        val screenCenter = Point(
            (routeStart.latitude + routeEnd.latitude) / 2,
            (routeStart.longitude + routeEnd.longitude) / 2
        )
        map_v.map.move(CameraPosition(screenCenter, 14f, 0f, 0f), Animation(SMOOTH, 1f), null)
    }

    override fun onMasstransitRoutes(routes: List<Route>) {
        if (routes.isNotEmpty()) {
            val pedestrianMapObject = carPedestrianObject.addPolyline(routes[0].geometry)
            pedestrianMapObject.strokeColor = getColor(this, R.color.colorDot)
            pedestrianMapObject.outlineColor = getColor(this, R.color.colorWayOutline)
            pedestrianMapObject.outlineWidth = 1f

            cameraPositionPedestrian()

            val distance = routes[0].sections[0].metadata.weight.walkingDistance.text
            val time = routes[0].sections[0].metadata.weight.time.text
            val combination = "$distance \u00B7 $time \u00B7\uD83D\uDEB6"
            distance_tv.text = combination
            distance_rl.visibility = VISIBLE

            isPedestrian = true
        }
    }

    override fun onMasstransitRoutesError(error: Error) {}

    private fun dialogMenuCar() {
        val deletePedestrian = getString(R.string.dialog_menu_item_delete_pedestrian)
        val deleteCar = getString(R.string.dialog_menu_item_delete_car)
        val menuTitle = getString(R.string.dialog_menu_title)
        val listItem = listOf(deletePedestrian, deleteCar)
        selector(menuTitle, listItem) { _, itemId ->
            when (itemId) {
                0 -> deletePedestrian()
                1 -> deleteCar()
            }
        }
    }

    private fun deletePedestrian() {
        if (isPedestrian) {
            carPedestrianObject.clear()

            distance_rl.visibility = GONE

            isPedestrian = false
        }
    }

    private fun deleteCar() {
        if (isCar) {
            deletePedestrian()

            carObject.clear()

            carViewModel.deleteTarget()
            carViewModel.deleteReminder()
            notificationViewModel.cancel()

            isPanorama = false
            isCar = false

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
        const val coordinatesPattern = "(^[-+]?(?:[1-8]?\\d(?:\\.\\d+)?|90(?:\\.0+)?))," +
                "\\s*([-+]?(?:180(?:\\.0+)?|(?:(?:1[0-7]\\d)|(?:[1-9]?\\d))(?:\\.\\d+)?))\$"
    }
}