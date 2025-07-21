package com.mobicom.s16.mco

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.mobicom.s16.mco.data.remote.api.RetrofitClient
import com.mobicom.s16.mco.data.remote.dto.ApiCard
import com.mobicom.s16.mco.data.remote.dto.CardsResponse
import com.mobicom.s16.mco.databinding.FragmentScannerBinding
import com.mobicom.s16.mco.domain.model.Card
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

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
        val screenSize = Size(1280, 720)

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

        // Get focusBox bounds
        val focusTop = binding.focusBox.top
        val focusBottom = binding.focusBox.bottom

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                Log.d("OCR", "Full recognized text:\n${visionText.text}")

                var cardNumberRaw: String? = null
                var cardName: String? = null
                val cardNumberRegex = Regex("""\b(\d{1,3})/(\d{1,3})\b""")

                for (block in visionText.textBlocks) {
                    for (line in block.lines) {
                        val y = line.boundingBox?.centerY() ?: continue
                        val lineText = line.text.trim()
                        val cleanedLine = lineText.replace(Regex("[^0-9/]"), "")

                        // Only use lines inside focus box vertical range
                        if (y in focusTop..focusBottom) {
                            Log.d("OCR_FILTERED_LINE", "Line (y=$y): $lineText")

                            if (cardNumberRaw == null && cardNumberRegex.containsMatchIn(cleanedLine)) {
                                cardNumberRaw = cardNumberRegex.find(cleanedLine)?.value
                            }

                            if (cardName == null && lineText.length in 3..30 && !lineText.contains("/")) {
                                cardName = lineText
                            }
                        }
                    }
                }

                // Fallback name if not found inside focus box
                if (cardName == null) {
                    cardName = visionText.text.lines()
                        .firstOrNull { it.length in 3..30 && !it.contains("/") }
                        ?.trim()
                }

                Log.d("OCR_RESULT", "Detected card number: $cardNumberRaw")
                Log.d("OCR_RESULT", "Detected card name: $cardName")

                if (cardNumberRaw != null && cardName != null) {
                    val parts = cardNumberRaw.split("/")
                    val cardNumber = parts.getOrNull(0)?.trim()
                    val printedTotal = parts.getOrNull(1)?.trim()?.toIntOrNull()

                    if (cardNumber != null && printedTotal != null) {
                        searchCardByNumberAndName(cardNumber, printedTotal, cardName)
                    } else {
                        Toast.makeText(requireContext(), "Invalid card number format", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Card number or name not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("OCR", "Text recognition failed", e)
                Toast.makeText(requireContext(), "OCR failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }




    private fun searchCardByNumberAndName(cardNumber: String, printedTotal: Int, cardName: String) {
        val query = "number:\"$cardNumber\""
        Log.d("API_CALL", "Searching with: number=$cardNumber, printedTotal=$printedTotal, name=$cardName")

        RetrofitClient.api.searchCardByNameAndNumber(query).enqueue(object : Callback<CardsResponse> {
            override fun onResponse(call: Call<CardsResponse>, response: Response<CardsResponse>) {
                if (response.isSuccessful) {
                    val cards = response.body()?.data ?: emptyList()
                    val matchedCards = cards.filter {
                        it.set.printedTotal == printedTotal &&
                                it.name.equals(cardName, ignoreCase = true)
                    }

                    if (matchedCards.isNotEmpty()) {
                        val matchedCard = matchedCards.first()
                        Log.d("CARD_MATCH", "Matched: ${matchedCard.name} ${matchedCard.number}/${matchedCard.set.printedTotal}")
                        Toast.makeText(requireContext(), "Detected: ${matchedCard.name} (#${matchedCard.number})", Toast.LENGTH_SHORT).show()
                        openCardInfo(matchedCard)
                    } else {
                        Toast.makeText(requireContext(), "No exact match found", Toast.LENGTH_SHORT).show()
                        Log.d("API_RESULT", "No matching cards found after filtering")
                    }
                } else {
                    Toast.makeText(requireContext(), "API Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                    Log.e("API_RESULT", "API error: ${response.code()} - ${response.message()}")
                }
            }

            override fun onFailure(call: Call<CardsResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "API Failure: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("API_RESULT", "API request failed", t)
            }
        })
    }

    private fun openCardInfo(apiCard: ApiCard) {
        val card = Card(
            name = apiCard.name,
            set = apiCard.set.name ?: "Unknown Set",
            hp = apiCard.hp ?: "N/A",
            supertype = apiCard.supertype ?: "N/A",
            firstAttack = apiCard.attacks?.firstOrNull()?.name ?: "None",
            price = apiCard.tcgplayer?.prices?.holofoil?.market?.toString() ?: "N/A",
            imageUrl = apiCard.images.large ?: "",
            rarity = apiCard.rarity ?: "Unknown"
        )

        val intent = Intent(requireContext(), CardInfoActivity::class.java).apply {
            putExtra("card_name", card.name)
            putExtra("card_set", card.set)
            putExtra("card_hp", card.hp)
            putExtra("card_supertype", card.supertype)
            putExtra("card_attack", card.firstAttack)
            putExtra("card_price", card.price)
            putExtra("card_image", card.imageUrl)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}