package dev.equo.swt;

import java.util.function.Function;
import java.util.function.IntFunction;

public class Convert {
    public static <T, I> T[] array(I[] input, Function<I, T> converter, IntFunction<T[]> arrayGenerator) {
        if (input == null)
            return null;
        T[] result = arrayGenerator.apply(input.length);
        for (int i = 0; i < input.length; i++) {
            result[i] = input[i] != null ? converter.apply(input[i]) : null;
        }
        return result;
    }
}
