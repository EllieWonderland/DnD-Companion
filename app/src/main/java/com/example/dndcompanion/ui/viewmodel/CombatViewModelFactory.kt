package com.example.dndcompanion.ui.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class CombatViewModelFactory(
    private val application: Application,
    private val characterVm: CharacterViewModel,
    private val spellVm: SpellViewModel,
    private val inventoryVm: InventoryViewModel
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return CombatViewModel(application, characterVm, spellVm, inventoryVm) as T
    }
}
