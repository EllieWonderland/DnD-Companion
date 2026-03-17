package com.example.dndcompanion.ui.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class InventoryViewModelFactory(
    private val application: Application,
    private val characterVm: CharacterViewModel
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return InventoryViewModel(application, characterVm) as T
    }
}
