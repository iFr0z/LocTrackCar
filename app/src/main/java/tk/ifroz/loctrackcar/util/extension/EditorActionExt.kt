package tk.ifroz.loctrackcar.util.extension

import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

fun TextInputEditText.onEditorAction(
    action: Int,
    regex: String,
    textInputLayout: TextInputLayout,
    requestStart: String,
    requestEnd: String,
    runAction: (List<String>?) -> Unit
) {
    this.setOnEditorActionListener { view, actionId, _ ->
        return@setOnEditorActionListener when (actionId) {
            action -> {
                if (view.text.matches(regex.toRegex())) {
                    runAction.invoke(view.text.split(",".toRegex()))
                } else {
                    textInputLayout.error = "$requestStart '${view.text}' $requestEnd"
                }

                true
            }
            else -> false
        }
    }
}