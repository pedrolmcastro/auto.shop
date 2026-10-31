package shop.auto.api.shared.utils;

public final class Ascii {
    private Ascii() {}

    public static boolean eLetraMinuscula(final int codePoint) {
        return codePoint >= 'a' && codePoint <= 'z';
    }
}
