package com.aranza.directorio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import retrofit2.HttpException
import java.io.IOException

class DirectoryViewModel(private val repo: Repository,
                         private val network: NetworkStatus) : ViewModel() {
    private val _users = MutableStateFlow<LoadState<List<User>>>(LoadState.Loading)
    val users = _users.asStateFlow()
    private val _posts = MutableStateFlow<LoadState<List<Post>>>(LoadState.Loading)
    val posts = _posts.asStateFlow()
    private val _favorites = MutableStateFlow<List<User>>(emptyList())
    val favorites = _favorites.asStateFlow()
    private val _notice = MutableStateFlow<String?>(null)
    val notice = _notice.asStateFlow()
    private val _selected = MutableStateFlow<User?>(null)
    val selected = _selected.asStateFlow()
    private val _saving = MutableStateFlow<Set<Int>>(emptySet())
    val saving = _saving.asStateFlow()
    private var usersJob: Job? = null
    private var postsJob: Job? = null

    init {
        viewModelScope.launch {
            repo.favorites.catch { _notice.value = "No se pudieron leer los favoritos." }
                .collect { _favorites.value = it }
        }
        refresh()
    }
    fun clearNotice() { _notice.value = null }
    private fun message(e: Exception): String = when (e) {
        is NoInternet -> "Sin conexión a Internet. Puedes consultar tus favoritos."
        is HttpException -> "El servidor respondió con error ${e.code()}. Reintenta."
        is IOException -> "No se pudo conectar. Comprueba Internet y reintenta."
        else -> "No se pudieron procesar los datos. Inténtalo otra vez."
    }
    fun refresh() {
        usersJob?.cancel()
        usersJob = viewModelScope.launch {
            _users.value = LoadState.Loading
            try {
                if (!network.online()) throw NoInternet()
                _users.value = LoadState.Success(repo.users())
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) { _users.value = LoadState.Error(message(e)) }
        }
    }
    fun select(user: User) { _selected.value = user; loadPosts() }
    fun back() { postsJob?.cancel(); _selected.value = null }
    fun loadPosts() {
        val id = _selected.value?.id ?: return
        postsJob?.cancel()
        postsJob = viewModelScope.launch {
            _posts.value = LoadState.Loading
            try {
                if (!network.online()) throw NoInternet()
                _posts.value = LoadState.Success(repo.posts(id))
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) { _posts.value = LoadState.Error(message(e)) }
        }
    }
    fun toggle(user: User) {
        if (user.id in _saving.value) return
        val remove = _favorites.value.any { it.id == user.id }
        _saving.value = _saving.value + user.id
        viewModelScope.launch {
            try {
                if (remove) repo.delete(user.id) else repo.save(user)
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) { _notice.value = "No se pudo guardar el cambio de favorito."
            } finally { _saving.value = _saving.value - user.id }
        }
    }
}
