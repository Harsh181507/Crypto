package com.example.crypto

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.crypto.databinding.ActivityMainBinding
import com.google.android.material.textfield.TextInputLayout
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var rvAdapter: RvAdapter
    private lateinit var data: ArrayList<Model>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root) // Correct binding
        supportActionBar?.hide()

        data = ArrayList()
        rvAdapter = RvAdapter(this, data)
        binding.Rv.layoutManager = LinearLayoutManager(this)
        binding.Rv.adapter = rvAdapter

        // Fetch the EditText from the TextInputLayout using the correct ID
        val searchEditText = findViewById<EditText>(R.id.searchEditText)

        // Add TextWatcher to the EditText
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {
                val filterData = ArrayList<Model>()
                for (item in data) {
                    if (item.name.lowercase(Locale.getDefault())
                            .contains(p0.toString().lowercase(Locale.getDefault()))
                    ) {
                        filterData.add(item)
                    }
                }
                if (filterData.isEmpty()) {
                    Toast.makeText(this@MainActivity, "No Data Found", Toast.LENGTH_LONG).show()
                } else {
                    rvAdapter.changeData(filterData)
                }
            }
        })

        // Fetch API data
        fetchApiData()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun fetchApiData() {
        val url = "https://pro-api.coinmarketcap.com/v1/cryptocurrency/listings/latest"
        val queue = Volley.newRequestQueue(this)

        val jsonObjectRequest = object : JsonObjectRequest(Method.GET, url, null, Response.Listener { response ->
            binding.progressBar.isVisible = false
            try {
                val dataArray = response.getJSONArray("data")
                for (i in 0 until dataArray.length()) {
                    val dataObject = dataArray.getJSONObject(i)
                    val symbol = dataObject.getString("symbol")
                    val name = dataObject.getString("name")
                    val quote = dataObject.getJSONObject("quote")
                    val usd = quote.getJSONObject("USD")
                    val price = String.format("$ "+"%.2f", usd.getDouble("price"))

                    data.add(Model(name, symbol, price))
                }
                rvAdapter.notifyDataSetChanged()
            } catch (e: Exception) {
                Log.e("API_DATA_PARSING", "Error parsing data: ${e.message}")
                Toast.makeText(this, "Parsing Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }, Response.ErrorListener { error ->
            handleApiError(error)
        }) {
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["X-CMC_PRO_API_KEY"] = "1f804fc2-68bb-4498-bcc4-d8fd278ea325"
                return headers
            }
        }
        jsonObjectRequest.retryPolicy = DefaultRetryPolicy(
            5000, // Initial timeout in ms
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES, // Number of retries
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        // Add the request to the queue
        queue.add(jsonObjectRequest)
    }

    // Function to handle detailed error logging
    private fun handleApiError(error: VolleyError) {
        error.printStackTrace()

        // Log detailed error
        Log.e("API_ERROR", "Error occurred: ${error.message}")

        // Display a user-friendly message
        val errorMessage = when (error.networkResponse?.statusCode) {
            401 -> "Unauthorized: Check your API key."
            429 -> "Too many requests: You've exceeded your rate limit."
            500 -> "Server error: Please try again later."
            else -> "Network error: Please check your connection."
        }
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
    }
}
