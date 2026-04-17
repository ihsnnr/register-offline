package com.registeroffline.presentation.navigation

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.registeroffline.core.util.TokenManager
import com.registeroffline.presentation.auth.AuthViewModel
import com.registeroffline.presentation.auth.LoginScreen
import com.registeroffline.presentation.auth.RegisterScreen
import com.registeroffline.presentation.home.HomeScreen
import com.registeroffline.presentation.member.form.MemberFormScreen
import com.registeroffline.presentation.profile.ProfileScreen
import com.registeroffline.presentation.splash.SplashScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    tokenManager: TokenManager,
) {
    val fullName by tokenManager.fullNameFlow.collectAsStateWithLifecycle(initialValue = "")

    NavHost(navController = navController, startDestination = Route.Splash.route) {

        // ── Splash ──
        composable(Route.Splash.route) {
            val authViewModel: AuthViewModel = hiltViewModel()
            val isLoggedIn by authViewModel.isLoggedIn.collectAsStateWithLifecycle()

            SplashScreen {
                when (isLoggedIn) {
                    true -> navController.navigate(Route.Home.route) {
                        popUpTo(Route.Splash.route) { inclusive = true }
                    }
                    false -> navController.navigate(Route.Login.route) {
                        popUpTo(Route.Splash.route) { inclusive = true }
                    }
                    null -> { /* Still loading */ }
                }
            }
        }

        // ── Login ──
        composable(Route.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Route.Home.route) {
                        popUpTo(Route.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Route.Register.route)
                },
            )
        }

        // ── Register ──
        composable(Route.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.popBackStack(Route.Login.route, inclusive = false)
                },
                onBackToLogin = {
                    navController.popBackStack()
                },
            )
        }

        // ── Home ──
        composable(Route.Home.route) {
            HomeScreen(
                userName = fullName,
                onNavigateToForm = { memberId ->
                    navController.navigate(Route.MemberForm.createRoute(memberId))
                },
                onNavigateToProfile = {
                    navController.navigate(Route.Profile.route)
                },
            )
        }

        // ── Member Form ──
        composable(
            route = Route.MemberForm.route,
            arguments = listOf(navArgument("memberId") {
                type = NavType.StringType
                defaultValue = "-1"
            }),
        ) {
            MemberFormScreen(
                onBack = { navController.popBackStack() },
            )
        }

        // ── Profile ──
        composable(Route.Profile.route) {
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Route.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }
    }
}