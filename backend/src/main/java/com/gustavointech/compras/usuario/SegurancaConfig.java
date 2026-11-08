package com.gustavointech.compras.usuario;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

@Configuration
public class SegurancaConfig {

        // Ferramenta que transforma a senha em hash e confere se uma senha bate com um
        // hash
        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        // O "porteiro" que confere e-mail e senha, usando o UsuarioDetalhesService e o
        // PasswordEncoder
        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration configuracao) throws Exception {
                return configuracao.getAuthenticationManager();
        }

        @Bean
        public SecurityFilterChain filtros(HttpSecurity http) throws Exception {
                // Com o nome do atributo nulo, o cookie XSRF-TOKEN já vai na primeira resposta
                // do servidor
                CsrfTokenRequestAttributeHandler csrf = new CsrfTokenRequestAttributeHandler();
                csrf.setCsrfRequestAttributeName(null);

                http
                                .authorizeHttpRequests(regras -> regras
                                                // Rotas abertas; todo o resto exige estar logado
                                                .requestMatchers("/api/saude", "/api/auth/cadastro", "/api/auth/login")
                                                .permitAll()
                                                .anyRequest().authenticated())
                                .csrf(c -> c
                                                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                                                .csrfTokenRequestHandler(csrf))
                                // Nada de tela de login nem janelinha do navegador: o login será nosso, por
                                // JSON
                                .formLogin(AbstractHttpConfigurer::disable)
                                .httpBasic(AbstractHttpConfigurer::disable)
                                .logout(l -> l
                                                .logoutUrl("/api/auth/logout")
                                                .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(
                                                                HttpStatus.NO_CONTENT))
                                                .deleteCookies("JSESSIONID"))
                                // Sem login, a resposta é 401 (e não um redirecionamento para uma página)
                                .exceptionHandling(e -> e.authenticationEntryPoint(
                                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));

                return http.build();
        }
}