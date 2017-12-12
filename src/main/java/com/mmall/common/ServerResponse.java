package com.mmall.common;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * ��������
 * @author lishuangqing
 * @createDate 2017��11��14��
 * @param <T>
 */
//ע�����ã���֤�����л� json ��ʱ�������null����keyҲ����ʧ
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ServerResponse<T> implements Serializable {
	
	private int status;
	private String message;
	private T data;
	
	private ServerResponse(int status) {
		super();
		this.status = status;
	}

	private ServerResponse(int status, String message) {
		super();
		this.status = status;
		this.message = message;
	}

	private ServerResponse(int status, T data) {
		super();
		this.status = status;
		this.data = data;
	}

	private ServerResponse(int status, String message, T data) {
		super();
		this.status = status;
		this.message = message;
		this.data = data;
	}
	
	//ʹ֮����json���л���
	@JsonIgnore
	public boolean isSuccess() {
		return this.status == ResponseCode.SUCCESS.getCode();
	}
	
	public int getStatus() {
		return status;
	}
	public String getMessage() {
		return message;
	}
	public T getData() {
		return data;
	}
	
	/**
	 * ʹ�óɹ�״̬�룬����ʵ��
	 * @createUser lishuangqing
	 * @createDate 2017��11��15��
	 * 
	 * @updateDate
	 * @updateUser
	 * @updateComment
	 * 
	 * @return ����ֻ����״̬���ʵ��
	 */
	public static <T> ServerResponse<T> createBySuccess() {
		return new ServerResponse<T>(ResponseCode.SUCCESS.getCode());
	}
	
	/**
	 * �������гɹ�״̬��ͳɹ���Ϣ��ʵ��
	 * @createUser lishuangqing
	 * @createDate 2017��11��15��
	 * 
	 * @updateDate
	 * @updateUser
	 * @updateComment
	 * 
	 * @param message �ɹ���Ϣ
	 * @return ����ֻ����״̬��ͳɹ���Ϣ��ʵ��
	 */
	public static <T> ServerResponse<T> createBySuccessMessage(String message) {
		return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(), message);
	}
	
	/**
	 * �������гɹ�״̬�룬�Լ��������� ��ʵ������
	 * @createUser lishuangqing
	 * @createDate 2017��11��15��
	 * 
	 * @updateDate
	 * @updateUser
	 * @updateComment
	 * 
	 * @param data T ���� ��������
	 * @return 
	 */
	public static <T> ServerResponse<T> createBySuccess(T data) {
		return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(), data);
	}
	
	/**
	 * ��������״̬�룬�ɹ���Ϣ���������ݵ�ʵ������
	 * @createUser lishuangqing
	 * @createDate 2017��11��15��
	 * 
	 * @updateDate
	 * @updateUser
	 * @updateComment
	 * 
	 * @param message
	 * @param data
	 * @return
	 */
	public static <T> ServerResponse<T> createBySuccess(String message, T data) {
		return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(), message, data);
	}
	
	public static <T> ServerResponse<T> createByError() {
		return new ServerResponse<T>(ResponseCode.ERROR.getCode());
	}
	
	public static <T> ServerResponse<T> createByErrorMessage(String errorMessage) {
		return new ServerResponse<T>(ResponseCode.ERROR.getCode(), errorMessage);
	}
	
	public static <T> ServerResponse<T> createByErrorCodeMessage(int errorCode, String errmsg) {
		return new ServerResponse<T>(errorCode, errmsg);
	}
}
