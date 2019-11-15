package app.exceptions;


import lombok.Data;

@Data
public class DerffException extends Exception {
    private String code;
    private Object temporaryObject = null;
    private Object[] parameters;
    private String redirectUrl;

    public DerffException(String code){
        this.code=code;
    }

    public DerffException(String code, Object temporaryObject){
        this.code=code;
        this.temporaryObject=temporaryObject;
    }

    public DerffException(String code, Object temporaryObject, Object[] parameters){
        this.code=code;
        this.temporaryObject=temporaryObject;
        this.parameters=parameters;
    }

    public DerffException(String code, Object temporaryObject, Object[] parameters, String redirectUrl){
        this.code=code;
        this.temporaryObject=temporaryObject;
        this.parameters=parameters;
        this.redirectUrl=redirectUrl;
    }

}
