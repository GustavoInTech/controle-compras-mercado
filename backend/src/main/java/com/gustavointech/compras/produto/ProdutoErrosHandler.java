package com.gustavointech.compras.produto;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ProdutoErrosHandler {

    // Vale para o produto que não existe E para o de outro usuário: a resposta é
    // igual de propósito
    @ExceptionHandler(ProdutoNaoEncontradoException.class)
    public ProblemDetail naoEncontrado(ProdutoNaoEncontradoException e) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        problema.setTitle("Produto não encontrado");
        return problema;
    }

    // 422: o pedido está bem escrito, mas aponta para uma categoria ou um mercado
    // que não existe
    @ExceptionHandler(ReferenciaInvalidaException.class)
    public ProblemDetail referenciaInvalida(ReferenciaInvalidaException e) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(422), e.getMessage());
        problema.setTitle("Referência inválida");
        return problema;
    }

    @ExceptionHandler(PedidoInvalidoException.class)
    public ProblemDetail pedidoInvalido(PedidoInvalidoException e) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
        problema.setTitle("Pedido inválido");
        return problema;
    }
}