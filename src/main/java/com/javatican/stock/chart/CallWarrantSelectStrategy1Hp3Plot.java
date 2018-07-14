package com.javatican.stock.chart;

import org.springframework.stereotype.Component;

@Component("cwss1Hp3Plot")
public class CallWarrantSelectStrategy1Hp3Plot extends  CallWarrantSelectStrategy1Plot{

	@Override
	public int getHoldPeriod() {
		return 3;
	}

	 
}
