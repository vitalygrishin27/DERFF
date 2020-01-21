package app.Models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;


//Class for Global variables for each http session
@AllArgsConstructor
@NoArgsConstructor
@Data
@Component()
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class Context {
    private Map<String, Object> context=new HashMap<>();

    public Object getFromContext(String key) {
        return this.context.get(key);
    }

    public void putToContext(String key, Object object){
        this.context.put(key,object);
    }

    public void deleteFromContext(String key){
        this.context.remove(key);
    }

    public void clear(){
        context.clear();
    }
}
