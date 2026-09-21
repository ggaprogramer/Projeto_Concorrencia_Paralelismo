package school.cesar.paralelismo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Aplicação Spring Boot — Projeto de Paralelismo e Concorrência.
 * Expõe endpoints REST para executar cada versão (sequencial, não estruturada,
 * estruturada, com DoubleAdder, com ConcurrentQueue) e a bateria de experimentos.
 */
@SpringBootApplication
public class ParalelismoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ParalelismoApplication.class, args);
    }
}
