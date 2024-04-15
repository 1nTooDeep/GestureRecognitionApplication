package com.intoodeep.myapplication.GestureRecognition

import android.content.Context
import android.util.Log
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.Tensor
import java.io.File
import java.io.FileOutputStream
import kotlin.random.Random


class GestureRecognitionModel(context: Context,name:String) {
    val TAG = "GestureRecognitionModel"
    val model:Module
    init {
        this.model = Module.load(assetFilePath(context,name))
    }
    fun predict(tensors:ArrayList<Tensor>): IValue {
        var input = IValue.from(tensors.get(0))
        for(i in 0 until tensors.size){
            if (i == 0) continue
            val tensorIValue = IValue.from(tensors.get(i))
            input = IValue.listFrom(input,tensorIValue)
        }

        lateinit var output : IValue
        try {
            output = this.model.forward(input)
        }
        catch (t:Throwable){
            t.printStackTrace()
        }
        Log.d(TAG,output.toString())
        return output
    }
    fun randomPredict(): List<Double> {
        val randomValues = List(10) { Random.nextDouble() }
        val sum = randomValues.sum()
        val probabilities = randomValues.map { it / sum }.toMutableList()
        val lastProbability = 1.0 - probabilities.sum()
        probabilities += lastProbability
        return probabilities.toList()
    }
    fun assetFilePath(context: Context, assetName: String): String {
        val file = File(context.filesDir, assetName)
        if (file.exists() && file.length() > 0) {
            return file.absolutePath
        }

        context.assets.open(assetName).use { input ->
            FileOutputStream(file).use { output ->
                val buffer = ByteArray(4 * 1024)
                var read: Int
                while (input.read(buffer).also { read = it } != -1) {
                    output.write(buffer, 0, read)
                }
                output.flush()
            }
        }
        return file.absolutePath
    }

}