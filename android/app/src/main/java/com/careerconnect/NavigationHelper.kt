package com.careerconnect

import android.app.Activity
import android.content.Intent
import com.google.android.material.bottomnavigation.BottomNavigationView

object NavigationHelper {

    fun setupBottomNavigation(activity: Activity, currentItemId: Int) {
        val bottomNav = activity.findViewById<BottomNavigationView>(R.id.bottomNavigationView) ?: return
        bottomNav.selectedItemId = currentItemId
        bottomNav.setOnItemSelectedListener { item ->
            if (item.itemId == currentItemId) {
                return@setOnItemSelectedListener true
            }

            val targetClass = when (item.itemId) {
                R.id.nav_home -> MainActivity::class.java
                R.id.nav_search -> DashboardActivity::class.java
                R.id.nav_applications -> ApplicationsActivity::class.java
                R.id.nav_cv -> CVBuilderActivity::class.java
                R.id.nav_profile -> ProfileActivity::class.java
                else -> null
            }

            if (targetClass != null) {
                val intent = Intent(activity, targetClass).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
                activity.startActivity(intent)
                if (activity !is MainActivity) {
                    activity.finish()
                }
                true
            } else {
                false
            }
        }
    }
}
