package com.example.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result {
    private Boolean flag;
    private String Message;
    private Object data;

    public Result(Boolean flag, String message) {
        this.flag = flag;
        Message = message;
    }
}
