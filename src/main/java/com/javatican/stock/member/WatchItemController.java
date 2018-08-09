package com.javatican.stock.member;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.javatican.stock.StockException;
import com.javatican.stock.model.PortfolioItem;
import com.javatican.stock.model.RealtimeMarketInfo;
import com.javatican.stock.model.RealtimeMarketInfo.StockItemMarketInfo;
import com.javatican.stock.model.SiteUser;
import com.javatican.stock.model.StockItem;
import com.javatican.stock.model.WatchItem;
import com.javatican.stock.model.WatchLog;
import com.javatican.stock.service.ChartService;
import com.javatican.stock.service.PortfolioService;
import com.javatican.stock.service.RealtimeQuoteService;
import com.javatican.stock.service.SiteUserService;
import com.javatican.stock.service.StockItemService;
import com.javatican.stock.service.StockService;
import com.javatican.stock.service.WatchItemService;
import com.javatican.stock.util.StockUtils;

@Controller
@RequestMapping("stock/*")
public class WatchItemController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private StockService stockService;
	@Autowired
	private StockItemService stockItemService;
	@Autowired
	private RealtimeQuoteService realtimeQuoteService;
	@Autowired
	private WatchItemService watchItemService;
	@Autowired
	private SiteUserService siteUserService;
	@Autowired
	private ChartService chartService;
	/*
	 * StringTrimmerEditor will convert empty/whitespace strings to null
	 */

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
	}

	@PostMapping("/addToWatchList")
	public ModelAndView addWatchItems(@ModelAttribute("command") FormCommand command) {
		String[] symbols = command.getSymbols();
		SiteUser su = siteUserService.findByUsername("ryan.nieh");
		List<WatchItem> wiList = new ArrayList<>();
		for (String symbol : symbols) {
			if (watchItemService.existsBySymbolAndSiteUser(symbol, su)) {
				continue;
			}
			StockItem si = stockItemService.getStockItem(symbol);
			if (si == null)
				continue;
			WatchItem wi = new WatchItem(si, su);
			wiList.add(wi);
		}
		watchItemService.saveAll(wiList);
		return new ModelAndView("redirect:" + "/stock/watchList");
	}

	@PostMapping("/{watchItemId}/addWatchLog")
	public ModelAndView addWatchLog(@PathVariable Long watchItemId, @ModelAttribute("wl") WatchLog wl) {
		SiteUser su = siteUserService.findByUsername("ryan.nieh");
		WatchItem wi = watchItemService.getByIdAndSiteUser(watchItemId, su);
		wl.setLogDate(StockUtils.todayWithoutTime());
		wl.setSymbol(wi.getSymbol());
		wl.setWatchItem(wi);
		watchItemService.saveWatchLog(wl);
		return new ModelAndView("redirect:" + "/stock/watchList");
	}

	@GetMapping("/watchList")
	public ModelAndView getWatchList(
			@RequestParam(value = "realtimeQuote", defaultValue = "false") boolean realtimeQuote,
			@RequestParam(value = "force", defaultValue = "false") boolean force) {
		Date date = stockService.getLatestTradingDate();
		ModelAndView mav = new ModelAndView("stock/watchList");
		SiteUser su = siteUserService.findByUsername("ryan.nieh");
		TreeMap<StockItem, WatchItem> wiMap = watchItemService.findBySiteUser2AsMap(su);
		// create stock charts
		chartService.createGraphs(wiMap.keySet(), force);
		//
		Map<String, StockItemMarketInfo> realtimeMap = new HashMap<>();
		if (realtimeQuote) {
			RealtimeMarketInfo mri;
			try {
				mri = realtimeQuoteService.getInfo2(wiMap.keySet());
				for (StockItemMarketInfo simi : mri.getMsgArray()) {
					realtimeMap.put(simi.getC(), simi);
				}
			} catch (StockException e) {
				logger.warn("Error getting realtime quote!");
				e.printStackTrace();
			}
		}
		//
		mav.addObject("realtimeMap", realtimeMap);
		mav.addObject("tradingDate", date);
		mav.addObject("dataMap", wiMap);
		mav.addObject("wl", new WatchLog());
		// stockItems with call and put warrants
		mav.addObject("swcwList", stockService.getStockSymbolsWithCallWarrant());
		mav.addObject("swpwList", stockService.getStockSymbolsWithPutWarrant());
		return mav;
	}

	/*
	 * delete WatchItem(also delete WatchLog relational items)
	 */
	@GetMapping("/watchList/{watchItemId}/delete")
	public ModelAndView deleteWatchItem(@PathVariable Long watchItemId) {
		SiteUser su = siteUserService.findByUsername("ryan.nieh");
		WatchItem wi = watchItemService.getByIdAndSiteUser(watchItemId, su);
		watchItemService.delete(wi);
		return new ModelAndView("redirect:" + "/stock/watchList");
	}

}
