package com.registeroffline.presentation.navigation

sealed class Route(val route: String) {
    data object Splash : Route("splash")
    data object Login : Route("login")
    data object Register : Route("register")
    data object Home : Route("home")
    data object MemberForm : Route("member_form?memberId={memberId}") {
        fun createRoute(memberId: Long? = null) =
            if (memberId != null) "member_form?memberId=$memberId" else "member_form?memberId=-1"
    }
    data object Profile : Route("profile")
}