package tw.mymis.iot.bluetooth

import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.refTo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import platform.CoreBluetooth.*
import platform.Foundation.*
import platform.darwin.NSObject
import platform.posix.memcpy
import tw.mymis.iot.viewmodel.LitonViewModel
data class CBServiceObject(
    val cbService: CBService,
    val cbChars: MutableList<CBCharacteristic> = mutableListOf()
)

class BluetoothServiceIOS(viewModel: LitonViewModel) : BluetoothService {

    private var centralManager: CBCentralManager? = null
    private var peripheral: CBPeripheral? = null
    private var characteristic: CBCharacteristic? = null
    private var isBluetoothEnabled = false
    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    private val _discoveredDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    private val _receivedData = MutableStateFlow<ByteArray?>(null)

    var cbServices: MutableMap<String, CBServiceObject> = mutableMapOf()

    private val centralDelegate = object : NSObject(), CBCentralManagerDelegateProtocol {

        // 只要 CentralManager 的狀態異動 便會呼叫此method
        override fun centralManagerDidUpdateState(central: CBCentralManager) {
            when (central.state) {
                CBManagerStatePoweredOn -> {
                    // 藍牙已開啟，可以開始掃描
                    isBluetoothEnabled = true
                    //central.scanForPeripheralsWithServices(null,null)
                }
                CBManagerStatePoweredOff -> {
                    _connectionState.value = ConnectionState.DISCONNECTED
                    isBluetoothEnabled = false
                }
                else -> {
                    // 處理其他狀態
                    _connectionState.value = ConnectionState.FAILED
                }
            }
        }

        // 一但啟動 StartScan 發現任何的裝置便會呼叫此方法
        override fun centralManager(
            central: CBCentralManager,
            didDiscoverPeripheral: CBPeripheral,
            advertisementData: Map<Any?, *>,
            RSSI: NSNumber
        )
        {
            val device = BluetoothDevice(
                name = didDiscoverPeripheral.name,
                address = didDiscoverPeripheral.identifier.UUIDString,
                rssi = RSSI.intValue
            )

            val currentDevices = _discoveredDevices.value.toMutableList()
            if (!currentDevices.any { it.address == device.address }) {
                currentDevices.add(device)
                _discoveredDevices.value = currentDevices
            }
        }

        override fun centralManager(
            central: CBCentralManager,
            didConnectPeripheral: CBPeripheral
        )
        {
            _connectionState.value = ConnectionState.CONNECTED
            peripheral = didConnectPeripheral
            didConnectPeripheral.delegate = peripheralDelegate
            didConnectPeripheral.discoverServices(null)
        }

        override fun centralManager(
            central: CBCentralManager,
            didFailToConnectPeripheral: CBPeripheral,
            error: NSError?
        ) {
            _connectionState.value = ConnectionState.FAILED
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private val peripheralDelegate = object : NSObject(), CBPeripheralDelegateProtocol {

        // 呼叫
        override fun peripheral(
            peripheral: CBPeripheral,
            didDiscoverServices: NSError?
        )
        {
            peripheral.services?.forEach { service ->
                val cbService = service as CBService
                val entity = CBServiceObject(cbService = cbService)
                val uuidString = cbService.UUID.UUIDString
                //val uuidString: String = cbService.UUID().toString()
                cbServices.put(uuidString,entity )
                viewModel._periServices.add(cbService.UUID().UUIDString())
                // 開始解析服務特徵
                peripheral.discoverCharacteristics(null, forService = cbService)
            }
        }

        override fun peripheral(
            peripheral: CBPeripheral,
            didDiscoverCharacteristicsForService: CBService,
            error: NSError?
        )
        {
            var entity: CBServiceObject? = null
            val uuidString: String = didDiscoverCharacteristicsForService.UUID.UUIDString
            //val uuidString: String = didDiscoverCharacteristicsForService.UUID().toString()
            if( cbServices.contains(uuidString)) {
                entity = cbServices.get(uuidString)
            } else {
                entity = CBServiceObject( didDiscoverCharacteristicsForService)
                cbServices.put(uuidString, entity)
            }

            didDiscoverCharacteristicsForService.characteristics?.forEach { char ->
                val cbChar = char as CBCharacteristic
                if (cbChar.properties.and(CBCharacteristicPropertyNotify.toInt().toULong()).toInt() != 0) {
                    characteristic = cbChar
                    entity?.cbChars?.add(cbChar)
                    peripheral.setNotifyValue(true, forCharacteristic = cbChar)
                }
            }
        }

        override fun peripheral(
            peripheral: CBPeripheral,
            didUpdateValueForCharacteristic: CBCharacteristic,
            error: NSError?
        ) {
            val data = didUpdateValueForCharacteristic.value

            data?.let {
                val bytes = ByteArray(it.length.toInt())
                memcpy(bytes.refTo(0), it.bytes, it.length)
                _receivedData.value = bytes

            }
            println("資料長度： ${_receivedData.value?.size}")
            println("資料內容： ${_receivedData.value?.toHexString()}")
            val value = (_receivedData.value?.get(0) )
            viewModel.batteryLevel.value = value!!.toInt()

        }

    }

    init {
        centralManager = CBCentralManager(centralDelegate, null)
    }

    override fun isBluetoothEnabled(): Boolean {
        return centralManager?.state == CBManagerStatePoweredOn
    }

    override fun startDeviceDiscovery(): Flow<BluetoothDevice> = flow {
        if (!isBluetoothEnabled()) {
            print("Discovery失敗 ！！！")
            throw BluetoothException.BluetoothDisabled
        }

        centralManager?.scanForPeripheralsWithServices(null, null)

        _discoveredDevices.collect { devices ->
            devices.forEach {
                emit(it)
            }
        }
    }

    override fun stopDeviceDiscovery() {
        centralManager?.stopScan()
    }

    override suspend fun connectToDevice(device: BluetoothDevice): Boolean {
        return try {
            _connectionState.value = ConnectionState.CONNECTING

            val uuid = NSUUID(device.address)
            val peripherals = centralManager?.retrievePeripheralsWithIdentifiers(listOf(uuid))
            val targetPeripheral = peripherals?.firstOrNull() as? CBPeripheral

            targetPeripheral?.let {
                centralManager?.connectPeripheral(it, null)
                peripheral = targetPeripheral
                peripheral?.delegate = peripheralDelegate
                peripheral?.discoverServices(null)

                true
            } ?: false
        } catch (e: Exception) {
            _connectionState.value = ConnectionState.FAILED
            false
        }
    }


    override suspend fun listServiceFromDevice() {
        // peripheral?.let { println( it.services()) }
        cbServices.keys.forEach{
           // println(" key: ${it}")
        }
    }

    override suspend fun listCharacteristicsFromDevice() {
        // peripheral?.let { println( it.services)}
    }
    override suspend fun disconnectFromDevice() {
        peripheral?.let {
            centralManager?.cancelPeripheralConnection(it)
        }
        _connectionState.value = ConnectionState.DISCONNECTED
        peripheral = null
        characteristic = null
    }

    override fun sendData(data: ByteArray): Boolean {
        return try {
            peripheral?.let { p ->
                characteristic?.let { c ->
                    val nsData = data.toNSData()
                    p.writeValue(nsData, forCharacteristic = c, type = CBCharacteristicWriteWithResponse)
                    true
                }
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

    override fun getCharacteristicByUUID(uuidString: String) {
        var cbSvcObj:CBServiceObject? = null
        if( cbServices.containsKey(uuidString)) {
            cbSvcObj = cbServices.get(uuidString)
//            println("服務： ${cbSvcObj?.cbService?.UUID?.UUIDString}")
//            println("特徵： ${cbSvcObj?.cbChars}")
            val cbService = cbSvcObj?.cbService
            val cbChara: CBCharacteristic = cbSvcObj!!.cbChars.get(0)
            peripheral?.readValueForCharacteristic(cbChara, )

        }
    }
}

// 擴展函數：ByteArray 轉換為 NSData
@OptIn(ExperimentalForeignApi::class)
private fun ByteArray.toNSData(): NSData {
    return NSData.dataWithBytes(this.refTo(0) as COpaquePointer?, this.size.toULong())
}

actual fun getBluetoothService(viewModel: LitonViewModel): BluetoothService = BluetoothServiceIOS(viewModel)