package tw.mymis.iot

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

import litonelevatorctrlsystem.composeapp.generated.resources.Res
import litonelevatorctrlsystem.composeapp.generated.resources.compose_multiplatform
import tw.mymis.iot.screen.BluetoothScreen
import tw.mymis.iot.viewmodel.LitonViewModel

@Composable
@Preview
fun App(viewModel: LitonViewModel) {
    MaterialTheme {
        var showContent by remember { mutableStateOf(false) }
        val scrollState = rememberScrollState()
        val navController: NavHostController = rememberNavController()
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding()
                .fillMaxSize()
                ,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SetupRoute(navController,viewModel = viewModel)
            //LitonApp(viewModel )
            //BluetoothApp(viewModel)
           // BluetoothScreen(viewModel,navController)
        }
    }
}