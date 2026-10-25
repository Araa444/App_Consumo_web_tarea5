# AraDirectorio - Kotlin / Android

Proyecto fuente de una app de directorio con Retrofit, Room, Compose y MVVM.
Estado: implementación preparada; sin compilación ni ejecución Android en este entorno.
No incluye APK, capturas reales ni repositorio publicado.

## 1. Abrir y ejecutar (Windows)
1. Instala Android Studio con Android SDK Platform 35 y un emulador API 35.
2. Extrae este ZIP. Abre la carpeta Directorio en Android Studio.
3. Usa JDK 17 para Gradle. La configuración fija AGP 8.7.3 y Gradle 8.9.
4. Si solicita el wrapper de Gradle, abre PowerShell en Directorio y ejecuta:
   powershell -ExecutionPolicy Bypass -File .\preparar-gradle.ps1
   El script descarga Gradle 8.9 desde su distribuidor y genera gradlew.
   Alternativa: con Gradle 8.9 instalado: gradle wrapper --gradle-version 8.9
5. Acepta instalar el SDK solicitado y pulsa Sync Project with Gradle Files.
6. Inicia el emulador y pulsa Run. La primera sincronización necesita Internet.
7. Para compilar desde la terminal: .\gradlew.bat assembleDebug

Las versiones están fijadas para una configuración reproducible, no se presentan
como las versiones más recientes. Si Android Studio actualiza el proyecto,
comprueba Kotlin, Compose, KSP y AGP como conjunto compatible.

## 2. Archivos
- Models.kt: modelos JSON y estados de interfaz.
- Data.kt: endpoints Retrofit, Room, DAO, repositorio y comprobación de conexión.
- DirectoryViewModel.kt: carga, errores, selección y favoritos.
- MainActivity.kt: pantallas Compose y navegación interna con botón Atrás.
- app/src/main/AndroidManifest.xml: permisos y Activity inicial.
- Informe_base.pdf: documentación y código; pendiente de evidencias reales.

## 3. Persistencia y desconexión
Room almacena el JSON completo del usuario en una fila por ID. Los favoritos
se leen sin consultar la API y sobreviven al cierre y reinicio del proceso.
Las publicaciones no se guardan offline: el detalle conserva el perfil del
favorito y muestra un aviso si no puede descargar sus publicaciones.
El directorio general se obtiene de Internet y tiene reintento manual.
No se modifica JSONPlaceholder al marcar favoritos.

La Tarea 4 no fue proporcionada. Se usa Room como solución de persistencia;
si la tarea anterior exigía otra implementación, habrá que integrar esa parte.

## 4. Pruebas manuales pendientes
- Con conexión: comprobar nombre, correo y company.name en la lista.
- Abrir dos usuarios: publicaciones del userId correcto, sin datos mezclados.
- Pulsar Atrás del sistema: volver a la lista sin cerrar la app.
- Marcar dos favoritos, cerrar por completo y abrir: ambos deben permanecer.
- Sin Wi-Fi ni datos: abrir Favoritos y su perfil completo.
- Quitar un favorito offline, reiniciar: debe continuar eliminado.
- Desconectar y pulsar Actualizar: aviso de conexión y botón Reintentar.
- Reconectar y reintentar: recuperar la lista y las publicaciones.
- Girar durante la carga y en detalle: conservar selección y estado sin nueva
  carga por la rotación; Room conserva favoritos también tras muerte de proceso.
- Un arranque tras muerte del proceso vuelve al directorio: no se persiste la
  pantalla de detalle, solo los favoritos. Esto es una limitación deliberada.
- Simular fallo HTTP usando un proxy de pruebas: mostrar error, sin cierre.
- Emulador con Internet lento: verificar indicador de carga.

## 5. Capturas para el PDF
Guarda imágenes reales en evidencias/:
01_carga.png: conexión lenta (Extended Controls > Cellular, si tu emulador lo ofrece),
pulsa Actualizar y captura el indicador. No introduzcas demoras en producción.
02_exito.png: lista con nombre, correo y empresa.
03_sin_conexion.png: desactiva Wi-Fi y datos del emulador; pulsa Actualizar.
04_detalle.png: perfil y publicaciones de un usuario.
05_favoritos_offline.png: guarda favoritos, cierra la app, desconecta y ábrela.
Opcional: 06_error_http.png para documentar error del servidor.
Inserta las capturas en el informe y sustituye las notas de evidencia pendiente.
No entregar el informe base como si estas pruebas ya estuvieran realizadas.

## 6. Repositorio
En GitHub, crea un repositorio vacío llamado AraDirectorio, sin README inicial.
Desde esta carpeta ejecuta los comandos, sustituyendo TU_USUARIO:

git init
git add .
git commit -m "Implementar directorio con Retrofit y favoritos Room"
git branch -M main
git remote add origin https://github.com/TU_USUARIO/AraDirectorio.git
git push -u origin main

Incluye en el PDF el enlace real que GitHub muestre. Ningún repositorio ha sido
creado o publicado desde este entorno. No subas local.properties ni claves.

## Fuentes oficiales
https://jsonplaceholder.typicode.com/
https://developer.android.com/topic/libraries/architecture/viewmodel
https://developer.android.com/training/data-storage/room
https://square.github.io/retrofit/
