package com.javatican.stock;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {
	private static final String MAIN_IMG_DIR_PATH = "file:./charts/";
	private static final String STRATEGY_IMG_DIR_PATH = "file:./charts/strategy/";

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/stock/imgs/**").addResourceLocations(MAIN_IMG_DIR_PATH);
		registry.addResourceHandler("/stock/imgs/strategy/**").addResourceLocations(STRATEGY_IMG_DIR_PATH);
	}

}
