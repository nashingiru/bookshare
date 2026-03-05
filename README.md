# 📚 BookShare — Comparte libros, conecta comunidades

> *"Uno viaja cuando lee"* — BookShare es una app Android nativa para el préstamo y circulación de libros físicos entre vecinos, a través de puntos de intercambio neutrales como locales comerciales del barrio.

---

## 🌟 Visión del Proyecto

BookShare nació con la idea de que los libros deben circular. En lugar de dormir en estanterías, un libro puede viajar de mano en mano, sumar kilómetros, cruzar barrios y enriquecer a múltiples lectores — sin costo alguno.

**¿Cómo funciona?**

1. Un usuario registra un libro y lo deja en un punto neutral (local comercial vecinal).
2. Otro usuario lo solicita a través de la app.
3. El dueño acepta o rechaza la solicitud.
4. El libro puede devolverse al propietario original **o seguir su vida en préstamo** entre nuevos usuarios.
5. Cada acción suma **puntos de reputación**: por libros compartidos, por libros bien devueltos, y los locales comerciales acumulan reputación como puntos de intercambio confiables.
6. Los libros acumulan kilometros de viaje cada vez que se comparten para saber lo lejos que ha llegado un pequeño gesto como compartir.

---

## 🏗 Arquitectura

La app sigue el patrón **MVVM (Model-View-ViewModel)** con una arquitectura en capas bien definida:

```
UI Layer         →   Fragments + ViewBinding + LiveData observers
ViewModel Layer  →   BooksViewModel / AuthViewModel / LoansViewModel
Repository Layer →   BookRepository / AuthRepository / LoanRepository
Data Layer       →   Room (local) + Firebase Firestore (remoto) + Retrofit (Open Library API)
```

### Diagrama de capas

```
┌─────────────────────────────────────────┐
│              UI (Fragments)             │
│  Splash · Login · Home · Detail · Loans │
│  Register · AddBook · Contact · Profile │
└────────────────┬────────────────────────┘
                 │ observa LiveData
┌────────────────▼────────────────────────┐
│            ViewModels (Hilt)            │
│  AuthViewModel · BooksViewModel         │
│  LoansViewModel                         │
└────────────────┬────────────────────────┘
                 │ llama a suspend fun
┌────────────────▼────────────────────────┐
│             Repositories                │
│  AuthRepository · BookRepository        │
│  LoanRepository                         │
└───────┬─────────────┬───────────────────┘
        │             │
┌───────▼──────┐ ┌────▼──────────────────┐
│  Room (local)│ │ Firebase Firestore     │
│  BookEntity  │ │ + Auth + Crashlytics   │
│  UserEntity  │ └──────────┬────────────┘
│  LoanEntity  │            │
└──────────────┘ ┌──────────▼────────────┐
                 │  Retrofit              │
                 │  Open Library API      │
                 └───────────────────────┘
```

---

## 📱 Pantallas

| Pantalla | Descripción |
|---|---|
| **Splash** | Animación de logo con fade-in. Redirige a Home si hay sesión activa, o a Login si no. |
| **Login / Registro** | Autenticación con Firebase Auth. Validaciones de email y contraseña en tiempo real. |
| **Home** | Lista de todos los libros disponibles con RecyclerView. Búsqueda en tiempo real. Pull-to-refresh desde Firestore. |
| **Detalle de libro** | Portada (cargada con Glide desde Open Library), título, autor, género, descripción, dueño, disponibilidad. Botón para solicitar préstamo. |
| **Solicitar préstamo (Contact)** | Formulario para enviar mensaje al dueño. Conectado al LoanRepository con Firestore + Room. |
| **Mis Préstamos (Loans)** | Dos pestañas: solicitudes recibidas (con acciones aceptar/rechazar/devolver) y enviadas. Badge con pendientes. |
| **Perfil** | Libros del usuario con opción de eliminar. Botón para agregar nuevo libro. Cierre de sesión. |
| **Agregar Libro** | Formulario con autocompletado desde Open Library API. Preview de portada en tiempo real al ingresar ISBN. |

---

## 🧩 Stack Tecnológico

| Categoría | Tecnología | Versión |
|---|---|---|
| Lenguaje | Kotlin | 1.9.24 |
| UI | Material Design 3 | 1.12.0 |
| Navegación | Jetpack Navigation + SafeArgs | 2.7.7 |
| Arquitectura | ViewModel + LiveData | 2.8.4 |
| Persistencia local | Room | 2.6.1 |
| Backend remoto | Firebase Firestore + Auth | BOM 33.1.2 |
| API REST | Retrofit 2 + OkHttp | 2.11.0 / 4.12.0 |
| Imágenes | Glide | 4.16.0 |
| Inyección de dependencias | Hilt (Dagger) | 2.51.1 |
| Async | Kotlin Coroutines | 1.8.1 |
| Monitoreo | Firebase Crashlytics | — |
| Testing unitario | JUnit 4 + Mockito | 4.13.2 / 5.12.0 |
| Testing instrumental | Espresso + Room Testing | 3.6.1 |

---

## 🌐 API Externa: Open Library

Se consume la API pública de [Open Library](https://openlibrary.org/developers/api) mediante Retrofit para:

- **Buscar libros** por título, autor o consulta general (`GET /search.json`)
- **Obtener detalle** de una obra (`GET /works/{workId}.json`)
- **Resolver portadas** automáticamente al agregar un libro:
   - Si hay ISBN → `https://covers.openlibrary.org/b/isbn/{isbn}-M.jpg`
   - Si no hay ISBN → búsqueda por título+autor para obtener `cover_i`

```kotlin
// Ejemplo de uso en BookRepository
suspend fun resolveCoverUrl(isbn: String, title: String, author: String): String {
    if (isbn.isNotBlank()) return "https://covers.openlibrary.org/b/isbn/$isbn-M.jpg"
    val query = if (author.isNotBlank()) "$title $author" else title
    val response = openLibraryApi.searchBooks(query, limit = 5)
    return response.body()?.docs?.firstOrNull { it.coverId != null }?.getCoverUrl() ?: ""
}
```

---

## 🗃 Persistencia Local (Room)

### Entidades definidas

- **BookEntity** — libros con título, autor, género, ISBN, portada, disponibilidad, dueño
- **UserEntity** — perfil de usuario con uid, nombre, email, bio, contadores de actividad
- **LoanRequestEntity** — solicitudes de préstamo con estado (`pending / accepted / rejected / returned`)

### DAOs

- `BookDao` — CRUD completo + búsqueda por texto/género + update de disponibilidad
- `UserDao` — insert/update de perfil
- `LoanRequestDao` — consultas de enviadas/recibidas, conteo de pendientes, update de estado

### Sincronización

Los datos se sincronizan de forma bidireccional: Firestore como fuente de verdad, Room como caché local para funcionamiento offline.

---

## 🔄 Ciclo de Vida y Navegación

- **Single Activity** con múltiples Fragments gestionados por Jetpack Navigation Component
- Cada Fragment maneja correctamente `onCreateView`, `onViewCreated` y `onDestroyView` (con limpieza de ViewBinding)
- Paso de parámetros con **Safe Args** (tipo seguro en tiempo de compilación)
- La navegación del Splash evalúa sesión activa con `lifecycleScope.launch + delay()`

---

## ⚡ Programación Asíncrona

Toda la lógica de red y base de datos corre fuera del hilo principal:

```kotlin
// En ViewModel
fun addBookWithCover(book: Book) {
    viewModelScope.launch {
        _addBookState.value = Resource.Loading()
        val coverUrl = bookRepository.resolveCoverUrl(book.isbn, book.title, book.author)
        _addBookState.value = bookRepository.addBook(book.copy(coverUrl = coverUrl))
    }
}

// En Splash
viewLifecycleOwner.lifecycleScope.launch {
    delay(2000)
    findNavController().navigate(if (authViewModel.isLoggedIn()) toHome else toLogin)
}
```

- `viewModelScope` para operaciones de ViewModel
- `lifecycleScope` para operaciones ligadas al ciclo de vida del Fragment
- `withContext(Dispatchers.IO)` en repositories para operaciones de red/base de datos

---

## 🧪 Pruebas Automatizadas

### Unitarias (`/src/test/`)

**`AuthViewModelTest`** — 7 tests con JUnit + Mockito + coroutines test:
- Login con credenciales válidas
- Validación de email vacío, email inválido, contraseña corta
- Registro con nombre vacío y con datos válidos
- Delegación de `isLoggedIn()` al repositorio

**`BookRepositoryTest`** — 7 tests:
- Búsqueda en API exitosa y con error de red
- `getBookById` cuando no existe
- Valores por defecto del modelo `Book`
- Generación de URLs de portada desde `BookSearchDoc`

### Instrumentales (`/src/androidTest/`)

**`BookDaoTest`** — 4 tests con Room in-memory:
- Insertar y recuperar libro
- Eliminar libro
- Conteo correcto de registros
- Update de disponibilidad

**`LoginFragmentTest`** — 4 tests con Espresso:
- Pantalla de login visible post-splash
- Error al enviar formulario vacío
- Navegación a pantalla de registro
- Presencia de todos los campos de registro

---

## 📁 Estructura del Proyecto

```
app/src/main/java/com/bookshare/app/
├── data/
│   ├── local/
│   │   ├── dao/          # BookDao, UserDao, LoanRequestDao
│   │   ├── entities/     # BookEntity, UserEntity, LoanRequestEntity
│   │   └── BookShareDatabase.kt
│   └── remote/
│       ├── api/          # OpenLibraryApi (Retrofit interface)
│       └── dto/          # BookDtos, OpenLibrarySearchResponse
├── di/
│   └── AppModule.kt      # Hilt: Room, Retrofit, Firebase, OkHttp
├── model/
│   ├── Models.kt         # Book, User, Resource<T>
│   ├── LoanRequest.kt    # LoanRequest, LoanStatus enum
│   └── Mappers.kt        # Extensiones toDomain() / toEntity()
├── repository/
│   ├── AuthRepository.kt
│   ├── BookRepository.kt
│   └── LoanRepository.kt
├── ui/
│   ├── auth/             # LoginFragment, RegisterFragment, AuthViewModel
│   ├── books/            # HomeFragment (indirecto), BookAdapter, BookDetailFragment,
│   │                     # AddBookFragment, ContactFragment, BooksViewModel
│   ├── home/             # HomeFragment
│   ├── loans/            # LoansFragment, LoansViewModel, LoanRequestAdapter
│   ├── profile/          # ProfileFragment
│   ├── splash/           # SplashFragment
│   └── MainActivity.kt
└── utils/
    ├── Extensions.kt     # showSnackbar, etc.
    └── ValidationUtils.kt
```

---

## 🚀 Cómo ejecutar el proyecto

### Prerrequisitos

- Android Studio Panda 1 | 2025.3.1 Patch 1 o superior
- JDK 17
- Cuenta en [Firebase Console](https://console.firebase.google.com)

### Configuración

1. Clona el repositorio:
   ```bash
   git clone https://github.com/nashingiru/bookshare.git
   cd bookshare
   ```

2. Configura Firebase:
   - Crea un proyecto en Firebase Console
   - Habilita **Authentication** (Email/Password) y **Firestore**
   - Descarga `google-services.json` y colócalo en `/app/`

3. Abre el proyecto en Android Studio y espera que Gradle sincronice.

4. Ejecuta en emulador o dispositivo físico con API 26+.

---

## 📦 Generación de APK

### APK Debug
```
Build → Build Bundle(s) / APK(s) → Build APK(s)
```

### APK Release (firmado)
```
Build → Generate Signed Bundle / APK
→ APK → Create new keystore (.jks)
→ Completar alias, contraseña, datos del certificado
→ Release → Finish
```

El APK release generado se encuentra en:
```
app/release/app-release.apk
```

---

## 🔥 Firebase y Crashlytics

La app utiliza los siguientes servicios de Firebase:

- **Firebase Authentication** — registro e inicio de sesión con email/contraseña
- **Cloud Firestore** — sincronización en tiempo real de libros y solicitudes de préstamo
- **Firebase Crashlytics** — monitoreo de errores en producción (configurado en `build.gradle.kts`)

---

## 🔮 Roadmap — Próximas Funcionalidades

Estas características están diseñadas pero pendientes de implementación:

- 📍 **Geolocalización** — Registro de dirección en libros y puntos de intercambio; búsqueda de libros disponibles cerca de la ubicación activa del usuario
- 🏪 **Puntos de intercambio** — Locales comerciales registrados como puntos neutrales con sistema de reputación propio
- ⭐ **Sistema de reputación** — Puntos por libros compartidos, por devoluciones en buen estado, por actividad como punto de intercambio
- ✈️ **Viaje del libro** — Acumulación de distancia recorrida por cada libro a lo largo de todos sus préstamos (el libro "viaja" junto al lector)
- 🔁 **Cadena de préstamos** — Un libro prestado puede continuar circulando entre usuarios sin necesidad de volver al propietario original
- 📬 **Notificaciones push** — Alertas al recibir solicitudes, aceptaciones o cuando un libro llega a un punto cercano

---

## 🗂 Control de Versiones

El proyecto usa Git con el siguiente flujo:

```bash
main          → rama principal estable
feature/*     → nuevas funcionalidades
fix/*         → correcciones de bugs
```

Historial de commits disponible en el repositorio de GitHub.

---

## 👤 Autor

Desarrollado con cariño por Nashingiru.

---

## 📄 Licencia

Este proyecto es de uso académico. Los datos de libros provienen de [Open Library](https://openlibrary.org) bajo licencia abierta.
