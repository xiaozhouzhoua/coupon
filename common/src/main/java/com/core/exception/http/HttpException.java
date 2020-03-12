package com.core.exception.http;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HttpException extends RuntimeException{
    protected Integer code;
    protected Integer httpStatusCode = 500;
}
