package com.mobicom.s16.mco


import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.mobicom.s16.mco.data.mapper.toDomainModel
import com.mobicom.s16.mco.data.remote.api.RetrofitClient
import com.mobicom.s16.mco.data.remote.dto.ApiCard
import com.mobicom.s16.mco.data.remote.dto.CardsResponse
import com.mobicom.s16.mco.data.remote.dto.SingleCardResponse
import com.mobicom.s16.mco.databinding.FragmentScannerBinding
import com.mobicom.s16.mco.domain.model.Card
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ScannerFragment : Fragment() {

    private var _binding: FragmentScannerBinding? = null
    private val binding get() = _binding!!
    private lateinit var imageCapture: ImageCapture

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScannerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) startCamera()
            else Toast.makeText(requireContext(), "Camera permission is required", Toast.LENGTH_SHORT).show()
        }

        requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)

        binding.button3.setOnClickListener {
            val photoFile = File(requireContext().cacheDir, "temp_image.jpg")
            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

            imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(requireContext()),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        Toast.makeText(requireContext(), "Image captured. Scanning...", Toast.LENGTH_SHORT).show()
                        runTextRecognition(photoFile)
                    }

                    override fun onError(exception: ImageCaptureException) {
                        Toast.makeText(requireContext(), "Capture failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                        Log.e("CameraX", "Capture failed", exception)
                    }
                }
            )
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        val screenSize = Size(1920, 1080)

        val resolutionSelector = ResolutionSelector.Builder()
            .setResolutionStrategy(ResolutionStrategy(screenSize, ResolutionStrategy.FALLBACK_RULE_NONE))
            .build()

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .setResolutionSelector(resolutionSelector)
                .build()
                .also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .setTargetResolution(screenSize)
                .build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(viewLifecycleOwner, cameraSelector, preview, imageCapture)
            } catch (e: Exception) {
                Log.e("CameraX", "Camera bind failed", e)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun runTextRecognition(imageFile: File) {
        val image = InputImage.fromFilePath(requireContext(), Uri.fromFile(imageFile))
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)

        val focusTop = binding.focusBox.top
        val focusBottom = binding.focusBox.bottom

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                Log.d("OCR", "Full recognized text:\n${visionText.text}")

                val cardNumberRegex = Regex("""\b(\d{1,3})/(\d{1,3})\b""")
                var cardNumberRaw: String? = null

                for (block in visionText.textBlocks) {
                    for (line in block.lines) {
                        val y = line.boundingBox?.centerY() ?: continue
                        val lineText = line.text.trim()
                        val cleanedLine = lineText.replace(Regex("[^0-9/]"), "")

                        Log.d("OCR_LINE", "Line: '$lineText' → Cleaned: '$cleanedLine'")

                        if (y in focusTop..focusBottom && cardNumberRaw == null && cardNumberRegex.containsMatchIn(cleanedLine)) {
                            cardNumberRaw = cardNumberRegex.find(cleanedLine)?.value
                            Log.d("OCR_NUMBER", "Detected card number: $cardNumberRaw")
                        }
                    }
                }

                if (cardNumberRaw != null) {
                    val parts = cardNumberRaw.split("/")
                    val cardNumber = parts.getOrNull(0)?.trim()
                    val printedTotal = parts.getOrNull(1)?.trim()?.toIntOrNull()

                    if (cardNumber != null && printedTotal != null) {
                        val cropped = cropSetSymbolFromImage(bitmap)
                        binding.loadingSpinner.visibility = View.VISIBLE

                        classifySetSymbol(cropped) { setId ->
                            if (setId != null) {
                                Log.d("SetClassifier", "Set symbol recognized as: $setId")
                                searchCardById(setId, cardNumber)

//                                searchCardBySetAndNumber(setId, cardNumber, printedTotal)
                            } else {
                                Log.d("SetClassifier", "Set symbol not recognized, attempting fallback")
                                searchCardByNumberAndTotal(cardNumber, printedTotal)
                            }
                        }

                    } else {
                        Toast.makeText(requireContext(), "Invalid number format", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Card number not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Log.e("OCR", "Text recognition failed", it)
                Toast.makeText(requireContext(), "OCR failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun searchCardBySetAndNumber(setSymbol: String, cardNumber: String, printedTotal: Int) {
        val query = "set.id:$setSymbol number:$cardNumber"
        val start = System.currentTimeMillis()
        Log.d("API_CALL", "Querying with: $query")
        binding.loadingSpinner.visibility = View.VISIBLE
        RetrofitClient.api.searchCardByNameAndNumber(query).enqueue(object : Callback<CardsResponse> {
            override fun onResponse(call: Call<CardsResponse>, response: Response<CardsResponse>) {
                binding.loadingSpinner.visibility = View.GONE
                val duration = System.currentTimeMillis() - start
                Log.d("API_CALL", "API call completed in $duration ms")
                // rest of your logic
            }

            override fun onFailure(call: Call<CardsResponse>, t: Throwable) {
                binding.loadingSpinner.visibility = View.GONE
                val duration = System.currentTimeMillis() - start
                Log.e("API_CALL", "API call failed in $duration ms", t)
            }
        })
    }

    private fun searchCardById(setSymbol: String, cardNumber: String) {
        val cardId = "${setSymbol.lowercase()}-$cardNumber"
        Log.d("API_CALL", "Fetching card by ID: $cardId")
        binding.loadingSpinner.visibility = View.VISIBLE

        RetrofitClient.api.getCardById(cardId).enqueue(object : Callback<SingleCardResponse> {
            override fun onResponse(call: Call<SingleCardResponse>, response: Response<SingleCardResponse>) {
                binding.loadingSpinner.visibility = View.GONE
                if (response.isSuccessful) {
                    val card = response.body()?.data
                    if (card != null) {
                        openCardInfo(card)
                    } else {
                        Toast.makeText(requireContext(), "Card not found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "API Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<SingleCardResponse>, t: Throwable) {
                binding.loadingSpinner.visibility = View.GONE
                Log.e("API_CALL", "API call failed", t)
                Toast.makeText(requireContext(), "API Failure: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun searchCardByNumberAndTotal(cardNumber: String, printedTotal: Int) {
        val query = "number:$cardNumber"

        binding.loadingSpinner.visibility = View.VISIBLE
        RetrofitClient.api.searchCardByNameAndNumber(query).enqueue(object : Callback<CardsResponse> {
            override fun onResponse(call: Call<CardsResponse>, response: Response<CardsResponse>) {
                binding.loadingSpinner.visibility = View.GONE
                if (response.isSuccessful) {
                    val matchedCard = response.body()?.data?.firstOrNull { card ->
                        card.set?.printedTotal == printedTotal
                    }

                    if (matchedCard != null) {
                        openCardInfo(matchedCard)
                    } else {
                        Toast.makeText(requireContext(), "No matching card found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "API Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<CardsResponse>, t: Throwable) {
                binding.loadingSpinner.visibility = View.GONE
                Log.e("API_CALL", "API call failed", t)
                Toast.makeText(requireContext(), "API Failure: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun buildCardInfoIntent(card: Card): Intent {
        return Intent(requireContext(), CardInfoActivity::class.java).apply {
            putExtra("CARD_ID", card.id)
            putExtra("CARD_NAME", card.name)
            putExtra("CARD_SET", card.set)
            putExtra("CARD_SET_SERIES", card.setSeries)
            putExtra("CARD_PRICE", card.price)
            putExtra("CARD_PRICE_SOURCE", card.priceSource)
            putExtra("CARD_NUMBER", card.number)
            putExtra("CARD_RARITY", card.rarity)
            putExtra("CARD_SUPERTYPE", card.supertype)
            putExtra("CARD_HP", card.hp)
            putExtra("CARD_ARTIST", card.artist)
            putExtra("CARD_FLAVOR_TEXT", card.flavorText)
            putExtra("CARD_IMAGE_URL", card.imageUrl)
            putExtra("CARD_IMAGE_URL_LARGE", card.imageUrlLarge)

            putStringArrayListExtra("CARD_TYPES", ArrayList(card.types ?: emptyList()))
            putStringArrayListExtra("CARD_SUBTYPES", ArrayList(card.subtypes ?: emptyList()))
            putExtra("CARD_EVOLVES_FROM", card.evolvesFrom ?: "")
            putStringArrayListExtra("CARD_EVOLVES_TO", ArrayList(card.evolvesTo ?: emptyList()))

            card.abilities?.firstOrNull()?.let {
                putExtra("CARD_ABILITY_NAME", it.name)
                putExtra("CARD_ABILITY_TEXT", it.text)
                putExtra("CARD_ABILITY_TYPE", it.type)
            }

            card.attacks?.getOrNull(0)?.let {
                putExtra("CARD_ATTACK1_NAME", it.name)
                putExtra("CARD_ATTACK1_DAMAGE", it.damage)
                putExtra("CARD_ATTACK1_TEXT", it.text)
                putExtra("CARD_ATTACK1_COST", it.convertedEnergyCost)
                putStringArrayListExtra("CARD_ATTACK1_COST_TYPES", ArrayList(it.cost ?: emptyList()))
            }

            card.attacks?.getOrNull(1)?.let {
                putExtra("CARD_ATTACK2_NAME", it.name)
                putExtra("CARD_ATTACK2_DAMAGE", it.damage)
                putExtra("CARD_ATTACK2_TEXT", it.text)
                putExtra("CARD_ATTACK2_COST", it.convertedEnergyCost)
                putStringArrayListExtra("CARD_ATTACK2_COST_TYPES", ArrayList(it.cost ?: emptyList()))
            }

            card.weaknesses?.firstOrNull()?.let {
                putExtra("CARD_WEAKNESS_TYPE", it.type)
                putExtra("CARD_WEAKNESS_VALUE", it.value)
            }

            card.resistances?.firstOrNull()?.let {
                putExtra("CARD_RESISTANCE_TYPE", it.type)
                putExtra("CARD_RESISTANCE_VALUE", it.value)
            }

            putExtra("CARD_RETREAT_COST", card.convertedRetreatCost)
            putStringArrayListExtra("CARD_RETREAT_COST_TYPES", ArrayList(card.retreatCost ?: emptyList()))
        }
    }



    private fun openCardInfo(apiCard: ApiCard) {
        val card: Card = apiCard.toDomainModel() // convert DTO to domain model
        val intent = buildCardInfoIntent(card)    // now pass the domain model
        startActivity(intent)
    }

    private fun loadKnownArtists(): List<String> {
        val inputStream = requireContext().assets.open("artists.txt")
        return inputStream.bufferedReader().useLines { it.map(String::trim).filter { it.isNotEmpty() }.toList() }
    }

    fun findClosestArtist(input: String, knownArtists: List<String>): String? {
        val threshold = 3 // Max allowable typo distance
        return knownArtists.minByOrNull { levenshtein(it.lowercase(), input.lowercase()) }
            ?.takeIf { levenshtein(it.lowercase(), input.lowercase()) <= threshold }
    }

    // Basic Levenshtein function
    fun levenshtein(lhs: String, rhs: String): Int {
        val dp = Array(lhs.length + 1) { IntArray(rhs.length + 1) }
        for (i in 0..lhs.length) dp[i][0] = i
        for (j in 0..rhs.length) dp[0][j] = j
        for (i in 1..lhs.length) {
            for (j in 1..rhs.length) {
                val cost = if (lhs[i - 1] == rhs[j - 1]) 0 else 1
                dp[i][j] = minOf(dp[i - 1][j] + 1, dp[i][j - 1] + 1, dp[i - 1][j - 1] + cost)
            }
        }
        return dp[lhs.length][rhs.length]
    }

    fun classifySetSymbol(imageFile: File, onResult: (String?) -> Unit) {
        val client = OkHttpClient()
        val requestBody = imageFile.asRequestBody("application/octet-stream".toMediaType())
        val request = Request.Builder()
            .url("https://api-inference.huggingface.co/models/cc44h16a/Pokemon_TCG_Set_Classifier")
            .addHeader("Authorization", "Bearer ${BuildConfig.HUGGINGFACE_TOKEN}")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                requireActivity().runOnUiThread {
                    binding.loadingSpinner.visibility = View.GONE
                    Log.e("SetClassifier", "Failed to classify set symbol", e)
                    onResult(null)
                }
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                val responseBody = response.body?.string()
                Log.d("SetClassifier", "Response: $responseBody")

                val label = Regex("\"label\"\\s*:\\s*\"([^\"]+)").find(responseBody ?: "")?.groupValues?.get(1)

                requireActivity().runOnUiThread {
                    binding.loadingSpinner.visibility = View.GONE
                    if (!response.isSuccessful) {
                        onResult(null)
                        return@runOnUiThread
                    }

                    onResult(label)
                }
            }



        })
    }

    fun cropSetSymbolFromImage(bitmap: Bitmap): File {
        val x = bitmap.width - 200
        val y = bitmap.height - 150
        val width = 170
        val height = 120

        val cropped = Bitmap.createBitmap(bitmap, x, y, width, height)
        val file = File(requireContext().cacheDir, "set_symbol.jpg")
        val out = FileOutputStream(file)
        cropped.compress(Bitmap.CompressFormat.JPEG, 90, out)
        out.close()
        return file
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
