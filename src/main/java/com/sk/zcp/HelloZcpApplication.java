package com.sk.zcp;

import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication(scanBasePackages = {"com.sk.zcp"})
public class HelloZcpApplication {

	private static final Logger logger = LoggerFactory.getLogger(HelloZcpApplication.class);
	
	public static void main(String[] args) {
		SpringApplication.run(HelloZcpApplication.class, args);
	}
	
	@GetMapping("/home") 
	public String home() {
		return "Hello ZCP";
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@GetMapping("/info") 
	public String info(@RequestHeader Map header, HttpServletRequest req) {
		StringBuffer sb = new StringBuffer();
		sb.append("<b>HTTP HEADER</b><br/>");
		header.forEach((key, value) -> {sb.append(String.format("%s : %s <br/>", key, value));});
		sb.append("<b>Remote</b><br/>");
		sb.append("getServerName : ").append(req.getServerName()).append("<br/>");
		sb.append("getServerPort : ").append(req.getServerPort()).append("<br/>");
		sb.append("getContextPath : ").append(req.getContextPath()).append("<br/>");
		sb.append("getServletPath : ").append(req.getServletPath()).append("<br/>");
		sb.append("getRequestURL : ").append(req.getRequestURL().toString()).append("<br/>");
		sb.append("getRemoteAddr : ").append(req.getRemoteAddr()).append("<br/>");
		sb.append("getRemoteHost : ").append(req.getRemoteHost()).append("<br/>");
		sb.append("getRemoteUser : ").append(req.getRemoteUser()).append("<br/>");
		sb.append("getRemotePort : ").append(req.getRemotePort()).append("<br/>");
				
		return sb.toString();
	}
  
  	@GetMapping("/printErrorLog") 
	public String error() throws Exception{
  		Exception exception = new Exception("Error service");
  		StringWriter sw = new StringWriter();
  		BufferedWriter bw = new BufferedWriter(sw);
  		PrintWriter pw = new PrintWriter(bw);
  		
  		exception.printStackTrace(pw);
  		pw.flush();
  		String stacktrace = sw.toString();
  		stacktrace = stacktrace.replaceAll("\n", "<br/>");
  		stacktrace = stacktrace.replaceAll("\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
  		pw.close();
		logger.error("print stacktrace", exception);
		return "log --------<br>" + stacktrace;
	}
  	
  	@GetMapping("/404") 
	public void notFound(HttpServletResponse httpResponse) throws Exception{
  		httpResponse.sendRedirect("/");
	}
  
  	@SuppressWarnings("static-access")
	@GetMapping("/timeout") 
	public String timeout(@RequestParam(defaultValue="60")String timeout) {
  		int intTimeoutMilSec = Integer.parseInt(timeout) * 1000;
  		logger.info("TIMEOUT : " + timeout);
  		try {
			Thread.currentThread().sleep(intTimeoutMilSec);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return "timeout " + timeout + " seconds";
	}
  	
  	@SuppressWarnings({ "rawtypes", "unchecked" })
  	@GetMapping("/bigJson") 
	public Map bigJson(@RequestParam(defaultValue="1000")String amount) {
  		long t1 = System.nanoTime();
		Map map = new HashMap();
		map.put("dataAmount", amount);
		Integer intAmount = Integer.parseInt(amount);
		List<Object> list = new ArrayList<Object>();
		for (int i = 0; i < intAmount; i++) {
			Map submap = new HashMap();
			submap.put("key", "value " + i);
			list.add(submap);
		}
  		map.put("data", list);
  		long t2 = System.nanoTime();
  		map.put("processTime", t2 - t1);
		return map;
	}

  	@GetMapping("/time")
  	public String getServerTime() {
  		Date d = Calendar.getInstance().getTime();
  		logger.debug(d.toString());
		return d.toString();
  	}
}
