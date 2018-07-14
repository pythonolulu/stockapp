package com.javatican.stock.chart;

import org.springframework.stereotype.Component;

@Component("cwss1Hp1Plot")
public class CallWarrantSelectStrategy1Hp1Plot extends  CallWarrantSelectStrategy1Plot{

	@Override
	public int getHoldPeriod() {
		return 1;
	}

	 
}
