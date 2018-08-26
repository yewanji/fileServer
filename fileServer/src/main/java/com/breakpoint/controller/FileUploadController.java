package com.breakpoint.controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.util.TextUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartRequest;

import com.alibaba.fastjson.JSON;
import com.util.UploadAttachDTO;

@RestController
public class FileUploadController {

	@Value("${file.upload.dir}")
	private String path;

	private ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();

	@RequestMapping("/uploadFile")
	public synchronized void uploadFile(HttpServletRequest request, String uploadAttachStr)
			throws IllegalStateException, IOException {
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		UploadAttachDTO uploadAttachDTO = JSON.parseObject(uploadAttachStr, UploadAttachDTO.class);
		init(uploadAttachDTO.getFileId());
		boolean isMultipart = ServletFileUpload.isMultipartContent(request);
		if (!isMultipart) {
			throw new IllegalArgumentException("请上传文件");
		}
		MultipartRequest multipartRequest = (MultipartRequest) request;
		List<MultipartFile> fileList = multipartRequest.getFiles(uploadAttachDTO.getFileId());
		MultipartFile file = fileList.get(0);
		System.out.println("文件大小" + file.getSize() + "请求头信息" + request.getHeader("Range") + "当前正在上传第"
				+ uploadAttachDTO.getFileIndex());
		// 将目标文件写入磁盘、
		// 将分块的文件建立一个临时文件夹
		String dir = path + File.separator + uploadAttachDTO.getFileId();
		File dirFile = new File(dir);
		if (!dirFile.exists()) {
			dirFile.mkdir();
		}
		String targetFilePath = dir + File.separator + map.get(uploadAttachDTO.getFileId());
		file.transferTo(new File(targetFilePath));

		// 如果分块的文件已经完全上传完毕 则开始整合文件
		if (uploadAttachDTO.getFileIndex() == uploadAttachDTO.getChunkCount()) {
			mergeFiles(path + File.separator + uploadAttachDTO.getFileName(), new File(dir).listFiles());
		}
	}

	public void init(String fileId) {
		if (!map.containsKey(fileId)) {
			map.put(fileId, 0);
		}
		int index = map.get(fileId);
		map.put(fileId, ++index);
	}

	public boolean mergeFiles(String resultPath, File[] targetFiles) {
		File resultFile = new File(resultPath);
		try {
			FileChannel resultFileChannel = new FileOutputStream(resultFile, true).getChannel();
			for (int i = 0; i < targetFiles.length; i++) {
				FileChannel blk = new FileInputStream(targetFiles[i]).getChannel();
				resultFileChannel.transferFrom(blk, resultFileChannel.size(), blk.size());
				blk.close();
			}
			resultFileChannel.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		// 移除临时分块文件
		/*
		 * for (File targetFile : targetFiles) { if (!targetFile.exists()) {
		 * continue; } targetFile.delete(); }
		 */
		// 删除临时文件夹
		File parentTargetFile = targetFiles[0].getParentFile();
		parentTargetFile.delete();
		return true;
	}

	public static boolean mergeFile(String[] fpaths, String resultPath) {
		if (fpaths == null || fpaths.length < 1 || TextUtils.isEmpty(resultPath)) {
			return false;
		}
		if (fpaths.length == 1) {
			return new File(fpaths[0]).renameTo(new File(resultPath));
		}

		File[] files = new File[fpaths.length];
		for (int i = 0; i < fpaths.length; i++) {
			files[i] = new File(fpaths[i]);
			if (TextUtils.isEmpty(fpaths[i]) || !files[i].exists() || !files[i].isFile()) {
				return false;
			}
		}

		File resultFile = new File(resultPath);

		try {
			int bufSize = 1024;
			BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(resultFile));
			byte[] buffer = new byte[bufSize];

			for (int i = 0; i < fpaths.length; i++) {
				BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(files[i]));
				int readcount;
				while ((readcount = inputStream.read(buffer)) > 0) {
					outputStream.write(buffer, 0, readcount);
				}
				inputStream.close();
			}
			outputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		for (int i = 0; i < fpaths.length; i++) {
			files[i].delete();
		}

		return true;
	}

	public static void merge(String from, String to) throws IOException {
		FileOutputStream output = new FileOutputStream(to, true);
		File[] files = new File(from).listFiles();
		for (File file : files) {
			InputStream input = new FileInputStream(file);
			IOUtils.copy(input, output);
			input.close();
		}
		output.flush();
		output.close();
	}
	/*
	 * File t = new File(to); FileInputStream in = null; FileChannel inChannel =
	 * null;
	 * 
	 * FileOutputStream out = new FileOutputStream(t, true); FileChannel
	 * outChannel = out.getChannel();
	 * 
	 * File f = new File(from); // 获取目录下的每一个文件名，再将每个文件一次写入目标文件 if
	 * (f.isDirectory()) { File[] files = f.listFiles(); // 记录新文件最后一个数据的位置 long
	 * start = 0; for (File file : files) {
	 * 
	 * in = new FileInputStream(file); inChannel = in.getChannel();
	 * 
	 * // 从inChannel中读取file.length()长度的数据，写入outChannel的start处
	 * outChannel.transferFrom(inChannel, start, file.length()); start +=
	 * file.length(); in.close(); inChannel.close(); } } out.close();
	 * outChannel.close(); }
	 */
	//合并文件
	public static void main(String[] args) {
		try {
			merge("D:\\breakpoint\\142b9df6-65a2-4565-ba69-66ab3bc3742f", "D:\\breakpoint\\sor.zip");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
