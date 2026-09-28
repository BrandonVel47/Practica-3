# Práctica 3: Aplicaciones Nativas

**Datos de Identificación**
* **Materia**: Desarrollo de Aplicaciones Móviles Nativas[cite: 5]
* **Profesor**: Hurtado Avilés Gabriel[cite: 5]
* **Equipo**: Caballero Perez Julio Cesar y Velazquez Beltran Brandon[cite: 5]
* **Boletas**: 2023630158 y 2023630925[cite: 5]
* **Grupo**: 7CV4[cite: 5]

## Ejercicio 1: Instalación de iOS/macOS en la Mejor PC del Equipo

### 1.1 Identificación del equipo
Se seleccionó la PC de Velazquez Beltran Brandon como el equipo anfitrión para el entorno macOS debido a sus especificaciones superiores, ideales para la virtualización con Docker.

**Especificaciones PC 1 (Seleccionada - Brandon Velazquez):**
* **CPU:** AMD Ryzen 7 5700G
* **RAM:** 32GB DDR4
* **Almacenamiento:** 1TB SSD
* **GPU:** Sapphire AMD Radeon RX 6600

**Especificaciones PC 2 (Compañero):**
* **CPU:** [ESPACIO PARA CPU]
* **RAM:** [ESPACIO PARA RAM]
* **Almacenamiento:** [ESPACIO PARA ALMACENAMIENTO]
* **GPU:** [ESPACIO PARA GPU]

### 1.2 Bitácora de sesiones
* **Sesión 1:** 27-09-2026 - Inicio: 4:15 - Modalidad: Remota - Integrante: Brandon Velazquez  - Actividad: Configuración de hardware y clonación de repositorio Docker.

### 1.3 Instalación del entorno macOS con Docker
Se clonó el repositorio oficial `MacOS-Docker` y se configuró la integración de Docker Desktop con WSL utilizando la distribución Ubuntu. Se habilitó la virtualización anidada en el archivo `.wslconfig` y se instalaron las dependencias de KVM (`qemu-system-x86`, `libvirt`, `x11-apps`, etc.). El contenedor se inicializó asignando 16 GB de RAM y 6 núcleos del procesador Ryzen 7 para asegurar un rendimiento óptimo de la máquina virtual.

### 1.4 Configuración del entorno de desarrollo iOS
*[Pendiente: Describir la instalación de Xcode, configuración de simuladores y ejecución del proyecto de prueba en Swift]*

### 1.5 Entregables del Ejercicio 1
* [x] Capturas de especificaciones comparativas de hardware.
* [x] Evidencia de la reunión de trabajo conjunta.
* [x] Capturas de instalación de dependencias KVM y ejecución en terminal WSL.
* [ ] Capturas del entorno macOS funcionando y acceso a internet en Safari.
* [ ] Captura de Xcode instalado y proyecto Swift ejecutándose en el simulador.

---

## Ejercicio 2: Gestor de Archivos para iPhone (Desarrollo Nativo)
**Responsable principal:** Velazquez Beltran Brandon
* **Descripción técnica:** [Describir el uso de `FileManager`, `UIDocumentPickerViewController` y persistencia de datos].
* **Temas aplicados:** [Documentar el funcionamiento del Tema Guinda y Azul].
* **Capturas de pantalla:** 
  * *[Insertar capturas de la interfaz en modo claro/oscuro desde el simulador de macOS]*

## Ejercicio 3: Aplicación de Cámara y Micrófono para iPhone (Desarrollo Nativo)
**Responsable principal:** Caballero Perez Julio Cesar
* **Descripción técnica:** [Describir el uso de `AVFoundation`, manejo de permisos en `Info.plist` y almacenamiento en Core Data].
* **Capturas de pantalla:** 
  * *[Insertar capturas de la galería y captura de fotos/audio]*

## Ejercicio 4: Desarrollo Multiplataforma con Flutter
**Responsable principal:** Velazquez Beltran Brandon
* **Opción elegida:** [Indicar si es Gestor de archivos o Cámara].
* **Arquitectura y Plugins:** [Describir los paquetes utilizados y la persistencia local elegida].
* **Capturas de pantalla:** 
  * *[Insertar capturas de la app compilada en Android e iOS]*

## Ejercicio 5: Desarrollo Multiplataforma con Kotlin Multiplatform (KMP)
**Responsable principal:** Caballero Perez Julio Cesar
* **Opción elegida:** [Indicar la opción contraria a la de Flutter].
* **Estructura Shared y UI:** [Describir el uso de `expect/actual` para permisos/recursos y la persistencia de datos].
* **Capturas de pantalla:** 
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

* **Conclusión comparativa:** [Escribir conclusión argumentada sobre qué enfoque resultó más adecuado para la aplicación desarrollada].

---

## Pruebas Realizadas
* **Funcionamiento Offline:** [Validar que las 4 apps funcionen sin conexión a internet y guarden datos localmente].
* **Entorno macOS-Docker:** [Documentar la fluidez y compilación de iOS dentro de la máquina virtual].

## Conclusiones
[Reflexión grupal sobre la experiencia desarrollando para el ecosistema Apple, las limitaciones del simulador, el uso de macOS virtualizado y los retos del desarrollo multiplataforma].

## Bibliografía
* [Fuente 1 en formato APA]
* [Fuente 2 en formato APA]