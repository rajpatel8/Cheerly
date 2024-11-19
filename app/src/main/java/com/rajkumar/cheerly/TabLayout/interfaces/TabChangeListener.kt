package com.rajkumar.cheerly.TabLayout.interfaces

interface TabChangeListener {
    fun onTabSelected(position: Int)
    fun refreshContent(mood: String)
}