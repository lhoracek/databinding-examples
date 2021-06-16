/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.databinding.basicsample.data

import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


/**
 * This class is used as a variable in the XML layout and it's fully observable, meaning that
 * changes to any of the exposed observables automatically refresh the UI. *
 */
class ProfileLiveDataViewModel : ViewModel() {
    private val _name = MutableLiveData("Ada")
    private val _lastName = MutableLiveData("Lovelace")
    private val _likes = MutableLiveData(0)

    val name: LiveData<String> = _name
    val lastName: LiveData<String> = _lastName
    val likes: LiveData<Int> = _likes

    // popularity is exposed as LiveData using a Transformation instead of a @Bindable property.
    val popularity: LiveData<Popularity> = Transformations.map(_likes) {
        when {
            it > 9 -> Popularity.STAR
            it > 4 -> Popularity.POPULAR
            else -> Popularity.NORMAL
        }
    }

    fun onLike() {
        viewModelScope.launch {
            _likes.value = (_likes.value ?: 0) + 1
        }
    }

    val email = MutableLiveData("")
    val emailState = MediatorLiveData<EmailState>().apply {
        addSource(email) {
            viewModelScope.launch {
                when {
                    it.isEmpty() -> value = EmailState.VOID
                    it.length < 3 && it.contains("@").not() -> value = EmailState.INVALID
                    else -> {
                        value = EmailState.CHECKING
                        withContext(Dispatchers.IO) {
                            delay(2000)
                        }
                        value = EmailState.OK
                    }
                }
            }
        }
        value = EmailState.VOID
    }
}

enum class Popularity {
    NORMAL,
    POPULAR,
    STAR
}

enum class EmailState { VOID, INVALID, CHECKING, TAKEN, OK }