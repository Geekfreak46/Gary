package com.rahuleus.gary


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.firebase.firestore.FirebaseFirestore
import com.rahuleus.gary.databinding.ActivityMainBinding


class MainActivity : ComponentActivity() {

    private val cameraPermission = android.Manifest.permission.CAMERA
    private lateinit var binding: ActivityMainBinding


    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if(isGranted) this.startScanner()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.button.setOnClickListener{ requestCameraAndStartScanner() }

    }

    private fun requestCameraAndStartScanner(){
        if(isPermissionGranted(cameraPermission)){
            startScanner()
        }
        else{
            requestCameraPermission()
        }
    }

    private fun startScanner(){
        ScannerActivity.startScanner(this){barcodes ->
            barcodes.forEach{barcode ->
                when(barcode.format){
                    Barcode.FORMAT_EAN_13 -> {
                        webReader(barcode.rawValue.toString())
                        //binding.textViewQrContent.text
                    }
                }
            }
        }
    }

    private fun webReader(code: String) {

        val db = FirebaseFirestore.getInstance()
        val collection = "Product"
        val field = "EAN13"
        val query = db.collection(collection).whereEqualTo(field, code)
        query.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                for (document in task.result!!.documents) {
                    val data = document.data
                    val allergens = data?.get("Allergen")?.toString() ?: " "
                    val ingredients = data?.get("ingredient")?.toString() ?: " "

                    binding.textViewQrAllergen.text = allergens
                    binding.textViewQrIngredient.text = ingredients
                }
            } else {
                "Error".also { binding.textViewQrAllergen.text = it }
                "Error".also { binding.textViewQrIngredient.text = it }
            }
        }
    }



    private fun requestCameraPermission() {
        when{
            shouldShowRequestPermissionRationale(cameraPermission) -> {
                cameraPermissionRequest {
                    openPermissionSetting()
                }
            }
            else -> {
                requestPermissionLauncher.launch(cameraPermission)
            }
        }

    }
}