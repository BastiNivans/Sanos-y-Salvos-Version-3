package com.sanosysalvos.mascotas.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    private final String uploadDir = "uploads/mascotas/";

    public FileStorageService() {
        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);
            System.out.println("📁 Directorio de uploads creado en: " + uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear el directorio de uploads", e);
        }
    }

    // ✅ NUEVO MÉTODO PARA GUARDAR IMÁGENES BASE64
    public List<String> guardarImagenesBase64(List<String> imagenesBase64) throws IOException {
        List<String> urls = new ArrayList<>();
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        
        for (String base64Completo : imagenesBase64) {
            // El formato es: data:image/jpeg;base64,/9j/4AAQSkZJRg...
            // Necesitamos separar el header del contenido real
            String[] partes = base64Completo.split(",");
            String base64Puro = partes.length > 1 ? partes[1] : partes[0];
            
            // Determinar la extensión
            String extension = ".jpg";
            if (base64Completo.contains("image/png")) {
                extension = ".png";
            } else if (base64Completo.contains("image/gif")) {
                extension = ".gif";
            }
            
            // Decodificar Base64 a bytes
            byte[] bytes = Base64.getDecoder().decode(base64Puro);
            
            // Generar nombre único
            String nombreArchivo = UUID.randomUUID().toString() + extension;
            Path rutaArchivo = uploadPath.resolve(nombreArchivo);
            
            // Guardar el archivo
            Files.write(rutaArchivo, bytes);
            
            // Agregar la URL relativa
            urls.add("/uploads/mascotas/" + nombreArchivo);
            System.out.println("✅ Imagen guardada: " + nombreArchivo);
        }
        
        return urls;
    }
}