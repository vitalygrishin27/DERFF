package app.Utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class EncryptedPasswordUtil {
    public static String encryptePassword(String password){
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.encode(password);
    }

    //!Important!!!!!!! There is a bug in Spring Security that has a regex always looking for “$2a”, but generator create “$2y”.
    //That's why needs to handle this!
    public  static void main(String[] args){
        String password ="123";
        String encryptedPassword =encryptePassword(password);
        System.out.println("EP   "+encryptedPassword);

    }
}
