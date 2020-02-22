package app;

import app.Models.Context;
import app.Models.Player;
import app.Models.Team;
import app.services.impl.PlayerServiceImpl;
import app.services.impl.TeamServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import javax.servlet.MultipartConfigElement;
import java.util.Locale;

@SpringBootApplication
public class DERFF {
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        return factory.createMultipartConfig();
    }


    public static void main(String[] args) {
   //     ApplicationContext context=new AnnotationConfigApplicationContext(Context.class);
     //   https://stackoverflow.com/questions/32650536/using-thymeleaf-variable-in-onclick-attribute
        Locale.setDefault(new Locale("ru"));
        SpringApplication.run(DERFF.class,args);
    }
}
