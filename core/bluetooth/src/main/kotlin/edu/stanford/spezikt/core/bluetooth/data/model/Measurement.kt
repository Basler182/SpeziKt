package edu.stanford.spezikt.core.bluetooth.data.model

import java.time.ZonedDateTime

/**
 * Represents a measurement
 */
sealed interface Measurement {

    /**
     * Represents a blood pressure measurement.
     */
    data class BloodPressure(
        val flags: Flags,
        val systolic: Float,
        val diastolic: Float,
        val meanArterialPressure: Float,
        val timestampYear: Int,
        val timestampMonth: Int,
        val timestampDay: Int,
        val timeStampHour: Int,
        val timeStampMinute: Int,
        val timeStampSecond: Int,
        val pulseRate: Float,
        val userId: Int,
        val measurementStatus: Status
    ) : Measurement {

        /**
         * Represents the flags indicating which data fields are present.
         */
        data class Flags(
            val bloodPressureUnitsFlag: Boolean,
            val timeStampFlag: Boolean,
            val pulseRateFlag: Boolean,
            val userIdFlag: Boolean,
            val measurementStatusFlag: Boolean
        )

        /**
         * Represents the status of the measurement.
         */
        data class Status(
            val bodyMovementDetectionFlag: Boolean,
            val cuffFitDetectionFlag: Boolean,
            val irregularPulseDetectionFlag: Boolean,
            val pulseRateRangeDetectionFlags: Int,
            val measurementPositionDetectionFlag: Boolean
        )
    }

    /**
     * Represents a weight measurement.
     */
    data class Weight(
        val weight: Double?,
        val zonedDateTime: ZonedDateTime?,
        val userId: Int?,
        val bmi: Double?,
        val height: Double?
    ) : Measurement
}

