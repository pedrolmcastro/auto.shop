package shop.auto.api.modulos.identidade.aplicacao.portas;

import shop.auto.api.modulos.identidade.dominio.recursos.Pepper;

public interface PepperProvider {
    public abstract Pepper obter();
}
