package com.javatican.stock.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javatican.stock.StockConfig;
import com.javatican.stock.StockException;
/*
 * http://mis.twse.com.tw/stock/api/getStockInfo.jsp
 * 	?ex_ch=tse_3008.tw|tse_2317.tw|tse_2330.tw&json=1&delay=0&_=1531705877469
 */
import com.javatican.stock.model.RealtimeMarketInfo;
import com.javatican.stock.model.StockItem;
import com.javatican.stock.model.RealtimeMarketInfo.StockItemMarketInfo;
import com.javatican.stock.util.StockUtils;

@Service("realtimeQuoteService")
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = StockException.class)
public class RealtimeQuoteService {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private static ObjectMapper objectMapper = new ObjectMapper();
	// private static DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
	final String TWSE_REALTIME_QUOTE_GET_URL = "http://mis.twse.com.tw/stock/api/getStockInfo.jsp?ex_ch=%s&json=1&delay=0&_=%s";
	@Autowired
	StockConfig stockConfig;

	public RealtimeQuoteService() {
	}

	public RealtimeMarketInfo getInfo2(Collection<StockItem> siCollection) throws StockException {
		Collection<String> symbolCollection = siCollection.stream().map(StockItem::getSymbol).collect(Collectors.toList());
		return getInfo(symbolCollection);
	}
	public RealtimeMarketInfo getInfo(Collection<String> symbolCollection) throws StockException {
		List<String> symbolList = new ArrayList<>(symbolCollection);
		RealtimeMarketInfo rmi = null;
		RealtimeMarketInfo mergedRmi = null;
		for (int i = 0; i < symbolList.size(); i += 50) {
			List<String> sublist = symbolList.subList(i, (i + 50 > symbolList.size()) ? symbolList.size() : i + 50);
			StringBuilder sb = new StringBuilder();
			for (String symbol : sublist) {
				sb.append(String.format("tse_%s.tw|", symbol));
			}
			logger.info(sb.toString());
			try {
				String strUrl = String.format(TWSE_REALTIME_QUOTE_GET_URL, sb.toString(),
						Long.toString(System.currentTimeMillis()));
				Document doc = Jsoup.connect(strUrl).get();
				String json = doc.text();
				// logger.info(json);
				if (json == null || json.equals("")) {
					throw new StockException();
				}
				rmi = objectMapper.readValue(json, new TypeReference<RealtimeMarketInfo>() {
				});
				for (StockItemMarketInfo simi : rmi.getMsgArray()) {
					try {
						simi.setPriceChangeP(StockUtils
								.roundDoubleDp4((Double.parseDouble(simi.getZ()) - Double.parseDouble(simi.getY()))
										/ Double.parseDouble(simi.getZ())));
					} catch (Exception ignored) {
						logger.warn("Error calculating priceChangeP for symbol:" + simi.getC());
					}
				}
				if (i == 0) {
					mergedRmi = rmi;
				} else {
					List<StockItemMarketInfo> updatedList = mergedRmi.getMsgArray();
					updatedList.addAll(rmi.getMsgArray());
					mergedRmi.setMsgArray(updatedList);
				}
				try {
					Thread.sleep(stockConfig.getSleepTime());
				} catch (InterruptedException ex) {
				}
			} catch (IOException e) {
				e.printStackTrace();
				logger.warn("Error reading realtime quote data");
				throw new StockException();
			}
		}

		return mergedRmi;
	}
}
