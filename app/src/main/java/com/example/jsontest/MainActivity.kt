package com.example.jsontest

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.os.AsyncTask
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.text.method.ScrollingMovementMethod
import android.util.Base64.*
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import com.example.jsontest.databinding.ActivityMainBinding
import com.example.jsontest.retrofit.AirQaulityResponse
import com.example.jsontest.retrofit.AirQualityService
import com.example.jsontest.retrofit.RetrofitConnection
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.IllegalArgumentException
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*


private val TAG = "jsontest"
private val IP_ADDRESS = "221.140.227.176:7000"
private const val TAG_JSON = "younsoo"
private const val TAG_ID = "id"
private const val TAG_NAME = "name"

private var TextviewIDCK: TextView? = null

private var mArrayList: ArrayList<HashMap<String, String>>? = null
private var mJsonString: String? = null

class MainActivity : AppCompatActivity() {

    //뒤로가기 버튼 누른 시간
    var backPressedTime: Long = 0

    lateinit var binding: ActivityMainBinding

    //런타임 권한 요청 시 필요한 요청 코드
    private val PERMISSION_REQUEST_CODE = 100
    //요청할 권한 목록
    var REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION)

    //==위치 서비스 요청 시 필요한 런처==
    lateinit var getGPSPermissionLauncher: ActivityResultLauncher<Intent>

    // 위도와 경도를 가져온다
    lateinit var locationProvider: LocationProvider


    var userID : String? = null
    var checkTime : String? = null
    var dustData : String? = null
    var weatherDatas : String? = null


    //뒤로가기 버튼 누르면 호출되는 함수
    override fun onBackPressed() {
        //현재시간보다 크면 종료
        if(backPressedTime+3000 > System.currentTimeMillis()){
            super.onBackPressed()
            ActivityCompat.finishAffinity(this)   //액티비티 종료
            System.runFinalization()    //현재 작업중인 쓰레드가 다 종료되면, 종료 시키라는 명령어
            System.exit(0)  //현재 액티비티를 종료시킨다
        }else{
            Toast.makeText(applicationContext, "한번 더 뒤로가기 버튼을 누르면 종료됩니다.",
                Toast.LENGTH_SHORT).show()
       }

        //현재 시간 담기
        backPressedTime = System.currentTimeMillis()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        TextviewIDCK = findViewById(binding.tvUserName.id) as  TextView?
        TextviewIDCK!!.movementMethod = ScrollingMovementMethod()

        //val intent = getIntent();
        if(intent.hasExtra("name")){
            userID=intent.getStringExtra("name")
            println("다른 액티비티에서 변수 불러오기!! : "+userID)

            //유저 아이디 화면에 표시하기(임시로 지움)
            //binding.tvUserName.text=userID
        }
        else{
            Toast.makeText(this, "Error!", Toast.LENGTH_SHORT).show()
        }

        val task = SearchData()
        mArrayList?.clear()
        task.execute(userID)

        binding.btnUserif.setOnClickListener {
            val nextintent = Intent(this, DiaryActivity::class.java)
            nextintent.putExtra("name", userID)
            nextintent.putExtra("time", checkTime)
            nextintent.putExtra("dust", dustData)
            nextintent.putExtra("weat", weatherDatas)
            startActivity(nextintent)
        }

        //사용자 정보 클릭하기
        binding.tvUserName.setOnClickListener {
            val userintent = Intent(this, UserIfActivity::class.java)
            userintent.putExtra("user", TextviewIDCK!!.getText().toString())
            userintent.putExtra("name", userID)
            userintent.putExtra("time", checkTime)
            userintent.putExtra("dust", dustData)
            userintent.putExtra("weat", weatherDatas)
            startActivity(userintent)
        }

        checkAllPermission()
        updateUI()  //====
        setRefreshButton()

    }



    private fun setRefreshButton() {
        binding.btnRefresh.setOnClickListener {
            updateUI()
        }
    }

    private fun updateUI() {
        locationProvider = LocationProvider(this@MainActivity)

        //위도와 경도 정보 가져옴
        val latitude: Double = locationProvider.getLocationLatitude()
        val longitude: Double = locationProvider.getLocationLongitude()

        println("$latitude"+" :위도   "+"$longitude"+" :경도")

        if(latitude != 0.0 || longitude != 0.0) {
            // 1. 현재 위치를 가져오고 UI 업데이트
            val address = getCurrentAddress(latitude, longitude)

            address?.let{
                val subadd = it.getAddressLine(0).toString().split(' ') //공백 기준으로 문자열 자르기

                binding.tvLocationTitle.text = "${subadd[2]}" //ex 부평구

                binding.tvLocationSubtitle.text = "${it.countryName} ${it.adminArea}"   //ex 대한민국 인천광역시
            }

            // 2. 현재 미세먼지 농도 가져오고 UI 업데이트
            getAirQualityData(latitude, longitude)
        }
        else {
            Toast.makeText(
                this@MainActivity,
                "위도, 경도 정보를 가져올 수 없습니다. 새로고침을 눌러주세요!",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // @desc 레트로핏 클래스를 이용하여 미세먼지 오염 정보를 가져옴
    private fun getAirQualityData(latitude: Double, longitude: Double) {
        val retrofitAPI = RetrofitConnection.getInstance().create(AirQualityService::class.java)    //레트로핏 객체를 이용해 AirQualityService 인터페이스 구현체를 가져옴

        retrofitAPI.getAirQualityData(
            latitude.toString(),
            longitude.toString(),
            "3aca78dd-9d03-40d6-a552-b1711d076458"
        ).enqueue(object : Callback<AirQaulityResponse> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(
                call: Call<AirQaulityResponse>,
                response: Response<AirQaulityResponse>
            ) {
                //정상적인 Respinse가 왔다면 UI 업데이트
                if(response.isSuccessful) {
                    Toast.makeText(this@MainActivity, "최신 정보 업데이트 완료!", Toast.LENGTH_SHORT).show()
                    //response.body()가 null이 아니면 updateAirUI()
                    response.body()?.let{updateAirUI(it)}
                }
                else{
                    Toast.makeText(this@MainActivity, "업데이트에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AirQaulityResponse>, t: Throwable) {
                t.printStackTrace()
                Toast.makeText(this@MainActivity, "업데이트에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    //가져온 데이터 정보를 바탕으로 화면 업데이트
    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateAirUI(airQualityData: AirQaulityResponse) {
        val pollutionData = airQualityData.data.current.pollution   //미세먼지 정보 가져오기
        val weatherData = airQualityData.data.current.weather      //날시 정보 가져오기

        //수치 지정 (메인 화면 가운데 숫자)
        binding.tvCount.text = pollutionData.aqius.toString()

        //측정된 날짜 지정
        val dateTime = ZonedDateTime.parse(pollutionData.ts).withZoneSameInstant(
            ZoneId.of("Asia/Seoul")).toLocalDateTime()
        val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

        //관측 시간 보여주기
        checkTime = dateTime.format(dateFormatter).toString()
        binding.tvCheckTime.text = checkTime

        when (pollutionData.aqius) {
            in 0..50 -> {
                binding.tvTitle.text = "좋음"
                binding.imgBg.setImageResource(R.drawable.bg_good)
                dustData="좋음"
            }
            in 51..150 -> {
                binding.tvTitle.text = "보통"
                binding.imgBg.setImageResource(R.drawable.bg_soso)
                dustData="보통"
            }
            in 151..200 -> {
                binding.tvTitle.text = "나쁨"
                binding.imgBg.setImageResource(R.drawable.bg_bad)
                dustData="나쁨"
            }
            else -> {
                binding.tvTitle.text = "매우 나쁨"
                binding.imgBg.setImageResource(R.drawable.bg_worst)
                dustData="매우 나쁨"
            }
        }

        println("######## 날씨정보: "+weatherData.ic)

        if(weatherData.ic.equals("01d")){
            binding.imgWt.setImageResource(R.drawable.ic_01d)
            weatherDatas="ic_01d"
        }
        else if(weatherData.ic.equals("01n")){
            binding.imgWt.setImageResource(R.drawable.ic_01n)
            weatherDatas="ic_01n"
        }
        else if(weatherData.ic.equals("02d")){
            binding.imgWt.setImageResource(R.drawable.ic_02d)
            weatherDatas="ic_02d"
        }
        else if(weatherData.ic.equals("02n")){
            binding.imgWt.setImageResource(R.drawable.ic_02n)
            weatherDatas="ic_02n"
        }
        else if(weatherData.ic.equals("03d") || weatherData.ic.equals("03n")){
            binding.imgWt.setImageResource(R.drawable.ic_03d)
            weatherDatas="ic_03d"
        }
        else if(weatherData.ic.equals("04d") || weatherData.ic.equals("04n")){
            binding.imgWt.setImageResource(R.drawable.ic_04d)
            weatherDatas="ic_04d"
        }
        else if(weatherData.ic.equals("09d") || weatherData.ic.equals("09n")){
            binding.imgWt.setImageResource(R.drawable.ic_09d)
            weatherDatas="ic_09d"
        }
        else if(weatherData.ic.equals("10n") || weatherData.ic.equals("10d")){
            binding.imgWt.setImageResource(R.drawable.ic_10n)
            weatherDatas="ic_10n"
        }
        else if(weatherData.ic.equals("11d") || weatherData.ic.equals("11n")){
            binding.imgWt.setImageResource(R.drawable.ic_11d)
            weatherDatas="ic_11d"
        }
        else if(weatherData.ic.equals("13d") || weatherData.ic.equals("13n")){
            binding.imgWt.setImageResource(R.drawable.ic_13d)
            weatherDatas="ic_13d"
        }
        else if(weatherData.ic.equals("50d") || weatherData.ic.equals("50n")){
            binding.imgWt.setImageResource(R.drawable.ic_50d)
            weatherDatas="ic_50d"
        }
        else{
            binding.imgWt.setImageResource(R.drawable.iocn_thunder)
            weatherDatas="iocn_thunder"
        }

    }

    fun getCurrentAddress(latitude: Double, longitude: Double) : Address? {
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses: List<Address>?   //Address 객체는 주소와 관련된 여러 정보 가짐

        addresses = try {
            geocoder.getFromLocation(latitude, longitude, 1)    //Geocoder 객체를 이용하여 위도와 경도로부터 리스트 가져옴
        } catch (ioException: IOException) {
            Toast.makeText(this, "지오코더 서비스 사용불가!", Toast.LENGTH_LONG).show()
            return null
        } catch (illegalArgumentException: IllegalArgumentException) {
            Toast.makeText(this, "잘못된 위도, 경도 입니다!", Toast.LENGTH_LONG).show()
            return null
        }

        //에러는 없지만 주소가 발견되지 않은 경우
        if (addresses == null || addresses.size == 0) {
            Toast.makeText(this, "주소가 발견되지 않았습니다.", Toast.LENGTH_LONG).show()
            return null
        }

        println(addresses.get(0).getAddressLine(0).toString())


        val address: Address = addresses[0]

        return address
    }

    private fun checkAllPermission() {
        // 1. gps가 켜져있는지 확인
        if(!isLocationServicesAvailable()){
            showDialogForLocationServiceSetting();
        }
        else {
            //2. 런타임 앱 권한이 모두 허용되어 있는지 확인
            isRunTimePermissionsGranted();
        }
    }

    fun isLocationServicesAvailable(): Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        return (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
    }

    fun isRunTimePermissionsGranted() {
        //위치 퍼미션을 가지고 있는지 체크
        val hasFineLocationPermission = ContextCompat.checkSelfPermission(
            this@MainActivity,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(
            this@MainActivity,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED ||
                hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED) {
            //권한이 한개라도 없다면 퍼미션 요청
            ActivityCompat.requestPermissions(this@MainActivity,REQUIRED_PERMISSIONS,PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.size == REQUIRED_PERMISSIONS.size){
            //요청코드가 PERMISSION_REQUEST_CODE이고, 요청한 퍼미션 개수만큼 수신되었다면
            var checkResult = true

            //모든 퍼미션을 허용했는지 체크
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    checkResult = false
                    break
                }
            }
            if (checkResult) {
                //위칫값을 가져올 수 있음
                updateUI()
            }
            else{
                //퍼미션이 거부되었다면 앱 종료
                Toast.makeText(
                    this@MainActivity,
                    "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요!",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
        }
    }

    private fun showDialogForLocationServiceSetting() {
        //런처를 이용하여 결과값을 반환해야 하는 인텐트를 실행
        getGPSPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {result ->
            //결과값을 받았을 때 로직
            if (result.resultCode == Activity.RESULT_OK) {
                //사용자가 gps를 활성화시켰는지 확인
                if (isLocationServicesAvailable()){
                    isRunTimePermissionsGranted()   //런타임 권한 확인
                }
                else {
                    //위치 서비스가 허용되지 않았다면 앱 종료
                    Toast.makeText(
                        this@MainActivity,
                        "위치 서비스를 사용할 수 없습니다.",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
            }
        }

        val builder: AlertDialog.Builder = AlertDialog.Builder(this@MainActivity)

        builder.setTitle("위치 서비스 비활성화") //제목 설정
        builder.setMessage("위치 서비스가 꺼져 있습니다. 설정해야 앱을 사용할 수 있습니다.") //내용설정

        builder.setCancelable(true) // 다이얼로그 창 바깥 터치 시 창 닫힘
        builder.setPositiveButton("설정", DialogInterface.OnClickListener{
            dialog, id->    //확인 버튼 설정
            val callGPSSettingIntent = Intent(
                Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                getGPSPermissionLauncher.launch(callGPSSettingIntent)
        })
        builder.setNegativeButton("취소", //취소버튼 설정
            DialogInterface.OnClickListener{
                dialog, id->
                dialog.cancel()
                Toast.makeText(this@MainActivity, "기기에서 위치서비스(GPS) 설정 후 사용해주세요!",
                Toast.LENGTH_LONG).show()
                finish()
            })
        builder.create().show() //다이얼로그 생성 및 보여주기
    }



    internal class SearchData : AsyncTask<String?, Void?, String?>() {
        var errorString: String? = null

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            TextviewIDCK!!.text = result
            Log.d(TAG, "response - $result")
            if (result == null) {
                //TextviewIDCK!!.text = errorString   //에러 메시지 출력
                TextviewIDCK!!.text = "비회원"         //에러 메시지 대신 "비회원" 문구 출력
            } else {
                mJsonString = result
                showResult()
            }
        }

        override fun doInBackground(vararg params: String?): String? {
            val searchKeyword1 = params[0]
            //val searchKeyword2 = params[1]
            val serverURL = "http://221.140.227.176:7000/namesh.php"  //경로 C:\\wamp64\www
            val postParameters = "name=$searchKeyword1"
            return try {
                val url = URL(serverURL)
                val httpURLConnection = url.openConnection() as HttpURLConnection
                httpURLConnection.readTimeout = 5000
                httpURLConnection.connectTimeout = 5000
                httpURLConnection.requestMethod = "POST"
                httpURLConnection.doInput = true
                httpURLConnection.connect()
                val outputStream = httpURLConnection.outputStream
                outputStream.write(postParameters.toByteArray(charset("UTF-8")))
                outputStream.flush()
                outputStream.close()
                val responseStatusCode = httpURLConnection.responseCode
                Log.d(TAG, "response code - $responseStatusCode")
                val inputStream: InputStream
                inputStream = if (responseStatusCode == HttpURLConnection.HTTP_OK) {
                    httpURLConnection.inputStream
                } else {
                    httpURLConnection.errorStream
                }
                val inputStreamReader = InputStreamReader(inputStream, "UTF-8")
                val bufferedReader = BufferedReader(inputStreamReader)
                val sb = java.lang.StringBuilder()
                var line: String?
                while (bufferedReader.readLine().also { line = it } != null) {
                    sb.append(line)
                }
                bufferedReader.close()
                sb.toString().trim { it <= ' ' }
            } catch (e: java.lang.Exception) {
                Log.d(TAG, "InsertData: Error ", e);
                errorString = e.toString();

                return null
            }
        }

        private fun showResult() {
            try {
                val jsonObject = JSONObject(mJsonString)
                val jsonArray: JSONArray = jsonObject.getJSONArray(TAG_JSON)
                for (i in 0 until jsonArray.length()) {
                    val item = jsonArray.getJSONObject(i)
                    val id = item.getString(TAG_ID)
                    val name = item.getString(TAG_NAME)
                    val hashMap: HashMap<String, String> = HashMap()
                    hashMap[TAG_ID] = id
                    hashMap[TAG_NAME] = name
                    mArrayList?.add(hashMap)
                }
                println("===리스트 출력!!   "+mArrayList)


                //로그인 후 다른 액티비티로 전환하기
                //val nextIntent = Intent(mContext, MainActivity::class.java)
                //mContext?.startActivity(nextIntent)


//                val adapter: ListAdapter = SimpleAdapter(
//                    this,
//                    mArrayList,
//                    R.layout.item_list,
//                    arrayOf(TAG_ID, TAG_NAME, TAG_ADDRESS),
//                    intArrayOf(
//                        R.id.textView_list_id,
//                        R.id.textView_list_name,
//                        R.id.textView_list_address
//                    )
//                )
//                mListViewList.setAdapter(adapter)
            } catch (e: JSONException) {
                Log.d(TAG, "showResult : ", e)
            }
        }
    }

}
