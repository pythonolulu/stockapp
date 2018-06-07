package com.javatican.stock;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:stock.properties")
public class StockConfig {
	@Value("${stock.data.download.sleeptime}")
	private int sleepTime;

	public int getSleepTime() {
		return sleepTime;
	}

}
