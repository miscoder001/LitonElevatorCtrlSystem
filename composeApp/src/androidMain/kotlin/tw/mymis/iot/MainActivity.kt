package tw.mymis.iot

import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RestrictTo
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import tw.mymis.iot.bluetooth.AndroidBluetoothPermissionHelper
import tw.mymis.iot.bluetooth.BluetoothService
import tw.mymis.iot.bluetooth.BluetoothServiceAndroid
import tw.mymis.iot.bluetooth.getBluetoothService
import tw.mymis.iot.viewmodel.LitonViewModel

class MainActivity : ComponentActivity() {

    private lateinit var bluetoothService: BluetoothServiceAndroid
    private lateinit var permissionHelper: AndroidBluetoothPermissionHelper
    private val viewModel: LitonViewModel = LitonViewModel()
    private val lifecycleScope: LifecycleCoroutineScope get() = lifecycle.coroutineScope

    private val bluetoothPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            initializeBluetooth()
        } else {
            // 處理權限被拒絕的情況
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
      //  viewModel = LitonViewModel()
       //  viewModel.context = applicationContext;
        viewModel.context = this;
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothService = getBluetoothService(viewModel) as BluetoothServiceAndroid
        bluetoothService.bluetoothManager = bluetoothManager
        bluetoothService.bluetoothAdapter = bluetoothManager.adapter
        bluetoothService.bluetoothLeScanner = bluetoothService.bluetoothAdapter.bluetoothLeScanner

        permissionHelper = AndroidBluetoothPermissionHelper(this)
        viewModel.bluetoothService = bluetoothService

        if (permissionHelper.hasAllPermissions()) {
            initializeBluetooth()
        } else {
            requestBluetoothPermissions()
        }
        setContent {
            App(viewModel)
        }
    }

    private fun requestBluetoothPermissions() {
        bluetoothPermissionLauncher.launch(permissionHelper.getRequiredPermissions())
    }

    private fun initializeBluetooth() {
        lifecycleScope.launch {
            try {
                // 初始化藍牙服務
                if (bluetoothService.isBluetoothEnabled()) {
                    // 藍牙已啟用，可以開始使用
                } else {
                    // 提示用戶啟用藍牙
                }
            } catch (e: Exception) {
                // 處理初始化錯誤
            }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    //App()
}