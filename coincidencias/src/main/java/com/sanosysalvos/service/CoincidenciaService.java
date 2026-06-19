package com.sanosysalvos.service;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CoincidenciaService {
    private final RestTemplate restTemplate = new RestTemplate();

    @CircuitBreaker(name = "mascotasService", fallbackMethod = "fallbackObtenerMascotas")
    public String buscarCoincidencias() {
        // Llama al microservicio de mascotas
        String url = "http://localhost:8081/api/mascotas";
        return restTemplate.getForObject(url, String.class);
    }

    // Método de respaldo si el microservicio de mascotas falla
    public String fallbackObtenerMascotas(Exception e) {
        return "Servicio de mascotas temporalmente inactivo. No se pueden calcular coincidencias ahora mismo.";
    }
}