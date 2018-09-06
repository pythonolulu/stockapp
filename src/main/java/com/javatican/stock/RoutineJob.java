package com.javatican.stock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javatican.stock.util.ResponseMessage;
import com.javatican.stock.util.StockUtils;

public class RoutineJob {
	private static ObjectMapper objectMapper = new ObjectMapper();

	public static void main(String[] args) {
		boolean toUpdateData = true;
		List<String> commandList = new ArrayList<>(Arrays.asList("updateTrustData", "updateForeignData",
				"updatePerformers", "createStockPrices", "updateMissingPriceField", "updatePriceDataForAll",
				"calculateAndSaveKDForAll", "updateCallWarrantData", "updatePutWarrantData", "updateDealerData",
				"prepareCallWarrantSelectStrategy1", "preparePutWarrantSelectStrategy1", "prepareSmaSelectStrategy2",
				"preparePriceBreakUpSelectStrategy3", "preparePriceBreakUpSelectStrategy4", "updateMarginData",
				"extractMarginData", "calculateIndexStatsData", "prepareSmaStatsData", "updateFutureData"));
		final String STOCK_GET_URL = "http://localhost:8080/stock/%s";
		String strUrl = null;
		ResponseMessage rm = null;
		if (toUpdateData) {
			strUrl = String.format(STOCK_GET_URL, "updateData");
			rm = doWork(strUrl);
			if (rm == null) {
				System.out.println("Error running 'updateData' job.");
				return;
			} else if (rm.getCategory().equals("Fail")) {
				System.out.println(rm);
				return;
			} else {

				String text = rm.getText();
				String latestTradingDate = text.substring(text.indexOf("***") + 3);
				if (StockUtils.isFriday(StockUtils.stringSimpleToDate(latestTradingDate).get())) {
					//
					System.out.println("Adding extra jobs on Friday");
					commandList.add("calculateWeeklyIndexStatsData");
					commandList.add("prepareWeeklyStockPriceForAll");
					commandList.add("calculateAndSaveWeeklyKDForAll");
				}

			}
		}
		Set<String> finishedSet = new HashSet<>();
		for (int i = 0; i < 3; i++) {
			commandList.removeAll(finishedSet);
			for (String command : commandList) {
				strUrl = String.format(STOCK_GET_URL, command);
				//
				rm = doWork(strUrl);
				if (rm == null || rm.getCategory().equals("Fail")) {
					break;
				}
				finishedSet.add(command);
			}
		}
	}

	public static ResponseMessage doWork(String strUrl) {
		Document doc;
		try {
			doc = Jsoup.connect(strUrl).ignoreContentType(true).timeout(0).get();
		} catch (IOException e) {
			System.out.println("Error connecting.");
			e.printStackTrace();
			return null;
		}
		String json = doc.text();
		// logger.info(json);
		if (json == null || json.equals("")) {
			System.out.println("Error getting response.");
			return null;
		}
		ResponseMessage rm;
		try {
			rm = objectMapper.readValue(json, new TypeReference<ResponseMessage>() {
			});
		} catch (IOException e) {
			System.out.println("Error getting json response message.");
			e.printStackTrace();
			return null;
		}
		System.out.println(rm);
		return rm;
	}
}
