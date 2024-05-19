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
import edu.stanford.spezikt.core.bluetooth.data.model.Measurement
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
        assertScanning(emptyList())
    }

    @Test
    fun `it should handle disconnected event after connection event correctly`() = runTestUnconfined {
        // given
        val event = BLEServiceEvent.Connected(device)
        setupDeviceConnectorEvent(event)
        val connected = bleService.state.value == BLEServiceState.Scanning(listOf(BLEDeviceSession(device, emptyList())))

        // when
        setupDeviceConnectorEvent(BLEServiceEvent.Disconnected(device))

        // then
        assertThat(connected).isTrue()
        assertThat(bleService.state.value).isEqualTo(BLEServiceState.Scanning(emptyList()))
    }

    @Test
    fun `it should handle disconnected event correctly`() = runTestUnconfined {
        // given
        val event = BLEServiceEvent.Disconnected(device)

        // when
        setupDeviceConnectorEvent(event)

        // then
        assertEvent(event)
        assertThat(bleService.state.value).isEqualTo(BLEServiceState.Idle)
    }

    @Test
    fun `it should handle MeasurementReceived after connect event correctly correctly`() = runTestUnconfined {
        // given
        setupDeviceConnectorEvent(BLEServiceEvent.Connected(device))

        // when
        val measurement: Measurement = mockk()
        val event = BLEServiceEvent.MeasurementReceived(device, measurement)
        setupDeviceConnectorEvent(event)

        // then
        assertEvent(event)
        assertScanning(listOf(measurement))
    }

    @Test
    fun `it should handle MeasurementReceived event correctly correctly`() = runTestUnconfined {
        // given
        val measurement: Measurement = mockk()
        val event = BLEServiceEvent.MeasurementReceived(device, measurement)

        // when
        setupDeviceConnectorEvent(event)

        // then
        assertEvent(event)
        assertScanning(listOf(measurement))
    }

    private fun start() {
        every { deviceScanner.isScanning } returns false
        every { bluetoothAdapter.isEnabled } returns true
        every { permissionChecker.isPermissionGranted(any()) } returns true
        bleService.start()
    }

    private fun assertScanning(measurements: List<Measurement>) {
        assertThat(bleService.state.value).isEqualTo(BLEServiceState.Scanning(listOf(BLEDeviceSession(device, measurements))))
    }

    private suspend fun emitDeviceFound() {
        deviceScannerEvents.emit(BLEDeviceScanner.Event.DeviceFound(device))
    }

    private suspend fun setupDeviceConnectorEvent(event: BLEServiceEvent) {
        start()
        emitDeviceFound()
        deviceConnectorEvents.emit(event)
    }

    private suspend fun assertEvent(event: BLEServiceEvent) {
        assertThat(bleService.events.first()).isEqualTo(event)
    }
}