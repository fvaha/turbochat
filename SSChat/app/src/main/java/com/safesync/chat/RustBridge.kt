package com.safesync.chat

class RustBridge {
    init {
        System.loadLibrary("safe_sync_sdk") // Load the Rust library
    }

    // Native method declaration
    external fun processMessage(input: String): String

    // Free the allocated memory in Rust
    external fun freeString(s: String)
}
