package com.example.webexandroid.search

import android.Manifest
import android.app.ActivityManager
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.webexandroid.R
import com.example.webexandroid.WebexViewModel
import com.example.webexandroid.databinding.ActivitySearch3Binding
import com.example.webexandroid.search.ui.dashboard.DashboardFragment
import com.example.webexandroid.search.ui.home.HomeFragment
import com.example.webexandroid.search.ui.notifications.NotificationsFragment
import com.example.webexandroid.utils.Constants
import com.example.webexandroid.utils.SharedPrefUtils
import kotlinx.android.synthetic.main.activity_dialer.*
import org.koin.android.viewmodel.ext.android.viewModel
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.webexandroid.WebexAndroidApp
import com.example.webexandroid.auth.LoginActivity

import androidx.navigation.fragment.NavHostFragment
import com.example.webexandroid.APIService
import com.example.webexandroid.SpeedHandler
import com.example.webexandroid.StatusMessageAPIService
import com.example.webexandroid.services.SpeedometerService
import com.example.webexandroid.databinding.FragmentNotificationsBinding
import com.example.webexandroid.person.PersonViewModel
import com.example.webexandroid.utils.showDialogWithMessage
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_search3.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import org.koin.android.ext.android.inject
import retrofit2.Retrofit
import java.util.*


class SearchActivity3 : AppCompatActivity(), SpeedHandler.SpeedUpdateListener {

    companion object {
        var isActivityRunning = false
    }

    private var isSpeedExceedLimit = false

    override fun onPause() {
        super.onPause()
        isActivityRunning = false
    }

    private fun isServiceRunning(serviceClassName: String?): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val services: List<ActivityManager.RunningServiceInfo> =
            activityManager.getRunningServices(Int.MAX_VALUE)
        for (runningServiceInfo in services) {
            if (runningServiceInfo.service.getClassName().equals(serviceClassName)) {
                return true
            }
        }
        return false
    }

    private lateinit var binding: ActivitySearch3Binding
    val webexViewModel: WebexViewModel by inject()
    val personViewModel: PersonViewModel by inject()
    val searchViewModel: SearchViewModel by viewModel()
    var currentFragment : Fragment? =null
    private var binding2: FragmentNotificationsBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try{
            binding = ActivitySearch3Binding.inflate(layoutInflater)
            setContentView(binding.root)

            webexViewModel.signOutListenerLiveData.observe(this@SearchActivity3, Observer {
                it?.let {
                    if (it) {
                        SharedPrefUtils.clearLoginTypePref(this)
                        (application as WebexAndroidApp).unloadKoinModules()
                        finish()
                    }
//                    else {
//                        progressLayout.visibility = View.GONE
//                    }
                }
            })

            val navView: BottomNavigationView = binding.navView

//            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
//            val navController = navHostFragment.navController
            val navController = Navigation.findNavController(this, R.id.fragment_nav)
            // Passing each menu ID as a set of Ids because each
            // menu should be considered as top level destinations.
            val appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.navigation_contacts,
                    R.id.navigation_aboutme,
                    R.id.navigation_logout
                )
            )
            setupActionBarWithNavController(navController, appBarConfiguration)
            navView.setupWithNavController(navController)
            nav_view.setOnItemSelectedListener { item ->
                var fragment: Fragment
                when (item.itemId) {
                    R.id.navigation_contacts -> {
                        toolbar?.setTitle("Webex Sample App")
                        fragment = HomeFragment()
                        replaceFragment(fragment)
                        true
                    }
//                    R.id.navigation_settings -> {
//                        //binding2?.sampleText?.visibility = View.GONE
//                        toolbar?.setTitle("Webex Sample App")
//                        fragment = DashboardFragment()
//                        replaceFragment(fragment)
//                        true
//                    }
                    R.id.navigation_aboutme -> {
                        toolbar?.setTitle("Webex Sample App")
                        fragment = NotificationsFragment()
                        replaceFragment(fragment)
                        true
                    }
                    R.id.navigation_logout -> {
                        //progressLayout.visibility = View.VISIBLE
                        webexViewModel.signOut()
                        //startActivity(Intent(this@SearchActivity3, LoginActivity::class.java))
                        true
                    }
                    else -> false
                }

            }
            navView.setOnItemReselectedListener { item ->
                when (item.itemId) {
                    R.id.navigation_settings -> {
                        Toast.makeText(this, "Home Item reselected", Toast.LENGTH_LONG).show()
                    }

                }
            }
            checkLocPermission()
            Log.e("AccessToekn","get access token")

            webexViewModel.getAccessToken()
            personViewModel.getMe()

        }
        catch (e: Exception) {
            Log.e(TAG, "onCreateView", e);
            throw e;
        }

    }

    fun checkLocPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val list = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            } else {
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                )
            }
            requestPermissions(
                list, 23
            )

        } else {
            Log.d("SpeedometerService", "Permission not granted")
            SpeedHandler.listener = this
            if (!isServiceRunning(SpeedometerService::class.java.name)) {
                Log.d("SpeedometerService", "Service is no running")
                startService(Intent(this, SpeedometerService::class.java))
            } else {
                Log.d("SpeedometerService", "Service already Running")
            }

            Log.d("SpeedometerService", "permission granted, requesting location updates")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        checkLocPermission()
    }

    override fun onDestroy() {
        super.onDestroy()
        SpeedHandler.listener = null
    }

    override fun onResume() {
        super.onResume()
        isActivityRunning = true
    }

    override fun onSpeedLimitUpdate(exceedLimit: Boolean) {
        isSpeedExceedLimit = exceedLimit
        if (exceedLimit) {
            rawJSON()
            setCustomStatusMessage()
            lockScreen(false)
            Log.d("SpeedLimit", "Speed is exceed limit")
        } else {
            lockScreen(true)
            Log.d("SpeedLimit", "Speed is normal")
        }
    }

    private fun lockScreen(shouldLock: Boolean) {
        if (!shouldLock) {
            this.window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            binding.ErrorTextView.visibility=View.VISIBLE
            binding.view2.visibility = View.VISIBLE
            setMargins(binding.container, 50, 50, 50, 50)
        } else {

            binding.ErrorTextView.visibility=View.GONE
            binding.view2.visibility = View.GONE
            setMargins(binding.container, 0, 0, 0, 0)
        }
    }

    private fun setMargins(v: View, l: Int, t: Int, r: Int, b: Int) {
        if (v.layoutParams is ViewGroup.MarginLayoutParams) {
            val p = v.layoutParams as ViewGroup.MarginLayoutParams
            p.setMargins(l, t, r, b)
            v.requestLayout()
        }
    }

    override fun isActivityRunning(): Boolean {
        return isActivityRunning
    }

    private fun setCustomStatusMessage() {

        val observer: Observer<String?> = Observer {
            var accessToken = it?: "No AccessToken Yet"
            var personId=""
            Log.e("AccessToken",accessToken)
            val retrofit = Retrofit.Builder()
                .baseUrl("https://usersub-a.wbx2.com")
                .build()

            // Create Service
            val service = retrofit.create(StatusMessageAPIService::class.java)
            personViewModel.person.observe(this) { model ->
                model?.let {
                    personId = it.personId
                    Log.e("personId",personId)
                    val jsonObject= JSONObject().apply {
                        put(
                            "compositions",
                            JSONArray().apply {
                                put(JSONObject().apply {
                                    put("composition",
                                        JSONObject().apply{
                                            put("customStatusMessage","Driving!")
                                        })
                                    put("ttl", "1")
                                    put("type","customStatus")
                                })
                            }
                        )
                        put("users",
                            JSONArray().apply {
                                put(personId)
                            }
                        )
                    }
                    val jsonObjectString = jsonObject.toString()
                    Log.d("PrettyJSON",jsonObjectString)

                    // Create RequestBody ( We're not using any converter, like GsonConverter, MoshiConverter e.t.c, that's why we use RequestBody )
                    val requestBody = jsonObjectString.toRequestBody("application/json".toMediaTypeOrNull())
                    CoroutineScope(Dispatchers.IO).launch {
                        // Do the POST request and get response
                        val response = service.createMessageChangeRequest("Bearer $accessToken",requestBody)

                        withContext(Dispatchers.Main) {
                            if (response.isSuccessful) {
                                Log.d("PrettyPrinted", response.code().toString())
                            } else {
                                Log.e("RETROFIT_ERROR", response.code().toString())
                            }
                        }
                    }
                }
            }
        }

        webexViewModel.accessToken.observe(this, observer)

    }

    fun rawJSON() {

        val observer: Observer<String?> = Observer {
            var accessToken = it?: "No AccessToken Yet"
            var personId=""
            Log.e("AccessToken",accessToken)
            // Create Retrofit
            val retrofit = Retrofit.Builder()
                .baseUrl("https://apheleia.a8.ciscospark.com")
                .build()

            // Create Service
            val service = retrofit.create(APIService::class.java)
            personViewModel.person.observe(this) { model ->
                model?.let {
                            personId = it.personId
                            Log.e("personId", personId)
                            // Create JSON using JSONObject
                            val jsonObject = JSONObject()
                            jsonObject.put("subject", personId)
                            jsonObject.put("eventType", "dnd")
                            jsonObject.put("ttl", "1")

                            // Convert JSONObject to String
                            val jsonObjectString = jsonObject.toString()
                            Log.d("PrettyJSON", jsonObjectString)

                            // Create RequestBody ( We're not using any converter, like GsonConverter, MoshiConverter e.t.c, that's why we use RequestBody )
                            val requestBody =
                                jsonObjectString.toRequestBody("application/json".toMediaTypeOrNull())

                            CoroutineScope(Dispatchers.IO).launch {
                                // Do the POST request and get response
                                val response = service.createEmployee(
                                    "Bearer $accessToken",
                                    requestBody
                                )

                                withContext(Dispatchers.Main) {
                                    if (response.isSuccessful) {

                                        // Convert raw JSON to pretty JSON using GSON library
                                        val gson = GsonBuilder().setPrettyPrinting().create()
                                        val prettyJson = gson.toJson(
                                            JsonParser.parseString(
                                                response.body()
                                                    ?.string() // About this thread blocking annotation : https://github.com/square/retrofit/issues/3255
                                            )
                                        )

                                        Log.d("PrettyPrinted", prettyJson)

                                    } else {

                                        Log.e("RETROFIT_ERROR", response.code().toString())

                                    }
                                }
                            }
                }
            }
        }

        webexViewModel.accessToken.observe(this, observer)
    }

    private fun loadFragment(fragment: Fragment) {
        // load fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_nav, fragment)
            .commit()
    }
    private fun replaceFragment(fragment: Fragment)
    {
        if(!fragment.equals(currentFragment))
        {
            val transaction=supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_nav, fragment)
            transaction.commit()
            currentFragment= fragment
        }
    }
}