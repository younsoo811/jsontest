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
import androidx.core.app.ActivityCompat
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

private var mContext: Context? = null
private var bool = 0

private val IP_ADDRESS = "192.168.55.155"
private val TAG = "jsontest"

private var mEditTextName: EditText? = null
private var mEditTextCountry: EditText? = null
private var mTextViewResult: TextView? = null

private const val TAG_JSON = "younsoo"
private const val TAG_ID = "id"
private const val TAG_NAME = "name"
private const val TAG_ADDRESS = "country"

private var mArrayList: ArrayList<HashMap<String, String>>? = null

private var mJsonString: String? = null
private var name: String? = null


class LoginActivity : AppCompatActivity() {

    //뒤로가기 버튼 누른 시간
    var backPressedTime: Long = 0

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


        // SharedPreferences 안에 값이 저장되어 있지 않을 때 -> Login
        if (MySharedPreferences.getUserId(this).isNullOrBlank()
            || MySharedPreferences.getUserPass(this).isNullOrBlank()) {
            Login()
        }
        else{   // SharedPreferences 안에 값이 저장되어 있을 때 -> MainActivity로 이동
            Toast.makeText(this, "${MySharedPreferences.getUserId(this)}님 자동 로그인 되었습니다.",
                                Toast.LENGTH_SHORT).show()

            val task = GetData()
            task.execute(MySharedPreferences.getUserPass(this), MySharedPreferences.getUserId(this))

            //finish()
        }

        buttonInsert.setOnClickListener {

            //회원가입 창으로 이동
            val nextIntent = Intent(this@LoginActivity, JoinActivity::class.java)
            startActivity(nextIntent)
        }

        binding.buttonMainUnknown.setOnClickListener {
            val ukIntent = Intent(this, MainActivity::class.java)
            ukIntent.putExtra("name", "비회원")
            startActivity(ukIntent)
        }


        mArrayList = ArrayList()

    }

    fun Login(){
            binding.buttonMainLogin.setOnClickListener {

                if (mEditTextName?.text.isNullOrBlank() || mEditTextCountry?.text.isNullOrBlank()) {
                    Toast.makeText(this, "아이디와 비밀번호를 확인하세요", Toast.LENGTH_SHORT).show()
                }
                else{
                    name = mEditTextName!!.text.toString()
                    val country = mEditTextCountry!!.text.toString()
                    val task = GetData()

                    mArrayList?.clear()

                    task.execute(country, name)

                    //val nextintent = Intent(this@LoginActivity, MainActivity::class.java)
                    println("넘겨줄 변수 값은?? : "+name)
                }
        }
    }



    internal class GetData : AsyncTask<String?, Void?, String?>() {
        var errorString: String? = null

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            val ck = "<br>"
            if (result != null) {
                if(result.contains(ck)) {
                    val str = result?.split("<br>")
                    mTextViewResult!!.text = str?.get(0).toString() + "\n" + str?.get(1).toString()
                }
                else
                    mTextViewResult!!.text = result
            }
            Log.d(TAG, "response - $result")
            if (result == null) {
                //mTextViewResult!!.text = errorString  //에러 메시지 출력
                mTextViewResult!!.text = "서버 응답 오류!\n 잠시 후에 다시 시도해 주세요."
            } else {
                mJsonString = result
                showResult()
            }
        }

        override fun doInBackground(vararg params: String?): String? {
            val searchKeyword1 = params[0]
            val searchKeyword2 = params[1]
            val serverURL = "http://192.168.55.155/query.php"
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

            } catch (e: JSONException) {
                //로그인 후 다른 액티비티로 전환하기
                if(mTextViewResult!!.text.toString().equals("로그인 성공!")) {
                    val nextIntent = Intent(mContext, MainActivity::class.java)

                    // SharedPreferences 안에 값이 저장되어 있지 않을 때 -> Login
                    if (mContext?.let { MySharedPreferences.getUserId(it).isNullOrBlank() }!!
                        || mContext?.let { MySharedPreferences.getUserPass(it).isNullOrBlank() }!!) {
                        mContext?.let { MySharedPreferences.setUserId(it, mEditTextName?.text.toString()) }
                        mContext?.let { MySharedPreferences.setUserPass(it, mEditTextCountry?.text.toString()) }

                        nextIntent.putExtra("name", name)
                    }
                    else{
                        nextIntent.putExtra("name", MySharedPreferences.getUserId(mContext!!))
                    }

                    mContext?.startActivity(nextIntent)
                }
                Log.d(TAG, "showResult : ", e)
            }
        }
    }

}