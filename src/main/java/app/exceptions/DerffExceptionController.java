package app.exceptions;

import app.Utils.MessageGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

@Controller
@ControllerAdvice
public class DerffExceptionController {

    @Autowired
    MessageGenerator messageGenerator;

    @Autowired
    ReloadableResourceBundleMessageSource messageSource;

    @ExceptionHandler(DerffException.class)
    public String handlerDerffException(DerffException exception, HttpServletRequest request) {
        messageGenerator.setMessage((messageSource.getMessage("error." + exception.getCode(), exception.getParameters(), Locale.getDefault())));
        messageGenerator.setTemporaryObjectForMessage(exception.getTemporaryObject());
        String redirectUrl=exception.getRedirectUrl()==null?request.getRequestURI():exception.getRedirectUrl();
        return "redirect:" + redirectUrl;
    }
}
