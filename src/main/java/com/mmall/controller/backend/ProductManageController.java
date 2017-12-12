package com.mmall.controller.backend;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.collect.Maps;
import com.mmall.common.Constants;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;
import com.mmall.pojo.User;
import com.mmall.service.IFileService;
import com.mmall.service.IProductService;
import com.mmall.service.IUserService;
import com.mmall.util.PropertiesUtil;

@Controller
@RequestMapping("/manage/product")
public class ProductManageController {
	
	@Autowired
	private IProductService productService;
	@Autowired
	private IFileService fileService;
	@Autowired
	private IUserService userService;
	
	@RequestMapping(value = "addProduct")
	@ResponseBody
	public ServerResponse productSave(HttpSession session, Product product) {
		User user = (User) session.getAttribute(Constants.SESSION_USER_INFO);
		if (user == null) {
			return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
		}
		if (user.getRole().intValue() != Constants.Role.ROLE_ADMIN) {
			return ServerResponse.createByErrorMessage("该用户没有此权限");
		}
		
		return productService.addOrUpdateProduct(product);
	}
	
	@RequestMapping(value = "set_product_status")
	@ResponseBody
	public ServerResponse setProductStatus(HttpSession session, Integer productId, Integer status) {
		User user = (User) session.getAttribute(Constants.SESSION_USER_INFO);
		if (user == null) {
			return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
		}
		if (user.getRole().intValue() != Constants.Role.ROLE_ADMIN) {
			return ServerResponse.createByErrorMessage("该用户没有此权限");
		}
		
		return productService.setProductStatus(productId, status);
	}
	
	@RequestMapping("list")
    @ResponseBody
	public ServerResponse list(HttpSession session, @RequestParam(value = "pageNum", defaultValue = "1")int pageNum, 
			@RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
		
		return productService.getList(pageNum, pageSize);
	}
	
	@RequestMapping("search.do")
    @ResponseBody
    public ServerResponse productSearch(HttpSession session,String productName,Integer productId, @RequestParam(value = "pageNum",defaultValue = "1") int pageNum,@RequestParam(value = "pageSize",defaultValue = "10") int pageSize){
        
        //填充业务
        return productService.searchProduct(productName,productId,pageNum,pageSize);
        
    }
	
	@RequestMapping(value = "upload", method = RequestMethod.POST)
	@ResponseBody
	public ServerResponse upload(HttpSession session, MultipartFile file, HttpServletRequest request) {
		User user = (User) session.getAttribute(Constants.SESSION_USER_INFO);
		if (user == null) {
			return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
		}
		if (user.getRole().intValue() != Constants.Role.ROLE_ADMIN) {
			return ServerResponse.createByErrorMessage("该用户没有此权限");
		}
		String path = request.getSession().getServletContext().getRealPath("upload");
		String targetName = fileService.upload(file, path);
		String url = PropertiesUtil.getProperty("ftp.server.http.prefix")+targetName;
		
		Map fileMap = Maps.newHashMap();
		fileMap.put("uri", targetName);
		fileMap.put("url", url);
		
		return ServerResponse.createBySuccess(fileMap);
	}
	
	 @RequestMapping("richtext_img_upload.do")
	    @ResponseBody
	    public Map richtextImgUpload(HttpSession session, @RequestParam(value = "upload_file",required = false) MultipartFile file, HttpServletRequest request, HttpServletResponse response){
	        Map resultMap = Maps.newHashMap();
	        User user = (User)session.getAttribute(Constants.SESSION_USER_INFO);
	        if(user == null){
	            resultMap.put("success",false);
	            resultMap.put("msg","请登录管理员");
	            return resultMap;
	        }
	        //富文本中对于返回值有自己的要求,我们使用是simditor所以按照simditor的要求进行返回
//	        {
//	            "success": true/false,
//	                "msg": "error message", # optional
//	            "file_path": "[real file path]"
//	        }
	        if(user.getRole().intValue() == Constants.Role.ROLE_ADMIN){
	            String path = request.getSession().getServletContext().getRealPath("upload");
	            String targetFileName = fileService.upload(file,path);
	            if(StringUtils.isBlank(targetFileName)){
	                resultMap.put("success",false);
	                resultMap.put("msg","上传失败");
	                return resultMap;
	            }
	            String url = PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFileName;
	            resultMap.put("success",true);
	            resultMap.put("msg","上传成功");
	            resultMap.put("file_path",url);
	            response.addHeader("Access-Control-Allow-Headers","X-File-Name");
	            return resultMap;
	        }else{
	            resultMap.put("success",false);
	            resultMap.put("msg","无权限操作");
	            return resultMap;
	        }
	    }
}
