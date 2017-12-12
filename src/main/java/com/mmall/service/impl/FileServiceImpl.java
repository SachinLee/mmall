package com.mmall.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.collect.Lists;
import com.mmall.service.IFileService;
import com.mmall.util.FTPUtil;

@Service
public class FileServiceImpl implements IFileService {

	private static final Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);
	
	@Override
	public String upload(MultipartFile file, String path) {
		String fileName = file.getOriginalFilename(); //获取文件的原始文件名
		
		//获取文件的扩展名
		String fileExtensionName = fileName.substring(fileName.lastIndexOf(".") + 1);
		//创建新的文件名，避免同名
		String uploadFileName = UUID.randomUUID().toString() + "." + fileExtensionName;
		
		logger.info("开始上传文件，文件的上传文件名:{},上传路径:{},新文件名:{}", fileName, path, uploadFileName);
		
		//上传路径是否存在，
		File fileDir = new File(path);
		if (!fileDir.exists()) {
			fileDir.setWritable(true); //添加写入权限
			fileDir.mkdirs(); //创建dir
		}
		File targetFile = new File(path, uploadFileName);
		
		try {
			file.transferTo(targetFile); //文件上传成功
			
			//上传到FTP服务器
			FTPUtil.uploadFile(Lists.newArrayList(targetFile));
			
			//删除上传目录的文件
			targetFile.delete();
			
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			logger.error("上传失败", e);
			e.printStackTrace();
		}
		
		return targetFile.getName();
	}

}
