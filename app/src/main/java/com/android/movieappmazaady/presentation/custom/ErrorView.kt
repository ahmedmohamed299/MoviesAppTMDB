package com.android.movieappmazaady.presentation.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.android.movieappmazaady.databinding.ViewErrorBinding
import com.android.movieappmazaady.util.ErrorType
import com.android.movieappmazaady.util.getErrorMessage

class ErrorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding = ViewErrorBinding.inflate(LayoutInflater.from(context), this, true)
    private var onRetryClick: (() -> Unit)? = null

    init {
        binding.btnRetry.setOnClickListener {
            onRetryClick?.invoke()
        }
    }

    fun showError(error: ErrorType) {
        binding.tvErrorMessage.text = error.getErrorMessage()
    }

    fun setOnRetryClickListener(listener: () -> Unit) {
        onRetryClick = listener
    }
} 