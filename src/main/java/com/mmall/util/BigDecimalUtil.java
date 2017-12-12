package com.mmall.util;

import java.math.BigDecimal;

public class BigDecimalUtil {
	
	private BigDecimalUtil() {}
	
	/**
	 * double类型数据，加法精确计算
	 * @createUser lishuangqing
	 * @createDate 2017年12月2日
	 * 
	 * @updateDate
	 * @updateUser
	 * @updateComment
	 * 
	 * @param v1 加数
	 * @param v2 被加数
	 * @return 返回double类型相加的BigDecimal类型精确结果
	 */
	public static BigDecimal add(double v1, double v2) {
		BigDecimal b1 = new BigDecimal(Double.toString(v1));
		BigDecimal b2 = new BigDecimal(Double.toString(v2));
		return b1.add(b2);
	}
	
	/**
	 * double类型数据，不丢失精度，相减
	 * @createUser lishuangqing
	 * @createDate 2017年12月2日
	 * 
	 * @updateDate
	 * @updateUser
	 * @updateComment
	 * 
	 * @param v1 被减数
	 * @param v2 减数
	 * @return 返回 v1-v2
	 */
	public static BigDecimal substract(double v1, double v2) {
		BigDecimal b1 = new BigDecimal(Double.toString(v1));
		BigDecimal b2 = new BigDecimal(Double.toString(v2));
		return b1.subtract(b2);
	}
	
	/**
	 * 
	 * @createUser lishuangqing
	 * @createDate 2017年12月2日
	 * 
	 * @updateDate
	 * @updateUser
	 * @updateComment
	 * 
	 * @param v1 被乘数
	 * @param v2 乘数
	 * @return 返回v1 * v2
	 */
	public static BigDecimal multiply(double v1, double v2) {
		BigDecimal b1 = new BigDecimal(Double.toString(v1));
		BigDecimal b2 = new BigDecimal(Double.toString(v2));
		return b1.multiply(b2);
	}
	
	/**
	 * 
	 * @createUser lishuangqing
	 * @createDate 2017年12月2日
	 * 
	 * @updateDate
	 * @updateUser
	 * @updateComment
	 * 
	 * @param v1 被除数
	 * @param v2 除数
	 * @return 返回 v1 / v2
	 */
	public static BigDecimal divide(double v1, double v2) {
		BigDecimal b1 = new BigDecimal(Double.toString(v1));
		BigDecimal b2 = new BigDecimal(Double.toString(v2));
		//保留两位小数，四舍五入
		return b1.divide(b2, 2, BigDecimal.ROUND_HALF_UP);
	}
}
