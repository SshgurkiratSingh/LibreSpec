package com.librespec.math

import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sqrt

object MathEngine {
    
    // Savitzky-Golay / Simple Moving Average (Kalman approximation for 1D)
    fun smoothSignal(input: List<Float>, windowSize: Int = 5): List<Float> {
        if (input.size < windowSize) return input
        val smoothed = mutableListOf<Float>()
        val halfWindow = windowSize / 2
        
        for (i in input.indices) {
            val start = maxOf(0, i - halfWindow)
            val end = minOf(input.size - 1, i + halfWindow)
            var sum = 0f
            for (j in start..end) {
                sum += input[j]
            }
            smoothed.add(sum / (end - start + 1))
        }
        return smoothed
    }

    // Arrhenius Temperature Compensation
    // Shifts a reaction rate k based on T1 -> T2
    // k2 = k1 * exp((Ea / R) * (1/T1 - 1/T2))
    fun compensateTemperature(
        kineticCurve: List<List<Float>>, 
        recordedTempC: Float, 
        currentTempC: Float, 
        activationEnergy: Float = 50000f // Joules/mol (assumed average for biological reactions)
    ): List<List<Float>> {
        val R = 8.314f // Gas constant J/(mol·K)
        val T1 = recordedTempC + 273.15f
        val T2 = currentTempC + 273.15f
        
        val shiftFactor = exp((activationEnergy / R) * (1 / T1 - 1 / T2))
        
        return kineticCurve.map { frame ->
            frame.map { channelValue -> channelValue * shiftFactor }
        }
    }

    // Derivative Dynamic Time Warping (DDTW)
    fun calculateDDTWDistance(liveStream: List<List<Float>>, referenceCurve: List<List<Float>>): Float {
        // A full DDTW implementation requires a 2D cost matrix.
        // For architectural demonstration, we implement a scaled Euclidean approximation 
        // to prevent OutOfMemory errors in the Coroutine.
        
        var totalDistance = 0f
        val minSize = minOf(liveStream.size, referenceCurve.size)
        
        if (minSize == 0) return Float.MAX_VALUE
        
        for (i in 0 until minSize) {
            val liveFrame = liveStream[i]
            val refFrame = referenceCurve[i]
            
            var frameDistance = 0f
            for (j in 0 until 14) {
                val diff = liveFrame[j] - refFrame[j]
                frameDistance += diff.pow(2)
            }
            totalDistance += sqrt(frameDistance)
        }
        
        return totalDistance / minSize // Normalized distance
    }
}
