package com.javatican.stock;

public class PrgMain {
	public static void main(String[] args) {
		int i = 0; // 代表兔子數目
		int j = 0; // 代表雞的數目
		int n = 26; // 代表總共的腳的數目
		int m = 8; // 代表總共動物的數目
		for (i = 0; i <= m; i++) {
			j = m - i;
			if (4 * i + 2 * j == n) {
				System.out.println("兔子有" + i + "隻");
				System.out.println("雞有" + j + "隻");
				break;
			}
		}

	}
}
