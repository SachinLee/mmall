package com.mmall.common;

public class Constants {
	
	public static final String SESSION_USER_INFO = "user_info";
	
	public static final String USER_NAME = "userName";
	public static final String USER_EMAIL = "userEmail";
	
	public interface Role {
		int ROLE_CUSTOMER = 0;
		int ROLE_ADMIN = 1;
	}
	
	public interface Cart {
		int CHECKED = 1;
		int UN_CHECKED = 0;
		
		String LIMIT_NUM_FAIL = "LIMIT_NUM_FAIL";
        String LIMIT_NUM_SUCCESS = "LIMIT_NUM_SUCCESS";
	}
	
	public enum ProductStatusEnum {
		ON_SALE(1, "在线");
		private String value;
		private int code;
		
		ProductStatusEnum(int code, String value) {
			this.code = code;
			this.value = value;
		}
		
		public String getValue() {
			return this.value;
		}
		public int getCode() {
			return this.code;
		}
	}
	
	public enum OrderStatusEnum {
		CANCLE(0, "已取消"),
		NO_PAY(10, "未支付"),
		PAID(20, "已支付"),
		SHIPPED(40, "已发货"),
		ORDER_SUCCESS(50, "订单完成"),
		ORDER_CLOSE(60, "订单关闭");
		
		private int code;
		private String value;
		
		OrderStatusEnum(int code, String value) {
			this.code = code;
			this.value = value;
		}
		
		public int getCode() {
			return code;
		}
		public String getValue() {
			return value;
		}
		
		public static OrderStatusEnum codeOf(int code) {
			for (OrderStatusEnum orderStatus : values()) {
				if (orderStatus.getCode() == code) {
					return orderStatus;
				}
			}
			throw new RuntimeException("未找到该枚举");
		}
	}
	
	public interface AlipayCallback {
		String TRADE_STATUS_WAIT_BUYER_PAY = "WAIT_BUYER_PAY"; //等待支付
		String TRADE_STATUS_TRADE_SUCCESS = "TRADE_SUCCESS"; //支付成功
		
		String RESPONSE_SUCCESS = "success";
		String RESPONSE_FAILED = "failed";
	}
	
	//支付方式
	public enum PayPlatFormEnum {
		ALIPAY(1, "支付宝支付"),
		WECHATPAY(2, "微信支付");
		private int code;
		private String desc;
		
		PayPlatFormEnum(int code, String desc) {
			this.code = code;
			this.desc = desc;
		}
		
		public int getCode() {
			return code;
		}
		public String getDesc() {
			return desc;
		}
	}
	
	public enum PaymentType {
		ONLINE_PAY(1, "在线支付");
		
		private int code;
		private String desc;
		
		PaymentType(int code, String desc) {
			this.code = code;
			this.desc = desc;
		}
		
		public int getCode() {
			return code;
		}
		public String getDesc() {
			return desc;
		}
		//通过 code 获取 paymentType
		public static PaymentType codeOf(int code) {
			for (PaymentType paymentType : values()) {
				if (paymentType.getCode() == code) {
					return paymentType;
				}
			}
			throw new RuntimeException("未找到对应的枚举");
		}
	}
}
