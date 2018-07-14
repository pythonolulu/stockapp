package com.javatican.stock.chart;

import org.springframework.stereotype.Component;

@Component("pwss1Hp5Plot")
public class PutWarrantSelectStrategy1Hp5Plot extends  PutWarrantSelectStrategy1Plot{

	@Override
	public int getHoldPeriod() {
		return 5;
	}

	 
}
