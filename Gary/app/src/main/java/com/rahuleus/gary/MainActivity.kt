package com.rahuleus.gary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.google.mlkit.vision.barcode.common.Barcode
import java.io.IOException
import org.jsoup.Jsoup
/*import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.rahuleus.gary.ui.theme.GaryTheme
import androidx.compose.ui.tooling.preview.Preview*/
import com.rahuleus.gary.databinding.ActivityMainBinding




class MainActivity : ComponentActivity() {

    private val cameraPermission = android.Manifest.permission.CAMERA
    private lateinit var binding: ActivityMainBinding
    private var we="https://in.openfoodfacts.org/product/"


    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if(isGranted) this.startScanner()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.button.setOnClickListener{
            requestCameraAndStartScanner()
        }

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
                    else ->{
                        "INVALID".also { binding.textViewQrAllergen.text = it }
                    }
                }
            }
        }
    }

    private fun webReader(bsite: String) {
        val url = buildString {
            append(we)
            append(bsite)
        }

        try {
            val document = Jsoup.connect(url).get()
            val content = "$document"
            val words =content.split(" ")
            extractor(words)
        } catch (e: IOException) {
            println("database found no match")
            e.printStackTrace()
        }
    }


    private fun extractor(sentence: List<String>){
        val Checker2= "Allergens"
        val Checker="ingredients"
        val breaker="Food"
        var foundtarget=false
        var foundtarget2=false
        val Allergens=mutableListOf<String>()
        val Ingredients= mutableListOf<String>()
        for ( words in sentence) {
            if(words == breaker)
                break
            else if (words == Checker) {
                foundtarget= true
            }
            else if(foundtarget==true){
                if(words == (Checker2)) {
                    foundtarget=false
                    foundtarget2=true
                }
                else
                Ingredients.add(words)
            }
            else if(foundtarget2==true){
                Allergens.add(words)

            }
        }
        binding.textViewQrAllergen.text = Allergens.toString()
        binding.textViewQrIngredient.text = Ingredients.toString()

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