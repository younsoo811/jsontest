package com.example.jsontest


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
import com.example.jsontest.databinding.ActivityUpdateBinding
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

private val TAG = "jsontest"
private val IP_ADDRESS = "192.168.55.155"

private var TextviewIDCK: TextView? = null
private var mTextViewResult: TextView? = null
private var EtextId: EditText? = null
private var EtextPw: EditText? = null
private var EtextName: EditText? = null
private var EtextCall: EditText? = null


private const val TAG_JSON = "younsoo"
private const val TAG_ID = "id"
private const val TAG_NAME = "name"

private var mArrayList: ArrayList<HashMap<String, String>>? = null

private var mJsonString: String? = null

class UpdateActivity : AppCompatActivity() {

    lateinit var binding: ActivityUpdateBinding

    var userID : String? = null
    var userInF : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        EtextId = findViewById(binding.editTextJoinId.id) as EditText?
        EtextPw = findViewById(binding.editTextJoinPasswd.id) as EditText?
        EtextName = findViewById(binding.editTextJoinName.id) as EditText?
        EtextCall = findViewById(binding.editTextJoinCall.id) as EditText?

        TextviewIDCK = findViewById(binding.textViewJoinIdck.id) as  TextView?
        TextviewIDCK!!.movementMethod = ScrollingMovementMethod()
        mTextViewResult = findViewById(binding.textViewMainResult.id) as TextView?
        mTextViewResult!!.movementMethod = ScrollingMovementMethod()


        //힌트값 셋팅
        if(intent.hasExtra("name")){
            userID=intent.getStringExtra("name")
            userInF=intent.getStringExtra("nameif")

            println("메인 액티비티에서 변수 불러오기!! : "+userID)
            println("메인 액티비티에서 변수 불러오기!! : "+userInF)

            var str_arr = userInF?.split("#")

            EtextId?.setText(str_arr?.get(0))
            EtextPw?.setText(str_arr?.get(1))
            EtextName?.setText(str_arr?.get(2))
            EtextCall?.setText(str_arr?.get(3))

/*            EtextId?.setHint(str_arr?.get(0))
            EtextPw?.setHint(str_arr?.get(1))
            EtextName?.setHint(str_arr?.get(2))
            EtextCall?.setHint(str_arr?.get(3))*/

        }
        else{
            Toast.makeText(this, "Error!", Toast.LENGTH_SHORT).show()
        }


       //중복 확인 버튼
        binding.buttonJoinCheck.setOnClickListener {
            val newId = binding.editTextJoinId!!.text.toString()
            val task = GetID()

            mArrayList?.clear()

            task.execute(newId)

        }
//
//        //회원가입 버튼
        binding.buttonJoin.setOnClickListener {
            val name = EtextId!!.text.toString()
            val country = EtextPw!!.text.toString()
            val uname = EtextName!!.text.toString()
            val ucall = EtextCall!!.text.toString()

            val task = JoinData()
            if (TextviewIDCK!!.text.toString().equals("사용가능한 ID") || TextviewIDCK!!.text.toString().equals("사용중인 ID")){
                println("아이디 생성!!!!")
                println(userID)
                task.execute("http://$IP_ADDRESS/update.php", userID, name, country, uname, ucall)

                TextviewIDCK!!.text="id 중복 확인"
            }
            else{
                println("아이디 생성 실패!!!!"+ TextviewIDCK!!.text)
                mTextViewResult!!.text = "아이디 중복을 확인해주세요!"
            }

        }

        //나가기 버튼
        binding.buttonJoinExit.setOnClickListener {
            /*val nextintent = Intent(this@UpdateActivity, DiaryActivity::class.java)
            startActivity(nextintent)*/
            finish()
        }

        mArrayList = ArrayList()


    }

    internal class JoinData : AsyncTask<String?, Void?, String>() {


        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            mTextViewResult?.setText(result)
            Log.d(TAG, "POST response  - $result")
        }

        override fun doInBackground(vararg params: String?): String {
            val oname = params[1]
            val name = params[2]
            val country = params[3]
            val uname = params[4]
            val ucall = params[5]
            val serverURL = params[0]
            val postParameters = "oname=$oname&name=$name&country=$country&uname=$uname&ucall=$ucall"
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
            val serverURL = "http://192.168.55.155/search.php"
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