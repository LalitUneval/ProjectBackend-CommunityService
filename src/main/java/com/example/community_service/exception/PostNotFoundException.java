package com.example.community_service.exception;

public class PostNotFoundException extends RuntimeException {
    public PostNotFoundException(String message){
        super(message);
    }

}
