package com.dinesh.codeflowanalyser.exception;

public class GenAIApiException extends Exception{
    public GenAIApiException(String message){
        super(message);
    }

    public GenAIApiException(String message, Throwable cause){
        super(message, cause);
    }
}
