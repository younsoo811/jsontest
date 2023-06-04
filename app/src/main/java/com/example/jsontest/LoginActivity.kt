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
private var bool = 0

private val IP_ADDRESS = "192.168.55.194"
private val TAG = "jsontest"

private var mEditTextName: EditText? = null
private var mEditTextCountry: EditText? = null
private var mTextViewResult: TextView? = null

private const val TAG_JSON = "webnautes"
private const val TAG_ID = "id"
private const val TAG_NAME = "name"
private const val TAG_ADDRESS = "country"

var mArrayList: ArrayList<HashMap<String, String>>? = null

var mJsonString: String? = null



class LoginActivity : AppCompatActivity() {
    var name: String? = null


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

        buttonInsert.setOnClickListener { v ->
            name = mEditTextName!!.text.toString()
            val country = mEditTextCountry!!.text.toString()
            val task = InsertData()
            task.execute("http://$IP_ADDRESS/insert.php", name, country)

            //mEditTextName!!.setText("")
            //mEditTextCountry!!.setText("")

            //로그인 후 다른 액티비티로 전환하기
            //val nextIntent = Intent(this, MainActivity::class.java)
            //startActivity(nextIntent)
        }

        buttonLognin.setOnClickListener {
            name = mEditTextName!!.text.toString()
            val country = mEditTextCountry!!.text.toString()
            val task = GetData()

            mArrayList?.clear();

            task.execute(country, name)

            val intent = Intent(this@LoginActivity, MainActivity::class.java)
            intent.putExtra("name", name)

            //mEditTextName!!.setText("")
            //mEditTextCountry!!.setText("")


        }



        mArrayList = ArrayList()

    }

    internal class InsertData : AsyncTask<String?, Void?, String>() {


        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            mTextViewResult?.setText(result)
            Log.d(TAG, "POST response  - $result")
        }

        override fun doInBackground(vararg params: String?): String {
            val name = params[1]
            val country = params[2]
            val serverURL = params[0]
            val postParameters = "name=$name&country=$country"
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

    internal class GetData : AsyncTask<String?, Void?, String?>() {
        var errorString: String? = null

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            mTextViewResult!!.text = result
            Log.d(TAG, "response - $result")
            if (result == null) {
                mTextViewResult!!.text = errorString
            } else {
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

                //로그인 후 다른 액티비티로 전환하기
                val nextIntent = Intent(mContext, MainActivity::class.java)
                mContext?.startActivity(nextIntent)



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