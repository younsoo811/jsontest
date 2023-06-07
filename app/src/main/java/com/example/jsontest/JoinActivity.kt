package com.example.jsontest


import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.jsontest.databinding.ActivityJoinBinding
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

private val TAG = "jsontest"
private var TextviewIDCK: TextView? = null

private const val TAG_JSON = "webnautes"
private const val TAG_ID = "id"
private const val TAG_NAME = "name"

private var mArrayList: ArrayList<HashMap<String, String>>? = null

private var mJsonString: String? = null

class JoinActivity : AppCompatActivity() {

    lateinit var binding: ActivityJoinBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJoinBinding.inflate(layoutInflater)
        setContentView(binding.root)


        TextviewIDCK = findViewById(binding.textViewJoinIdck.id) as  TextView?
        TextviewIDCK!!.movementMethod = ScrollingMovementMethod()

       //중복 확인 버튼
        binding.buttonJoinCheck.setOnClickListener {
            val newId = binding.textViewJoinId!!.text.toString()
            val task = GetID()

            mArrayList?.clear()

            task.execute(newId)

        }
//
//        //회원가입 버튼
//        buttonJoins.setOnClickListener {
//
//        }

        //나가기 버튼
        binding.buttonJoinExit.setOnClickListener {
            val nextintent = Intent(this@JoinActivity, LoginActivity::class.java)
            startActivity(nextintent)
        }

        mArrayList = ArrayList()

    }

    internal class GetID : AsyncTask<String?, Void?, String?>() {
        var errorString: String? = null

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            //TextviewIDCK!!.text="사용중인 ID"
            TextviewIDCK!!.text = result
            Log.d(TAG, "response - $result")
            if (result == null) {
                //TextviewIDCK!!.text="사용중인 ID"
                TextviewIDCK!!.text = errorString
            } else {
                //TextviewIDCK!!.text="사용중인 ID"
                mJsonString = result
                showResult()
            }
        }

        override fun doInBackground(vararg params: String?): String? {
            val searchKeyword1 = params[0]
            //val searchKeyword2 = params[1]
            val serverURL = "http://192.168.55.194/search.php"
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