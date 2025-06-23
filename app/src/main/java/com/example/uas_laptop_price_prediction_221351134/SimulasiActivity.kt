package com.example.uas_laptop_price_prediction_221351134

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import org.json.JSONObject // Untuk membaca JSON scaler params
import java.io.InputStreamReader

class SimulasiActivity : AppCompatActivity() {

    private lateinit var tflite: Interpreter
    private lateinit var scalerMean: FloatArray
    private lateinit var scalerScale: FloatArray
    private lateinit var featureNames: List<String>

    private val brands = listOf("Acer", "Asus", "Dell", "HP", "Lenovo")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simulasi)

        val btnBack: ImageButton = findViewById(R.id.btnBack)
        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val spinner: Spinner = findViewById(R.id.spinnerBrand)
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, brands)

        val modelByteBuffer = loadModelFile("laptop_price_prediction.tflite")
        tflite = Interpreter(modelByteBuffer)

        loadScalerParams("scaler_params.json")

        findViewById<Button>(R.id.btnPrediksi).setOnClickListener {
            val input = prepareInputForModel()
            if (input != null) {
                val output = Array(1) { FloatArray(1) }
                try {
                    tflite.run(input, output)
                    val hasil = output[0][0]
                    findViewById<TextView>(R.id.txtHasil).text = "Estimasi Harga: ₽ ${"%,.0f".format(hasil)}"
                } catch (e: Exception) {
                    Toast.makeText(this, "Error prediksi: ${e.message}", Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }
            }
        }
    }

    private fun loadScalerParams(filename: String) {
        try {
            val inputStream = assets.open(filename)
            val jsonString = InputStreamReader(inputStream).use { it.readText() }
            val jsonObject = JSONObject(jsonString)

            val meanArray = jsonObject.getJSONArray("mean")
            val scaleArray = jsonObject.getJSONArray("scale")
            val featureNamesArray = jsonObject.getJSONArray("feature_names")

            scalerMean = FloatArray(meanArray.length()) { i -> meanArray.getDouble(i).toFloat() }
            scalerScale = FloatArray(scaleArray.length()) { i -> scaleArray.getDouble(i).toFloat() }
            featureNames = List(featureNamesArray.length()) { i -> featureNamesArray.getString(i) }

        } catch (e: Exception) {
            Toast.makeText(this, "Gagal memuat scaler: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun prepareInputForModel(): Array<FloatArray>? {
        return try {
            val processor = findViewById<EditText>(R.id.inputProcessor).text.toString().toFloat()
            val ram = findViewById<EditText>(R.id.inputRAM).text.toString().toFloat()
            val storage = findViewById<EditText>(R.id.inputStorage).text.toString().toFloat()
            val screen = findViewById<EditText>(R.id.inputScreen).text.toString().toFloat()
            val weight = findViewById<EditText>(R.id.inputWeight).text.toString().toFloat()
            val selectedBrand = findViewById<Spinner>(R.id.spinnerBrand).selectedItem.toString()

            val rawFeatures = floatArrayOf(
                processor,
                ram,
                storage,
                screen,
                weight
            )

            val fullInput = FloatArray(featureNames.size) { 0.0f }
            var rawFeatureIndex = 0

            for (i in featureNames.indices) {
                when {
                    featureNames[i] == "Processor_Speed" -> {
                        fullInput[i] = rawFeatures[0]
                        rawFeatureIndex++
                    }
                    featureNames[i] == "RAM_Size" -> {
                        fullInput[i] = rawFeatures[1]
                        rawFeatureIndex++
                    }
                    featureNames[i] == "Storage_Capacity" -> {
                        fullInput[i] = rawFeatures[2]
                        rawFeatureIndex++
                    }
                    featureNames[i] == "Screen_Size" -> {
                        fullInput[i] = rawFeatures[3]
                        rawFeatureIndex++
                    }
                    featureNames[i] == "Weight" -> {
                        fullInput[i] = rawFeatures[4]
                        rawFeatureIndex++
                    }
                    featureNames[i].startsWith("Brand_") -> {
                        val brandName = featureNames[i].substringAfter("Brand_")
                        if (brandName == selectedBrand) {
                            fullInput[i] = 1.0f
                        } else {
                            fullInput[i] = 0.0f
                        }
                    }
                }
            }

            val scaledInput = FloatArray(fullInput.size)
            for (i in fullInput.indices) {
                if (scalerScale[i] != 0f) {
                    scaledInput[i] = (fullInput[i] - scalerMean[i]) / scalerScale[i]
                } else {
                    scaledInput[i] = fullInput[i] - scalerMean[i]
                }
            }
            return arrayOf(scaledInput)

        } catch (e: Exception) {
            Toast.makeText(this, "Input tidak valid atau format salah: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
            null
        }
    }

    private fun loadModelFile(filename: String): MappedByteBuffer {
        val fileDescriptor = assets.openFd(filename)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    override fun onDestroy() {
        tflite.close()
        super.onDestroy()
    }
}
