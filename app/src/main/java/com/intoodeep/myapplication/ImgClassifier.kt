package com.intoodeep.myapplication

val IMAGE_CLASSIFICATION = arrayOf(  // 这个就是你的神经网络能够识别的图片种类数目
    "Sliding Two Fingers Up",
    "Sliding Two Fingers Left",
    "Sliding Two Fingers Right",
    "Sliding Two Fingers Down",
    "Zooming In With Two Fingers",
    "Zooming Out With Two Fingers",
    "Swiping Right",
    "Swiping Up",
    "Swiping Down",
    "Doing other things",
    "No gesture"
)
class ImgClassifier(private val index: Int, private val value: Float) {

    fun getImageClassification() = IMAGE_CLASSIFICATION[index]
    fun getGarbageIndex() = index
    fun getGarbageValue() = value
}