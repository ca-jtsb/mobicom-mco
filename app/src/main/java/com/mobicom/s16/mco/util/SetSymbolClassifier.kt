package com.mobicom.s16.mco

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.vision.classifier.ImageClassifier
import org.tensorflow.lite.task.vision.classifier.ImageClassifier.ImageClassifierOptions

fun classifySetSymbol(context: Context, bitmap: Bitmap): String? {
    val image = TensorImage.fromBitmap(bitmap)

    val options = ImageClassifierOptions.builder()
        .setScoreThreshold(0.0f) // Allow all predictions
        .setBaseOptions(
            BaseOptions.builder()
                .setNumThreads(2)
                .build()
        )
        .build()

    val classifier = ImageClassifier.createFromFileAndOptions(
        context,
        "model_quant.tflite",
        options
    )

    val results = classifier.classify(image)

    if (results.isEmpty() || results[0].categories.isEmpty()) {
        Log.e("SET_CLASSIFIER", "No classification result.")
        return null
    }

    val sorted = results[0].categories.sortedByDescending { it.score }

    sorted.forEachIndexed { i, cat ->
        Log.d("SET_CLASSIFIER", "[$i] ${cat.label} — ${"%.2f".format(cat.score * 100)}%")
    }

    val top = sorted.first()

    Log.i("SET_CLASSIFIER", "Returning: ${top.label} — ${"%.2f".format(top.score * 100)}%")

    return top.label  // Always return the top result
}

