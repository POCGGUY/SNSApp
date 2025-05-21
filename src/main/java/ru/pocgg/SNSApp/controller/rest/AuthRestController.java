package ru.pocgg.SNSApp.controller.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.pocgg.SNSApp.DTO.display.ErrorDisplayDTO;
import ru.pocgg.SNSApp.model.exceptions.EntityNotFoundException;
import ru.pocgg.SNSApp.security.JWTUtils;

import javax.swing.text.html.parser.Entity;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Аутентификация пользователей")
public class AuthRestController{
    private final AuthenticationManager authenticationManager;
    private final JWTUtils jwtUtils;

    @Operation(security = @SecurityRequirement(name = ""))
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String username, @RequestParam String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
            String role = authentication.getAuthorities().iterator().next().getAuthority();
            String token = jwtUtils.generateToken(username, role);
            return ResponseEntity.ok(token);
        } catch (Exception ex){
            Throwable cause = ex.getCause();
            if(cause instanceof EntityNotFoundException) {
                return buildErrorResponse("AUTHENTICATION_FAILED", "wrong username or password",
                        HttpStatus.NOT_FOUND);
            } else {
                throw ex;
            }
        }
    }

    private ResponseEntity<ErrorDisplayDTO> buildErrorResponse(String error, String message, HttpStatus status) {
        return ResponseEntity.status(status).body(
                ErrorDisplayDTO.builder()
                        .error(error)
                        .message(message)
                        .build()
        );
    }
}
