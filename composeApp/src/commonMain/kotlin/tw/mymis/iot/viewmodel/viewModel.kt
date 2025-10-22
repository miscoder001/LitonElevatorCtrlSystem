package tw.mymis.iot.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

import kotlinx.coroutines.launch
import tw.mymis.iot.bluetooth.BluetoothDevice
import tw.mymis.iot.bluetooth.BluetoothException
import tw.mymis.iot.bluetooth.BluetoothService
import tw.mymis.iot.bluetooth.BluetoothState
import tw.mymis.iot.bluetooth.getBluetoothService

private const val PRICE_PER_CUPCAKE = 2.00

/** Additional cost for same day pickup of an order */
private const val PRICE_FOR_SAME_DAY_PICKUP = 3.00
class LitonViewModel : ViewModel() {

    public lateinit  var context: Any
    public lateinit  var bluetoothService: BluetoothService
    public lateinit  var bluetoothDevice: BluetoothDevice
    public lateinit var  receiveData : StateFlow<*>
    val batteryLevel = mutableStateOf(0)
    val serviceUUID =  mutableStateOf("")
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)

    //private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
    public val _periServices = mutableStateListOf<String>()
    // val periService:  StateFlow<String> = _periServices
    private val _uiState = MutableStateFlow(BluetoothState())
    val uiState: StateFlow<BluetoothState> = _uiState
    init {
        observeConnectionState()
    }

    fun cleanDevices() {
        // val cleanDevice : List<BluetoothDevice> = emptyList()
        _uiState.value.copy( discoveredDevices = emptyList())

    }

    fun startScanning() {
        scope.launch {
            try {
                _uiState.value = _uiState.value.copy(isScanning = true)
                bluetoothService.startDeviceDiscovery().collect { device ->
                    val currentDevices = _uiState.value.discoveredDevices.toMutableList()
                    if (!currentDevices.any { it.address == device.address }) {
                        currentDevices.add(device)
                        _uiState.value = _uiState.value.copy(discoveredDevices = currentDevices)
                    }
                }
            } catch (e: BluetoothException) {
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isScanning = false
                )
            }
        }
    }

    fun stopScanning() {
        bluetoothService.stopDeviceDiscovery()
        _uiState.value = _uiState.value.copy(isScanning = false)
    }

    fun connectToDevice(device: BluetoothDevice) {
        _periServices.clear()
        scope.launch {
            val success = bluetoothService.connectToDevice(device)
            if (!success) {
                _uiState.value = _uiState.value.copy(error = "連接失敗")
            } else {
                _uiState.value = _uiState.value.copy()
                bluetoothDevice = device
            }
        }
    }

    fun disconnectFromDevice() {
        scope.launch {
            bluetoothService.disconnectFromDevice()

        }
    }
    fun listServices() {
        scope.launch {
            bluetoothService.listServiceFromDevice()
        }
    }

    fun readDeviceCharacter() {
        println("--===== 等候資料 ------========")
        scope.launch {

        }
    }

    fun fetchCharacteristicByUUID(uuidString : String) {
        bluetoothService.getCharacteristicByUUID(uuidString)
    }

    fun processCharData() : Flow<ByteArray> {
        return bluetoothService.receiveData()
    }

    fun sendMessage(message: String) {
        val data = message.encodeToByteArray()
        val success = bluetoothService.sendData(data)
        if (!success) {
            _uiState.value = _uiState.value.copy(error = "發送失敗")
        }
    }

    private fun observeConnectionState() {
        scope.launch {
            bluetoothService.getConnectionState().collect { state ->
                _uiState.value = _uiState.value.copy(connectionState = state)
            }
        }

        scope.launch {
            bluetoothService.receiveData().collect { data ->
                val message = data.decodeToString()
                val currentMessages = _uiState.value.receivedMessages.toMutableList()
                currentMessages.add(message)
                _uiState.value = _uiState.value.copy(receivedMessages = currentMessages)
            }
        }
    }
}