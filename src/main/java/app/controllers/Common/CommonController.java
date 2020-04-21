package app.controllers.Common;

import app.Models.*;
import app.Utils.MessageGenerator;
import app.services.impl.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class CommonController {

    @Autowired
    Statistic statistic;

    @Autowired
    ReloadableResourceBundleMessageSource messageSource;

    @Autowired
    MessageGenerator messageGenerator;

    @Autowired
    GoalServiceImpl goalService;

    @Autowired
    TeamServiceImpl teamService;

    @Autowired
    OffenseServiceImpl offenseService;

    @Autowired
    GameServiceImpl gameService;

    @Autowired
    ManualSkipGameServiceImpl manualSkipGameService;

    @Autowired
    Context context;

    @GetMapping(value = "/start")
    public String getMainPage(Model model) {
        if (messageGenerator.isActive())
            model.addAttribute("message", messageGenerator.getMessageWithSetNotActive());
        model.addAttribute("bombardiers", statistic.getContext().getFromContext("bombardiersFirsts"));
        model.addAttribute("yellowCards", statistic.getContext().getFromContext("yellowCardsFirsts"));
        model.addAttribute("skipGames", statistic.getContext().getFromContext("skipGamesLastTour"));
        model.addAttribute("needShowAllBombardiers", statistic.getContext().getFromContext("needShowAllBombardiers") == null ? Boolean.TRUE : statistic.getContext().getFromContext("needShowAllBombardiers"));
        model.addAttribute("needShowAllYellowCards", statistic.getContext().getFromContext("needShowAllYellowCards") == null ? Boolean.TRUE : statistic.getContext().getFromContext("needShowAllYellowCards"));
        model.addAttribute("needShowAllSkipGames", statistic.getContext().getFromContext("needShowAllSkipGames") == null ? Boolean.TRUE : statistic.getContext().getFromContext("needShowAllSkipGames"));
        return "administration/mainPage";
    }

    @GetMapping(value = "/")
    public String getMainPageWithLoadingMessage(Model model) {
        if (statistic.isStatisticReady()) {
            return "redirect:/start";
        } else {
            Thread threadForStatistic = new Thread(statistic);
            threadForStatistic.start();
            return "administration/mainPageWithLoadingMessage";
        }
    }

    @GetMapping(value = "/clearCache")
    public String getMainPageWithClearCache(Model model) {
        statistic.getContext().clear();
        context.clear();
        return "administration/mainPageWithLoadingMessage";
    }

    @GetMapping(value = "/login")
    public String geLoginPage(Model model) {
        if (messageGenerator.isActive())
            model.addAttribute("message", messageGenerator.getMessageWithSetNotActive());
        model.addAttribute("user", new User());
        return "login";
    }

    @GetMapping(value = "/bombardiers")
    public String getBombardiers(Model model, HttpServletRequest request) {
        if (request.getParameter("show").equals("all")) {
            model.addAttribute("bombardiers", statistic.getContext().getFromContext("bombardiersAll"));
        } else {
            model.addAttribute("bombardiers", statistic.getContext().getFromContext("bombardiersFirsts"));
        }
        model.addAttribute("needShowAllBombardiers", statistic.getContext().getFromContext("needShowAllBombardiers") == null ? Boolean.TRUE : statistic.getContext().getFromContext("needShowAllBombardiers"));
        return "common/statistic/bombardiers";
    }

    @GetMapping(value = "/yellowCards")
    public String getYellowCardsStatistic(Model model, HttpServletRequest request) {
        if (request.getParameter("show").equals("all")) {
            model.addAttribute("yellowCards", statistic.getContext().getFromContext("yellowCardsAll"));
        } else {
            model.addAttribute("yellowCards", statistic.getContext().getFromContext("yellowCardsFirsts"));
        }
        model.addAttribute("needShowAllYellowCards", statistic.getContext().getFromContext("needShowAllYellowCards") == null ? Boolean.TRUE : statistic.getContext().getFromContext("needShowAllYellowCards"));
        return "common/statistic/yellowCards";
    }

    @GetMapping(value = "/skipGames")
    public String getSkipGamesStatistic(Model model, HttpServletRequest request) {
        if (request.getParameter("show").equals("all")) {
            model.addAttribute("skipGames", statistic.getContext().getFromContext("skipGamesAll"));
        } else {
            model.addAttribute("skipGames", statistic.getContext().getFromContext("skipGamesLastTour"));
        }
        model.addAttribute("needShowAllSkipGames", statistic.getContext().getFromContext("needShowAllSkipGames") == null ? Boolean.TRUE : statistic.getContext().getFromContext("needShowAllSkipGames"));
        return "common/statistic/skipGames";
    }
}

