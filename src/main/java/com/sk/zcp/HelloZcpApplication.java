package com.sk.zcp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication
public class HelloZcpApplication {

	public static void main(String[] args) {
		SpringApplication.run(HelloZcpApplication.class, args);
	}
	
	@GetMapping("/home") 
	public String home() {
		return "Hello ZCP";
	}
  
  	@GetMapping("/errorservice") 
	public String error() throws Exception{
		throw new Exception("Error service");
	}
  
  	@SuppressWarnings("static-access")
	@GetMapping("/timeout") 
	public String timeout(@RequestParam(defaultValue="60")String timeout) {
  		int intTimeoutMilSec = Integer.parseInt(timeout) * 1000;
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
}
