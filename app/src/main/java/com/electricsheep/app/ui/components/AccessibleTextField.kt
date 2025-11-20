package com.electricsheep.app.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material3.ExperimentalMaterial3Api

/**
 * Accessible text field component with built-in error handling and accessibility features.
 * 
 * Features:
 * - Always includes label for accessibility
 * - Error state announced to screen readers
 * - Descriptive content descriptions
 * - Consistent styling
 * 
 * @param value The current text value
 * @param onValueChange Callback when text changes
 * @param label The field label (required for accessibility)
 * @param modifier Additional modifier to apply
 * @param placeholder Optional placeholder text
 * @param enabled Whether the field is enabled
 * @param isError Whether the field is in error state
 * @param errorMessage Error message to display and announce
 * @param supportingText Supporting text (hint or error message)
 * @param contentDescription Detailed description for screen readers
 * @param keyboardType The keyboard type (Email, Number, etc.)
 * @param imeAction The IME action (Next, Done, etc.)
 * @param singleLine Whether the field is single line
 * @param visualTransformation Visual transformation (e.g., PasswordVisualTransformation)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessibleTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    enabled: Boolean = true,
    isError: Boolean = false,
    errorMessage: String? = null,
    supportingText: String? = null,
    contentDescriptionParam: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Default,
    singleLine: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    val desc = contentDescriptionParam ?: label
    val effectiveSupportingText = errorMessage ?: supportingText
    
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        enabled = enabled,
        isError = isError,
        supportingText = effectiveSupportingText?.let { { Text(it) } },
        modifier = modifier
            .semantics {
                contentDescription = desc
                if (errorMessage != null) {
                    error(errorMessage)
                }
            },
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        singleLine = singleLine,
        visualTransformation = visualTransformation,
        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
            errorLabelColor = MaterialTheme.colorScheme.error,
            errorSupportingTextColor = MaterialTheme.colorScheme.error
        )
    )
}

