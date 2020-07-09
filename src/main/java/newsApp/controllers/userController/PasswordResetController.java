package newsApp.controllers.userController;


import lombok.extern.log4j.Log4j2;
import newsApp.exceptions.userException.UserNotFoundException;
import newsApp.models.userModels.NUser;
import newsApp.models.userModels.PasswordReset;
import newsApp.repo.userRepo.NUserRepository;
import newsApp.services.userService.EmailSenderService;
import newsApp.services.userService.UserService;
import newsApp.services.userService.UserTokenizeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.UUID;

@Log4j2
@Controller
@RequestMapping("/user")
public class PasswordResetController {

    private final EmailSenderService senderService;
    private final UserTokenizeService userTokenizeService;
    private final UserService userService;
    private final String contextPath = "http://localhost:8000"; // TODO In Production will not work cget host from heroku env;

    public PasswordResetController(EmailSenderService senderService, NUserRepository repository,
                                   UserTokenizeService userTokenizeService, UserService userService) {
        this.senderService = senderService;
        this.userTokenizeService = userTokenizeService;
        this.userService = userService;
    }

    @GetMapping("/reset-password")
    public String password_reset(){
        return "password-reset-require";
    }

    @PostMapping("/reset-password")
    public  String resetPassword(HttpServletRequest request,
                              @RequestParam("email") String email){
        UUID token = UUID.randomUUID();

        NUser nUser = userService.findByEmail(email);

        userTokenizeService.createUserPasswordResetToken(nUser, token);
        senderService.sendEmail(contextPath,token.toString(),nUser);
        return "reset-send";
    }

    @GetMapping("/change-password")
    public RedirectView changePasswordPage(Model model, @RequestParam("token") UUID token){
        log.info("Token: "+ token.toString());
        String result = userTokenizeService.validateToken(token);
        log.info("Result of validation: "+ result);
        if (result != null){
            return new RedirectView("/login?password-reset=false");
        }else {
            model.addAttribute("token",token.toString());
            return new RedirectView("/change-password");
        }
    }

    @PostMapping("/save-password")
    public RedirectView saveNewPassword(PasswordReset passwordReset){
        String result = userTokenizeService.validateToken(passwordReset.getToken());
        if (result != null ) return new RedirectView("/login?password-reset=false");
        Optional<NUser> userByToken = userTokenizeService.getUserByToken(passwordReset.getToken());
        if(userByToken.isPresent()){
            userService.changeUserPassword(userByToken.get(),passwordReset.getNewPassword());
            return new RedirectView("/login?password-reset=true");
        }else{
            return new RedirectView("/login?password-reset=false");
        }
    }


}