package app.exceptions;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DerffException extends Exception {
    private String code;
    private Object temporaryObject = null;
    private Object[] parameters;

    public DerffException(String code){
        this.code=code;
    }

    public DerffException(String code, Object temporaryObject){
        this.code=code;
        this.temporaryObject=temporaryObject;
    }

}
