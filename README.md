# Práctica 3: Aplicaciones Nativas

**Datos de Identificación**
- **Materia**: Desarrollo de Aplicaciones Móviles Nativas
- **Profesor**: Hurtado Avilés Gabriel
- **Equipo**: Caballero Perez Julio Cesar y Velazquez Beltran Brandon
- **Boletas**: 2023630158 y 2023630925
- **Grupo**: 7CV4

## Ejercicio 1: Instalación de iOS/macOS en la Mejor PC del Equipo

### 1.1 Identificación del equipo
Se seleccionó la PC de Velazquez Beltran Brandon como el equipo anfitrión para el entorno macOS debido a sus especificaciones superiores, ideales para la virtualización con Docker.

**Especificaciones PC 1 (Seleccionada - Brandon Velazquez):**
- **CPU:** AMD Ryzen 7 5700G
- **RAM:** 32GB DDR4
- **Almacenamiento:** 1TB SSD
- **GPU:** Sapphire AMD Radeon RX 6600

**Especificaciones PC 2 (Candidata - Caballero Pérez):**
- **CPU:** Intel Core i5-1135G7
- **RAM:** 16GB DDR4
- **Almacenamiento:** 512GB SSD
- **GPU:** NVIDIA GeForce MX350

### 1.2 Bitácora de sesiones
- **Sesión 1:** 27-09-2026 - Inicio: 4:15 - Modalidad: Remota - Integrante: Brandon Velazquez  - Actividad: Configuración de hardware y clonación de repositorio Docker.

### 1.3 Instalación del entorno macOS con Docker
Se clonó el repositorio oficial `MacOS-Docker` y se configuró la integración de Docker Desktop con WSL utilizando la distribución Ubuntu. Se habilitó la virtualización anidada en el archivo `.wslconfig` y se instalaron las dependencias de KVM (`qemu-system-x86`, `libvirt`, `x11-apps`, etc.). El contenedor se inicializó asignando 16 GB de RAM y 6 núcleos del procesador Ryzen 7 para asegurar un rendimiento óptimo de la máquina virtual.

### 1.4 Configuración del entorno de desarrollo iOS
Se ingresó a la Utilidad de Discos (Disk Utility) desde el menú de recuperación para formatear el disco raíz virtual de QEMU (270 GB) utilizando el esquema GUID Partition Map y el formato APFS. Posteriormente, se inició la instalación formal de macOS Ventura sobre el nuevo disco asignado. Una vez en el sistema, se verificó la conexión a Internet mediante Safari y se procedió a instalar Xcode desde la Mac App Store, configurando los simuladores de iPhone para el desarrollo.

### 1.5 Entregables del Ejercicio 1
- [x] Capturas de especificaciones comparativas de hardware.
- [x] Evidencia de la reunión de trabajo conjunta.
- [x] Capturas de instalación de dependencias KVM y ejecución en terminal WSL.
- [x] Capturas del proceso de formateo e inicio de instalación de macOS.
- [x] Capturas del entorno macOS funcionando y acceso a internet en Safari.
- [x] Captura de Xcode instalado y proyecto Swift ejecutándose en el simulador.

---

## Ejercicio 2: Gestor de Archivos para iPhone (Desarrollo Nativo)
**Responsable principal:** Velazquez Beltran Brandon
- **Descripción técnica:** Desarrollo nativo en Swift/SwiftUI utilizando `FileManager` para explorar el sandbox de iOS. Se implementó `QLPreviewController` para vistas previas, gestos táctiles nativos, y `UIDocumentPickerViewController` para importar archivos externos conservando permisos de seguridad. La persistencia de favoritos se manejó de forma local.
- **Temas aplicados:** Adaptación automática a modo claro/oscuro implementando los temas Guinda (IPN) y Azul (ESCOM).
- **Capturas de pantalla:** 
  * *[Insertar capturas de la navegación jerárquica y vista previa de archivos]*

## Ejercicio 3: Aplicación de Cámara y Micrófono para iPhone (Desarrollo Nativo)
**Responsable principal:** Caballero Perez Julio Cesar
- **Descripción técnica:** Aplicación en Swift utilizando `AVFoundation` (`AVCaptureSession` para fotos y `AVAudioRecorder` para audio). Se gestionaron permisos explícitos en el `Info.plist` y se integró Core Data para guardar los metadatos (fecha, etiquetas) en el almacenamiento local del dispositivo.
- **Temas aplicados:** Implementación de temas Guinda y Azul responsivos al sistema.
- **Capturas de pantalla:** 
  * *[Insertar capturas de la cámara/micrófono y galería integrada]*

## Ejercicio 4: Desarrollo Multiplataforma con Flutter
**Responsable principal:** Velazquez Beltran Brandon
- **Opción elegida:** [Indicar si desarrollaste el Gestor de Archivos o la Cámara]
- **Arquitectura y Plugins:** Uso de Clean Architecture con gestión de estado mediante [Provider/Riverpod/Bloc]. El almacenamiento local se resolvió utilizando [Hive/SQLite] garantizando su funcionamiento 100% offline en Android e iOS.
- **Capturas de pantalla:** 
  * *[Insertar capturas de la app compilada en Android e iOS]*

## Ejercicio 5: Desarrollo Multiplataforma con Kotlin Multiplatform (KMP)
**Responsable principal:** Caballero Perez Julio Cesar
- **Opción elegida:** [Indicar la opción contraria a la de Flutter]
- **Estructura Shared y UI:** La lógica de negocio se centralizó en `commonMain`. El acceso a recursos específicos del dispositivo (permisos, cámara/archivos) se implementó mediante el mecanismo `expect/actual`. La UI se desarrolló con Compose Multiplatform y la persistencia local con [SQLDelight/Room].
- **Capturas de pantalla:** 
  * *[Insertar capturas de la app ejecutándose en Android e iOS]*

### 5.5 Comparación entre frameworks (Flutter vs KMP)

| Criterio | Flutter | Kotlin Multiplatform (KMP) |
| :--- | :--- | :--- |
| **Lenguaje principal** | Dart | Kotlin |
| **Construcción de interfaz** | | |
| **Acceso a APIs nativas** | | |
| **Cantidad de código compartido**| | |
| **Tamaño del binario resultante**| | |
| **Curva de aprendizaje** | | |
| **Madurez del ecosistema** | | |

- **Conclusión comparativa:** [Escribir conclusión argumentada sobre qué enfoque resultó más adecuado para la aplicación desarrollada según la tabla anterior].

---

## Pruebas Realizadas
- **Funcionamiento Offline:** Se verificó rigurosamente que las 4 aplicaciones funcionen sin conexión a Internet, almacenando datos y metadatos localmente.
- **Entorno macOS-Docker:** Se probó la compilación de los binarios nativos de iOS y de KMP directamente desde el entorno virtualizado de macOS mediante Xcode.

## Conclusiones
[Reflexión grupal sobre la experiencia desarrollando para el ecosistema Apple, la configuración del entorno virtualizado con Docker y la comparativa técnica entre construir con Flutter frente a Kotlin Multiplatform].

## Bibliografía
- [Fuente 1 en formato APA]
- [Fuente 2 en formato APA]