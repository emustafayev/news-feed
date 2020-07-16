package newsApp.controllers.newsController;

import lombok.extern.log4j.Log4j2;
import newsApp.exceptions.NewsNotFound;
import newsApp.models.newsModel.Domain;
import newsApp.models.newsModel.News;
import newsApp.models.userModels.NUser;
import newsApp.models.userModels.NUserDetails;
import newsApp.services.newsService.NewsService;
import newsApp.services.userService.UserService;
import org.springframework.data.domain.Page;
//import org.springframework.messaging.handler.annotation.MessageMapping;
//import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Log4j2
@Controller
@RequestMapping("/")
public class NewsController {

    private final UserService userService;
    private final NewsService newsService;

    public NewsController(UserService userService, NewsService newsService) {
        this.userService = userService;
        this.newsService = newsService;
    }

    @GetMapping
    public RedirectView mainPage(){
        return new RedirectView("news");
    }
    @GetMapping("news")
    public String loadByPage(Model model,
                             @RequestParam(value = "page",required = false,defaultValue = "0") Integer page, OAuth2AuthenticationToken oAuth2AuthenticationToken){
        log.info("Main-page GET request worked!");
        Page<News> pages = newsService.loadLatestNewsPages(page);




        model.addAttribute("pages",pages);
        return "main-page";
    }

    @GetMapping("v2/news")
    public String loadByPage02(Model model,
                             @RequestParam(value = "page",required = false,defaultValue = "0") Integer pageNumber, Principal principal){
        NUserDetails nUserDetails = (NUserDetails) principal;
        log.info("User: "+nUserDetails.toString());
        log.info("Main-page GET request worked!");
        Page<News> pages = newsService.loadLatestNewsPages_02(pageNumber,nUserDetails.getId());

        model.addAttribute("pages",pages);
        return "main-page";
    }

    @GetMapping("news/{newsId}")
    public String getOneNewsDetailed(@PathVariable("newsId") long newsId,
                                     Model model) throws NewsNotFound {
        log.info(String.format("NewsId:%d sent to service!",newsId));
        News newsById = newsService.getNewsById(newsId);

        model.addAttribute("news",newsById);

        return "open-tab";
    }

    @PostMapping("news/search")
    public String search_news(@RequestParam("query") String query, Model model, Principal principal){
        log.info(principal.getName());
        Page<News> searchRes = newsService.search(query, principal.getName());
        model.addAttribute("pages",searchRes );

        return "main-page";
    }


//TODO remove this if you dont need
//    @MessageMapping("/search")  // receive => /app/search
//    @SendTo("/queue/endpoint")  // send =>  to broker
//    public List<News> search_news(String query, Principal principal){
//        log.info("Query: " + query);
//        log.info("Principal name: "+principal.getName());
//        newsService.search(query,principal.getName());
//        return new ArrayList<>(Arrays.asList(new News("title", "newsLink", "mewAddress", new Domain("domain", "domain info", "domainlink"), "fjef", LocalDateTime.now())));
//    }
}
