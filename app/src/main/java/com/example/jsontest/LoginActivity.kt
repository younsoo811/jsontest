//@file:Suppress("DEPRECATION")

package com.example.jsontest

import android.app.ProgressDialog
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.jsontest.databinding.ActivityLoginBinding
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

private val IP_ADDRESS = "192.168.55.194"
private val TAG = "jsontest"

private var mEditTextName: EditText? = null
private var mEditTextCountry: EditText? = null
private var mTextViewResult: TextView? = null

class LoginActivity : AppCompatActivity() {


    lateinit var binding: ActivityLoginBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mEditTextName = findViewById(binding.editTextMainName.id) as EditText?
        mEditTextCountry = findViewById(binding.editTextMainCountry.id) as EditText?
        mTextViewResult = findViewById(binding.textViewMainResult.id) as TextView?
        mTextViewResult!!.movementMethod = ScrollingMovementMethod()
        val buttonInsert: Button = findViewById(binding.buttonMainInsert.id) as Button
        buttonInsert.setOnClickListener { v ->
            val name = mEditTextName!!.text.toString()
            val country = mEditTextCountry!!.text.toString()
            val task = InsertData()
            task.execute("http://$IP_ADDRESS/insert.php", name, country)

            mEditTextName!!.setText("")
            mEditTextCountry!!.setText("")

            //로그인 후 다른 액티비티로 전환하기
            val nextIntent = Intent(this, MainActivity::class.java)
            startActivity(nextIntent)
        }
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
}