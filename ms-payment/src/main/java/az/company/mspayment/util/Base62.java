package az.company.mspayment.util;

import java.math.BigInteger;

public class Base62 {
    private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final BigInteger BASE = BigInteger.valueOf(62);

    public static String encode(BigInteger number) {
        StringBuilder sb = new StringBuilder();
        if (number.equals(BigInteger.ZERO)) {
            return "0";
        }
        while (number.compareTo(BigInteger.ZERO) > 0) {
            BigInteger[] divmod = number.divideAndRemainder(BASE);
            sb.append(ALPHABET.charAt(divmod[1].intValue()));
            number = divmod[0];
        }
        return sb.reverse().toString();
    }
}
