package org.uniquindio.edu.co.gpsanjuan_backend.DTO;

import java.math.BigDecimal;

public record FinalizarExamenResponseDTO(
        String mensajeConfirmacion, // Mensaje de éxito o error específico de la operación
        BigDecimal calificacion,    // La calificación final obtenida, puede ser null si no se calcula inmediatamente
        String estadoPresentacion   // Ej: "Finalizado", "Calificado", "Error"
) {}

