package com.javatican.stock.util;

public class ResponseMessage {
	private String text;
	private String category;
	private String path;

	public ResponseMessage() {
		super();
	}

	public ResponseMessage(String text, String category, String path) {
		super();
		this.text = text;
		this.category = category;
		this.path = path;
	}

	public ResponseMessage(String path) {
		super();
		this.path = path;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	@Override
	public String toString() {
		return String.format("Category:%s, message:%s, path:%s", this.category, this.text, this.path);
	}
}
