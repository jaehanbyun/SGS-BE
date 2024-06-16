package pnu.cse.studyhub.chat.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ByteArrayToStringConverterTest {

    @Test
    public void testConvert() {
        ByteArrayToStringConverter converter = new ByteArrayToStringConverter();

        // 테스트할 16진수 문자열
        String hexValues = "7B 22 75 73 65 72 5F 69 64 22 3A 22 6A 68 6C 38 31 30 39 22 2C 22 73 74 75 64 79 5F 74 69 6D 65 22 3A 22 30 30 3A 30 30 3A 30 37 22 7D";

        // 16진수 문자열을 문자열로 변환
        String result = converter.convert(hexValues);

        // 변환 결과와 기대값 비교
        String expected = "{\"user_id\":\"jhl8109\",\"study_time\":\"00:00:07\"}";
        assertEquals(expected, result);
    }
}
