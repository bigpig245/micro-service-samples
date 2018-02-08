package com.example.utils;

import com.example.dto.ErrorDto;
import com.example.dto.enumeration.BUMessage;
import com.example.exception.CustomRuntimeException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ResponseBody;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.Optional;

@UtilityClass
@Slf4j
public class RestClientHelper {
    public static <T> Response<T> execute(Call<T> request) {
        try {
            Response<T> response = request.execute();
            if (!response.isSuccessful()) {
                return handleError(response);
            }
            return response;
        } catch (IOException e) {
            log.error("Problem with the server handling the request", e);
            throw new CustomRuntimeException(BUMessage.UNKNOWN_ERROR, e);
        }
    }

    private static <T> Response<T> handleError(Response<T> response) {
        String contentType = Optional
                .ofNullable(response.errorBody())
                .map(ResponseBody::contentType)
                .map(okhttp3.MediaType::toString)
                .orElse(response.headers().get(HttpHeaders.CONTENT_TYPE));
        if (contentType != null && MediaType.APPLICATION_JSON.includes(MediaType.parseMediaType(contentType))) {
            try {
                ErrorDto errorDto = parseError(response);
                throw new CustomRuntimeException(errorDto.getMessage(), null);
            } catch (IOException e) {
                log.error("Could not parse the JSON response as an ErrorDto", e);
            }
        }
        throw new CustomRuntimeException(BUMessage.UNKNOWN_ERROR);
    }

    private static ErrorDto parseError(Response<?> response)
            throws IOException {
        return Constants.MAPPER.readValue(response.errorBody().byteStream(), ErrorDto.class);
    }
}
