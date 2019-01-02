package com.sk.zcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class HelloZcpService {

	private static final Logger logger = LoggerFactory.getLogger(HelloZcpService.class);
	
	public void method1() {
		logger.info("---- HelloZcpService.method1 ----");
	}
	public void method2() {
		logger.info("---- HelloZcpService.method2 ----");
	}
	public void method3() {
		logger.info("---- HelloZcpService.method3 ----");
	}
}
