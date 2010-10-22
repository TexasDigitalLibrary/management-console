/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.app.controller;

import java.util.Date;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * The default view for this application
 * 
 * @contributor dbernstein
 */
@Controller
public class HomeController extends AbstractController {

	@RequestMapping(value = { "/index.html", "/", "", "/home.html", "/index",
			"/home" })
	public ModelAndView home() {
		log.info("serving up the home page at {}", System.currentTimeMillis());
		ModelAndView mav = new ModelAndView();
		mav.setViewName("home");
		mav.addObject("time", new Date());
		return mav;
	}

	@RequestMapping(value = { "/logout" })
	public ModelAndView logout() {
		
		return home();
	}

	@RequestMapping(value = { "/login" })
	public String login() {
		return "login";
	}

}
