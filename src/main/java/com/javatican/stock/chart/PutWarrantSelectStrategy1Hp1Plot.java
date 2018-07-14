package com.javatican.stock.chart;

import org.springframework.stereotype.Component;

@Component("pwss1Hp1Plot")
public class PutWarrantSelectStrategy1Hp1Plot extends  PutWarrantSelectStrategy1Plot{

	@Override
	public int getHoldPeriod() {
		return 1;
	}

	 
}
