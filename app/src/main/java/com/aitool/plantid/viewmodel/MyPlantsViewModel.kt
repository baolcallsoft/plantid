package com.aitool.plantid.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aitool.plantid.data.CollectionDao
import com.aitool.plantid.data.CollectionEntity
import com.aitool.plantid.data.CollectionWithPlants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyPlantsViewModel @Inject constructor(
    private val collectionDao: CollectionDao
) : ViewModel() {

    // Lấy danh sách thư mục kèm cây bên trong
    val collectionsState: StateFlow<List<CollectionWithPlants>> =
        collectionDao.getCollectionsWithPlants()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    // Hàm tạo thư mục mới (Dùng cho nút Create)
    fun createNewCollection(name: String) {
        viewModelScope.launch {
            collectionDao.insertCollection(CollectionEntity(name = name))
        }
    }

    // Hàm xóa thư mục
    fun deleteCollection(collectionId: String) {
        viewModelScope.launch {
            collectionDao.deleteCollection(collectionId)
        }
    }
}