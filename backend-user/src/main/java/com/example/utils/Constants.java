package com.example.utils;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Constants {

    public static final ObjectMapper MAPPER = new ObjectMapper();
    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm";
    public static final String SEPARATOR = ",";
    public static final String EMAIL_PATTERN =
            "^\\p{Alnum}[\\p{Alnum}_.-]+@((\\p{Alnum}[\\p{Alnum}-]*)\\.)+\\p{Alnum}+$";


    static {
        MAPPER.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
        MAPPER.registerModule(new JavaTimeModule());
    }
}
