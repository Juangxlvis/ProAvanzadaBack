package org.uniquindio.edu.co.gpsanjuan_backend.controllers;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uniquindio.edu.co.gpsanjuan_backend.DTO.LoginDTO;
import org.uniquindio.edu.co.gpsanjuan_backend.DTO.MensajeDTO;
import org.uniquindio.edu.co.gpsanjuan_backend.services.interfaces.AutenticationService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AutenticationController {
    private final AutenticationService autenticacionService;

    @PostMapping("/login")
    public ResponseEntity<MensajeDTO<Boolean>> login(@RequestBody LoginDTO loginDTO ){

        if (loginDTO.rol() == null || loginDTO.id() == null) {
            return ResponseEntity.badRequest().body(new MensajeDTO<>(true, "Credenciales invalidas", false));
        } else if (autenticacionService.login(loginDTO) == '0') {
            return ResponseEntity.badRequest().body(new MensajeDTO<>(true, "Usuario inexistente", false));
        } else if (autenticacionService.login(loginDTO) == '1') {
            return ResponseEntity.ok().body(new MensajeDTO<>(false, "Usuario correcto", true));
        } else {
            return ResponseEntity.badRequest().body(new MensajeDTO<>(true, "Todo mal", false));
        }

    }
}
