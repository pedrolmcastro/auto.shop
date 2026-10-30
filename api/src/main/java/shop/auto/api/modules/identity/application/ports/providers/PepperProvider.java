package shop.auto.api.modules.identity.application.ports.providers;

import shop.auto.api.modules.identity.domain.resources.Pepper;

public interface PepperProvider {
    public abstract Pepper obter();
}
