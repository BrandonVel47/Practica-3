//
//  PreviewController.swift
//  GestorArchivos
//
//  Práctica 3 · Ejercicio 2 — Gestor de Archivos para iPhone
//  Desarrollo de Aplicaciones Móviles Nativas · ESCOM-IPN
//

import SwiftUI
import QuickLook

/// Envuelve `QLPreviewController` (Quick Look de iOS) para usarlo desde SwiftUI.
/// Muestra la vista previa nativa de cualquier tipo de archivo soportado por el sistema
/// (PDF, imágenes, texto, documentos de Office, audio, video, etc.).
struct PreviewController: UIViewControllerRepresentable {
    /// Ruta del archivo que se va a previsualizar.
    let url: URL
    
    /// Crea el controlador de Quick Look y le asigna el coordinador como fuente de datos.
    func makeUIViewController(context: Context) -> QLPreviewController {
        let controller = QLPreviewController()
        controller.dataSource = context.coordinator
        return controller
    }
    
    /// No se requiere actualizar el controlador: cada vista previa se abre en una hoja nueva.
    func updateUIViewController(_ uiViewController: QLPreviewController, context: Context) {}
    
    func makeCoordinator() -> Coordinator {
        Coordinator(parent: self)
    }
    
    /// Fuente de datos de Quick Look: siempre entrega un único archivo, el recibido en `url`.
    class Coordinator: NSObject, QLPreviewControllerDataSource {
        let parent: PreviewController
        
        init(parent: PreviewController) {
            self.parent = parent
        }
        
        /// Número de archivos a mostrar (solo uno).
        func numberOfPreviewItems(in controller: QLPreviewController) -> Int {
            return 1
        }
        
        /// `URL` ya cumple con `QLPreviewItem`, por lo que se entrega directamente.
        func previewController(_ controller: QLPreviewController, previewItemAt index: Int) -> QLPreviewItem {
            return parent.url as QLPreviewItem
        }
    }
}
