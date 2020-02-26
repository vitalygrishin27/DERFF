package app.controllers.Administration;

import app.Utils.MessageGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AdministrationController {

    @Autowired
    MessageGenerator messageGenerator;


}
