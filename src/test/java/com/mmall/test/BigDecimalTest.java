package com.mmall.test;

import java.math.BigDecimal;

import org.junit.Test;

public class BigDecimalTest {
	
	@Test
	public void test1() {
		System.out.println(0.05 +0.01);
		System.out.println(1.0 - 0.42);
	}
	
	@Test
	public void test2() {
		BigDecimal b1 = new BigDecimal(0.05);
		BigDecimal b2 = new BigDecimal(0.01);
		System.out.println(b1.add(b2));
	}
	
	@Test
	public void test3() {
		BigDecimal b1 = new BigDecimal("0.05");
		BigDecimal b2 = new BigDecimal("0.01");
		System.out.println(b1.add(b2));
	}
}
