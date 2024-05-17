package com.intoodeep.myapplication.GestureService

import android.graphics.Bitmap
import org.pytorch.MemoryFormat
import org.pytorch.Tensor
import org.pytorch.torchvision.TensorImageUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.LinkedList
import java.util.concurrent.locks.ReentrantReadWriteLock

class DataStore(size:Int) {
    private val floatBufferList = LinkedList<ByteBuffer>()
    private val tensorList = LinkedList<Tensor>()
    private var maxSize:Int = size
    private val reentrantReadWriteLock = ReentrantReadWriteLock()
    private val readLock = reentrantReadWriteLock.readLock()
    private val writeLock = reentrantReadWriteLock.writeLock()
    fun put(byteBuffer: ByteBuffer){
        writeLock.lock()
        if(floatBufferList.size == maxSize && maxSize != 0){
            floatBufferList.removeFirst()
        }
        floatBufferList.add(byteBuffer)
        writeLock.unlock()
    }
    fun put(bitmap: Bitmap){
        val mean = floatArrayOf(0.2674f, 0.2676f, 0.2648f)
        val std = floatArrayOf(0.4377f, 0.4047f, 0.3925f)
        writeLock.lock()
        if(tensorList.size == maxSize && maxSize != 0){
            tensorList.removeFirst()
        }
        tensorList.add(TensorImageUtils.bitmapToFloat32Tensor(bitmap,mean,std,MemoryFormat.CONTIGUOUS))
        writeLock.unlock()
    }
    fun getByteBuffer(): LinkedList<ByteBuffer> {
        writeLock.lock()
        val clone = floatBufferList.clone() as LinkedList<ByteBuffer>
        writeLock.unlock()

        return clone
    }
    fun getTensor(): Tensor {
        writeLock.lock()
        val clone = tensorList.clone() as LinkedList<Tensor>
        writeLock.unlock()
        val buffer = ByteBuffer.allocateDirect( 3 * 30 * 112 * 112 * 4)
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        for(tensor in clone){
            for (value in tensor.dataAsFloatArray){
                buffer.putFloat(value)
            }
        }
        buffer.rewind()
        val tensor = Tensor.fromBlob(buffer.asFloatBuffer(), longArrayOf(1,30,3,112,112),MemoryFormat.CONTIGUOUS)

        return tensor
    }
    fun getSize():Int{
        readLock.lock()
        val size = floatBufferList.size + tensorList.size
        readLock.unlock()
        return size
    }
    fun clear(){
        writeLock.lock()
        tensorList.clear()
        floatBufferList.clear()
        writeLock.unlock()
    }
}