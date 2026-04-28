package com.example.chemlearn.lms.exception;

public class CustomExceptions {
    public static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String msg) {
            super(msg);
        }
    }

    public static class BadRequestException extends RuntimeException {
        public BadRequestException(String msg) {
            super(msg);
        }
    }

}
