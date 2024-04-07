package com.intoodeep.myapplication.GestureRecognition

import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.Tensor

class GestureRecognitionModel {
    private lateinit var model:Module
    init {
        var model = Module.load("assets/C3D.pth")
    }

    fun predict(input:Tensor): IValue? {

        return this.model.forward()
    }
}