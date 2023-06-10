//@file:Suppress("DEPRECATION")

package com.example.jsontest

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import com.example.jsontest.databinding.ActivityLoginBinding
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

var mContext: Context? = null

private val TAG = "jsontest"

private var mEditTextName: EditText? = null
private var mEditTextCountry: EditText? = null
private var mTextViewResult: TextView? = null

private const val TAG_JSON = "webnautes"
private const val TAG_ID = "id"
private const val TAG_NAME = "name"
private const val TAG_ADDRESS = "country"

private var mArrayList: ArrayList<HashMap<String, String>>? = null

private var mJsonString: String? = null
private var name: String? = null


class LoginActivity : AppCompatActivity() {

    lateinit var binding: ActivityLoginBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mContext = this //생성자

        mEditTextName = findViewById(binding.editTextMainName.id) as EditText?
        mEditTextCountry = findViewById(binding.editTextMainCountry.id) as EditText?
        mTextViewResult = findViewById(binding.textViewMainResult.id) as TextView?
        mTextViewResult!!.movementMethod = ScrollingMovementMethod()

        val buttonInsert: Button = findViewById(binding.buttonMainInsert.id) as Button
        val buttonLognin: Button = findViewById(binding.buttonMainLogin.id) as Button

        buttonInsert.setOnClickListener {

            //로그인 후 다른 액티비티로 전환하기
            val nextIntent = Intent(this@LoginActivity, JoinActivity::class.java)
            startActivity(nextIntent)
        }

        buttonLognin.setOnClickListener {
            name = mEditTextName!!.text.toString()
            val country = mEditTextCountry!!.text.toString()
            val task = GetData()

            mArrayList?.clear()

            task.execute(country, name)

            println("넘겨줄 변수 값은?? : "+name)


        }

        binding.buttonMainUnknown.setOnClickListener {
            val ukIntent = Intent(this, MainActivity::class.java)
            ukIntent.putExtra("name", "비회원")
            startActivity(ukIntent)
        }


        mArrayList = ArrayList()

    }


    internal class GetData : AsyncTask<String?, Void?, String?>() {
        var errorString: String? = null

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            mTextViewResult!!.text = result
            Log.d(TAG, "response - $result")
            if (result == null) {
                println("if문 실행!")
                mTextViewResult!!.text = errorString
            } else {
                println("else문 실행!")
                mJsonString = result
                showResult()
            }
        }

        override fun doInBackground(vararg params: String?): String? {
            val searchKeyword1 = params[0]
            val searchKeyword2 = params[1]
            val serverURL = "http://192.168.55.194/query.php"
            val postParameters = "country=$searchKeyword1&name=$searchKeyword2"
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
                    val address = item.getString(TAG_ADDRESS)
                    val hashMap: HashMap<String, String> = HashMap()
                    hashMap[TAG_ID] = id
                    hashMap[TAG_NAME] = name
                    hashMap[TAG_ADDRESS] = address
                    mArrayList?.add(hashMap)
                }
                println("===리스트 출력!!   "+mArrayList)


            } catch (e: JSONException) {
                //로그인 후 다른 액티비티로 전환하기
                if(mTextViewResult!!.text.toString().equals("로그인 성공!")) {
                    val nextIntent = Intent(mContext, MainActivity::class.java)
                    nextIntent.putExtra("name", name)
                    mContext?.startActivity(nextIntent)
                }
                Log.d(TAG, "showResult : ", e)
            }
        }
    }

}