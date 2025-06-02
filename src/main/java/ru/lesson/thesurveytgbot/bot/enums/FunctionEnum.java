package ru.lesson.thesurveytgbot.bot.enums;

import java.util.Arrays;

public enum FunctionEnum {
    START("/start"),
    FORM("/form"),
    REPORT("/report");

    FunctionEnum(String value) {
        this.value = value;
    }

    private String value;

    public String getValue() {
        return value;
    }

    public static FunctionEnum fromValue(String value) {
        if(value == null){
            throw new IllegalStateException("Value is null");
        }
        return Arrays.stream(FunctionEnum.values())
                .filter(it -> value.contains(it.value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown function: " + value));
    }
}
