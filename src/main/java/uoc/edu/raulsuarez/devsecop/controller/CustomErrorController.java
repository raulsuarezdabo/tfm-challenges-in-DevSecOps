package uoc.edu.raulsuarez.devsecop.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;

public class CustomErrorController implements ErrorController {

    @Override
    public String getErrorPath() {
        return null;
    }

    @RequestMapping("/error")
    public String handleError() {
        return "error";
    }
}
