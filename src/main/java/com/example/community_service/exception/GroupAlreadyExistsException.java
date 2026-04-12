package com.example.community_service.exception;

public class GroupAlreadyExistsException extends RuntimeException {
    public GroupAlreadyExistsException(String message){
        super(message);
    }
}
