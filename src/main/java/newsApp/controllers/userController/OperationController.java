package newsApp.controllers.userController;

import lombok.extern.log4j.Log4j2;
import newsApp.models.newsModel.Domain;
import newsApp.models.userModels.NUserDetails;
import newsApp.services.userService.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;
@Log4j2
@Controller
public class OperationController {
    private final UserService userService;

    public OperationController(UserService userService) {
        this.userService = userService;
    }

    /**
     * http://localhost:8080/disable-news
     *@return
     */
    @GetMapping("/disable-news")
    public String getDisableNewsPage(Authentication auth, Model model){
        NUserDetails user = (NUserDetails) auth.getPrincipal();
        List<Domain> domains = userService.getActiveDomainsOfUser(user.getId());
        log.info("User's domain list returned!");
        model.addAttribute("allDomains",domains);
        return "disable-news";
    }
    /**
     * http://localhost:8080/disable-news/{id}
     *@return
     */
    @PostMapping("/disable-news/{domainId}")
    public RedirectView getDisableNewsPage(@PathVariable("domainId") long domainId, Authentication auth){
        NUserDetails user = (NUserDetails) auth.getPrincipal();

        userService.disableDomain(user.getId(),domainId);
        return new RedirectView("/disable-news");
    }

    /**
     * http://localhost:8080/disable-news
     *@return
     */
    @GetMapping("/enable-news")
    public String getEnableNewsPage(Authentication auth, Model model){
        NUserDetails user = (NUserDetails) auth.getPrincipal();

        List<Domain> domains = userService.getDisabledDomainsOfUser(user.getId());
        log.info("User's disabled list returned!"+domains);
        model.addAttribute("allDomains",domains);
        return "enable-news";
    }
    /**
     * http://localhost:8080/disable-news/{id}
     *@return
     */
    @PostMapping("/enable-news/{domainId}")
    public RedirectView getEnableNewsPage(@PathVariable("domainId") long domainId, Authentication auth){
        NUserDetails user = (NUserDetails) auth.getPrincipal();

        userService.enableDomain(user.getId(),domainId);
        return new RedirectView("/enable-news");
    }
}
