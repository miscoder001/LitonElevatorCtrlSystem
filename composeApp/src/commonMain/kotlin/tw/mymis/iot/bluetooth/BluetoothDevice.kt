package tw.mymis.iot.bluetooth

data class BluetoothDevice(
    val id: String = "",
    val name: String?,
    val address: String,
    val isConnected: Boolean = false,
    val rssi: Int? = null,
    val services: MutableList<String> = mutableListOf()
)
