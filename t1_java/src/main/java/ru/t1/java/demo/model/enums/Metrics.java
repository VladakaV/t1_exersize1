package ru.t1.java.demo.model.enums;

import lombok.Getter;

@Getter
public enum Metrics {
    CLIENT_CONTROLLER_REQUEST_COUNT("t1_java_demo_client_controller_request_count");

    private final String value;

    Metrics(String name) {
        this.value = name;
    }
}
