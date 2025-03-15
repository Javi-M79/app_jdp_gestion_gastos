package com.example.app_jdp_gestion_gastos.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_jdp_gestion_gastos.data.model.Group
import com.example.app_jdp_gestion_gastos.data.repository.GroupRepository
import kotlinx.coroutines.launch

class GroupViewModel(private val repository: GroupRepository) : ViewModel() {

    private val _userGroups = MutableLiveData<List<Group>>()
    val userGroups: LiveData<List<Group>> get() = _userGroups

    fun createGroup(groupName: String, creatorId: String) {
        viewModelScope.launch {
            val newGroup = Group(
                id = "",
                name = groupName,
                createdBy = creatorId,
                members = listOf(creatorId),
                createdAt = com.google.firebase.Timestamp.now()
            )
            repository.createGroup(newGroup)
        }
    }

    fun addUserToGroup(groupId: String, userId: String) {
        viewModelScope.launch {
            repository.addUserToGroup(groupId, userId)
        }
    }

    fun fetchUserGroups(userId: String) {
        viewModelScope.launch {
            val groups = repository.getUserGroups(userId)
            _userGroups.postValue(groups)
        }
    }
}
