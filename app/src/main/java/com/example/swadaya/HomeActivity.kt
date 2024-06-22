// HomeActivity.kt
package com.example.swadaya

import TagihanAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.swadaya.databinding.ActivityHomeBinding
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var tagihanAdapter: TagihanAdapter

    companion object {
        private const val TAG = "HomeActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val username = MainActivity.SharedPreferencesUtil.getUsername(this)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Welcome, $username"

        initRecyclerView()
        fetchDataFromApi()
        initViews()
    }


    private fun initViews() {
        binding.fab.setOnClickListener {
            checkPermissionCamera()
        }
    }

    private fun initBinding() {
        binding = ActivityHomeBinding.inflate(layoutInflater)
    }

    private fun checkPermissionCamera() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            showCamera()
        } else if (shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)) {
            // Menampilkan penjelasan mengapa izin diperlukan
            AlertDialog.Builder(this)
                .setMessage("Aplikasi membutuhkan izin kamera untuk memindai QR Code.")
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                    requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                }
                .show()
        } else {
            requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }



    private fun showCamera() {
        val options = ScanOptions()
            .setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            .setPrompt("Scan QR Code")
            .setCameraId(0)
            .setBeepEnabled(false)
            .setBarcodeImageEnabled(true)
            .setOrientationLocked(false)

        scanLauncher.launch(options)
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                showCamera()
            } else {
                Toast.makeText(this, "Dibutuhkan izin kamera", Toast.LENGTH_SHORT).show()
            }
        }
    private val scanLauncher =
        registerForActivityResult(ScanContract()) { result: ScanIntentResult ->
            if (result.contents == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            } else {
                showScanResultDialog(result.contents)
            }
        }

    private fun showScanResultDialog(scanResult: String) {
        val dialogView = layoutInflater.inflate(R.layout.result_dialog, null)
        val textViewScanResult = dialogView.findViewById<TextView>(R.id.text_view_scan_result)
        val editTextNumber = dialogView.findViewById<EditText>(R.id.edit_text_number)

        textViewScanResult.text = "Nomor Meter: $scanResult"

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Save") { dialog, _ ->
                val enteredNumber = editTextNumber.text.toString()
                sendDataToApi(scanResult, enteredNumber)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun sendDataToApi(scanResult: String, number: String) {
        val retroInstance = Retro()
        val retrofit = retroInstance.getRetroClientInstance()
        val apiService = retrofit.create(ApiService::class.java)

        val sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("TOKEN", "")
        val userId = sharedPref.getInt("USER_ID", -1)

        if (!token.isNullOrEmpty() && userId != -1) {
            val call = apiService.sendData(token, scanResult, number, userId.toString())

            call.enqueue(object : Callback<InsertResponse> {
                override fun onResponse(call: Call<InsertResponse>, response: Response<InsertResponse>) {
                    if (response.isSuccessful) {
                        Toast.makeText(applicationContext, "Data berhasil disimpan", Toast.LENGTH_SHORT).show()
                        fetchDataFromApi()
                    } else {
                        try {
                            val errorBody = response.errorBody()?.string()
                            val jsonObject = JSONObject(errorBody)
                            val errorMessage = jsonObject.getString("message")
                            Log.e(TAG, "Error: $errorMessage")
                            Toast.makeText(applicationContext, errorMessage, Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing error message: ${e.message}")
                            Toast.makeText(applicationContext, "Terjadi kesalahan", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onFailure(call: Call<InsertResponse>, t: Throwable) {
                    Log.e(TAG, "Error: ${t.message}", t)
                }
            })
        } else {
            Toast.makeText(this, "Anda harus login terlebih dahulu", Toast.LENGTH_SHORT).show()
            logout()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initRecyclerView() {
        tagihanAdapter = TagihanAdapter(listOf()) { tagihan ->
            showUpdateDialog(tagihan)
        }
        binding.recyclerView.adapter = tagihanAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun fetchDataFromApi() {
        val sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("TOKEN", "")

        if (token.isNullOrEmpty()) {
            // Token kosong atau tidak ada, tampilkan pesan toast
            Toast.makeText(this, "Anda harus login terlebih dahulu", Toast.LENGTH_SHORT).show()
            Toast.makeText(this, token, Toast.LENGTH_SHORT).show()


            // Berikan pengguna opsi untuk login
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finishAffinity()
        } else {

            val retroInstance = Retro()
            val retrofit = retroInstance.getRetroClientInstance()
            val apiService = retrofit.create(TagihanGetApi::class.java)

            val request = GetTagihanRequest(token)

            val call = apiService.tagihan(request)
            call.enqueue(object : Callback<GetTagihanResponse> {
                override fun onResponse(call: Call<GetTagihanResponse>, response: Response<GetTagihanResponse>) {
                    if (response.isSuccessful) {
                        val tagihanList: List<Tagihan> = response.body()?.data ?: listOf()
                        tagihanAdapter.updateData(tagihanList)
                    } else {
                        Log.e(TAG, "Error: ${response.code()}")
                        Toast.makeText(this@HomeActivity, "Anda harus login terlebih dahulu", Toast.LENGTH_SHORT).show()
                        Toast.makeText(this@HomeActivity, token, Toast.LENGTH_SHORT).show()
                        logout()
                    }
                }

                override fun onFailure(call: Call<GetTagihanResponse>, t: Throwable) {
                    Log.e(TAG, "Error: ${t.message}", t)
                    logout()
                }
            })
        }
    }


    private fun showUpdateDialog(tagihan: Tagihan) {
        val dialogView = layoutInflater.inflate(R.layout.update_dialog, null)
        val editTextKodeClient = dialogView.findViewById<TextView>(R.id.edit_text_kode_client)
        val editTextNamaClient = dialogView.findViewById<TextView>(R.id.edit_text_nama_client)
        val editTextNomorMeter = dialogView.findViewById<EditText>(R.id.edit_text_nomor_meter)

        editTextKodeClient.setText(tagihan.kodeClient)
        editTextNamaClient.setText(tagihan.namaClient)
        editTextNomorMeter.setText(tagihan.nomorMeter.toString())

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Update") { dialog, _ ->
                val updatedKodeClient = editTextKodeClient.text.toString()
                val updatedNomorMeter = editTextNomorMeter.text.toString().toInt()
                updateTagihan(tagihan.id, updatedKodeClient, updatedNomorMeter)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun updateTagihan(id: Int, kodeClient: String, nomorMeter: Int) {
        val retroInstance = Retro()
        val retrofit = retroInstance.getRetroClientInstance()
        val apiService = retrofit.create(UpdateApi::class.java)

        val sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("TOKEN", "")

        if (!token.isNullOrEmpty()) {
            val request = UpdateTagihanRequest(token, id, kodeClient, nomorMeter)
            val call = apiService.updateTagihan(request)

            call.enqueue(object : Callback<UpdateTagihanResponse> {
                override fun onResponse(call: Call<UpdateTagihanResponse>, response: Response<UpdateTagihanResponse>) {
                    if (response.isSuccessful) {
                        val updateResponse = response.body()
                        if (updateResponse?.success == true) {
                            Toast.makeText(this@HomeActivity, "Data berhasil diperbarui", Toast.LENGTH_SHORT).show()
                            fetchDataFromApi()
                        } else {
                            Log.e(TAG, "Gagal memperbarui data: ${updateResponse?.message}")
                            Toast.makeText(this@HomeActivity, "Gagal memperbarui data: ${updateResponse?.message}", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.e(TAG, "Gagal memperbarui data: ${response.errorBody()?.string()}")
                        Toast.makeText(this@HomeActivity, "Gagal memperbarui data", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<UpdateTagihanResponse>, t: Throwable) {
                    Log.e(TAG, "Gagal memperbarui data: ${t.message}", t)
                    Toast.makeText(this@HomeActivity, "Gagal memperbarui data: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(this, "Anda harus login terlebih dahulu", Toast.LENGTH_SHORT).show()
            logout()
        }
    }


    private fun logout() {
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove("TOKEN")
        editor.apply()

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finishAffinity() // Mengganti finish() dengan finishAffinity() untuk menutup semua aktivitas terkait
    }

}