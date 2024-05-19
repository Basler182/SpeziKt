package edu.stanford.spezikt.core.bluetooth.domain

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import com.google.common.truth.Truth.assertThat
import edu.stanford.spezi.core.testing.SpeziTestScope
import edu.stanford.spezi.core.testing.runTestUnconfined
import edu.stanford.spezikt.core.bluetooth.data.model.BLEDeviceSession
import edu.stanford.spezikt.core.bluetooth.data.model.BLEServiceEvent
import edu.stanford.spezikt.core.bluetooth.data.model.BLEServiceState
import io.mockk.Called
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import org.junit.Before
import org.junit.Test

class BLEServiceTest {
    private val bluetoothAdapter: BluetoothAdapter = mockk()
    private val permissionChecker: PermissionChecker = mockk()
    private val deviceScanner: BLEDeviceScanner = mockk()
    private val deviceConnectorFactory: BLEDeviceConnector.Factory = mockk()
    private val deviceConnector: BLEDeviceConnector = mockk()
    private val deviceConnectorEvents = MutableSharedFlow<BLEServiceEvent>()
    private val deviceScannerEvents = MutableSharedFlow<BLEDeviceScanner.Event>()
    private val device: BluetoothDevice = mockk {
        every { address } returns "some device address"
    }

    private val bleService by lazy {
        BLEServiceImpl(
            bluetoothAdapter = bluetoothAdapter,
            permissionChecker = permissionChecker,
            deviceScanner = deviceScanner,
            scope = SpeziTestScope(),
            deviceConnectorFactory = deviceConnectorFactory,
        )
    }

    @Before
    fun setup() {
        every { deviceScanner.isScanning } returns false
        every { bluetoothAdapter.isEnabled } returns true
        every { permissionChecker.isPermissionGranted(any()) } returns true
        every { deviceConnectorFactory.create(any()) } returns deviceConnector
        with(deviceConnector) {
            every { events } returns deviceConnectorEvents
            every { connect() } just Runs
            every { disconnect() } just Runs
        }
        with(deviceScanner) {
            every { events } returns deviceScannerEvents
            every { startScanning() } just Runs
            every { stopScanning() } just Runs
        }
    }

    @Test
    fun `it should have idle state initially`() {
        // given
        val sut = bleService

        // when
        val state = sut.state.value

        // then
        assertThat(state).isEqualTo(BLEServiceState.Idle)
    }

    @Test
    fun `it should do nothing on start if device scanner is scanning`() {
        // given
        every { deviceScanner.isScanning } returns true

        // when
        bleService.start()

        // then
        verify { bluetoothAdapter wasNot Called }
        verify { permissionChecker wasNot Called }
        verify(exactly = 0) { deviceScanner.startScanning() }
    }

    @Test
    fun `it should emit BluetoothNotEnabled if adapter returns not enabled`() = runTestUnconfined {
        // given
        every { bluetoothAdapter.isEnabled } returns false

        // when
        bleService.start()

        // then
        assertEvent(BLEServiceEvent.BluetoothNotEnabled)
    }

    @Test
    fun `it should notify missing permissions correctly`() = runTestUnconfined {
        // given
        every { permissionChecker.isPermissionGranted(any()) } returns false
        val expectedPermissions = listOf(
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
        )

        // when
        bleService.start()

        // then
        assertEvent(BLEServiceEvent.MissingPermissions(expectedPermissions))
    }

    @Test
    fun `it should handle start correctly given preconditions are met`() = runTestUnconfined {
        // given
        every { deviceScanner.isScanning } returns false
        every { bluetoothAdapter.isEnabled } returns true
        every { permissionChecker.isPermissionGranted(any()) } returns true

        // when
        bleService.start()

        // then
        verify { deviceScanner.events }
        verify { deviceScanner.startScanning() }
        assertEvent(BLEServiceEvent.ScanningStarted)
    }

    @Test
    fun `it should handle failure scanner event correctly`() = runTestUnconfined {
        // given
        val errorCode = 42
        start()

        // when
        deviceScannerEvents.emit(BLEDeviceScanner.Event.Failure(errorCode))

        // then
        assertEvent(BLEServiceEvent.ScanningFailed(errorCode))
    }

    @Test
    fun `it should handle found even correctly`() = runTestUnconfined {
        // given
        start()

        // when
        emitDeviceFound()

        // then
        verify { deviceConnectorFactory.create(device) }
        verify { deviceConnector.connect() }
        verify { deviceConnector.events }
    }

    @Test
    fun `it should handle connected event correctly`() = runTestUnconfined {
        // given
        val event = BLEServiceEvent.Connected(device)

        // when
        setupDeviceConnectorEvent(event)

        // then
        assertEvent(event)
        assertThat(bleService.state.value).isEqualTo(BLEServiceState.Scanning(listOf(BLEDeviceSession(device, emptyList()))))
    }

    private fun start() {
        every { deviceScanner.isScanning } returns false
        every { bluetoothAdapter.isEnabled } returns true
        every { permissionChecker.isPermissionGranted(any()) } returns true
        bleService.start()
    }

    private suspend fun emitDeviceFound() {
        deviceScannerEvents.emit(BLEDeviceScanner.Event.DeviceFound(device))
    }

    private suspend fun setupDeviceConnectorEvent(event: BLEServiceEvent) {
        start()
        emitDeviceFound()
        deviceConnectorEvents.emit(event)
    }

    private suspend fun connect() {
        setupDeviceConnectorEvent(BLEServiceEvent.Connected(device))
    }

    private suspend fun assertEvent(event: BLEServiceEvent) {
        assertThat(bleService.events.first()).isEqualTo(event)
    }
}