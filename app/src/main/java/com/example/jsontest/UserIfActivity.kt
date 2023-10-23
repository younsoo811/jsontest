package com.example.jsontest


import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.jsontest.databinding.ActivityJoinBinding
import com.example.jsontest.databinding.ActivityUserifBinding
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

private var UContext: Context? = null

private val TAG = "jsontest"
private val IP_ADDRESS = "221.140.227.176:7000"

private var mTextViewResult: TextView? = null
private var TviewId: TextView? = null
private var TviewCall: TextView? = null

private var userif : String? = null
private var userID : String? = null


class UserIfActivity : AppCompatActivity() {

    lateinit var binding: ActivityUserifBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserifBinding.inflate(layoutInflater)
        setContentView(binding.root)

        UContext = this

        TviewId = findViewById(binding.textViewName.id) as TextView?
        TviewCall = findViewById(binding.textViewCall.id) as TextView?

        mTextViewResult = findViewById(binding.textViewMainResult.id) as TextView?
        mTextViewResult!!.movementMethod = ScrollingMovementMethod()

        var userName:String?=null
        var checkTime : String? = null
        var dustD : String? = null
        var weatherD : String? = null

        if(intent.hasExtra("user")){
            userName=intent.getStringExtra("user")
            userID=intent.getStringExtra("name")
            checkTime=intent.getStringExtra("time")
            dustD=intent.getStringExtra("dust")
            weatherD=intent.getStringExtra("weat")

            TviewId?.setText(userName)
            //TviewId!!.text = userName
            //TviewCall!!.text = "테스트중"
            TviewCall?.setText(userID)
        }
        else{
            Toast.makeText(this, "Error!", Toast.LENGTH_SHORT).show()
        }


        //일기 작성하기 버튼
        binding.diaryButton.setOnClickListener {
            val nextintent = Intent(this@UserIfActivity, DiaryActivity::class.java)
            nextintent.putExtra("name",userID)
            nextintent.putExtra("time",checkTime)
            nextintent.putExtra("dust",dustD)
            nextintent.putExtra("weat",weatherD)
            startActivity(nextintent)
        }

        //정보 수정 버튼
        binding.buttonJoin.setOnClickListener {
            val task = seData()
            task.execute("http://$IP_ADDRESS/allsh.php", userID)
        }

        //나가기 버튼
        binding.buttonJoinExit.setOnClickListener {
            /*val mintent = Intent(this@UserIfActivity, MainActivity::class.java)
            mintent.putExtra("name", userID)
            startActivity(mintent)*/
            finish()
        }

        // 로그아웃 버튼
        binding.logoutButton.setOnClickListener {
            val outintent=Intent(this@UserIfActivity, LoginActivity::class.java)
            finish()

            MySharedPreferences.setUserId(this, "")
            MySharedPreferences.setUserPass(this, "")

            startActivity(outintent)
        }

    }

    internal class seData : AsyncTask<String?, Void?, String>() {

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            userif = result

            val nextIntent = Intent(UContext, UpdateActivity::class.java)
            nextIntent.putExtra("name", userID)
            nextIntent.putExtra("nameif", userif)
            UContext?.startActivity(nextIntent)

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
}