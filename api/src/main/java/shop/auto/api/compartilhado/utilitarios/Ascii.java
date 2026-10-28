package shop.auto.api.compartilhado.utilitarios;

public final class Ascii {
    private Ascii() {}

    public static boolean eLetraMinuscula(final int pontoDeCodigo) {
        return pontoDeCodigo >= 'a' && pontoDeCodigo <= 'z';
    }
}
