package com.aranza.directorio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = Graph.repository(applicationContext)
        val network = NetworkStatus(applicationContext)

        setContent {
            val vm: DirectoryViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(
                        modelClass: Class<T>
                    ): T {
                        return DirectoryViewModel(
                            repository,
                            network
                        ) as T
                    }
                }
            )

            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color(0xFF7255C5),
                    onPrimary = Color.White,
                    primaryContainer = Color(0xFFEDE5FF),
                    onPrimaryContainer = Color(0xFF302052),

                    secondary = Color(0xFF9A6285),
                    onSecondary = Color.White,
                    secondaryContainer = Color(0xFFF8DFEF),
                    onSecondaryContainer = Color(0xFF48243D),

                    background = Color(0xFFFAF8FF),
                    onBackground = Color(0xFF292338),

                    surface = Color(0xFFFAF8FF),
                    onSurface = Color(0xFF292338),
                    surfaceVariant = Color(0xFFF0EBF8),
                    onSurfaceVariant = Color(0xFF625A70),

                    outline = Color(0xFFC9BFD9),
                    error = Color(0xFFBA1A1A)
                ),
                shapes = Shapes(
                    extraSmall = RoundedCornerShape(8.dp),
                    small = RoundedCornerShape(12.dp),
                    medium = RoundedCornerShape(20.dp),
                    large = RoundedCornerShape(24.dp),
                    extraLarge = RoundedCornerShape(28.dp)
                )
            ) {
                DirectoryApp(vm)
            }
        }
    }
}

@Composable
fun DirectoryApp(vm: DirectoryViewModel) {

    val users by vm.users.collectAsStateWithLifecycle()
    val posts by vm.posts.collectAsStateWithLifecycle()
    val favorites by vm.favorites.collectAsStateWithLifecycle()
    val selected by vm.selected.collectAsStateWithLifecycle()
    val notice by vm.notice.collectAsStateWithLifecycle()
    val saving by vm.saving.collectAsStateWithLifecycle()

    var favoritesTab by rememberSaveable {
        mutableStateOf(false)
    }

    val snackbar = remember {
        SnackbarHostState()
    }

    LaunchedEffect(notice) {
        notice?.let {
            snackbar.showSnackbar(it)
            vm.clearNotice()
        }
    }

    BackHandler(enabled = selected != null) {
        vm.back()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = {
            SnackbarHost(snackbar)
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // Encabezado principal.
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor =
                        MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "TU AGENDA DIGITAL",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "AraDirectorio",
                        style = MaterialTheme.typography.headlineLarge,
                        color =
                            MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Text(
                        text = "Personas y conexiones",
                        style = MaterialTheme.typography.bodyMedium,
                        color =
                            MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            val user = selected

            if (user == null) {

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilterChip(
                        selected = !favoritesTab,
                        onClick = {
                            favoritesTab = false
                        },
                        label = {
                            Text("Directorio")
                        }
                    )

                    FilterChip(
                        selected = favoritesTab,
                        onClick = {
                            favoritesTab = true
                        },
                        label = {
                            Text("Favoritos (${favorites.size})")
                        }
                    )
                }

                Spacer(Modifier.height(8.dp))

                if (favoritesTab) {

                    Text(
                        text = "Tus personas favoritas",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = "Guardadas en este dispositivo y " +
                                "disponibles sin Internet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color =
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    UserList(
                        users = favorites,
                        favorites = favorites,
                        saving = saving,
                        select = vm::select,
                        toggle = vm::toggle,
                        modifier = Modifier.weight(1f)
                    )

                } else {

                    Text(
                        text = "Encuentra tu próxima conexión",
                        style = MaterialTheme.typography.titleLarge
                    )

                    TextButton(
                        onClick = vm::refresh
                    ) {
                        Text("Actualizar directorio")
                    }

                    when (val state = users) {
                        LoadState.Loading -> {
                            Loading()
                        }

                        is LoadState.Error -> {
                            ErrorPanel(
                                message = state.message,
                                retry = vm::refresh
                            )
                        }

                        is LoadState.Success -> {
                            UserList(
                                users = state.data,
                                favorites = favorites,
                                saving = saving,
                                select = vm::select,
                                toggle = vm::toggle,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

            } else {

                TextButton(
                    onClick = vm::back
                ) {
                    Text("← Volver al listado")
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 2.dp
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement =
                                    Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "PERFIL",
                                    style =
                                        MaterialTheme.typography.labelMedium,
                                    color =
                                        MaterialTheme.colorScheme.primary
                                )

                                Text(
                                    text = user.name,
                                    style =
                                        MaterialTheme.typography.headlineSmall
                                )

                                Text(
                                    text = "@${user.username}",
                                    color =
                                        MaterialTheme.colorScheme
                                            .onSurfaceVariant
                                )

                                HorizontalDivider(
                                    modifier = Modifier.padding(
                                        vertical = 8.dp
                                    ),
                                    color = MaterialTheme.colorScheme
                                        .primaryContainer
                                )

                                ProfileField("Correo", user.email)
                                ProfileField("Teléfono", user.phone)
                                ProfileField("Sitio web", user.website)
                                ProfileField(
                                    "Empresa",
                                    user.company.name
                                )
                                ProfileField(
                                    "Descripción de la empresa",
                                    user.company.catchPhrase
                                )
                                ProfileField(
                                    "Actividad",
                                    user.company.bs
                                )
                                ProfileField(
                                    "Dirección",
                                    "${user.address.street}, " +
                                            "${user.address.suite}, " +
                                            user.address.city
                                )
                                ProfileField(
                                    "Código postal",
                                    user.address.zipcode
                                )
                                ProfileField(
                                    "Ubicación",
                                    "${user.address.geo.lat}, " +
                                            user.address.geo.lng
                                )

                                Spacer(Modifier.height(4.dp))

                                Button(
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = user.id !in saving,
                                    onClick = {
                                        vm.toggle(user)
                                    }
                                ) {
                                    Text(
                                        text = if (
                                            favorites.any {
                                                it.id == user.id
                                            }
                                        ) {
                                            "Quitar de favoritos"
                                        } else {
                                            "Guardar favorito"
                                        }
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Publicaciones",
                            modifier = Modifier.padding(top = 8.dp),
                            style = MaterialTheme.typography.titleLarge
                        )
                    }

                    when (val state = posts) {
                        LoadState.Loading -> {
                            item {
                                Loading()
                            }
                        }

                        is LoadState.Error -> {
                            item {
                                ErrorPanel(
                                    message = state.message +
                                            " Las publicaciones necesitan " +
                                            "conexión; si guardaste este " +
                                            "perfil en favoritos, sus datos " +
                                            "siguen disponibles.",
                                    retry = vm::loadPosts
                                )
                            }
                        }

                        is LoadState.Success -> {
                            if (state.data.isEmpty()) {
                                item {
                                    Text(
                                        "Este usuario no tiene publicaciones."
                                    )
                                }
                            }

                            items(
                                items = state.data,
                                key = {
                                    it.id
                                }
                            ) { post ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(24.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White
                                    ),
                                    elevation =
                                        CardDefaults.cardElevation(
                                            defaultElevation = 2.dp
                                        )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(20.dp),
                                        verticalArrangement =
                                            Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(
                                            text = "PUBLICACIÓN ${post.id}",
                                            style = MaterialTheme
                                                .typography.labelSmall,
                                            color = MaterialTheme
                                                .colorScheme.primary
                                        )

                                        Text(
                                            text = post.title,
                                            style = MaterialTheme
                                                .typography.titleMedium
                                        )

                                        Text(
                                            text = post.body,
                                            style = MaterialTheme
                                                .typography.bodyMedium,
                                            color = MaterialTheme
                                                .colorScheme
                                                .onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileField(label: String, value: String) {
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun UserList(
    users: List<User>,
    favorites: List<User>,
    saving: Set<Int>,
    select: (User) -> Unit,
    toggle: (User) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(
            top = 14.dp,
            bottom = 24.dp
        )
    ) {
        if (users.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor =
                            MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Todavía no hay personas aquí",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text = "Los usuarios que guardes como " +
                                    "favoritos aparecerán en esta sección."
                        )
                    }
                }
            }
        }

        items(
            items = users,
            key = {
                it.id
            }
        ) { user ->

            val isFavorite = favorites.any {
                it.id == user.id
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        select(user)
                    },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color =
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color =
                            MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = user.company.name,
                            modifier = Modifier.padding(
                                horizontal = 12.dp,
                                vertical = 6.dp
                            ),
                            style =
                                MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme
                                .onPrimaryContainer
                        )
                    }

                    Spacer(Modifier.height(2.dp))

                    TextButton(
                        enabled = user.id !in saving,
                        onClick = {
                            toggle(user)
                        }
                    ) {
                        Text(
                            text = if (isFavorite) {
                                "Quitar de favoritos"
                            } else {
                                "Guardar favorito"
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Loading() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Buscando conexiones...",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "Estamos cargando la información.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ErrorPanel(
    message: String,
    retry: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "No pudimos cargar la información",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )

            Text(
                text = message,
                color = MaterialTheme.colorScheme.onErrorContainer
            )

            Button(
                onClick = retry
            ) {
                Text("Reintentar")
            }
        }
    }
}