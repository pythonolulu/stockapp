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

public class RoutineJob {

	public static void main(String[] args) {
		List<String> commandList = new ArrayList<>(Arrays.asList("updateData", "updateTrustData", "updateForeignData",
				"updatePerformers", "createStockPrices", "updateMissingPriceField", "updatePriceDataForAll",
				"calculateAndSaveKDForAll", "updateCallWarrantData", "updatePutWarrantData", "updateDealerData",
				"prepareCallWarrantSelectStrategy1", "preparePutWarrantSelectStrategy1", "prepareSmaSelectStrategy2",
				"preparePriceBreakUpSelectStrategy3", "preparePriceBreakUpSelectStrategy4", "updateMarginData",
				"extractMarginData"));
		Set<String> finishedSet = new HashSet<>();
		ObjectMapper objectMapper = new ObjectMapper();
		final String STOCK_GET_URL = "http://localhost:8080/stock/%s";
		String strUrl = null;
		for (int i = 0; i < 3; i++) {
			commandList.removeAll(finishedSet);
			for (String command : commandList) {
				strUrl = String.format(STOCK_GET_URL, command);
				Document doc;
				try {
					doc = Jsoup.connect(strUrl).ignoreContentType(true).timeout(0).get();
				} catch (IOException e) {
					System.out.println("Error connecting.");
					e.printStackTrace();
					break;
				}
				String json = doc.text();
				// logger.info(json);
				if (json == null || json.equals("")) {
					System.out.println("Error getting response.");
					break;
				}
				ResponseMessage rm;
				try {
					rm = objectMapper.readValue(json, new TypeReference<ResponseMessage>() {
					});
				} catch (IOException e) {
					System.out.println("Error getting json response message.");
					e.printStackTrace();
					break;
				}
				System.out.println(rm);
				if (rm.getCategory().equals("Fail")) {
					break;
				}
				finishedSet.add(command);
			}
		}
	}

}
