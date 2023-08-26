package tk.ifroz.loctrackcar.ui.intent

sealed class MainIntent {

    data object FetchAddress : MainIntent()
}