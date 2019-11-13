package app.Utils;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;


@Data
@NoArgsConstructor
@Service
public class MessageGenerator {
    private  String message;
    private  Object temporaryObjectForMessage;
    private  boolean active;

    public void setMessage(String message) {
       this.message = message;
        this.active = true;
    }

    public String getMessageWithSetNotActive() {
        this.active=false;
        return message;
    }

    public Object getTemporaryObjectForMessageWithSetNull() {
        Object result=this.temporaryObjectForMessage;
        this.temporaryObjectForMessage=null;
        return result;
    }

    public boolean isActive() {
        return this.active;
    }
}
