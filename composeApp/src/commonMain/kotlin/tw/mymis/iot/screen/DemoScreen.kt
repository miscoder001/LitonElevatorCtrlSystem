package tw.mymis.iot.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import tw.mymis.iot.viewmodel.LitonViewModel


@Composable
fun DemoScreen(navController: NavHostController, viewModel: LitonViewModel) {

    Column(modifier = Modifier.fillMaxWidth()) {
        Button(
            onClick = {
               navController.popBackStack()
            }
        ) {
            Text("回上一頁")
        }
    }
}