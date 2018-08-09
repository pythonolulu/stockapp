package com.javatican.stock.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.javatican.stock.model.PortfolioItem;
import com.javatican.stock.model.StockItem;
import com.javatican.stock.service.StockItemService;

@Component("portfolioItemValidator")
public class PortfolioItemValidator implements Validator {
	@Autowired
	StockItemService stockItemService;
	@Override
	public boolean supports(Class<?> clazz) {
		return PortfolioItem.class.equals(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		PortfolioItem pi = (PortfolioItem) target;
		if(pi.getId()==null) {
			if(pi.getSymbol()!=null) {
				StockItem si = stockItemService.getStockItem(pi.getSymbol());
				if(si==null) {
					errors.rejectValue("symbol", "stockSymbolNotExist.pi.symbol");
				}
			}
		}
		if (pi.getIsShort()) {
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "sellPrice", "NotNull.pi.sellPrice");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "sellDate", "NotNull.pi.sellDate");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "sellQuantity", "NotNull.pi.sellQuantity");
			if (pi.getBuyDate() != null || pi.getBuyPrice() != null || pi.getBuyQuantity() != null) {
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "buyPrice", "NotNull.pi.buyPrice");
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "buyDate", "NotNull.pi.buyDate");
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "buyQuantity", "NotNull.pi.buyQuantity");
			}
			if (pi.getSellQuantity() != null && pi.getBuyQuantity() != null && pi.getSellQuantity() < pi.getBuyQuantity()) {
				errors.rejectValue("buyQuantity", "BuyQuantityTooLarge.pi.buyQuantity");
			}
			if(pi.getBuyDate() != null && pi.getSellDate() != null && pi.getSellDate().after(pi.getBuyDate())) {
				errors.rejectValue("buyDate", "buyDateBeforeSellDate.pi.buyDate");
			}
		} else {
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "buyPrice", "NotNull.pi.buyPrice");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "buyDate", "NotNull.pi.buyDate");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "buyQuantity", "NotNull.pi.buyQuantity");
			if (pi.getSellDate() != null || pi.getSellPrice() != null || pi.getSellQuantity() != null) {
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "sellPrice", "NotNull.pi.sellPrice");
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "sellDate", "NotNull.pi.sellDate");
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "sellQuantity", "NotNull.pi.sellQuantity");
			}
			if (pi.getBuyQuantity() != null && pi.getSellQuantity() != null && pi.getBuyQuantity() < pi.getSellQuantity()) {
				errors.rejectValue("sellQuantity", "SellQuantityTooLarge.pi.sellQuantity");
			}
			if(pi.getBuyDate() != null && pi.getSellDate() != null && pi.getBuyDate().after(pi.getSellDate())) {
				errors.rejectValue("sellDate", "sellDateBeforeBuyDate.pi.sellDate");
			}
			//TODO check isShort and isWarrant
		}

	}
}