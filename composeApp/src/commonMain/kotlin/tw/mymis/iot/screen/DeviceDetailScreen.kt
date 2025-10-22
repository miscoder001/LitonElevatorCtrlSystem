package tw.mymis.iot.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import tw.mymis.iot.LitonScreen
import tw.mymis.iot.bluetooth.BluetoothDevice
import tw.mymis.iot.viewmodel.LitonViewModel
import kotlin.collections.emptyList

@Composable
fun DeviceDetailScreen(navController: NavHostController, viewModel: LitonViewModel )
{
    //val services =  remember {mutableStateOf<List<String>>() }
    val services = remember { mutableStateListOf<String>() }

    LaunchedEffect(Unit) {
        delay(3000)
        if (viewModel._periServices.size > 0)
            services.addAll(viewModel._periServices.toList())
    }

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp) ) {

        Column(modifier = Modifier.fillMaxWidth()) {
            Text("設備詳細內容：服務與特徵")
            Button({
                println("服務內容 ${viewModel._periServices}")
                services.clear()
                if (viewModel._periServices.size > 0)
                    services.addAll(viewModel._periServices.toList())
            })
            {
                Text("列出服務")
            }
        }
        HorizontalDivider(thickness = 5.dp)
        Text("test")
        Column( modifier = Modifier.fillMaxWidth().height(450.dp) ) {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(items = services) { service ->
                    Column(modifier = Modifier.fillMaxWidth()
                        .clickable(enabled = true, onClick = {
                            viewModel.serviceUUID.value = service
                            navController.navigate(LitonScreen.CharaInfo.name)
                        })
                    ) {
                        Text( service)
                    }
                    HorizontalDivider(color = Color.Red, thickness = 1.dp)
                }
            }
        }
    }

}