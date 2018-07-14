package com.javatican.stock.chart;

import org.springframework.stereotype.Component;

@Component("cwss1Hp5Plot")
public class CallWarrantSelectStrategy1Hp5Plot extends  CallWarrantSelectStrategy1Plot{

	@Override
	public int getHoldPeriod() {
		return 5;
	}

	 
}
