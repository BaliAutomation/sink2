package com.sensetif.sink.model.security;

public class CryptoException extends RuntimeException
{
    public CryptoException( String message, Throwable cause )
    {
        super( message, cause );
    }
}