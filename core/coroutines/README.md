# Coroutines module

Module that provides access to different `CoroutineDispatcher`s and `CoroutineScope`s in coroutine-based applications to manage concurrency and threading.

### Example usage

```kotlin
class MyClass @Inject constructor(
    private val dispatchersProvider: DispatchersProvider,
    @Dispatching.IO private val ioDispatcher: CoroutineDispatcher,
    @Dispatching.Main private val mainScope: CoroutineScope,
) {
    
    suspend fun executeWithDispatcher() = withContext(ioDispatcher) {
        // Perform IO-bound operation here
    }
    
    suspend fun executeWithProvider() = withContext(dispatchersProvider.default()) {
        // Perform operation using a dispatcher from the provider
    }
    
    fun launchScope() {
        mainScope.launch {
            // Perform UI-related task here
        }
    }
}
