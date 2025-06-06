package edu.stanford.spezi.storage.local

import java.security.KeyPair

sealed interface LocalStorageSetting {
    data object Unencrypted : LocalStorageSetting
    data class Encrypted(val keyPair: KeyPair) : LocalStorageSetting
    data object EncryptedUsingKeyStore : LocalStorageSetting
}
