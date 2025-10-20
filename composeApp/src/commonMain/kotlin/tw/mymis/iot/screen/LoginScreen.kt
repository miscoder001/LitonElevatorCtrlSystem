package tw.mymis.iot.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import tw.mymis.iot.LitonScreen


@Composable
fun Login(navController: NavHostController) {

    Column(
        modifier = Modifier.padding(20.dp)
    ) {
        Text(text = "Hello from liton")
        Button(
            onClick = {
                navController.navigate(LitonScreen.Bluetooth.name)
            }
        ) {
            Text("藍芽搜尋")
        }
        Button(
            onClick = {
                navController.navigate(route = LitonScreen.Demo.name)
            }
        ) {
            Text("Demo")
        }
    }
}