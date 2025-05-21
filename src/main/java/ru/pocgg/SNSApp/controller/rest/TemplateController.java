package ru.pocgg.SNSApp.controller.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import ru.pocgg.SNSApp.model.User;
import ru.pocgg.SNSApp.model.exceptions.DeactivatedAccountException;
import ru.pocgg.SNSApp.services.UserService;

public abstract class TemplateController {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());
}
