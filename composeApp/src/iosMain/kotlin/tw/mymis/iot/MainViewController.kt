package tw.mymis.iot

import androidx.compose.ui.window.ComposeUIViewController
import androidx.lifecycle.viewmodel.compose.viewModel
import tw.mymis.iot.bluetooth.getBluetoothService
import tw.mymis.iot.viewmodel.LitonViewModel

fun MainViewController() = ComposeUIViewController {
    val viewModel : LitonViewModel = viewModel { LitonViewModel() }
    val bluetoothService = getBluetoothService(viewModel)
    viewModel.bluetoothService = bluetoothService
    App(viewModel)
}