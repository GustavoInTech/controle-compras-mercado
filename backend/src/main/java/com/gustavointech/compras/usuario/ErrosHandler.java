package com.gustavointech.compras.usuario;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

    // 403 (e não 401): o usuário ESTÁ logado, só errou a confirmação. Um 401 faria
    // o frontend achar que a sessão caiu.
    @ExceptionHandler(SenhaIncorretaException.class)
    public ProblemDetail senhaIncorreta(SenhaIncorretaException e) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, e.getMessage());
        problema.setTitle("Senha incorreta");
        return problema;
    }

    // Pedido com campos inválidos: responde 400 listando só os NOMES dos campos e o
    // motivo.
    // Tratar aqui também impede o Spring de registrar no log o valor recusado
    // (que poderia ser uma senha, ou um texto de nota com CPF).
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail pedidoInvalido(MethodArgumentNotValidException e) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Pedido inválido.");
        problema.setTitle("Pedido inválido");
        Map<String, String> campos = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(erro -> campos.put(erro.getField(), erro.getDefaultMessage()));
        problema.setProperty("campos", campos);
        return problema;
    }
}