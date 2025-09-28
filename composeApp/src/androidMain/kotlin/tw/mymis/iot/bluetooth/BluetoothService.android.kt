package tw.mymis.iot.bluetooth

import android.content.Context
import android.Manifest
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import tw.mymis.iot.viewmodel.LitonViewModel
import java.util.*


class BluetoothServiceAndroid(viewModel: LitonViewModel) : BluetoothService {

    private val context = viewModel.context as Context
    //private  val viewModel = viewModel
    lateinit var bluetoothManager: BluetoothManager
    lateinit var bluetoothAdapter: BluetoothAdapter //= bluetoothManager.adapter
    lateinit var bluetoothLeScanner : BluetoothLeScanner

    private var bluetoothGatt: BluetoothGatt? = null
    private var targetCharacteristic: BluetoothGattCharacteristic? = null

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    private val _receivedData = MutableStateFlow<ByteArray?>(null)

    // Service 和 Characteristic UUID (需要根據你的設備設定)
    companion object {
        private val SERVICE_UUID = UUID.fromString("0000180F-0000-1000-8000-00805F9B34FB")
        private val CHARACTERISTIC_UUID = UUID.fromString("00002A19-0000-1000-8000-00805F9B34FB")
        private val DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    }

    // BLE 掃描回調
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.let { scanResult ->
                // 透過 callbackFlow 發送發現的設備
            }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            results?.forEach { result ->
                // 處理批量掃描結果
            }
        }

        override fun onScanFailed(errorCode: Int) {
            // 掃描失敗處理
        }
    }

    // GATT 回調
    private val gattCallback = object : BluetoothGattCallback() {
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    _connectionState.value = ConnectionState.CONNECTED
                    // 發現服務
                    gatt?.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    _connectionState.value = ConnectionState.DISCONNECTED
                    bluetoothGatt = null
                }
                BluetoothProfile.STATE_CONNECTING -> {
                    _connectionState.value = ConnectionState.CONNECTING
                }
            }
        }


        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // 查找目標服務和特徵
                val service = gatt?.getService(SERVICE_UUID)
                targetCharacteristic = service?.getCharacteristic(CHARACTERISTIC_UUID)

                // 啟用通知
                targetCharacteristic?.let { characteristic ->
                    gatt?.setCharacteristicNotification(characteristic, true)
                    val descriptor = characteristic.getDescriptor(DESCRIPTOR_UUID)
                    descriptor?.let {
                        it.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        gatt?.writeDescriptor(it) ?: ""

                    }
                }
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            characteristic?.value?.let { data ->
                _receivedData.value = data
            }
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            // 寫入完成回調
        }
    }

    override fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }

    @androidx.annotation.RequiresPermission(allOf = arrayOf(android.Manifest.permission.BLUETOOTH_SCAN,android.Manifest.permission.BLUETOOTH_CONNECT))
    override fun startDeviceDiscovery(): Flow<BluetoothDevice> = callbackFlow()
      {
        if (!checkBluetoothPermissions()) {
            // throw BluetoothException.PermissionDenie
            throw Exception()
        }

        if (!isBluetoothEnabled()) {
            throw BluetoothException.BluetoothDisabled
        }


        val scanCallback = object : ScanCallback() {
            @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                result?.let { scanResult ->
                    val device = BluetoothDevice(
                        name = scanResult.device.name,
                        address = scanResult.device.address,
                        rssi = scanResult.rssi
                    )
                    trySend(device)
                }
            }

            override fun onScanFailed(errorCode: Int) {
                close(BluetoothException.ConnectionFailed)
            }
        }

        bluetoothLeScanner?.startScan(scanCallback)

        awaitClose {
            bluetoothLeScanner?.stopScan(scanCallback)
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    override fun stopDeviceDiscovery() {
        bluetoothLeScanner?.stopScan(scanCallback)
    }


    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override suspend fun connectToDevice(device: BluetoothDevice): Boolean {

        return try {
            if (!checkBluetoothPermissions()) {
               // throw BluetoothException.PermissionDenied
                throw Exception()
            }

            _connectionState.value = ConnectionState.CONNECTING

            val bluetoothDevice = bluetoothAdapter?.getRemoteDevice(device.address)
            bluetoothGatt = bluetoothDevice?.connectGatt(context as Context, false, gattCallback)

            bluetoothGatt != null
        } catch (e: Exception) {
            _connectionState.value = ConnectionState.FAILED
            false
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override suspend fun disconnectFromDevice() {
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
        _connectionState.value = ConnectionState.DISCONNECTED
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun sendData(data: ByteArray): Boolean {
        return try {
            targetCharacteristic?.let { characteristic ->
                characteristic.value = data
                bluetoothGatt?.writeCharacteristic(characteristic) == true
            } ?: false
        } catch (e: Exception) {
            false
        }
    }

    override fun receiveData(): Flow<ByteArray> = flow {
        _receivedData.collect { data ->
            data?.let { emit(it) }
        }
    }

    override fun getConnectionState(): Flow<ConnectionState> {
        return _connectionState
    }

    private fun checkBluetoothPermissions(): Boolean {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }

        return permissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
}
actual fun getBluetoothService(viewModel: LitonViewModel): BluetoothService = BluetoothServiceAndroid(viewModel)