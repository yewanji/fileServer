package com.breakpoint.controller;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartRequest;

@RestController
public class FileUploadController {

	@Value("${file.upload.dir}")
	private String path;

	@RequestMapping("/uploadFile")
	public void uploadFile(HttpServletRequest request) throws IllegalStateException, IOException {
		boolean isMultipart = ServletFileUpload.isMultipartContent(request);
		if (!isMultipart) {
			throw new IllegalArgumentException("请上传文件");
		}
		MultipartRequest multipartRequest = (MultipartRequest) request;
		List<MultipartFile> fileList = multipartRequest.getFiles("uploadAccessory");
		MultipartFile file = fileList.get(0);
		System.out.println("文件大小"+file.getSize()+"请求头信息"+request.getHeader("Range"));
		String originName = file.getOriginalFilename();
		String uuid = UUID.randomUUID().toString().substring(0, 5);
		// 将目标文件写入磁盘
		String targetFilePath = path + File.separator + uuid + originName;
		file.transferTo(new File(targetFilePath));
	}

}
