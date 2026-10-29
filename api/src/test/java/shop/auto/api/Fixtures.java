package shop.auto.api;

import java.util.Arrays;
import shop.auto.api.modules.identity.domain.resources.Pepper;

public final class Fixtures {
    private Fixtures() {}

    public static Pepper pepper() {
        return pepper((byte) 1);
    }

    public static Pepper pepper(final byte byte_) {
        final var valor = new byte[Pepper.COMPRIMENTO];
        Arrays.fill(valor, byte_);
        return new Pepper(valor);
    }
}
