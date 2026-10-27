package shop.auto.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Aplicacao {
    protected Aplicacao() {}

    public static void main(final String[] args) {
        SpringApplication.run(Aplicacao.class, args);
    }
}
