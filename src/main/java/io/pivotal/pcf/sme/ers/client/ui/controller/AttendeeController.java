package io.pivotal.pcf.sme.ers.client.ui.controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.pivotal.pcf.sme.ers.client.model.Attendee;

/**
 * AttendeeController
 * 
 * This is the MVC controller for the application. All UI HTTP requests get
 * here. We're using Thymeleaf as the template engine.
 * 
 * 
 * @author mborges
 *
 */
@Controller
public class AttendeeController {

	private Log log = LogFactory.getLog(AttendeeController.class);

	@Autowired
	private AttendeeService attendeeService;

	/**
	 * INDEX
	 * 
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/")
	public String index(HttpServletRequest request, Model model) throws Exception {
		addAppEnv(request, model);
		return "index";
	}

	/**
	 * BASICS
	 * 
	 * Action to initiate shutdown of the system. In CF, the application
	 * <em>should</em> restart. In other environments, the application runtime
	 * will be shut down.
	 * 
	 * @throws Exception
	 */
	@GetMapping("/basics")
	public String kill(HttpServletRequest request, @RequestParam(required = false) boolean doit, Model model) throws Exception {

		addAppEnv(request, model);

		if (doit) {
			model.addAttribute("killed", true);
			log.warn("*** The system is shutting down. ***");
			Runnable killTask = () -> {
				try {
					String name = Thread.currentThread().getName();
					log.warn("killing shortly " + name);
					TimeUnit.SECONDS.sleep(5);
					log.warn("killed " + name);
					System.exit(0);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			};
			new Thread(killTask).start();
		}

		return "basics";

	}
	
	@SuppressWarnings("resource")
	@GetMapping("/ssh-file")
	public String writeFile(HttpServletRequest request, Model model) throws Exception {
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yy HH:mm:ss");
		Date date = new Date();
		File f = new File("ers-ssh-demo.log");
		FileWriter fw = new FileWriter(f, true);
		Writer w = new BufferedWriter(fw);
		w.write(dateFormat.format(date) + "\n");
		w.flush();
		
		model.addAttribute("ssh_file", f.getAbsoluteFile());
		addAppEnv(request, model);

		return "basics";
		
	}

	/**
	 * SERVICES
	 * 
	 * @param model
	 *            The model for this action.
	 * @return The path to the view.
	 */
	@GetMapping("/services")
	public String attendees(HttpServletRequest request, Model model) throws Exception {

		model.addAttribute("attendees", attendeeService.getAttendees());
		
		addAppEnv(request, model);
		return "services";
	}
	
	/**
	 * SERVICES - clean DB
	 * 
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/clean")
	public String clean(HttpServletRequest request, Model model) throws Exception {

		attendeeService.deleteAll();
		return attendees(request, model);
	}
	

	/**
	 * SERVICES - Add Attendee
	 * 
	 * NOTE: this method chains (calls) the "attendees" method so it returns the
	 * services template with the updated attendees list.
	 * 
	 * TODO:
	 * - Turn this this into REST call
	 * 
	 * @param firstName
	 * @param lastName
	 * @param emailAddress
	 * @param model
	 * 
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/add-attendee")
	public String addAttendee(HttpServletRequest request, @RequestParam String firstName, @RequestParam String lastName,
			@RequestParam String emailAddress, Model model) throws Exception {

		Attendee attendee = new Attendee();
		attendee.setFirstName(firstName);
		attendee.setLastName(lastName);
		attendee.setEmailAddress(emailAddress);

		attendeeService.add(attendee);
		model.addAttribute("attendees", attendeeService.getAttendees());

		addAppEnv(request, model);
		return "services";
	}

	/**
	 * BLUEGREEN
	 * 
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/bluegreen")
	public String bluegreen(HttpServletRequest request, Model model) throws Exception {

		for (String key : System.getenv().keySet()) {
			System.out.println(key + ":" + System.getenv(key));
		}

		addAppEnv(request, model);

		return "bluegreen";
	}

	///////////////////////////////////////
	// Helper Methods
	///////////////////////////////////////

	private void addAppEnv(HttpServletRequest request, Model model) throws Exception {

		Map<String, Object> modelMap = attendeeService.addAppEnv(request);
		model.addAllAttributes(modelMap);
	}

}
