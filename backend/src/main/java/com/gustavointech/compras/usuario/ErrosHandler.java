package com.gustavointech.compras.usuario;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ErrosHandler {

    @ExceptionHandler(EmailJaCadastradoException.class)
    public ProblemDetail emailJaCadastrado(EmailJaCadastradoException e) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
        problema.setTitle("E-mail já cadastrado");
        return problema;
    }

    // Mesma mensagem para e-mail inexistente e senha errada: não revela quais
    // e-mails têm conta
    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail credenciaisInvalidas(AuthenticationException e) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos.");
        problema.setTitle("Não autenticado");
        return problema;
    }
}