package com.mmall.service.impl;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alipay.api.AlipayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.model.ExtendParams;
import com.alipay.demo.trade.model.GoodsDetail;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.alipay.demo.trade.utils.ZxingUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mmall.common.Constants;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CartMapper;
import com.mmall.dao.OrderItemMapper;
import com.mmall.dao.OrderMapper;
import com.mmall.dao.PayInfoMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.dao.ShippingMapper;
import com.mmall.pojo.Cart;
import com.mmall.pojo.Order;
import com.mmall.pojo.OrderItem;
import com.mmall.pojo.PayInfo;
import com.mmall.pojo.Product;
import com.mmall.pojo.Shipping;
import com.mmall.service.IOrderService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.FTPUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.OrderItemVo;
import com.mmall.vo.OrderProductVo;
import com.mmall.vo.OrderVo;
import com.mmall.vo.ShippingVo;

@Service
@Transactional
public class OrderServiceImpl implements IOrderService {
	
	private static  AlipayTradeService tradeService;
    static {

        /** 一定要在创建AlipayTradeService之前调用Configs.init()设置默认参数
         *  Configs会读取classpath下的zfbinfo.properties文件配置信息，如果找不到该文件则确认该文件是否在classpath目录
         */
        Configs.init("zfbinfo.properties");

        /** 使用Configs提供的默认参数
         *  AlipayTradeService可以使用单例或者为静态成员对象，不需要反复new
         */
        tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();
    }
	
	private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);
	
	@Autowired
	private OrderMapper orderMapper;
	@Autowired
	private OrderItemMapper orderItemMapper;
	@Autowired
	private PayInfoMapper payInfoMapper;
	@Autowired
	private CartMapper cartMapper;
	@Autowired
	private ProductMapper productMapper;
	@Autowired
	private ShippingMapper shippingMapper;
	
	@Override
	public ServerResponse pay(Long orderNo, Integer userId, String path) {
		//构建数据返回的map
		Map<String, String> resultMap = Maps.newHashMap();
		//查询订单信息
		Order order = orderMapper.selectByOrderNoAndUserId(orderNo, userId);
		//如果该用户没有该订单，返回错误信息
		if (order == null) {
			return ServerResponse.createByErrorMessage("用户没有该订单");
		}
		resultMap.put("orderNo", order.getOrderNo().toString());
		
		//复制支付宝demo中，生成二维码部分的代码，然后修改
		Map<String, String> trade = tradePrecreate(order, userId, path);
		String tradeStatus = trade.get("status");
        if (tradeStatus.equals("success")) {
        	resultMap.put("qrUrl", trade.get("url"));
    		return ServerResponse.createBySuccess("支付成功", resultMap);
        } else {
        	return ServerResponse.createByErrorMessage(trade.get("url"));
        }
		
	}
	
	private Map<String, String> tradePrecreate(Order order, Integer userId, String path) {
		Map<String, String> resultTrade = Maps.newHashMap();
		// (必填) 商户网站订单系统中唯一订单号，64个字符以内，只能包含字母、数字、下划线，
        // 需保证商户系统端不能重复，建议通过数据库sequence生成，
        String outTradeNo = order.getOrderNo().toString();

        // (必填) 订单标题，粗略描述用户的支付目的。如“xxx品牌xxx门店当面付扫码消费”
        String subject = new StringBuilder().append("订单号：").append(outTradeNo).toString();

        // (必填) 订单总金额，单位为元，不能超过1亿元
        // 如果同时传入了【打折金额】,【不可打折金额】,【订单总金额】三者,则必须满足如下条件:【订单总金额】=【打折金额】+【不可打折金额】
        String totalAmount = order.getPayment().toString();

        // (可选) 订单不可打折金额，可以配合商家平台配置折扣活动，如果酒水不参与打折，则将对应金额填写至此字段
        // 如果该值未传入,但传入了【订单总金额】,【打折金额】,则该值默认为【订单总金额】-【打折金额】
        String undiscountableAmount = "0";

        // 卖家支付宝账号ID，用于支持一个签约账号下支持打款到不同的收款账号，(打款到sellerId对应的支付宝账号)
        // 如果该字段为空，则默认为与支付宝签约的商户的PID，也就是appid对应的PID
        String sellerId = "";

        // 订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品2件共15.00元"
        String body = new StringBuilder().append("订单：").append(outTradeNo)
        		.append("购买商品共").append(totalAmount).append("元").toString();

        // 商户操作员编号，添加此参数可以为商户操作员做销售统计
        String operatorId = "test_operator_id";

        // (必填) 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
        String storeId = "test_store_id";

        // 业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
        ExtendParams extendParams = new ExtendParams();
        extendParams.setSysServiceProviderId("2088100200300400500");

        // 支付超时，定义为120分钟
        String timeoutExpress = "120m";

        // 商品明细列表，需填写购买商品详细信息，
        List<GoodsDetail> goodsDetailList = new ArrayList<GoodsDetail>();
        
        List<OrderItem> orderItemList = orderItemMapper.getByOrderNoAndUserId(order.getOrderNo(), userId);
        for (OrderItem orderItem : orderItemList) {
        	// 创建一个商品信息，参数含义分别为商品id（使用国标）、名称、单价（单位为分）、数量，如果需要添加商品类别，详见GoodsDetail
        	//GoodsDetail goods1 = GoodsDetail.newInstance("goods_id001", "xxx小面包", 1000, 1);
        	GoodsDetail goods = GoodsDetail.newInstance(orderItem.getProductId().toString(), orderItem.getProductName(), 
        			BigDecimalUtil.multiply(orderItem.getCurrentUnitPrice().doubleValue(), new Double(100).doubleValue()).longValue(), 
        			orderItem.getQuantity());
        	goodsDetailList.add(goods);
        }
        
        
        /*// 创建一个商品信息，参数含义分别为商品id（使用国标）、名称、单价（单位为分）、数量，如果需要添加商品类别，详见GoodsDetail
        GoodsDetail goods1 = GoodsDetail.newInstance("goods_id001", "xxx小面包", 1000, 1);
        // 创建好一个商品后添加至商品明细列表
        goodsDetailList.add(goods1);

        // 继续创建并添加第一条商品信息，用户购买的产品为“黑人牙刷”，单价为5.00元，购买了两件
        GoodsDetail goods2 = GoodsDetail.newInstance("goods_id002", "xxx牙刷", 500, 2);
        goodsDetailList.add(goods2);*/

        // 创建扫码支付请求builder，设置请求参数
        AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder()
            .setSubject(subject).setTotalAmount(totalAmount).setOutTradeNo(outTradeNo)
            .setUndiscountableAmount(undiscountableAmount).setSellerId(sellerId).setBody(body)
            .setOperatorId(operatorId).setStoreId(storeId).setExtendParams(extendParams)
            .setTimeoutExpress(timeoutExpress)
            .setNotifyUrl(PropertiesUtil.getProperty("alipay.callback.url"))//支付宝服务器主动通知商户服务器里指定的页面http路径,根据需要设置
            .setGoodsDetailList(goodsDetailList);
        
        AlipayF2FPrecreateResult result = tradeService.tradePrecreate(builder);
        switch (result.getTradeStatus()) {
            case SUCCESS:
            	logger.info("支付宝预下单成功: )");

                AlipayTradePrecreateResponse response = result.getResponse();
                dumpResponse(response);

                File folder = new File(path);
                if (!folder.exists()) {
                	folder.setWritable(true);
                	folder.mkdirs();
                }
                
                // 需要修改为运行机器上的路径
                String qrPath = String.format(path+"/qr-%s.png",
                    response.getOutTradeNo());
                String qrName = String.format("qr-%s.png", response.getOutTradeNo());
                ZxingUtils.getQRCodeImge(response.getQrCode(), 256, qrPath);
                
                File targetFile = new File(path, qrName);
                
				try {
					FTPUtil.uploadFile(Lists.newArrayList(targetFile));
				} catch (IOException e) {
					logger.error("上传失败", e);
				}
                
                logger.info("qrPath:" + qrPath);
                
                String qrUrl = PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFile.getName();
                
                
                //                ZxingUtils.getQRCodeImge(response.getQrCode(), 256, filePath);
                resultTrade.put("status", "success");
                resultTrade.put("url", qrUrl);
                return resultTrade;

            case FAILED:
            	logger.error("支付宝预下单失败!!!");
            	resultTrade.put("status", "error");
                resultTrade.put("url", "支付宝预下单失败!!!");
                return resultTrade;

            case UNKNOWN:
            	logger.error("系统异常，预下单状态未知!!!");
            	resultTrade.put("status", "error");
                resultTrade.put("url", "系统异常，预下单状态未知!!!");
                return resultTrade;

            default:
            	logger.error("不支持的交易状态，交易返回异常!!!");
            	resultTrade.put("status", "error");
                resultTrade.put("url", "不支持的交易状态，交易返回异常!!!");
                return resultTrade;
        }
	}

	// 简单打印应答
    private void dumpResponse(AlipayResponse response) {
        if (response != null) {
            logger.info(String.format("code:%s, msg:%s", response.getCode(), response.getMsg()));
            if (StringUtils.isNotEmpty(response.getSubCode())) {
                logger.info(String.format("subCode:%s, subMsg:%s", response.getSubCode(),
                    response.getSubMsg()));
            }
            logger.info("body:" + response.getBody());
        }
    }

	@Override
	public ServerResponse alipayCallback(Map<String, String> params) {
		//获取订单号
		Long orderNo = Long.parseLong(params.get("out_trade_no"));
		//获取支付宝交易号
		String  tradeNo = params.get("trade_no");
		//获取交易状态
		String tradeStatus = params.get("trade_status");
		
		Order order = orderMapper.selectByOrderNo(orderNo);
		if (order == null) {
			return ServerResponse.createByErrorMessage("订单不存在，回调忽略");
		}
		if (order.getStatus() >= Constants.OrderStatusEnum.PAID.getCode()) {
			return ServerResponse.createBySuccessMessage("支付宝重复调用");
		}
		
		if (Constants.AlipayCallback.TRADE_STATUS_TRADE_SUCCESS.equals(tradeStatus)) {
			//获取交易时间
			order.setPaymentTime(DateTimeUtil.strToDate(params.get("gmt_payment")));
			order.setStatus(Constants.OrderStatusEnum.PAID.getCode());
			orderMapper.updateByPrimaryKeySelective(order);
		}
		
		PayInfo payInfo = new PayInfo();
		payInfo.setUserId(order.getUserId());
		payInfo.setOrderNo(order.getOrderNo());
		//支付方式
		payInfo.setPayPlatform(Constants.PayPlatFormEnum.ALIPAY.getCode());
		//支付宝交易号
		payInfo.setPlatformNumber(tradeNo);
		//交易状态
		payInfo.setPlatformStatus(tradeStatus);
		
		payInfoMapper.insert(payInfo);
		
		return ServerResponse.createBySuccess();
	}

	@Override
	public ServerResponse queryOrderPayStatus(Integer userId, Long orderNo) {
		Order order = orderMapper.selectByOrderNoAndUserId(orderNo, userId);
		if (order == null) {
			return ServerResponse.createByErrorMessage("用户没有该订单");
		}
		if (order.getStatus() >= Constants.OrderStatusEnum.PAID.getCode()) {
			return ServerResponse.createBySuccess();
		}
		
		return ServerResponse.createByError();
	}

	@Override
	public ServerResponse createOrder(Integer userId, Integer shippingId) {
		//1.获取购物车中的已选中的内容
		List<Cart> cartList = cartMapper.selectCheckedCartByUserId(userId);
		//2.通过购物车，获取订单明细
		ServerResponse serverResponse = this.getCartOrderItem(userId, cartList);
		if (!serverResponse.isSuccess()) {
			return serverResponse;
		}
		List<OrderItem> orderItemList = (List<OrderItem>) serverResponse.getData();
		//获取订单的总价
		BigDecimal payment = this.getOrderTotalPrice(orderItemList);
		
		//生成订单
		Order order = this.assembleOrder(userId, shippingId, payment);
		if (order == null) {
			return ServerResponse.createByErrorMessage("创建订单失败");
		}
		if (CollectionUtils.isEmpty(orderItemList)) {
			return ServerResponse.createByErrorMessage("购物车为空");
		}
		//给每一个订单详情，赋值订单
		for (OrderItem orderItem : orderItemList) {
			orderItem.setOrderNo(order.getOrderNo());
		}
		//批量插入
		orderItemMapper.batchInsert(orderItemList);
		
		//生成成功，减少库存，情况购物车
		this.reduceProductStock(orderItemList);
		this.cleanCart(cartList);
		
		OrderVo orderVo = assembleOrderVo(order,orderItemList);
	    return ServerResponse.createBySuccess(orderVo);
	}
	
	private OrderVo assembleOrderVo(Order order, List<OrderItem> orderItemList) {
		OrderVo orderVo = new OrderVo();
		
		orderVo.setOrderNo(order.getOrderNo());
		orderVo.setPayment(order.getPayment());
		orderVo.setPaymentType(order.getPaymentType());
		orderVo.setPaymentTypeDesc(Constants.PaymentType.codeOf(order.getPaymentType()).getDesc());
		orderVo.setPostage(order.getPostage());
		
		orderVo.setStatus(order.getStatus());
		orderVo.setStatusDesc(Constants.OrderStatusEnum.codeOf(order.getStatus()).getValue());
		
		orderVo.setShippingId(order.getShippingId());
		
		Shipping shipping = shippingMapper.selectByPrimaryKey(order.getShippingId());
		if (shipping != null) {
			orderVo.setReceiverName(shipping.getReceiverAddress());
			orderVo.setShippingVo(this.assembleShippingVo(shipping));
		}
		
		orderVo.setPaymentTime(DateTimeUtil.dateToStr(order.getPaymentTime()));
		orderVo.setSendTime(DateTimeUtil.dateToStr(order.getSendTime()));
		orderVo.setEndTime(DateTimeUtil.dateToStr(order.getEndTime()));
		orderVo.setCreateTime(DateTimeUtil.dateToStr(order.getCreateTime()));
		orderVo.setCloseTime(DateTimeUtil.dateToStr(order.getCloseTime()));
		
		orderVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
		
		List<OrderItemVo> orderItemVoList = Lists.newArrayList();
		for (OrderItem orderItem : orderItemList) {
			OrderItemVo orderItemVo = this.assembleOrderItemVo(orderItem);
			orderItemVoList.add(orderItemVo);
		}
		orderVo.setOrderItemVoList(orderItemVoList);
		
		return orderVo;
	}
	
	private OrderItemVo assembleOrderItemVo(OrderItem orderItem) {
		OrderItemVo orderItemVo = new OrderItemVo();
		
		orderItemVo.setOrderNo(orderItem.getOrderNo());
        orderItemVo.setProductId(orderItem.getProductId());
        orderItemVo.setProductName(orderItem.getProductName());
        orderItemVo.setProductImage(orderItem.getProductImage());
        orderItemVo.setCurrentUnitPrice(orderItem.getCurrentUnitPrice());
        orderItemVo.setQuantity(orderItem.getQuantity());
        orderItemVo.setTotalPrice(orderItem.getTotalPrice());

        orderItemVo.setCreateTime(DateTimeUtil.dateToStr(orderItem.getCreateTime()));
		
		return orderItemVo;
	}
	
	private ShippingVo assembleShippingVo(Shipping shipping) {
		ShippingVo shippingVo = new ShippingVo();
		
		BeanUtils.copyProperties(shipping, shippingVo);
//		shippingVo.setReceiverAddress(shipping.getReceiverAddress());
		
		return shippingVo;
	}
	
	private void cleanCart(List<Cart> cartList) {
		for (Cart cartItem : cartList) {
			cartMapper.deleteByPrimaryKey(cartItem.getId());
		}
	}
	
	//减少产品库存
	private void reduceProductStock(List<OrderItem> orderItemList) {
		for (OrderItem orderItem : orderItemList) {
			Product product = productMapper.selectByPrimaryKey(orderItem.getProductId());
			product.setStock(product.getStock() - orderItem.getQuantity());
			productMapper.updateByPrimaryKeySelective(product);
		}
	}
	
	//生成订单
	private Order assembleOrder(Integer userId, Integer shippingId, BigDecimal payment) {
		Order order = new Order();
		long orderNo = this.assembleOrderNo();
		
		order.setOrderNo(orderNo);
		order.setStatus(Constants.OrderStatusEnum.NO_PAY.getCode());
		order.setPostage(0); //运费
		order.setPaymentType(Constants.PaymentType.ONLINE_PAY.getCode()); //支付方式
		order.setPayment(payment);
		
		order.setUserId(userId);
		order.setShippingId(shippingId);
		//发货时间，付款时间
		
		int rowCount = orderMapper.insert(order);
		if (rowCount > 0) {
			return order;
		}
		return null;
	}
	//生成订单号
	private long assembleOrderNo() {
		long currentTime = System.currentTimeMillis();
		return currentTime + new Random().nextInt(100);
	}
	
	//获取订单总价
	private BigDecimal getOrderTotalPrice(List<OrderItem> orderItemList) {
		BigDecimal payment = new BigDecimal("0");
		for (OrderItem orderItem : orderItemList) {
			payment = BigDecimalUtil.add(payment.doubleValue(), orderItem.getTotalPrice().doubleValue());
		}
		return payment;
	}
	
	//通过购物车，获取订单详情
	private ServerResponse<List<OrderItem>> getCartOrderItem(Integer userId, List<Cart> cartList) {
		List<OrderItem> orderItemList = Lists.newArrayList();
		if (CollectionUtils.isEmpty(cartList)) {
			return ServerResponse.createBySuccessMessage("购物车为空");
		}
		//校验购物车中的数据，
		for (Cart cartItem : cartList) {
			OrderItem orderItem = new OrderItem();
			Product product = productMapper.selectByPrimaryKey(cartItem.getProductId());
			//如果该商品的状态不是在售卖中
			if (Constants.ProductStatusEnum.ON_SALE.getCode() != product.getStatus()) {
				return ServerResponse.createByErrorMessage("产品" + product.getName() + "不是在售卖");
			}
			//校验库存
			if (cartItem.getQuantity() > product.getStock()) {
				return ServerResponse.createByErrorMessage("产品" + product.getName() + "库存不足");
			}
			
			orderItem.setUserId(userId);
			orderItem.setProductId(product.getId());
			orderItem.setProductName(product.getName());
			orderItem.setProductImage(product.getMainImage());
			orderItem.setCurrentUnitPrice(product.getPrice());
			orderItem.setQuantity(cartItem.getQuantity());
			orderItem.setTotalPrice(BigDecimalUtil.multiply(product.getPrice().doubleValue(), cartItem.getQuantity().doubleValue()));
			
			orderItemList.add(orderItem);
		}
		
		return ServerResponse.createBySuccess(orderItemList);
	}

	@Override
	public ServerResponse<String> cancleOrder(Integer userId, Long orderNo) {
		Order order = orderMapper.selectByOrderNoAndUserId(orderNo, userId);
		if (order == null) {
			return ServerResponse.createByErrorMessage("该订单不存在");
		}
		
		Integer orderStatus = order.getStatus();
		if (orderStatus != Constants.OrderStatusEnum.NO_PAY.getCode()) {
			return ServerResponse.createByErrorMessage("已付款，不能取消订单");
		}
		
		Order updateOrder = new Order();
		updateOrder.setId(order.getId());
		updateOrder.setStatus(Constants.OrderStatusEnum.CANCLE.getCode());
		
		int rowCount = orderMapper.updateByPrimaryKeySelective(updateOrder);
		if (rowCount > 0) {
			return ServerResponse.createBySuccess();
		}
		return ServerResponse.createByError();
	}

	@Override
	public ServerResponse getOrderCartProduct(Integer userId) {
		OrderProductVo orderProductVo = new OrderProductVo();
        //从购物车中获取数据

        List<Cart> cartList = cartMapper.selectCheckedCartByUserId(userId);
        ServerResponse serverResponse =  this.getCartOrderItem(userId,cartList);
        if(!serverResponse.isSuccess()){
            return serverResponse;
        }
        List<OrderItem> orderItemList =( List<OrderItem> ) serverResponse.getData();

        List<OrderItemVo> orderItemVoList = Lists.newArrayList();

        BigDecimal payment = new BigDecimal("0");
        for(OrderItem orderItem : orderItemList){
            payment = BigDecimalUtil.add(payment.doubleValue(),orderItem.getTotalPrice().doubleValue());
            orderItemVoList.add(assembleOrderItemVo(orderItem));
        }
        orderProductVo.setProductTotalPrice(payment);
        orderProductVo.setOrderItemVoList(orderItemVoList);
        orderProductVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        return ServerResponse.createBySuccess(orderProductVo);
	}

	@Override
	public ServerResponse<OrderVo> detail(Integer userId, Long orderNo) {
		Order order = orderMapper.selectByOrderNoAndUserId(orderNo, userId);
		if (order == null) {
			return ServerResponse.createByErrorMessage("订单不存在");
		}
		
		List<OrderItem> orderItemList = orderItemMapper.getByOrderNoAndUserId(orderNo, userId);
		OrderVo orderVo = this.assembleOrderVo(order, orderItemList);
		
		return ServerResponse.createBySuccess(orderVo);
	}

	@Override
	public ServerResponse<PageInfo> list(Integer userId, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		List<Order> orderList = orderMapper.selectByUserId(userId);
		
		List<OrderVo> orderVoList = this.assembleOrderVoList(orderList, userId);
		
		PageInfo pageInfo = new PageInfo(orderList);
		pageInfo.setList(orderVoList);
		
		return ServerResponse.createBySuccess(pageInfo);
	}
	
	private List<OrderVo> assembleOrderVoList(List<Order> orderList,Integer userId){
        List<OrderVo> orderVoList = Lists.newArrayList();
        for(Order order : orderList){
            List<OrderItem>  orderItemList = Lists.newArrayList();
            if(userId == null){
                //todo 管理员查询的时候 不需要传userId
                orderItemList = orderItemMapper.getByOrderNo(order.getOrderNo());
            }else{
                orderItemList = orderItemMapper.getByOrderNoAndUserId(order.getOrderNo(),userId);
            }
            OrderVo orderVo = assembleOrderVo(order,orderItemList);
            orderVoList.add(orderVo);
        }
        return orderVoList;
    }

	@Override
	public ServerResponse<PageInfo> manageList(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		List<Order> orderList = orderMapper.findAll();
		
		List<OrderVo> orderVoList = this.assembleOrderVoList(orderList, null);
		
		PageInfo pageResult = new PageInfo(orderList);
		pageResult.setList(orderVoList);
		return ServerResponse.createBySuccess(pageResult);
	}

	@Override
	public ServerResponse<OrderVo> manageDetail(Long orderNo) {
		Order order = orderMapper.selectByOrderNo(orderNo);
		if (order == null) {
			return ServerResponse.createByErrorMessage("该订单不存在");
		}
		List<OrderItem> orderItemList = orderItemMapper.getByOrderNo(orderNo);
		
		OrderVo orderVo = this.assembleOrderVo(order, orderItemList);
		
		return ServerResponse.createBySuccess(orderVo);
	}

	@Override
	public ServerResponse<PageInfo> manageSearch(Long orderNo, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		Order order = orderMapper.selectByOrderNo(orderNo);
		if (order == null) {
			return ServerResponse.createByErrorMessage("该订单不存在");
		}
		List<OrderItem> orderItemList = orderItemMapper.getByOrderNo(orderNo);
		
		OrderVo orderVo = this.assembleOrderVo(order, orderItemList);
		
		PageInfo pageResult = new PageInfo(Lists.newArrayList(order));
		pageResult.setList(Lists.newArrayList(orderVo));
		
		return ServerResponse.createBySuccess(pageResult);
	}

	@Override
	public ServerResponse<String> sendGoods(Long orderNo) {
		Order order = orderMapper.selectByOrderNo(orderNo);
		if (order == null) {
			return ServerResponse.createByErrorMessage("该订单不存在");
		}
		if (order.getStatus() == Constants.OrderStatusEnum.PAID.getCode()) {
			order.setStatus(Constants.OrderStatusEnum.SHIPPED.getCode());
			order.setSendTime(new Date());
			orderMapper.updateByPrimaryKeySelective(order);
		}
		
		return ServerResponse.createBySuccess("发货成功");
	}
	
	
}
