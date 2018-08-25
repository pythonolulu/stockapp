package com.javatican.stock;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.javatican.stock.service.FutureService;
import com.javatican.stock.util.ResponseMessage;

@RestController
@RequestMapping("stock/*")
public class FutureController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass()); 
	@Autowired
	private FutureService futureService;

	@GetMapping("/updateFutureData")
	public ResponseMessage updateFutureData(HttpServletRequest request) {
		ResponseMessage mes = new ResponseMessage(request.getServletPath());
		try {
			futureService.downloadAndSaveFutureData();
			mes.setCategory("Success");
			mes.setText("Future data have been updated.");
		} catch (Exception ex) {
			ex.printStackTrace();
			mes.setCategory("Fail");
			mes.setText("Future data fail to be updated.");
		}
		return mes;
	}
}
