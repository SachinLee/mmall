package com.mmall.service.impl;

import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mmall.common.Constants;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CartMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Cart;
import com.mmall.pojo.Product;
import com.mmall.service.ICartService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.CartProductVo;
import com.mmall.vo.CartVo;

@Service
public class CartServiceImpl implements ICartService {

	@Autowired
	private CartMapper cartMapper;
	@Autowired
	private ProductMapper productMapper;
	
	@Override
	public ServerResponse<CartVo> addProductToCart(Integer userId, Integer productId, Integer count) {
		if (productId == null || count == null) {
			return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
		}
		
		Cart cart = cartMapper.selectCartByUserIdAndProductId(userId, productId);
		if (cart == null) { 
			//如果购物车中，没有当前添加的商品，新增商品记录
			Cart cartItem = new Cart();
			cartItem.setUserId(userId);
			cartItem.setProductId(productId);
			cartItem.setChecked(Constants.Cart.CHECKED);
			cartItem.setQuantity(count);
			
			cartMapper.insert(cartItem);
		} else {
			//如果已经存在，相加
			count = cart.getQuantity() + count;
			cart.setQuantity(count);
			cartMapper.updateByPrimaryKeySelective(cart);
		}
		
		return this.list(userId);
	}
	
	@Override
	public ServerResponse<CartVo> list (Integer userId){
        CartVo cartVo = this.getCartVoLimit(userId);
        return ServerResponse.createBySuccess(cartVo);
    }

	private CartVo getCartVoLimit(Integer userId) {
		CartVo cartVo = new CartVo();
		List<Cart> cartList = cartMapper.selectCartByUserId(userId);
		
		List<CartProductVo> cartProductVoList = Lists.newArrayList();
		BigDecimal cartTotalPrice = new BigDecimal("0");
		
		if (CollectionUtils.isNotEmpty(cartList)) {
			for (Cart cartItem : cartList) {
				CartProductVo cartProductVo = new CartProductVo();
				cartProductVo.setId(cartItem.getId());
				cartProductVo.setUserId(cartItem.getUserId());
				cartProductVo.setProductId(cartItem.getProductId());
				
				Product product = productMapper.selectByPrimaryKey(cartItem.getProductId());
				if (product != null) {
					cartProductVo.setProductMainImage(product.getMainImage());
					cartProductVo.setProductName(product.getName());
					cartProductVo.setProductSubtitle(product.getSubtitle());
                    cartProductVo.setProductStatus(product.getStatus());
                    cartProductVo.setProductPrice(product.getPrice());
                    cartProductVo.setProductStock(product.getStock());
                    //判断库存
                    int buyLimitCount = 0;
                    if (product.getStock() >= cartItem.getQuantity()) {
                    	buyLimitCount = cartItem.getQuantity();
                    	cartProductVo.setLimitQuantity(Constants.Cart.LIMIT_NUM_SUCCESS);
                    } else {
                    	buyLimitCount = product.getStock();
                    	cartProductVo.setLimitQuantity(Constants.Cart.LIMIT_NUM_FAIL);
                    	//购物车更新有效库存
                    	Cart cartForQuantity = new Cart();
                    	cartForQuantity.setId(cartItem.getId());
                    	cartForQuantity.setQuantity(buyLimitCount);
                    	cartMapper.updateByPrimaryKeySelective(cartForQuantity);
                    }
                    cartProductVo.setQuantity(buyLimitCount);
                    //计算总价
                    cartProductVo.setProductTotalPrice(BigDecimalUtil.multiply(product.getPrice().doubleValue(), cartProductVo.getQuantity()));
                    cartProductVo.setProductChecked(cartItem.getChecked());
				}
				if (cartItem.getChecked() == Constants.Cart.CHECKED) {
					//如果已经勾选,增加到整个的购物车总价中
                    cartTotalPrice = BigDecimalUtil.add(cartTotalPrice.doubleValue(),cartProductVo.getProductTotalPrice().doubleValue());
				}
				cartProductVoList.add(cartProductVo);
			}
		}
		cartVo.setCartTotalPrice(cartTotalPrice);
        cartVo.setCartProductVoList(cartProductVoList);
        cartVo.setAllChecked(this.getAllCheckedStatus(userId));
        cartVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));

        return cartVo;
	}
	
	private boolean getAllCheckedStatus(Integer userId){
        if(userId == null){
            return false;
        }
        return cartMapper.selectCartProductCheckedStatusByUserId(userId) == 0;

    }

	@Override
	public ServerResponse<CartVo> updateProductToCart(Integer userId, Integer productId, Integer count) {
		if (productId == null || count == null) {
			return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
		}
		Cart cart = cartMapper.selectCartByUserIdAndProductId(userId, productId);
		if (cart != null) {
			cart.setQuantity(count);
		}
		cartMapper.updateByPrimaryKeySelective(cart);
		
		return this.list(userId);
	}

	@Override
	public ServerResponse<CartVo> delete(Integer userId, String productIds) {
		List<String> productList = Splitter.on(",").splitToList(productIds);
		if (CollectionUtils.isEmpty(productList)) {
			return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
		}
		cartMapper.deleteCartByUserIdAndProductId(userId, productList);
		return this.list(userId);
	}
}
