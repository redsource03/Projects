package com.redsource.fundamentals.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.redsource.fundamentals.rest.model.CompanyInfo;
import com.redsource.fundamentals.service.StockInfoService;

@RestController
@RequestMapping("/api")
public class FundamentalsRestController {

	private final Logger log = LoggerFactory.getLogger(FundamentalsRestController.class);
	@Autowired
	StockInfoService stockInfoService;
	// inject via application.properties
	@Value("${welcome.message:test}")
	private String message = "Hello World";

	@RequestMapping("/getStock/{stock}/{year}")
	public @ResponseBody CompanyInfo getGrowth(@PathVariable(value="stock") String stock,
			@PathVariable(value="year") String year) {
		
		return stockInfoService.getStockInfo(stock,year);
	}
	@RequestMapping("/you")
	public  @ResponseBody String fundamentals() throws Exception{
		stockInfoService.load();
		return "{\"Result\":\"OK\"}";
	}

}