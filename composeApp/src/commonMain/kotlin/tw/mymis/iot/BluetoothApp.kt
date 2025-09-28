package tw.mymis.iot

// commonMain/kotlin/ui/BluetoothApp.kt
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tw.mymis.iot.bluetooth.BluetoothDevice
import tw.mymis.iot.bluetooth.ConnectionState
import tw.mymis.iot.viewmodel.LitonViewModel

@Composable
fun BluetoothApp(viewModel: LitonViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 連接狀態顯示
        ConnectionStatusCard(uiState.connectionState)

        Spacer(modifier = Modifier.height(16.dp))

        // 控制按鈕
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = {
                    if (uiState.isScanning) {
                        viewModel.stopScanning()
                    } else {
                        viewModel.startScanning()
                    }
                }
            ) {
                Text(if (uiState.isScanning) "停止掃描" else "開始掃描")
            }

            Button(
                onClick = {},
                //onClick = { viewModel.disconnectFromDevice() },
                enabled = uiState.connectionState == ConnectionState.CONNECTED
            ) {
                Text("斷開連接")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 設備列表
        if (uiState.discoveredDevices.isNotEmpty()) {
            Text("發現的設備:", style = MaterialTheme.typography.headlineSmall)
            LazyColumn {
                items(uiState.discoveredDevices) { device ->
                    DeviceItem(
                        device = device,
                        onDeviceClick = { viewModel.connectToDevice(device) }
                    )
                }
            }
        }

        // 錯誤顯示
        uiState.error?.let { error ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
private fun ConnectionStatusCard(connectionState: ConnectionState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (connectionState) {
                ConnectionState.CONNECTED -> MaterialTheme.colorScheme.primaryContainer
                ConnectionState.CONNECTING -> MaterialTheme.colorScheme.secondaryContainer
                ConnectionState.FAILED -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "狀態: ${connectionState.name}",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun DeviceItem(
    device: BluetoothDevice,
    onDeviceClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onDeviceClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = device.name ?: "未知設備",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = device.address,
                style = MaterialTheme.typography.bodySmall
            )
            device.rssi?.let { rssi ->
                Text(
                    text = "信號強度: $rssi dBm",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}