package com.javatican.stock.chart;

import org.springframework.stereotype.Component;

@Component("pwss1Hp3Plot")
public class PutWarrantSelectStrategy1Hp3Plot extends  PutWarrantSelectStrategy1Plot{

	@Override
	public int getHoldPeriod() {
		return 3;
	}

	 
}
