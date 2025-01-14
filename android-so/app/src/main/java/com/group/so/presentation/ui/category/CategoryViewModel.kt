package com.group.so.presentation.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.group.so.core.RemoteException
import com.group.so.core.State
import com.group.so.data.entities.model.Category
import com.group.so.data.entities.request.CategoryDataRequest
import com.group.so.domain.category.GetCategoriesUseCase
import com.group.so.domain.category.RegisterCategoryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class CategoryViewModel(
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val registerCategoryUseCase: RegisterCategoryUseCase
) : ViewModel() {

    private val _categoryState = MutableStateFlow<State<List<Category>>>(State.Idle)
    val categoryState = _categoryState.asStateFlow()

    private val _registerCategoryState = MutableStateFlow<State<Category>>(State.Idle)
    val registerCategoryState = _registerCategoryState.asStateFlow()

    init {
        fetchLatestCategories()
    }

    fun fetchLatestCategories() {
        fetchCategories()
    }

    private fun fetchCategories() {
        viewModelScope.launch {
            getCategoriesUseCase().onStart {
                _categoryState.value = State.Loading
            }.catch {
                with(RemoteException("Could not connect to Service Order API")) {
                    _categoryState.value = State.Error(this)
                }
            }.collect {
                it.data?.let { posts ->
                    _categoryState.value = State.Success(posts)
                }
                it.error?.let { error ->
                    with(RemoteException(error.message.toString())) {
                        _categoryState.value = State.Error(this)
                    }
                }
            }
        }
    }

    private fun registerNewCategory(categoryDataRequest: CategoryDataRequest) {
        viewModelScope.launch {
            registerCategoryUseCase(categoryDataRequest)
                .onStart {
                    _registerCategoryState.value = (State.Loading)
                }.catch {
                    with(RemoteException("Could not connect to Service Orders API")) {
                        _registerCategoryState.value = State.Error(this)
                    }
                }
                .collect {
                    it.data?.let { category ->
                        _registerCategoryState.value = State.Success(category)
                        fetchLatestCategories()
                    }
                    it.error?.let { throwable ->
                        with(RemoteException(throwable.message.toString())) {
                            _registerCategoryState.value = State.Error(this)
                        }
                    }
                }
        }
    }

    fun register(name: String) {
        registerNewCategory(CategoryDataRequest(name = name))
    }
}
