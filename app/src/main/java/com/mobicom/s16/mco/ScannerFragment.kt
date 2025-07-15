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

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val resultText = visionText.text
                Log.d("OCR", "Full recognized text:\n$resultText")

                // Log by line
                for (block in visionText.textBlocks) {
                    for (line in block.lines) {
                        Log.d("OCR_LINE", line.text)
                    }
                }

                // Regex match for card number like 12/108
                val cardNumberRegex = Regex("""\b\d{1,3}/\d{1,3}\b""")
                val match = cardNumberRegex.find(resultText)
                val cardNumber = match?.value

                Log.d("OCR_MATCH", "Extracted card number: $cardNumber")

                if (cardNumber != null) {
                    searchCardByNumber(cardNumber)
                } else {
                    Toast.makeText(requireContext(), "Card number not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("OCR", "Text recognition failed", e)
                Toast.makeText(requireContext(), "OCR failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun searchCardByNumber(cardNumber: String) {
        val query = "number:$cardNumber"
        Log.d("API_CALL", "Searching card with query: $query")

        RetrofitClient.api.getCards(1, 250).enqueue(object : Callback<CardsResponse> {
            override fun onResponse(call: Call<CardsResponse>, response: Response<CardsResponse>) {
                if (response.isSuccessful) {
                    val cards = response.body()?.data ?: emptyList()
                    if (cards.isNotEmpty()) {
                        val matchedCard = cards.first()
                        Log.d("API_RESULT", "Matched card: ${matchedCard.name} (${matchedCard.set.name}) - Number: ${matchedCard.number}")
                        openCardInfo(matchedCard)
                    } else {
                        Toast.makeText(requireContext(), "No card found", Toast.LENGTH_SHORT).show()
                        Log.d("API_RESULT", "No card found in response")
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
            imageUrl = apiCard.images.large ?: ""
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
