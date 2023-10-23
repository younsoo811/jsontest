package com.example.jsontest

import android.R
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.jsontest.databinding.ActivityDiaryBinding
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL


private var dContext: Context? = null

private val TAG = "jsontest"
private val IP_ADDRESS = "221.140.227.176:7000"

private var TextviewIDCK: TextView? = null
private var dTextViewResult: TextView? = null
private var EtextId: EditText? = null
private var EtextPw: EditText? = null
private var EtextName: EditText? = null
private var EtextCall: EditText? = null


private const val TAG_JSON = "younsoo"
private const val TAG_ID = "uid"
private const val TAG_CONT = "dcontent"
private const val TAG_DATE = "ddate"
private const val TAG_DUST = "finedust"
private const val TAG_WEATHER = "weather"

private var mArrayList: ArrayList<HashMap<String, String>>? = null

private var mJsonString: String? = null

private var userID : String? = null
private var userif : String? = null
private var checkTime : String? = null
private var dustD : String? = null
private var weatherD : String? = null

private var dogList = arrayListOf<Dog>(
    //Dog("일기 내용 수정 테스트!!", "2023-06-05 10:00", "보통", "ic_01d"),
    //Dog("TEST", "2023-06-01 10:00", "좋음", "ic_01n")
)

class DiaryActivity : AppCompatActivity(), View.OnClickListener {

    //바인딩 객체 선언
    lateinit var binding: ActivityDiaryBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_diary)

        binding = ActivityDiaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dContext = this

        dTextViewResult = findViewById(binding.textViewDiaryResult.id) as TextView?
        //dTextViewResult!!.movementMethod = ScrollingMovementMethod()

        val dogAdapter = DiaryListAdapter(this, dogList)
        binding.mainListView.adapter = dogAdapter

        if(intent.hasExtra("name")){
            userID=intent.getStringExtra("name")
            checkTime=intent.getStringExtra("time")
            dustD=intent.getStringExtra("dust")
            weatherD=intent.getStringExtra("weat")

            binding.edtDate.setText(checkTime)
            println("메인 액티비티에서 변수 불러오기!! : "+userID)

            //회원의 다이어리 정보 불러오기
            updateUI()
        }
        else{
            Toast.makeText(this, "Error!", Toast.LENGTH_SHORT).show()
        }


        //리스트에 내용 추가하기
        binding.btnAdd.setOnClickListener{
            var contents=binding.edtDiary.text.toString()
            var date=binding.edtDate.text.toString()
            var dust=dustD.toString()
            var weather= weatherD.toString()

            mArrayList?.clear()

            //dogList.add(Dog(contents,date,dust,weather))

            //미세먼지 정보하고, 날씨정보 메인 액티비티에서 받아오기
            val task = JoinData()
            task.execute("http://$IP_ADDRESS/add_diary.php", userID, contents, date, dust, weather)

            updateUI()

        }


        binding.btnUserUp.setOnClickListener{
            //val nextintent = Intent(this, UpdateActivity::class.java)

            val task = seData()
            task.execute("http://$IP_ADDRESS/allsh.php", userID)

            //nextintent.putExtra("name", userID)
            //nextintent.putExtra("nameif", userif)
            //startActivity(nextintent)
        }

        binding.btnUserDel.setOnClickListener(this)

        mArrayList = ArrayList()

    }

    override fun onClick(v: View?) {
        when(v?.id){
            binding.btnUserDel.id -> {
                val dlg= DelDialog(this)
                dlg.setOnOKClickedListener{content ->
                    val nextIntent = Intent(this,LoginActivity::class.java)
                    startActivity(nextIntent)

                    //회원기록 삭제하기
                    val task = DelData()
                    task.execute("http://$IP_ADDRESS/delete.php", userID)

                }
                dlg.show("회원탈퇴를 하시겠습니까?")
            }
        }
    }

    private fun updateUI(){
        dogList.clear() //리스트 초기화
        val task = getData()    //리스트 그리기
        task.execute("http://$IP_ADDRESS/get_diary.php", userID)

    }



    internal class JoinData : AsyncTask<String?, Void?, String>() {

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            dTextViewResult?.setText(result)
            Log.d(TAG, "POST response  - $result")
            if (result == null) {
                //mTextViewResult!!.text = errorString  //에러 메시지 출력
                dTextViewResult!!.text = "서버 응답 오류!\n 잠시 후에 다시 시도해 주세요."
            }
        }

        override fun doInBackground(vararg params: String?): String {
            val name = params[1]
            val dcontent = params[2]
            val ddate = params[3]
            val ddust = params[4]
            val dweather = params[5]
            val serverURL = params[0]
            val postParameters = "name=$name&dcontent=$dcontent&ddate=$ddate&ddust=$ddust&dweather=$dweather"
            return try {
                val url = URL(serverURL)
                val httpURLConnection: HttpURLConnection = url.openConnection() as HttpURLConnection
                httpURLConnection.setReadTimeout(5000)
                httpURLConnection.setConnectTimeout(5000)
                httpURLConnection.setRequestMethod("POST")
                httpURLConnection.connect()
                val outputStream: OutputStream = httpURLConnection.getOutputStream()
                outputStream.write(postParameters.toByteArray(charset("UTF-8")))
                outputStream.flush()
                outputStream.close()
                val responseStatusCode: Int = httpURLConnection.getResponseCode()
                Log.d(TAG, "POST response code - $responseStatusCode")
                val inputStream: InputStream
                inputStream = if (responseStatusCode == HttpURLConnection.HTTP_OK) {
                    httpURLConnection.getInputStream()
                } else {
                    httpURLConnection.getErrorStream()
                }
                val inputStreamReader = InputStreamReader(inputStream, "UTF-8")
                val bufferedReader = BufferedReader(inputStreamReader)
                val sb = StringBuilder()
                var line: String? = null
                while (bufferedReader.readLine().also { line = it } != null) {
                    sb.append(line)
                }
                bufferedReader.close()
                sb.toString()
            } catch (e: Exception) {
                Log.d(TAG, "InsertData: Error ", e)
                "Error: " + e.message
            }
        }
    }

    //다이어리 정보 가져오기
    internal class getData : AsyncTask<String?, Void?, String>() {


        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            //dTextViewResult?.setText(result)
            Log.d(TAG, "POST response  - $result")

            if (result == null) {
                //mTextViewResult!!.text = errorString  //에러 메시지 출력
                dTextViewResult!!.text = "서버 응답 오류!\n 잠시 후에 다시 시도해 주세요."
            }
            else{
                mJsonString = result
                showResult()
            }
        }

        override fun doInBackground(vararg params: String?): String {
            val name = params[1]
            val serverURL = params[0]
            val postParameters = "name=$name"
            return try {
                val url = URL(serverURL)
                val httpURLConnection: HttpURLConnection = url.openConnection() as HttpURLConnection
                httpURLConnection.setReadTimeout(5000)
                httpURLConnection.setConnectTimeout(5000)
                httpURLConnection.setRequestMethod("POST")
                httpURLConnection.connect()
                val outputStream: OutputStream = httpURLConnection.getOutputStream()
                outputStream.write(postParameters.toByteArray(charset("UTF-8")))
                outputStream.flush()
                outputStream.close()
                val responseStatusCode: Int = httpURLConnection.getResponseCode()
                Log.d(TAG, "POST response code - $responseStatusCode")
                val inputStream: InputStream
                inputStream = if (responseStatusCode == HttpURLConnection.HTTP_OK) {
                    httpURLConnection.getInputStream()
                } else {
                    httpURLConnection.getErrorStream()
                }
                val inputStreamReader = InputStreamReader(inputStream, "UTF-8")
                val bufferedReader = BufferedReader(inputStreamReader)
                val sb = StringBuilder()
                var line: String? = null
                while (bufferedReader.readLine().also { line = it } != null) {
                    sb.append(line)
                }
                bufferedReader.close()
                sb.toString()
            } catch (e: Exception) {
                Log.d(TAG, "InsertData: Error ", e)
                "Error: " + e.message
            }
        }


        private fun showResult() {
            try {
                val jsonObject = JSONObject(mJsonString)
                val jsonArray: JSONArray = jsonObject.getJSONArray(TAG_JSON)
                for (i in 0 until jsonArray.length()) {
                    val item = jsonArray.getJSONObject(i)
                    //val id = item.getString(TAG_ID)
                    val cont = item.getString(TAG_CONT)
                    val date = item.getString(TAG_DATE)
                    val dust = item.getString(TAG_DUST)
                    val weat = item.getString(TAG_WEATHER)
                    //val hashMap: HashMap<String, String> = HashMap()
                    //hashMap[TAG_ID] = id
                    //hashMap[TAG_CONT] = cont
                    //hashMap[TAG_DATE] = date
                    //hashMap[TAG_DUST] = dust
                    //hashMap[TAG_WEATHER] = weat
                    //mArrayList?.add(hashMap)
                    println("===리스트 변수들!!   $cont")
                    dogList.add(Dog(cont, date, dust, weat))
                }
                //println("===리스트 출력!!   "+mArrayList)
                println("===리스트 출력!!   "+dogList)


            } catch (e: JSONException) {

                Log.d(TAG, "showResult : ", e)
            }
        }
    }


    internal class seData : AsyncTask<String?, Void?, String>() {


        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            userif = result

            val nextIntent = Intent(dContext, UpdateActivity::class.java)
            nextIntent.putExtra("name", userID)
            nextIntent.putExtra("nameif", userif)
            dContext?.startActivity(nextIntent)

            //EtextId?.setText(result)
            Log.d(TAG, "POST response  - $result")
        }

        override fun doInBackground(vararg params: String?): String {
            val name = params[1]
            val serverURL = params[0]
            val postParameters = "name=$name"
            return try {
                val url = URL(serverURL)
                val httpURLConnection: HttpURLConnection = url.openConnection() as HttpURLConnection
                httpURLConnection.setReadTimeout(5000)
                httpURLConnection.setConnectTimeout(5000)
                httpURLConnection.setRequestMethod("POST")
                httpURLConnection.connect()
                val outputStream: OutputStream = httpURLConnection.getOutputStream()
                outputStream.write(postParameters.toByteArray(charset("UTF-8")))
                outputStream.flush()
                outputStream.close()
                val responseStatusCode: Int = httpURLConnection.getResponseCode()
                Log.d(TAG, "POST response code - $responseStatusCode")
                val inputStream: InputStream
                inputStream = if (responseStatusCode == HttpURLConnection.HTTP_OK) {
                    httpURLConnection.getInputStream()
                } else {
                    httpURLConnection.getErrorStream()
                }
                val inputStreamReader = InputStreamReader(inputStream, "UTF-8")
                val bufferedReader = BufferedReader(inputStreamReader)
                val sb = StringBuilder()
                var line: String? = null
                while (bufferedReader.readLine().also { line = it } != null) {
                    sb.append(line)
                }
                bufferedReader.close()
                sb.toString()
            } catch (e: Exception) {
                Log.d(TAG, "InsertData: Error ", e)
                "Error: " + e.message
            }
        }
    }

    internal class DelData : AsyncTask<String?, Void?, String>() {


        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            //mTextViewResult?.setText(result)
            Log.d(TAG, "POST response  - $result")
        }

        override fun doInBackground(vararg params: String?): String {
            val name = params[1]
            val serverURL = params[0]
            val postParameters = "name=$name"
            return try {
                val url = URL(serverURL)
                val httpURLConnection: HttpURLConnection = url.openConnection() as HttpURLConnection
                httpURLConnection.setReadTimeout(5000)
                httpURLConnection.setConnectTimeout(5000)
                httpURLConnection.setRequestMethod("POST")
                httpURLConnection.connect()
                val outputStream: OutputStream = httpURLConnection.getOutputStream()
                outputStream.write(postParameters.toByteArray(charset("UTF-8")))
                outputStream.flush()
                outputStream.close()
                val responseStatusCode: Int = httpURLConnection.getResponseCode()
                Log.d(TAG, "POST response code - $responseStatusCode")
                val inputStream: InputStream
                inputStream = if (responseStatusCode == HttpURLConnection.HTTP_OK) {
                    httpURLConnection.getInputStream()
                } else {
                    httpURLConnection.getErrorStream()
                }
                val inputStreamReader = InputStreamReader(inputStream, "UTF-8")
                val bufferedReader = BufferedReader(inputStreamReader)
                val sb = StringBuilder()
                var line: String? = null
                while (bufferedReader.readLine().also { line = it } != null) {
                    sb.append(line)
                }
                bufferedReader.close()
                sb.toString()
            } catch (e: Exception) {
                Log.d(TAG, "InsertData: Error ", e)
                "Error: " + e.message
            }
        }
    }


}