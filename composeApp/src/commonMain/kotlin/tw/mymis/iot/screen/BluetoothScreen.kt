package tw.mymis.iot.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import tw.mymis.iot.LitonScreen
import tw.mymis.iot.bluetooth.BluetoothDevice
import tw.mymis.iot.bluetooth.ConnectionState
import tw.mymis.iot.viewmodel.LitonViewModel


@Composable
fun BluetoothScreen(viewModel: LitonViewModel, navController: NavHostController) {
    val uiState by viewModel.uiState.collectAsState()
    val scrllState = rememberScrollState()


    Column(
        modifier = Modifier
            .fillMaxWidth()

            .padding(top = 16.dp, start = 10.dp, end = 10.dp)
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
                onClick = { viewModel.disconnectFromDevice()},
                //onClick = { viewModel.disconnectFromDevice() },
                enabled = uiState.connectionState == ConnectionState.CONNECTED
            ) {
                Text("斷開連接")
            }
            Button(
                onClick = {
                    viewModel.uiState.value.copy( connectionState = ConnectionState.EMPTY )
                    viewModel.cleanDevices() },
            ) {
                Text( text = "登出")
            }
            Button(
                onClick = {
                    navController.navigate(LitonScreen.Login.name)
                }
            ) {
                Text("登入")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 設備列表
        Text("發現的設備:", style = MaterialTheme.typography.headlineSmall)
      //  if (uiState.discoveredDevices.isNotEmpty()) {

                LazyColumn {
                    items(uiState.discoveredDevices) { device ->
                        if( device.name != null ) {
                            DeviceItem(
                                device = device,
                                onDeviceClick = { viewModel.connectToDevice(device) }
                            )
                            HorizontalDivider(thickness = 2.dp, color = Color.Red)
                        }
                    }
                }

      //  }

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