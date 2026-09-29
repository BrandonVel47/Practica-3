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
- **Descripción técnica:** Desarrollo nativo en Swift/SwiftUI utilizando `FileManager` para explorar de manera segura y persistente el directorio de documentos (*Sandbox*) de iOS. Se implementó `UIDocumentPickerViewController` mediante el modificador `.fileImporter` con manejo de recursos de seguridad (*security-scoped bookmarks*) para importar archivos externos. La previsualización de documentos se resolvió encapsulando `QLPreviewController` a través del protocolo `UIViewControllerRepresentable`. La gestión incluyó gestos táctiles nativos como desplazamiento para eliminar (*swipe to delete*) y menús contextuales para invocar la hoja de compartir nativa del sistema con `UIActivityViewController`.
- **Temas aplicados:** Adaptación automática a modo claro y oscuro del sistema operativo configurando catálogos de activos (*Assets.xcassets*) con variantes *Any, Light, Dark* para el **Tema Guinda (IPN)** (`#6C1D45` / `#8A2558`) y el **Tema Azul (ESCOM)** (`#005E90` / `#4A90E2`), controlados mediante persistencia con `@AppStorage`.
- **Evidencias y Capturas de pantalla:** 
  * `ej2_fase1_importacion_sandbox.png`: Importación exitosa y renderizado del archivo de prueba en la lista principal del sandbox.
  * `ej2_fase2_qlpreview_nativo.png`: Visualización a pantalla completa mediante el componente QuickLook.
  * `ej2_tema_guinda_claro.png` / `ej2_tema_guinda_oscuro.png`: Adaptabilidad responsiva del Tema Guinda (IPN) en modo claro y oscuro.
  * `ej2_tema_azul_claro.png` / `ej2_tema_azul_oscuro.png`: Adaptabilidad responsiva del Tema Azul (ESCOM) en modo claro y oscuro.
  * `ej2_fase4_uiactivityviewcontroller.png`: Invocación de la hoja nativa de compartir (*UIActivityViewController*).
  * `ej2_fase4_swipe_delete.png`: Acceso al botón de borrado físico mediante el gesto táctil de deslizamiento (*swipe to delete*).

---

## Ejercicio 3: Aplicación de Cámara y Micrófono para iPhone (Desarrollo Nativo)
**Responsable principal:** Caballero Perez Julio Cesar
- **Descripción técnica:** Creación de la estructura base del proyecto `CamaraMicrofonoApp` en Xcode. Se configuraron de manera obligatoria las claves institucionales de privacidad en el archivo `Info.plist` (`NSCameraUsageDescription` y `NSMicrophoneUsageDescription`)[cite: 1] para autorizar el uso de hardware. Se diseñó una interfaz multimedia interactiva dividida en secciones para selección de imágenes mediante `PhotosPicker` (como fuente alternativa adaptada para simulador)[cite: 1] y controles de simulación/grabación de audio.
- **Temas aplicados:** Integración de los selectores de paleta institucional Guinda (IPN) y Azul (ESCOM) con soporte dinámico.
- **Evidencias y Capturas de pantalla:** 
  * `ej3_interfaz_principal.png`: Interfaz principal con pestañas multimedia y selector de temas responsivo.

---

## Ejercicio 4: Desarrollo Multiplataforma con Flutter
**Responsable principal:** Velazquez Beltran Brandon
- **Opción elegida:** [Pendiente de desarrollo]
- **Arquitectura y Plugins:** Uso de Clean Architecture con gestión de estado y persistencia local 100% offline.
- **Capturas de pantalla:** 
  * *[Insertar capturas de la app compilada en Android e iOS]*

---

## Ejercicio 5: Desarrollo Multiplataforma con Kotlin Multiplatform (KMP)
**Responsable principal:** Caballero Perez Julio Cesar
- **Opción elegida:** [Pendiente de desarrollo]
- **Estructura Shared y UI:** Lógica centralizada en `commonMain` con implementación `expect/actual` para recursos de plataforma.
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

- **Conclusión comparativa:** [Pendiente de redacción final].

---

## Pruebas Realizadas
- **Funcionamiento Offline:** Se verificó rigurosamente que las aplicaciones funcionen sin conexión a Internet, almacenando datos localmente en el contenedor de la app.
- **Entorno macOS-Docker:** Se probó la compilación de binarios nativos de iOS y la ejecución de simuladores mediante Xcode virtualizado.

## Conclusiones
[Pendiente de redacción grupal].

## Bibliografía
- [Fuente 1 en formato APA]
- [Fuente 2 en formato APA]