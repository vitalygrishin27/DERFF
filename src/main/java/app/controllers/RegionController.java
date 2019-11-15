package app.controllers;

import app.Models.Competition;
import app.Models.Context;
import app.Models.Region;
import app.Utils.MessageGenerator;
import app.services.impl.RegionServiceImpl;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Controller
public class RegionController {

    @Autowired
    ReloadableResourceBundleMessageSource messageSource;

    @Autowired
    MessageGenerator messageGenerator;

    @Autowired
    RegionServiceImpl regionService;

    @Autowired
    Context context;

    @GetMapping(value = "/regions")
    public String getRegions(Model model) {
        List<Region> regions = regionService.findAllRegions();
        Collections.reverse(regions);
        if (messageGenerator.isActive())
            model.addAttribute("errorMessage", messageGenerator.getMessageWithSetNotActive());

        model.addAttribute("regions", regions);
        return "region/regions";
    }

    @PostMapping(value = "/selectRegion")
    public void selectRegion(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        long id = Long.valueOf(req.getParameter("regionId"));
        Region region = regionService.findRegionById(id);
        // TODO: 15.11.2019 Может нужно обнулять весь контекст
        context.putToContext("region", region);
        JSONObject jsonObjectResponse = new JSONObject();
        jsonObjectResponse.put("url", "/competitions");
        resp.getWriter().write(String.valueOf(jsonObjectResponse));
        resp.flushBuffer();
    }

}
