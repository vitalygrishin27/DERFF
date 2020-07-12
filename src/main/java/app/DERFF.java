package app;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import javax.servlet.MultipartConfigElement;
import java.util.Locale;

@SpringBootApplication
public class DERFF {
    public static void main(String[] args) {
   //     ApplicationContext context=new AnnotationConfigApplicationContext(Context.class);
     //   https://stackoverflow.com/questions/32650536/using-thymeleaf-variable-in-onclick-attribute
        //@Temporal(TemporalType.DATE)

        //https://www.thymeleaf.org/doc/articles/springmvcaccessdata.html
        Locale.setDefault(new Locale("ru"));
        SpringApplication.run(DERFF.class,args);
    }
}
