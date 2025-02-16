package com.akiniyalocts.tail

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.House
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.House
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.akiniyalocts.tail.ui.favorites.FavoritesListScreen
import com.akiniyalocts.tail.ui.cocktailDetail.CocktailDetailScreen
import com.akiniyalocts.tail.ui.home.HomeScreen
import com.akiniyalocts.tail.ui.theme.TailTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TailTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    TailBottomNavigation()
                }
            }
        }
    }
}

sealed class Screen(val route: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector){
    object HomeScreen: Screen("Home", Icons.Filled.House, Icons.Outlined.House)
    object FavoritesScreen: Screen("Favorites", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder)
}

object DeepLinkScreen {
    const val drinkIdArg: String = "drinkId"
}

@Composable
fun TailBottomNavigation(){
    val navController = rememberNavController()
    val bottomNavItems = listOf(
        Screen.HomeScreen,
        Screen.FavoritesScreen
    )
    Scaffold (
        bottomBar = {
            BottomNavigation{
                val navBackStackEntry = navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry.value?.destination?.route

                bottomNavItems.forEach { screen ->
                    BottomNavigationItem(
                        selected = currentRoute == screen.route,
                        label = {
                            Text(screen.route)
                        },
                        icon = {
                            Icon(
                                imageVector = if(currentRoute == screen.route) screen.selectedIcon else screen.unselectedIcon,
                                contentDescription = screen.route
                            )
                        },
                        onClick = {
                            navController.navigate(screen.route) {

                                navController.graph.startDestinationRoute?.let { route ->
                                    popUpTo(route) {
                                        saveState = true
                                    }
                                }
                                // Avoid multiple copies of the same destination when
                                // reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        },
                        alwaysShowLabel = false
                    )

                }
            }
        }
    ){
        TailNavHost(navController = navController, it)
    }
}
@Composable
fun TailNavHost(navController: NavHostController, paddingValues: PaddingValues) {
    NavHost(navController = navController, startDestination = Screen.HomeScreen.route, modifier = Modifier.padding(paddingValues)){
        composable(Screen.HomeScreen.route){
            HomeScreen(navController)
        }

        composable("drink/{drinkId}"){
            CocktailDetailScreen(it.arguments?.getString(DeepLinkScreen.drinkIdArg))
        }

        composable(Screen.FavoritesScreen.route){
            FavoritesListScreen(navController = navController)
        }
    }
}