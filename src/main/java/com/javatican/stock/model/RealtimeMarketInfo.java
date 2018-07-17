package com.javatican.stock.model;

import java.util.List;

/*
 * Current stock trading info: 
 * http://mis.twse.com.tw/stock/api/getStockInfo.jsp?ex_ch=tse_3008.tw|tse_2317.tw|tse_2330.tw&json=1&delay=0&_=1531705877469
 */
public class RealtimeMarketInfo {

	private List<StockItemMarketInfo> msgArray;
	private Long userDelay;
	private String rtmessage = "";
	private String referer = "";
	private QueryTime queryTime;
	private String rtcode = "";

	public List<StockItemMarketInfo> getMsgArray() {
		return msgArray;
	}

	//
	public Long getUserDelay() {
		return userDelay;
	}

	public String getRtmessage() {
		return rtmessage;
	}

	public String getReferer() {
		return referer;
	}

	public QueryTime getQueryTime() {
		return queryTime;
	}

	public String getRtcode() {
		return rtcode;
	}

	public void setMsgArray(List<StockItemMarketInfo> msgArray) {
		this.msgArray = msgArray;
	}

	public void setUserDelay(Long userDelay) {
		this.userDelay = userDelay;
	}

	public void setRtmessage(String rtmessage) {
		this.rtmessage = rtmessage;
	}

	public void setReferer(String referer) {
		this.referer = referer;
	}

	public void setQueryTime(QueryTime queryTime) {
		this.queryTime = queryTime;
	}

	public void setRtcode(String rtcode) {
		this.rtcode = rtcode;
	}

	public RealtimeMarketInfo() {
		super();
	}

	public static class QueryTime {

		private String sysTime = "";
		private Long sessionLatestTime;
		private String sysDate = "";
		private Long sessionFromTime;
		private Long stockInfoItem;
		private boolean showChart;
		private String sessionStr = "";
		private Long stockInfo;

		public String getSysTime() {
			return sysTime;
		}

		public Long getSessionLatestTime() {
			return sessionLatestTime;
		}

		public String getSysDate() {
			return sysDate;
		}

		public Long getSessionFromTime() {
			return sessionFromTime;
		}

		public Long getStockInfoItem() {
			return stockInfoItem;
		}

		public boolean isShowChart() {
			return showChart;
		}

		public String getSessionStr() {
			return sessionStr;
		}

		public Long getStockInfo() {
			return stockInfo;
		}

		public void setSysTime(String sysTime) {
			this.sysTime = sysTime;
		}

		public void setSessionLatestTime(Long sessionLatestTime) {
			this.sessionLatestTime = sessionLatestTime;
		}

		public void setSysDate(String sysDate) {
			this.sysDate = sysDate;
		}

		public void setSessionFromTime(Long sessionFromTime) {
			this.sessionFromTime = sessionFromTime;
		}

		public void setStockInfoItem(Long stockInfoItem) {
			this.stockInfoItem = stockInfoItem;
		}

		public void setShowChart(boolean showChart) {
			this.showChart = showChart;
		}

		public void setSessionStr(String sessionStr) {
			this.sessionStr = sessionStr;
		}

		public void setStockInfo(Long stockInfo) {
			this.stockInfo = stockInfo;
		}
	}

	public static class StockItemMarketInfo {
		private String nu = ""; // etf
		private String ts = "";
		private String fv = ""; //
		private String tk0 = "";
		private String tk1 = "";
		private String oa = ""; //
		private String ob = ""; //
		private String tlong = ""; //* last data update time(long, in milliseconds)
		private String ot = ""; //
		private String f = ""; //* sell 5 quantities
		private String ex = "";
		private String g = ""; //* buy 5 quantities
		private String ov = ""; //
		private String d = ""; //* today's date
		private String it = "";
		private String b = ""; //buy 5 prices
		private String c = ""; //symbol
		private String mt = "";
		private String a = ""; //* sell 5 prices
		private String n = ""; //* company short name
		private String o = ""; //* open price
		private String l = ""; //* lowest price
		private String oz = ""; //
		private String io = ""; // RR
		private String h = ""; //* highest price
		private String ip = "";
		private String i = "";
		private String w = ""; //* 10% down limit
		private String v = ""; //*volume
		private String u = ""; //* 10% up limit
		private String t = ""; //* last data update time
		private String s = "";
		private String pz = "";
		private String tv = "";
		private String p = "";
		private String nf = ""; //* company full name
		private String ch = ""; //*symbol.tw
		private String z = ""; // *current trading price
		private String y = ""; // *yesterday's close
		private String ps = "";
		//domain added fields
		//store price change in percent
		private double priceChangeP = 0.0;

		//

		public double getPriceChangeP() {
			return priceChangeP;
		}

		public void setPriceChangeP(double priceChangeP) {
			this.priceChangeP = priceChangeP;
		}
		public String getIo() {
			return io;
		}

		public void setIo(String io) {
			this.io = io;
		}

		public String getNu() {
			return nu;
		}

		public void setNu(String nu) {
			this.nu = nu;
		}

		public String getOa() {
			return oa;
		}

		public void setOa(String oa) {
			this.oa = oa;
		}

		public String getOb() {
			return ob;
		}

		public void setOb(String ob) {
			this.ob = ob;
		}

		public String getOt() {
			return ot;
		}

		public void setOt(String ot) {
			this.ot = ot;
		}

		public String getOv() {
			return ov;
		}

		public void setOv(String ov) {
			this.ov = ov;
		}

		public String getOz() {
			return oz;
		}

		public void setOz(String oz) {
			this.oz = oz;
		}

		@Override
		public String toString() {
			return String.format("Symbol: %s, name:%s, currentPrice: %s, volume: %s", c, n, z, v);
		}

		public String getTs() {
			return ts;
		}

		public String getTk0() {
			return tk0;
		}

		public String getFv() {
			return fv;
		}

		public String setFv(String fv) {
			return this.fv = fv;
		}

		public String getTk1() {
			return tk1;
		}

		public String getTlong() {
			return tlong;
		}

		public String getF() {
			return f;
		}

		public String getEx() {
			return ex;
		}

		public String getG() {
			return g;
		}

		public String getD() {
			return d;
		}

		public String getIt() {
			return it;
		}

		public String getB() {
			return b;
		}

		public String getC() {
			return c;
		}

		public String getMt() {
			return mt;
		}

		public String getA() {
			return a;
		}

		public String getN() {
			return n;
		}

		public String getO() {
			return o;
		}

		public String getL() {
			return l;
		}

		public String getH() {
			return h;
		}

		public String getIp() {
			return ip;
		}

		public String getI() {
			return i;
		}

		public String getW() {
			return w;
		}

		public String getV() {
			return v;
		}

		public String getU() {
			return u;
		}

		public String getT() {
			return t;
		}

		public String getS() {
			return s;
		}

		public String getPz() {
			return pz;
		}

		public String getTv() {
			return tv;
		}

		public String getP() {
			return p;
		}

		public String getNf() {
			return nf;
		}

		public String getCh() {
			return ch;
		}

		public String getZ() {
			return z;
		}

		public String getY() {
			return y;
		}

		public String getPs() {
			return ps;
		}

		public void setTs(String ts) {
			this.ts = ts;
		}

		public void setTk0(String tk0) {
			this.tk0 = tk0;
		}

		public void setTk1(String tk1) {
			this.tk1 = tk1;
		}

		public void setTlong(String tlong) {
			this.tlong = tlong;
		}

		public void setF(String f) {
			this.f = f;
		}

		public void setEx(String ex) {
			this.ex = ex;
		}

		public void setG(String g) {
			this.g = g;
		}

		public void setD(String d) {
			this.d = d;
		}

		public void setIt(String it) {
			this.it = it;
		}

		public void setB(String b) {
			this.b = b;
		}

		public void setC(String c) {
			this.c = c;
		}

		public void setMt(String mt) {
			this.mt = mt;
		}

		public void setA(String a) {
			this.a = a;
		}

		public void setN(String n) {
			this.n = n;
		}

		public void setO(String o) {
			this.o = o;
		}

		public void setL(String l) {
			this.l = l;
		}

		public void setH(String h) {
			this.h = h;
		}

		public void setIp(String ip) {
			this.ip = ip;
		}

		public void setI(String i) {
			this.i = i;
		}

		public void setW(String w) {
			this.w = w;
		}

		public void setV(String v) {
			this.v = v;
		}

		public void setU(String u) {
			this.u = u;
		}

		public void setT(String t) {
			this.t = t;
		}

		public void setS(String s) {
			this.s = s;
		}

		public void setPz(String pz) {
			this.pz = pz;
		}

		public void setTv(String tv) {
			this.tv = tv;
		}

		public void setP(String p) {
			this.p = p;
		}

		public void setNf(String nf) {
			this.nf = nf;
		}

		public void setCh(String ch) {
			this.ch = ch;
		}

		public void setZ(String z) {
			this.z = z;
		}

		public void setY(String y) {
			this.y = y;
		}

		public void setPs(String ps) {
			this.ps = ps;
		}
	}

}
