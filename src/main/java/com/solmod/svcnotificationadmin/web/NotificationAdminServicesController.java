package com.solmod.svcnotificationadmin.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NotificationAdminServicesController {

    Logger log = LoggerFactory.getLogger(getClass());

    @GetMapping("/do-it")
    public String doSomething() {
        log.info("You're in!");

        return "SUCCESS!";
    }

}
