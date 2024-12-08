package io.pivotal.pcf.sme.ers.client.ui.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.pivotal.pcf.sme.ers.server.model.Attendee;
import io.pivotal.pcf.sme.ers.server.repo.AttendeeRepository;

/**
 * AttendeeServices
 * 
 * The purpose of this service class is to have a clear separation from the UI
 * to the client.
 * 
 * This is a RestController and all the UI stuff is done by the MVC controller.
 * 
 * @see AttendeeController
 * 
 * @author mborges
 *
 */
@RestController
public class AttendeeService {

	private static final Logger logger = LoggerFactory
			.getLogger(AttendeeService.class);
	
	/// Polluting with server code (Attendee Model Object)
	// http://blog.zenika.com/2012/06/15/hateoas-paging-with-spring-mvc-and-spring-data-jpa/
	
	@Autowired
	private AttendeeRepository attendeeRepository;
	
	void add(io.pivotal.pcf.sme.ers.client.model.Attendee a1) {
		Attendee a2 = new Attendee();
		a2.setFirstName(a1.getFirstName());
		a2.setLastName(a1.getLastName());
		a2.setEmailAddress(a1.getEmailAddress());
		attendeeRepository.saveAndFlush(a2);
	}
	
	void deleteAll() {
		attendeeRepository.deleteAll();
	}
	
	// returning server object
	Iterable<Attendee> getAttendees() {
		return attendeeRepository.findAll();
	}
	
	// returning server object
	Iterable<Attendee> searchName(String firstName) {
		return attendeeRepository.findByFirstNameContainsIgnoreCase(firstName, PageRequest.of(0,100));
	}
	

	/**
	 * bluegreenRequest - It really just returns the app name.
	 * 
	 * The purpose is to show PCF load balancing between applications that share
	 * routes
	 * 
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("/bluegreen-check")
	public String[] bluegreenRequest() throws Exception {
		
		List<String> values = new ArrayList<String>();
		values.add((String) getVcapApplicationMap().getOrDefault("application_name", "no name environment variable"));
		values.add(getVcapApplicationMap().getOrDefault("instance_index", "running locally").toString());
		
		return values.toArray(new String[0]);
	}

	/**
	 * addAppEnv - Retrieve information about the application
	 * 
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> addAppEnv(HttpServletRequest request)
			throws Exception {

		Map<String, Object> modelMap = new HashMap<String, Object>();

		if (System.getenv("PORT") == null) {
			modelMap.put("containerAddr", "localhost");
		} else {
			modelMap.put("containerAddr", request.getLocalAddr() + ":"
					+ request.getLocalPort());
		}

		String instanceIndex = getVcapApplicationMap().getOrDefault(
				"instance_index", "no index environment variable").toString();
		modelMap.put("instanceIndex", instanceIndex);

		String instanceAddr = System.getenv("CF_INSTANCE_ADDR");
		if (instanceAddr == null) {
			instanceAddr = "localhost";
		}
		modelMap.put("instanceAddr", instanceAddr);

		String applicationName = (String) getVcapApplicationMap().getOrDefault(
				"application_name", "no name environment variable");
		modelMap.put("applicationName", applicationName);

		Map<String, ?> services = getVcapServicesMap();
		services = parseServices(services);
		modelMap.put("applicationServices", services);
		String javaVersion = System.getProperty("java.version");
		logger.debug("Java Version (unfiltered): {}", javaVersion);

		int pos = javaVersion.indexOf("-");
		if (pos > -1) {
			javaVersion = javaVersion.substring(0, pos);
		}
		logger.debug("Java Version (filtered): {}", javaVersion);

		modelMap.put("javaVersion", javaVersion);
		return modelMap;

	}

	///////////////////////////////////////
	// Helper Methods
	///////////////////////////////////////

	   @SuppressWarnings("rawtypes")
		private Map<String,Object> parseServices(Map<String, ?> services) {
			Map<String,Object> servicesMap  = Collections.synchronizedMap(new LinkedHashMap<String,Object>());
			for (Map.Entry<String,?> entry : services.entrySet()) {
				List list = (List)entry.getValue();
			    
			    for (Object object : list) {
					logger.debug("list: {}", object.getClass());
			    	Map map = (Map)object;
			    	//weird delimiter and UUID is to deal with multiple services of the same type
				    servicesMap.put(entry.getKey() + "~~~" + UUID.randomUUID().toString(), map.get("name"));		    		
				}
			}
			return servicesMap;
			
		}	
	@SuppressWarnings("rawtypes")
	private Map getVcapApplicationMap() throws Exception {
		return getEnvMap("VCAP_APPLICATION");
	}

	@SuppressWarnings("rawtypes")
	private Map getVcapServicesMap() throws Exception {
		return getEnvMap("VCAP_SERVICES");
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Map getEnvMap(String vcap) throws Exception {
		String vcapEnv = System.getenv(vcap);
		ObjectMapper mapper = new ObjectMapper();

		if (vcapEnv != null) {
			Map<String, ?> vcapMap = mapper.readValue(vcapEnv, Map.class);
			return vcapMap;
		}

		logger.warn(vcap + " not defined, returning empty Map");
		return new HashMap<String, String>();
	}

}
