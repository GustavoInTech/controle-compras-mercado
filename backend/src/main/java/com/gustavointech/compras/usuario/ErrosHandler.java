package com.gustavointech.compras.usuario;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
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
}