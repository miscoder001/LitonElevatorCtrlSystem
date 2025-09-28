package tw.mymis.iot.bluetooth

data class BluetoothState(
  val isScanning: Boolean = false,
  val discoveredDevices: List<BluetoothDevice> = emptyList(),
  val connectionState: ConnectionState = ConnectionState.DISCONNECTED,
  val receivedMessages: List<String> = emptyList(),
  val error: String? = null
)
