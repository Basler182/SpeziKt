package edu.stanford.spezikt.core.bluetooth.data.mapper

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import com.google.common.truth.Truth.assertThat
import edu.stanford.spezikt.core.bluetooth.data.model.BLEServiceType
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import java.util.UUID

class BloodPressureMapperTest {
    private val serviceType = BLEServiceType.BLOOD_PRESSURE
    private val mapper = BloodPressureMapper()
    private val service: BluetoothGattService = mockk {
        every { uuid } returns serviceType.service
    }
    private val characteristic: BluetoothGattCharacteristic = mockk {
        every { service } returns this@BloodPressureMapperTest.service
        every { uuid } returns serviceType.characteristic
    }

    @Test
    fun `it should recognise characteristic with BLOOD_PRESSURE values`() {
        // given
        val sut = mapper

        // when
        val result = sut.recognises(characteristic)

        // then
        assertThat(result).isTrue()
    }

    @Test
    fun `it should recognise characteristic with unknown values`() {
        // given
        every { characteristic.uuid } returns UUID.randomUUID()
        val sut = mapper

        // when
        val result = sut.recognises(characteristic)

        // then
        assertThat(result).isFalse()
    }
}