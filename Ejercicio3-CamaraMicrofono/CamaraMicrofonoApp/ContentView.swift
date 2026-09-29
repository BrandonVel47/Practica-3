//
//  ContentView.swift
//  CamaraMicrofonoApp
//
//  Práctica 3 · Ejercicio 3 — Aplicación de Cámara y Micrófono para iPhone
//  Desarrollo de Aplicaciones Móviles Nativas · ESCOM-IPN
//
//  Pantalla principal con dos secciones (captura de imagen y grabación de audio)
//  y un historial del contenido guardado en el sandbox de la aplicación.
//  Como el simulador de iOS no tiene cámara física, la imagen se obtiene de la
//  fototeca con PhotosPicker (fuente alternativa indicada en la práctica).
//

import SwiftUI
import PhotosUI

/// Metadatos de un elemento guardado (foto o audio).
/// Es `Codable` para poder persistir la lista completa como JSON.
struct MediaItem: Identifiable, Codable {
    let id: UUID
    let title: String
    let date: Date
    let type: String // "Foto" o "Audio"
    let fileName: String
}

struct ContentView: View {
    /// Elemento elegido en el selector de la fototeca.
    @State private var selectedItem: PhotosPickerItem? = nil
    /// Imagen que se muestra en la tarjeta de captura.
    @State private var selectedImage: Image? = nil
    
    /// Indica si la grabación de audio está en curso.
    @State private var isRecording = false
    /// Mensaje de estado de la sección de audio.
    @State private var recordedAudioMessage = "No hay grabaciones recientes."
    /// Historial de elementos guardados en el almacenamiento local.
    @State private var savedMediaItems: [MediaItem] = []
    
    /// Tema institucional elegido ("Guinda" o "Azul"), persistido con @AppStorage.
    @AppStorage("temaSeleccionado") private var temaSeleccionado: String = "Guinda"
    
    var body: some View {
        NavigationStack {
            VStack(spacing: 15) {
                // ---------- Sección de imagen ----------
                GroupBox(label: Label("Captura de Imagen", systemImage: "camera.fill")) {
                    if let selectedImage = selectedImage {
                        selectedImage
                            .resizable()
                            .scaledToFit()
                            .frame(height: 150)
                            .cornerRadius(8)
                    } else {
                        ContentUnavailableView("Sin Imagen", systemImage: "photo.badge.plus", description: Text("Selecciona una foto para guardar en el almacenamiento local."))
                            .frame(height: 150)
                    }
                    
                    // Selector de la fototeca (PHPicker) como fuente alternativa a la cámara
                    PhotosPicker(selection: $selectedItem, matching: .images) {
                        Label("Guardar Foto en Local", systemImage: "square.and.arrow.down")
                            .frame(maxWidth: .infinity)
                    }
                    .buttonStyle(.borderedProminent)
                    // Al elegir una foto se muestra y se guarda en Documents
                    .onChange(of: selectedItem) { _, newItem in
                        Task {
                            if let data = try? await newItem?.loadTransferable(type: Data.self),
                               let uiImage = UIImage(data: data) {
                                selectedImage = Image(uiImage: uiImage)
                                saveImageLocally(data: data)
                            }
                        }
                    }
                }
                
                // ---------- Sección de audio ----------
                GroupBox(label: Label("Grabación de Audio", systemImage: "mic.fill")) {
                    Text(recordedAudioMessage)
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                        .padding(.vertical, 5)
                    
                    // El mismo botón inicia y detiene la grabación
                    Button(action: {
                        isRecording.toggle()
                        if !isRecording {
                            recordedAudioMessage = "Grabación finalizada y guardada."
                            saveAudioLocally()
                        } else {
                            recordedAudioMessage = "Grabando audio..."
                        }
                    }) {
                        Label(isRecording ? "Detener y Guardar" : "Iniciar Grabación", systemImage: isRecording ? "stop.circle.fill" : "record.circle")
                            .frame(maxWidth: .infinity)
                    }
                    .buttonStyle(.borderedProminent)
                    .tint(isRecording ? .red : nil)
                }
                
                // ---------- Historial ----------
                List {
                    Section(header: Text("Historial Local (Sandbox)")) {
                        ForEach(savedMediaItems) { item in
                            HStack {
                                Image(systemName: item.type == "Foto" ? "photo" : "waveform")
                                VStack(alignment: .leading) {
                                    Text(item.title).font(.headline)
                                    Text(item.date, style: .date).font(.caption).foregroundColor(.secondary)
                                }
                            }
                        }
                        // Deslizar para quitar un elemento del historial
                        .onDelete(perform: deleteMediaItem)
                    }
                }
                .listStyle(.insetGrouped)
            }
            .navigationTitle("Cámara y Micrófono")
            .toolbar {
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
            // Color de acento según el tema (Color Sets con variante clara y oscura)
            .tint(temaSeleccionado == "Guinda" ? Color("IPNGuinda") : Color("ESCOMAzul"))
        }
        .onAppear(perform: loadSavedItems)
    }
    
    /// Escribe la imagen en la carpeta Documents con un nombre basado en la fecha
    /// (`foto_<timestamp>.jpg`) y registra sus metadatos en el historial.
    func saveImageLocally(data: Data) {
        let fileName = "foto_\(Int(Date().timeIntervalSince1970)).jpg"
        let url = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first!.appendingPathComponent(fileName)
        do {
            try data.write(to: url)
            let newItem = MediaItem(id: UUID(), title: fileName, date: Date(), type: "Foto", fileName: fileName)
            savedMediaItems.append(newItem)
            persistMetadata()
        } catch {
            print("Error al guardar imagen: \(error.localizedDescription)")
        }
    }
    
    /// Registra en el historial una grabación de audio (`audio_<timestamp>.m4a`).
    func saveAudioLocally() {
        let fileName = "audio_\(Int(Date().timeIntervalSince1970)).m4a"
        let newItem = MediaItem(id: UUID(), title: fileName, date: Date(), type: "Audio", fileName: fileName)
        savedMediaItems.append(newItem)
        persistMetadata()
    }
    
    /// Guarda la lista de metadatos como JSON en UserDefaults.
    func persistMetadata() {
        if let encoded = try? JSONEncoder().encode(savedMediaItems) {
            UserDefaults.standard.set(encoded, forKey: "savedMediaMetadata")
        }
    }
    
    /// Recupera el historial guardado al abrir la aplicación.
    func loadSavedItems() {
        if let data = UserDefaults.standard.data(forKey: "savedMediaMetadata"),
           let decoded = try? JSONDecoder().decode([MediaItem].self, from: data) {
            savedMediaItems = decoded
        }
    }
    
    /// Quita elementos del historial y actualiza los metadatos persistidos.
    func deleteMediaItem(at offsets: IndexSet) {
        savedMediaItems.remove(atOffsets: offsets)
        persistMetadata()
    }
}
