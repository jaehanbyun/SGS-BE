package pnu.cse.studyhub.chat.util;

import org.springframework.core.convert.converter.Converter;

import java.nio.charset.StandardCharsets;

public class ByteArrayToStringConverter {
    public String convert(String asciiValues) {
        String[] asciiStrs = asciiValues.split(",");
        byte[] bytes = new byte[asciiStrs.length];
        for (int i = 0; i < asciiStrs.length; i++) {
            String asciiStr = asciiStrs[i].trim(); // 공백 제거
            if (!asciiStr.isEmpty()) { // 빈 문자열 체크
                bytes[i] = (byte) Integer.parseInt(asciiStr);
            }
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
