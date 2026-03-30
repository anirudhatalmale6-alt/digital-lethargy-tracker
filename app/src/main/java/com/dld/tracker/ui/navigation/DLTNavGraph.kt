package com.dld.tracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.dld.tracker.DLTApplication
import com.dld.tracker.ui.screen.dashboard.DashboardScreen
import com.dld.tracker.ui.screen.export.ExportScreen
import com.dld.tracker.ui.screen.privacy.PrivacyDisclosureScreen
import com.dld.tracker.ui.screen.reactiontest.ReactionTestScreen
import com.dld.tracker.ui.screen.selfreport.SelfReportScreen
import com.dld.tracker.ui.screen.settings.SettingsScreen

object Routes {
    const val PRIVACY = "privacy"
    const val DASHBOARD = "dashboard"
    const val SELF_REPORT = "self_report"
    const val REACTION_TEST = "reaction_test"
    const val EXPORT = "export"
    const val SETTINGS = "settings"
}

@Composable
fun DLTNavGraph(navController: NavHostController) {
    val app = DLTApplication.instance
    val startDest = if (app.securePrefs.isPrivacyAccepted()) Routes.DASHBOARD else Routes.PRIVACY

    NavHost(navController = navController, startDestination = startDest) {
        composable(Routes.PRIVACY) {
            PrivacyDisclosureScreen(
                onAccepted = {
                    app.securePrefs.setPrivacyAccepted(true)
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.PRIVACY) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.DASHBOARD) {
            DashboardScreen(
                onNavigateToSelfReport = { navController.navigate(Routes.SELF_REPORT) },
                onNavigateToReactionTest = { navController.navigate(Routes.REACTION_TEST) },
                onNavigateToExport = { navController.navigate(Routes.EXPORT) },
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }
        composable(Routes.SELF_REPORT) {
            SelfReportScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.REACTION_TEST) {
            ReactionTestScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.EXPORT) {
            ExportScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
