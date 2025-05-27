package org.uniquindio.edu.co.gpsanjuan_backend.controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uniquindio.edu.co.gpsanjuan_backend.DTO.*;
import org.uniquindio.edu.co.gpsanjuan_backend.services.interfaces.AlumnoService;

import java.util.List;

@RestController
@RequestMapping("/api/estudiante")
@AllArgsConstructor
@CrossOrigin(origins = "*")
public class AlumnoController {
    private final AlumnoService alumnoService;

    @PostMapping("/guardar-pregunta")
    public ResponseEntity<MensajeDTO<String>> guardarPregunta(@RequestBody PreguntaDTO preguntaDTO) {
        return ResponseEntity.ok().body(new MensajeDTO<>(false, "", alumnoService.guardarPregunta(preguntaDTO)));
    }

    // FUNCIONA
    @PostMapping("/obtener-nota")
    public ResponseEntity<MensajeDTO<Float>> obtenerNota(@RequestBody Integer id_presentacion_examen) {
        return ResponseEntity.ok().body(new MensajeDTO<>(false, "", alumnoService.obtenerNotaPresentacionExamen(id_presentacion_examen)));
    }

    @PostMapping("/presentar-examen")
    public ResponseEntity<MensajeDTO<String>> presentarExamen(@RequestBody PresentacionExamenDTO p) {


        return ResponseEntity.ok().body(new MensajeDTO<>(false, "", alumnoService.crearPresentacionExamen(p.tiempo(),
                p.presentado(),p.ipSource(),p.fechaHoraPresentacion(),p.id_examen(),p.id_alumno())));
    }

    @GetMapping("/nombre/{id}/{rol}")
    public ResponseEntity<MensajeDTO<String>> obtenerNombre(@PathVariable String id, @PathVariable String rol) {
        return ResponseEntity.ok().body(new MensajeDTO<>(false, "", alumnoService.obtenerNombre(id, rol)));
    }

    @GetMapping("/cursos/{id}/{rol}")
    public ResponseEntity<MensajeDTO<List<CursoSimpleDTO>>> obtenerCursos(@PathVariable String id, @PathVariable String rol) {
        return ResponseEntity.ok().body(new MensajeDTO<>(false, "", alumnoService.obtenerCursos(id, rol)));
    }


    @GetMapping("/examenes-pendientes/{id}/{id_grupo}")
    public ResponseEntity<MensajeDTO<List<ExamenPendienteDTO>>> obtenerExamenesPendientes(@PathVariable String id, @PathVariable Integer id_grupo) {
        return ResponseEntity.ok().body(new MensajeDTO<>(false, "", alumnoService.obtenerExamenesPendiente(id, id_grupo)));
    }

    @GetMapping("/examenes-hechos/{id}/{id_grupo}")
    public ResponseEntity<MensajeDTO<List<ExamenHechoDTO>>> obtenerExamenesHechos(@PathVariable String id, @PathVariable Integer id_grupo) {
        return ResponseEntity.ok().body(new MensajeDTO<>(false, "", alumnoService.obtenerExamenesHechos(id, id_grupo)));
    }

    @PostMapping("/iniciar") // Ejemplo de endpoint: POST /api/examenes/iniciar
    public ResponseEntity<MensajeDTO<ExamenParaPresentarDTO>> iniciarExamen(
            @RequestBody IniciarExamenRequestDTO requestDTO,
            // Authentication authentication, // Descomentar si usas Spring Security
            HttpServletRequest httpRequest) {

        Integer idAlumno;
        String ipCliente = httpRequest.getRemoteAddr();

        // --- OBTENCIÓN DEL ID DEL ALUMNO ---
        // En un entorno de producción, obtendrías el ID del alumno del usuario autenticado.
        // Ejemplo con Spring Security (si 'name' es el ID del alumno):
        /*
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body(new MensajeDTO<>(true, "Usuario no autenticado.", null));
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        // Aquí necesitarías una forma de convertir userDetails.getUsername() o un claim del token a Integer idAlumno.
        // Esto es solo un placeholder, adapta según tu implementación de seguridad.
        try {
            idAlumno = Integer.parseInt(userDetails.getUsername());
        } catch (NumberFormatException e) {
             return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                  .body(new MensajeDTO<>(true, "ID de usuario inválido en el token.", null));
        }
        */

        // Para propósitos de este ejemplo, si no tienes seguridad configurada aún,
        // podrías pasarlo en el DTO de solicitud o como path variable (menos seguro para este caso).
        // Aquí simularé un ID de alumno. ¡DEBES CAMBIAR ESTO!
        idAlumno = 1; // !!! EJEMPLO: REEMPLAZA CON TU LÓGICA REAL PARA OBTENER EL ID DEL ALUMNO !!!
        if (requestDTO.idExamen() == null) {
            return ResponseEntity.badRequest()
                    .body(new MensajeDTO<>(true, "El idExamen es requerido.", null));
        }
        // --- FIN OBTENCIÓN DEL ID DEL ALUMNO ---


        try {
            ExamenParaPresentarDTO examenParaPresentar = alumnoService.iniciarCargarExamenParaPresentar(
                    idAlumno,
                    requestDTO.idExamen(),
                    ipCliente
            );
            return ResponseEntity.ok().body(new MensajeDTO<>(false, "Examen listo para presentar.", examenParaPresentar));
        } catch (RuntimeException e) {
            // Captura excepciones de lógica de negocio (ej. "Error: El examen no existe.")
            // o errores durante la ejecución del servicio.
            System.err.println("Error en ExamenController al llamar a iniciarYCargarExamen: " + e.getMessage());
            // Podrías querer loggear e.printStackTrace() en un log real
            return ResponseEntity.badRequest().body(new MensajeDTO<>(true, e.getMessage(), null));
        } catch (Exception e) {
            // Captura cualquier otra excepción inesperada.
            System.err.println("Error inesperado en ExamenController: " + e.getMessage());
            e.printStackTrace(); // Loguear el stack trace completo
            return ResponseEntity.internalServerError().body(new MensajeDTO<>(true, "Error interno del servidor al intentar iniciar el examen.", null));
        }
    }
}
