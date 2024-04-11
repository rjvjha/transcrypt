/*
* Lowe's Companies Inc., Android Application
* Copyright (C) 2023 Lowe's Companies Inc.
*
* The Lowe's Application is the private property of
* Lowe's Companies Inc. Any distribution of this software
* is unlawful and prohibited.
*/
package com.neutron.transcrypt.ui.state

data class MessageUiState(
    val text: String,
    val isTranslated: Boolean = false
)