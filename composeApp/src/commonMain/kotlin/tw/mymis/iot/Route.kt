package tw.mymis.iot

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import litonelevatorctrlsystem.composeapp.generated.resources.Res
import litonelevatorctrlsystem.composeapp.generated.resources.app_name
import litonelevatorctrlsystem.composeapp.generated.resources.bluetooth_device_detail
import litonelevatorctrlsystem.composeapp.generated.resources.bluetooth_screen
import litonelevatorctrlsystem.composeapp.generated.resources.characteristic
import litonelevatorctrlsystem.composeapp.generated.resources.choose_flavor
import litonelevatorctrlsystem.composeapp.generated.resources.choose_pickup_date
import litonelevatorctrlsystem.composeapp.generated.resources.demo_screen
import litonelevatorctrlsystem.composeapp.generated.resources.login
import litonelevatorctrlsystem.composeapp.generated.resources.order_summary
import org.jetbrains.compose.resources.StringResource
import tw.mymis.iot.screen.BluetoothScreen
import tw.mymis.iot.screen.CharacteristicsScreen
import tw.mymis.iot.screen.DemoScreen
import tw.mymis.iot.screen.DeviceDetailScreen
import tw.mymis.iot.screen.Login
import tw.mymis.iot.viewmodel.LitonViewModel


enum class LitonScreen(val title: StringResource) {
    Start(title = Res.string.app_name),
    Login(title = Res.string.login),
    Bluetooth(title = Res.string.bluetooth_screen),
    Demo(title = Res.string.demo_screen),
    DeviceInfo(title = Res.string.bluetooth_device_detail),
    CharaInfo(Res.string.characteristic)
}


@Composable
fun SetupRoute(navController: NavHostController, viewModel: LitonViewModel) {
    NavHost(
        navController = navController,
        startDestination = LitonScreen.Login.name,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(5.dp)
    ) {

//            composable(route = LitonScreen.Start.name) {
//                SelectOptionScreen(
//                    subtotal = uiState.price,
//                    onNextButtonClicked = { navController.navigate(LitonScreen.Login.name) },
//                    onCancelButtonClicked = {
//                        cancelOrderAndNavigateToStart(viewModel, navController)
//                    },
//                    options = DataSource.flavors.map { id -> stringResource(id) },
//                    onSelectionChanged = { viewModel.setFlavor(it) },
//                    modifier = Modifier.fillMaxHeight()
//                )
//            }
        composable(route = LitonScreen.Login.name) {
            Login(navController = navController)
        }

        composable(route = LitonScreen.Bluetooth.name) {
            BluetoothScreen(viewModel = viewModel, navController = navController)
        }

        composable(route = LitonScreen.Demo.name ) {
            DemoScreen(navController, viewModel)
        }

        composable(route = LitonScreen.DeviceInfo.name) {
            DeviceDetailScreen(navController,viewModel )
        }

        composable(route = LitonScreen.CharaInfo.name) {
            CharacteristicsScreen(navController, viewModel)
        }
    }
}