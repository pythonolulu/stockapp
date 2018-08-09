package com.javatican.stock.member;

import java.util.Date;
import java.util.List;

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
import org.springframework.web.servlet.ModelAndView;

import com.javatican.stock.model.PortfolioItem;
import com.javatican.stock.model.SiteUser;
import com.javatican.stock.model.StockItem;
import com.javatican.stock.service.PortfolioService;
import com.javatican.stock.service.SiteUserService;
import com.javatican.stock.service.StockItemService;
import com.javatican.stock.service.StockService;

@Controller
@RequestMapping("stock/*")
public class PortfolioController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private StockService stockService;
	@Autowired
	private StockItemService stockItemService;
	//@Autowired
	//private RealtimeQuoteService realtimeQuoteService;
	@Autowired
	private PortfolioService portfolioService; 
	@Autowired
	private SiteUserService siteUserService;
	//@Autowired
	//private ChartService chartService;
	@Autowired
	@Qualifier("portfolioItemValidator")
	private Validator piValidator;

	/*
	 * StringTrimmerEditor will convert empty/whitespace strings to null
	 */

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
		binder.setValidator(piValidator);
	}
 
	@GetMapping("/portfolio")
	public ModelAndView getPortfolio() {
		ModelAndView mav = new ModelAndView("stock/portfolio");
		SiteUser su = siteUserService.findByUsername("ryan.nieh");
		List<PortfolioItem> piList = portfolioService.findBySiteUser(su);
		Date date = stockService.getLatestTradingDate();
		//
		mav.addObject("piList", piList);
		mav.addObject("tradingDate", date);
		return mav;
	}

	@GetMapping(value = { "/portfolio/{portfolioId}/update", "/portfolio/create" })
	public ModelAndView portfolioEditForm(@PathVariable(required = false) Long portfolioId) {
		ModelAndView mav = null;
		if (portfolioId == null) {
			mav = new ModelAndView("stock/portfolioEditForm");
			mav.addObject("pi", new PortfolioItem());
		} else {
			SiteUser su = siteUserService.findByUsername("ryan.nieh");
			PortfolioItem pi = portfolioService.getByIdAndSiteUser(portfolioId, su);
			if (pi != null) {
				mav = new ModelAndView("stock/portfolioEditForm");
				mav.addObject("pi", pi);
				mav.addObject("stockItemName", pi.getStockItem().getName());
				mav.addObject("sybmolReadonly", true);
			} else {
				mav = new ModelAndView("stock/error");
			}
		}
		return mav;
	}

	@PostMapping("/portfolio/update")
	public String updatePortfolioItem(@Valid @ModelAttribute("pi") PortfolioItem pi, BindingResult bindingResult,
			Model model) {
		if (bindingResult.hasErrors()) {
			logger.warn("BINDING RESULT ERROR");
			logger.info(bindingResult.toString());
			if (pi.getSymbol() != null) {
				StockItem si = stockItemService.getStockItem(pi.getSymbol());
				if (si != null)
					model.addAttribute("stockItemName", si.getName());
			}
			if (pi.getId() != null) {
				model.addAttribute("sybmolReadonly", true);
			}
			return "stock/portfolioEditForm";
		} else {
			SiteUser su = siteUserService.findByUsername("ryan.nieh");
			if (pi.getId() != null) {
				// do update
				PortfolioItem original = portfolioService.getByIdAndSiteUser(pi.getId(), su);
				if (original == null) {
					return "stock/error";
				}
				manageAndSavePortfolioItem(pi, original);

			} else {
				// do insert
				pi.setSiteUser(su);
				StockItem si = stockItemService.getStockItem(pi.getSymbol());
				//below won't happen, since the symbol existence has been check during validation
				if (si == null) {
					return "stock/error";
				}
				pi.setStockItem(si);
				manageAndSavePortfolioItem2(pi);
			}
			return "redirect:" + "/stock/portfolio";
		}
	}
	private void manageAndSavePortfolioItem2(PortfolioItem original) {
		double res = 0.0;
		if (original.getIsShort()) {
			if (original.getBuyQuantity() != null && original.getSellQuantity() != null
					&& original.getBuyQuantity() < original.getSellQuantity()) {
				res = original.getSellQuantity() - original.getBuyQuantity();
				original.setSellQuantity(original.getBuyQuantity());
			}
		} else {
			if (original.getBuyQuantity() != null && original.getSellQuantity() != null
					&& original.getSellQuantity() < original.getBuyQuantity()) {
				res = original.getBuyQuantity() - original.getSellQuantity();
				original.setBuyQuantity(original.getSellQuantity());
			}
		}
		// symbol can not change
		if (original.getBuyPrice() != null && original.getBuyQuantity() != null) {
			original.setBuyValue(original.getBuyPrice() * original.getBuyQuantity());
		} else {
			original.setBuyValue(null);
		}
		if (original.getSellPrice() != null && original.getSellQuantity() != null) {
			original.setSellValue(original.getSellPrice() * original.getSellQuantity());
		} else {
			original.setSellValue(null);
		}
		if (original.getBuyDate() != null && original.getSellDate() != null && original.getSellValue() != null
				&& original.getBuyValue() != null) {
			original.setIsClosed(true);
			original.setProfit(original.getSellValue() - original.getBuyValue());
		}
		//
		if (original.getWarrantSymbol() != null) {
			original.setIsWarrant(true);
		} else {
			original.setIsWarrant(false);
		}
		portfolioService.save(original);
		// if res not zero, create a new portfolioItem instance
		if (res != 0.0) {
			PortfolioItem piNew = new PortfolioItem(original.getSymbol(), original.getSiteUser());
			piNew.setStockItem(original.getStockItem());
			piNew.setWarrantSymbol(original.getWarrantSymbol());
			piNew.setIsWarrant(original.getIsWarrant());
			piNew.setIsShort(original.getIsShort());
			piNew.setProfit(null);
			piNew.setIsClosed(false);
			if (piNew.getIsShort()) {
				piNew.setSellPrice(original.getSellPrice());
				piNew.setSellDate(original.getSellDate());
				piNew.setSellQuantity(res);
				piNew.setSellValue(piNew.getSellPrice() * piNew.getSellQuantity());
				piNew.setBuyQuantity(null);
				piNew.setBuyPrice(null);
				piNew.setBuyDate(null);
				piNew.setBuyValue(null);
			} else {
				piNew.setBuyPrice(original.getBuyPrice());
				piNew.setBuyDate(original.getBuyDate());
				piNew.setBuyQuantity(res);
				piNew.setBuyValue(original.getBuyPrice() * original.getBuyQuantity());
				piNew.setSellQuantity(null);
				piNew.setSellPrice(null);
				piNew.setSellDate(null);
				piNew.setSellValue(null);
			}
			portfolioService.save(piNew);
		}
	}

	
	private void manageAndSavePortfolioItem(PortfolioItem pi, PortfolioItem original) {

		original.setBuyPrice(pi.getBuyPrice());
		original.setSellPrice(pi.getSellPrice());
		original.setBuyQuantity(pi.getBuyQuantity());
		original.setSellQuantity(pi.getSellQuantity());
		original.setBuyDate(pi.getBuyDate());
		original.setSellDate(pi.getSellDate());
		original.setIsShort(pi.getIsShort());
		original.setWarrantSymbol(pi.getWarrantSymbol());
		double res = 0.0;
		if (original.getIsShort()) {
			if (original.getBuyQuantity() != null && original.getSellQuantity() != null
					&& original.getBuyQuantity() < original.getSellQuantity()) {
				res = original.getSellQuantity() - original.getBuyQuantity();
				original.setSellQuantity(original.getBuyQuantity());
			}
		} else {
			if (original.getBuyQuantity() != null && original.getSellQuantity() != null
					&& original.getSellQuantity() < original.getBuyQuantity()) {
				res = original.getBuyQuantity() - original.getSellQuantity();
				original.setBuyQuantity(original.getSellQuantity());
			}
		}
		// symbol can not change
		if (original.getBuyPrice() != null && original.getBuyQuantity() != null) {
			original.setBuyValue(original.getBuyPrice() * original.getBuyQuantity());
		} else {
			original.setBuyValue(null);
		}
		if (original.getSellPrice() != null && original.getSellQuantity() != null) {
			original.setSellValue(original.getSellPrice() * original.getSellQuantity());
		} else {
			original.setSellValue(null);
		}
		if (original.getBuyDate() != null && original.getSellDate() != null && original.getSellValue() != null
				&& original.getBuyValue() != null) {
			original.setIsClosed(true);
			original.setProfit(original.getSellValue() - original.getBuyValue());
		}
		//
		if (original.getWarrantSymbol() != null) {
			original.setIsWarrant(true);
		} else {
			original.setIsWarrant(false);
		}
		portfolioService.save(original);
		// if res not zero, create a new portfolioItem instance
		if (res != 0.0) {
			PortfolioItem piNew = new PortfolioItem(original.getSymbol(), original.getSiteUser());
			piNew.setStockItem(original.getStockItem());
			piNew.setWarrantSymbol(original.getWarrantSymbol());
			piNew.setIsWarrant(original.getIsWarrant());
			piNew.setIsShort(original.getIsShort());
			piNew.setProfit(null);
			piNew.setIsClosed(false);
			if (piNew.getIsShort()) {
				piNew.setSellPrice(original.getSellPrice());
				piNew.setSellDate(original.getSellDate());
				piNew.setSellQuantity(res);
				piNew.setSellValue(piNew.getSellPrice() * piNew.getSellQuantity());
				piNew.setBuyQuantity(null);
				piNew.setBuyPrice(null);
				piNew.setBuyDate(null);
				piNew.setBuyValue(null);
			} else {
				piNew.setBuyPrice(original.getBuyPrice());
				piNew.setBuyDate(original.getBuyDate());
				piNew.setBuyQuantity(res);
				piNew.setBuyValue(original.getBuyPrice() * original.getBuyQuantity());
				piNew.setSellQuantity(null);
				piNew.setSellPrice(null);
				piNew.setSellDate(null);
				piNew.setSellValue(null);
			}
			portfolioService.save(piNew);
		}
	}

	/*
	 * delete WatchItem(also delete WatchLog relational items)
	 */
	@GetMapping("/portfolio/{portfolioItemId}/delete")
	public ModelAndView deletePortfolioItem(@PathVariable Long portfolioItemId) {
		SiteUser su = siteUserService.findByUsername("ryan.nieh");
		PortfolioItem pi = portfolioService.getByIdAndSiteUser(portfolioItemId, su);
		portfolioService.delete(pi);
		return new ModelAndView("redirect:" + "/stock/portfolio");
	}
}
