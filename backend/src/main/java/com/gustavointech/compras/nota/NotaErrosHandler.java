package com.gustavointech.compras.nota;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class NotaErrosHandler {

    // 422: o pedido está bem escrito, mas a nota em si não pode ser salva. Os
    // motivos vão numa lista.
    @ExceptionHandler(NotaInvalidaException.class)
    public ProblemDetail notaInvalida(NotaInvalidaException e) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(422), e.getMessage());
        problema.setTitle("Nota inválida");
        problema.setProperty("motivos", e.getMotivos());
        return problema;
    }

    @ExceptionHandler(NotaJaRegistradaException.class)
    public ProblemDetail jaRegistrada(NotaJaRegistradaException e) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
        problema.setTitle("Nota já registrada");
        return problema;
    }
}