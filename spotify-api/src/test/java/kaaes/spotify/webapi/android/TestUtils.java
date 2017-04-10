package kaaes.spotify.webapi.android;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.robolectric.Robolectric;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;

import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;

/*
 * Restore spotify test services with:
  * https://riggaroo.co.za/retrofit-2-mocking-http-responses/
 */
public class TestUtils {
    private static final String TEST_DATA_DIR = "/fixtures/";
    private static final int MAX_TEST_DATA_FILE_SIZE = 131072;

    public static <T> Response getResponseFromModel(int statusCode, T model) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            ResponseBody responseBody = ResponseBody.create(
                    MediaType.parse("application/json"),
                    objectMapper.writeValueAsString(model)

            );
            return createResponse(statusCode, responseBody);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> Response getResponseFromModel(T model) {
        return getResponseFromModel(200, model);
    }

    private static Response createResponse(int statusCode, ResponseBody responseBody) {
        return new Response.Builder().code(statusCode).body(responseBody).build();
    }

    public static String readTestData(String fileName) {
        try {
            String path = Robolectric.class.getResource("/fixtures/" + fileName).toURI().getPath();
            return readFromFile(new File(path));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String readFromFile(File file) throws IOException {
        Reader reader = new FileReader(file);
        CharBuffer charBuffer = CharBuffer.allocate(MAX_TEST_DATA_FILE_SIZE);
        reader.read(charBuffer);
        charBuffer.position(0);
        return charBuffer.toString().trim();
    }
}
