//
//  ContentView.swift
//  GestorArchivos
//
//  Práctica 3 · Ejercicio 2 — Gestor de Archivos para iPhone
//  Desarrollo de Aplicaciones Móviles Nativas · ESCOM-IPN
//
//  Pantalla principal: lista los archivos de la carpeta Documents del sandbox,
//  permite importarlos desde la app Archivos / iCloud Drive, previsualizarlos
//  con Quick Look, compartirlos y eliminarlos. Incluye los temas Guinda (IPN)
//  y Azul (ESCOM), que se adaptan al modo claro/oscuro desde Assets.xcassets.
//

import SwiftUI
import UniformTypeIdentifiers

/// Envoltorio de `URL` que cumple con `Identifiable`,
/// necesario para presentar hojas con `.sheet(item:)`.
struct IdentifiableURL: Identifiable {
    let id = UUID()
    let url: URL
}

/// Hoja de compartir nativa del sistema (`UIActivityViewController`) para SwiftUI.
/// Permite exportar el archivo a otras apps, AirDrop, Archivos, correo, etc.
struct ShareSheet: UIViewControllerRepresentable {
    /// Elementos a compartir (en esta app, la URL del archivo).
    var items: [Any]
    
    func makeUIViewController(context: Context) -> UIActivityViewController {
        UIActivityViewController(activityItems: items, applicationActivities: nil)
    }
    
    func updateUIViewController(_ uiViewController: UIActivityViewController, context: Context) {}
}

struct ContentView: View {
    /// Controla la presentación del selector de documentos del sistema.
    @State private var isImporting = false
    /// Archivos que se muestran en la lista (contenido de Documents).
    @State private var files: [URL] = [] 
    /// Archivo que se abrirá en la vista previa de Quick Look.
    @State private var selectedFileURL: IdentifiableURL?
    /// Archivo que se enviará a la hoja de compartir.
    @State private var fileToShare: IdentifiableURL?
    
    /// Tema institucional elegido ("Guinda" o "Azul"); se guarda en UserDefaults
    /// mediante @AppStorage y se conserva al cerrar la aplicación.
    @AppStorage("temaSeleccionado") private var temaSeleccionado: String = "Guinda"
    
    var body: some View {
        NavigationStack {
            List {
                ForEach(files, id: \.self) { url in
                    // Tocar un archivo abre su vista previa con Quick Look
                    Button(action: {
                        selectedFileURL = IdentifiableURL(url: url)
                    }) {
                        HStack {
                            Image(systemName: "doc")
                            Text(url.lastPathComponent)
                        }
                    }
                    // Mantener presionado muestra el menú contextual
                    .contextMenu {
                        Button(action: {
                            fileToShare = IdentifiableURL(url: url)
                        }) {
                            Label("Compartir", systemImage: "square.and.arrow.up")
                        }
                    }
                }
                // Deslizar hacia la izquierda elimina el archivo
                .onDelete(perform: deleteFile) 
            }
            .navigationTitle("Gestor de Archivos")
            .toolbar {
                // Botón "+" para importar un archivo
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: { isImporting = true }) {
                        Image(systemName: "plus")
                    }
                }
                // Selector de tema institucional
                ToolbarItem(placement: .navigationBarLeading) {
                    Menu {
                        Button("Tema Guinda (IPN)") { temaSeleccionado = "Guinda" }
                        Button("Tema Azul (ESCOM)") { temaSeleccionado = "Azul" }
                    } label: {
                        Image(systemName: "paintpalette")
                    }
                }
            }
            // Color de acento según el tema; los Color Sets tienen variante clara y oscura
            .tint(temaSeleccionado == "Guinda" ? Color("IPNGuinda") : Color("ESCOMAzul"))
        }
        .onAppear(perform: loadExistingFiles)
        // Selector de documentos del sistema (UIDocumentPickerViewController)
        .fileImporter(isPresented: $isImporting, allowedContentTypes: [.content], allowsMultipleSelection: false) { result in
            switch result {
            case .success(let urls):
                guard let selectedUrl = urls.first else { return }
                // Acceso temporal al archivo externo (fuera del sandbox) mediante security-scoped resource
                guard selectedUrl.startAccessingSecurityScopedResource() else { return }
                defer { selectedUrl.stopAccessingSecurityScopedResource() }
                
                // El archivo se copia a Documents para que quede dentro del sandbox de la app
                let documentsDirectory = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first!
                let destinationUrl = documentsDirectory.appendingPathComponent(selectedUrl.lastPathComponent)
                
                do {
                    // Si ya existe un archivo con el mismo nombre, se reemplaza
                    if FileManager.default.fileExists(atPath: destinationUrl.path) {
                        try FileManager.default.removeItem(at: destinationUrl)
                    }
                    try FileManager.default.copyItem(at: selectedUrl, to: destinationUrl)
                    DispatchQueue.main.async {
                        if !files.contains(destinationUrl) { files.append(destinationUrl) }
                    }
                } catch {
                    print("Error: \(error.localizedDescription)")
                }
            case .failure(let error):
                print("Error: \(error.localizedDescription)")
            }
        }
        // Vista previa nativa (QLPreviewController)
        .sheet(item: $selectedFileURL) { identifiableURL in
            PreviewController(url: identifiableURL.url)
                .edgesIgnoringSafeArea(.all)
        }
        // Hoja de compartir (UIActivityViewController)
        .sheet(item: $fileToShare) { identifiableURL in
            ShareSheet(items: [identifiableURL.url])
        }
    }
    
    /// Elimina físicamente del sandbox los archivos seleccionados y los quita de la lista.
    func deleteFile(at offsets: IndexSet) {
        for index in offsets {
            let url = files[index]
            do {
                try FileManager.default.removeItem(at: url)
            } catch {
                print("Error: \(error.localizedDescription)")
            }
        }
        files.remove(atOffsets: offsets)
    }
    
    /// Carga el contenido actual de la carpeta Documents al abrir la pantalla,
    /// de modo que los archivos importados persisten entre ejecuciones.
    func loadExistingFiles() {
        let documentsDirectory = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first!
        do {
            let directoryContents = try FileManager.default.contentsOfDirectory(at: documentsDirectory, includingPropertiesForKeys: nil)
            files = directoryContents
        } catch {}
    }
}
