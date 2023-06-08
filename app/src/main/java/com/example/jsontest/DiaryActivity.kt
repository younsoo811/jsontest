package com.example.jsontest

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.jsontest.databinding.ActivityDiaryBinding
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL


var dContext: Context? = null

private val TAG = "jsontest"
private val IP_ADDRESS = "192.168.55.194"

private var TextviewIDCK: TextView? = null
private var mTextViewResult: TextView? = null
private var EtextId: EditText? = null
private var EtextPw: EditText? = null
private var EtextName: EditText? = null
private var EtextCall: EditText? = null


private const val TAG_JSON = "webnautes"
private const val TAG_ID = "id"
private const val TAG_NAME = "name"

private var mArrayList: ArrayList<HashMap<String, String>>? = null

private var mJsonString: String? = null

private var userID : String? = null
private var userif : String? = null

class DiaryActivity : AppCompatActivity(), View.OnClickListener {

    //바인딩 객체 선언
    lateinit var binding: ActivityDiaryBinding

    var dogList = arrayListOf<Dog>(
        Dog("Chow Chow", "2023-06-05 10:00", "보통", "ic_01d"),
        Dog("Breed Pomeranian", "2023-06-04 08:00", "좋음", "ic_11d"),
        Dog("Golden Retriver", "2023-06-03 10:00", "보통", "ic_10n"),
        Dog("Yorkshire Terrier", "2023-06-02 20:00", "나쁨", "ic_50d"),
        Dog("Pug", "2023-06-02 09:00", "나쁨", "ic_02n"),
        Dog("Alaskan Malamute", "2023-06-01 19:00", "나쁨", "ic_03d"),
        Dog("Shih Tzu", "2023-06-01 10:00", "좋음", "ic_01n")
    )




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_diary)

        binding = ActivityDiaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dContext = this

        val dogAdapter = DiaryListAdapter(this, dogList)
        binding.mainListView.adapter = dogAdapter

        if(intent.hasExtra("name")){
            userID=intent.getStringExtra("name")
            println("메인 액티비티에서 변수 불러오기!! : "+userID)

        }
        else{
            Toast.makeText(this, "Error!", Toast.LENGTH_SHORT).show()
        }

        binding.btnUserUp.setOnClickListener{
            val nextintent = Intent(this, UpdateActivity::class.java)

            val task = DiaryActivity.seData()
            task.execute("http://$IP_ADDRESS/allsh.php", userID)

            //nextintent.putExtra("name", userID)
            //nextintent.putExtra("nameif", userif)
            //startActivity(nextintent)
        }

        binding.btnUserDel.setOnClickListener(this)
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