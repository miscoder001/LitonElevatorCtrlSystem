package tw.mymis.iot.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import tw.mymis.iot.viewmodel.LitonViewModel


@Composable
fun CharacteristicsScreen(hostController: NavHostController, viewModel: LitonViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var charData: Flow<ByteArray>
    val count = mutableStateOf(0)

    LaunchedEffect(Unit) {
        viewModel.fetchCharacteristicByUUID(viewModel.serviceUUID.value)
        delay(3000)
        viewModel.processCharData().collect {
            data ->
            println( data.size)
        }
    }


    Column(modifier = Modifier.fillMaxWidth()) {
        Text( text = "該藍牙裝置目前電池容量：${viewModel.batteryLevel.value}", fontSize = 20.sp)
    }
}