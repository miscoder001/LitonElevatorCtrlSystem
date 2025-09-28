package tw.mymis.iot

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import litonelevatorctrlsystem.composeapp.generated.*
import litonelevatorctrlsystem.composeapp.generated.resources.*
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import tw.mymis.iot.data.DataSource
import tw.mymis.iot.screen.Login
import tw.mymis.iot.screen.SelectOptionScreen
import tw.mymis.iot.viewmodel.LitonViewModel

/**
 * enum values that represent the screens in the app
 */
enum class LitonScreen(val title: StringResource) {
    Start(title = Res.string.app_name),
    Login(title = Res.string.login),
    Flavor(title = Res.string.choose_flavor),
    Pickup(title = Res.string.choose_pickup_date),
    Summary(title = Res.string.order_summary)
}

/**
 * Composable that displays the topBar and displays back button if back navigation is possible.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LitonAppBar(
    currentScreen: LitonScreen,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = { Text(stringResource(currentScreen.title)) },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = modifier,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(Res.string.back_button)
                    )
                }
            }
        }
    )
}

@Composable
fun LitonApp(
    viewModel: LitonViewModel ,
    navController: NavHostController = rememberNavController()
) {
    // Get current back stack entry
    val backStackEntry by navController.currentBackStackEntryAsState()
    // Get the name of the current screen
    val currentScreen = LitonScreen.valueOf(
        backStackEntry?.destination?.route ?: LitonScreen.Start.name
    )

    Scaffold(
        topBar = {
            LitonAppBar(
                currentScreen = currentScreen,
                canNavigateBack = navController.previousBackStackEntry != null,
                navigateUp = { navController.navigateUp() }
            )
        }
    ) { innerPadding ->
        val uiState by viewModel.uiState.collectAsState()

        NavHost(
            navController = navController,
            startDestination = LitonScreen.Login.name,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
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
                Login()
            }

        }
    }
}

/**
 * Resets the [OrderUiState] and pops up to [CupcakeScreen.Start]
 */
private fun cancelOrderAndNavigateToStart(
    viewModel: LitonViewModel,
    navController: NavHostController
) {

    navController.popBackStack(LitonScreen.Login.name, inclusive = false)
}

/**
 * Creates an intent to share order details
 */
private fun shareOrder(subject: String, summary: String) {
    // TODO
}