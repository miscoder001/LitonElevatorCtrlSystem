package tw.mymis.iot.bluetooth

import kotlinx.coroutines.flow.Flow
import tw.mymis.iot.viewmodel.LitonViewModel

interface BluetoothService {
    fun isBluetoothEnabled(): Boolean
    fun startDeviceDiscovery(): Flow<BluetoothDevice>
    fun stopDeviceDiscovery()
    suspend fun connectToDevice(device: BluetoothDevice): Boolean
    suspend fun disconnectFromDevice()
    suspend fun listServiceFromDevice()
    suspend fun listCharacteristicsFromDevice()
    fun sendData(data: ByteArray): Boolean
    fun receiveData(): Flow<ByteArray>
    fun getConnectionState(): Flow<ConnectionState>
    fun getCharacteristicByUUID(uuidString: String)
}

enum class ConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    FAILED,
    EMPTY
}

sealed class BluetoothException : Exception() {
    object BluetoothNotSupported : BluetoothException()
    object BluetoothDisabled : BluetoothException()
    object DeviceNotFound : BluetoothException()
    object ConnectionFailed : BluetoothException()
}

expect fun getBluetoothService(viewModel: LitonViewModel) : BluetoothService