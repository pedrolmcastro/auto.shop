package shop.auto.api.compartilhado.utilitarios;

public final class Ascii {
    private Ascii() {}

    public static boolean eLetra(final int pontoDeCodigo) {
        return (pontoDeCodigo >= 'A' && pontoDeCodigo <= 'Z') || (pontoDeCodigo >= 'a' && pontoDeCodigo <= 'z');
    }
}
