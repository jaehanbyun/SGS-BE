package pnu.cse.studyhub.chat.util;

import org.springframework.core.convert.converter.Converter;

import java.nio.charset.StandardCharsets;

public class ByteArrayToStringConverter {
    public String convert(String asciiValues) {
        String[] asciiStrs = asciiValues.split(",");
        byte[] bytes = new byte[asciiStrs.length];
        for (int i = 0; i < asciiStrs.length; i++) {
            bytes[i] = (byte) Integer.parseInt(asciiStrs[i].trim());
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
