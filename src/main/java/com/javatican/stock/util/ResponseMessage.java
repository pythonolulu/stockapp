package com.javatican.stock.util;

public class ResponseMessage {
	private String text;
	private String Category;
	public ResponseMessage() {
		super();
	}
	public ResponseMessage(String text, String category) {
		super();
		this.text = text;
		Category = category;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getCategory() {
		return Category;
	}
	public void setCategory(String category) {
		Category = category;
	}
}
