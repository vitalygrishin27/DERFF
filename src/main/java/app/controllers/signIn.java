package app.controllers;

import app.Models.Photo;
import app.Models.Team;
import app.Models.User;
import app.services.impl.PhotoServiceImpl;
import app.services.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Base64;

@Controller
public class signIn {

    @Autowired
    UserServiceImpl userService;

    @Autowired
    PhotoServiceImpl photoService;



    @GetMapping(value = "/use")
    public String getBook(Model model){

        model.addAttribute("title", "Welcome");
        return "page";
    }

    @GetMapping(value = "/photo")
    public String getPhoto(Model model){
            Photo photo=photoService.getPhotoById(5);

           String encodedString =Base64.getEncoder().encodeToString(photo.getData());
        model.addAttribute("photo",encodedString);
        //  userService.save(new User(34,"oiuoiu"));
        return "pagePhoto";
    }


}


//@RequestMapping("/greeting")
//    public Greeting greeting(@RequestParam(value="name", defaultValue="World") String name) {



//If the URL http://localhost:8080/MyApp/user/1234/invoices?date=12-05-2013 gets the invoices for user 1234 on December 5th, 2013, the controller method would look like:
//
//@RequestMapping(value="/user/{userId}/invoices", method = RequestMethod.GET)
//public List<Invoice> listUsersInvoices(
//            @PathVariable("userId") int user,
//            @RequestParam(value = "date", required = false) Date dateOrNull) {
//  ...