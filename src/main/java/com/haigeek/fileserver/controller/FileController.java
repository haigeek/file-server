package com.haigeek.fileserver.controller;

import com.haigeek.fileserver.base.response.ResponseData;
import com.haigeek.fileserver.base.response.ResponseUtil;
import com.haigeek.fileserver.model.MyFile;
import com.haigeek.fileserver.service.FileService;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600) // 允许所有域名访问
@RestController
public class FileController {

	@Autowired
	private FileService fileService;

	@Resource
	private MongoDbFactory mongoDbFactory;

	@Value("${server.address}")
	private String serverAddress;

	@Value("${server.port}")
	private String serverPort;

	@RequestMapping(value = "/file/all")
	public ResponseData index() {
		// 展示最新二十条数据
		List<MyFile> myFiles=fileService.getAllFile();
		return ResponseUtil.success(myFiles);
	}

	/**
	 * 分页查询文件
	 * 
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 */
	@GetMapping("files/{pageIndex}/{pageSize}")
	@ResponseBody
	public List<MyFile> listFilesByPage(@PathVariable int pageIndex, @PathVariable int pageSize) {
		return fileService.listFilesByPage(pageIndex, pageSize);
	}

	/**
	 * 下载文件
	 * 
	 * @param id
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	@GetMapping("file/{id}")
	@ResponseBody
	public void downloadFile(@PathVariable String id, HttpServletRequest request, HttpServletResponse response) throws IOException {
		GridFSFile gridFSFile=fileService.getFileById(id);
		GridFsResource gridFsResource=new GridFsResource(gridFSFile,
				GridFSBuckets.create(mongoDbFactory.getDb()).openDownloadStream(gridFSFile.getId()));
		String fileName = gridFSFile.getFilename().replace(",", "");
		//处理中文文件名乱码
		if (request.getHeader("User-Agent").toUpperCase().contains("MSIE") ||
				request.getHeader("User-Agent").toUpperCase().contains("TRIDENT")
				|| request.getHeader("User-Agent").toUpperCase().contains("EDGE")) {
			fileName = java.net.URLEncoder.encode(fileName, "UTF-8");
		} else {
			//非IE浏览器的处理：
			fileName = new String(fileName.getBytes("UTF-8"), "ISO-8859-1");
		}
		// 通知浏览器进行文件下载
		response.setHeader("Content-Disposition", "attachment;filename=\"" + fileName + "\"");
		IOUtils.copy(gridFsResource.getInputStream(),response.getOutputStream());

	}

	/**
	 * 预览文件
	 * 
	 * @param id
	 * @return
	 */
	@GetMapping("/view/{id}")
	@ResponseBody
	public ResponseData serveFileOnline(@PathVariable String id,HttpServletResponse response) throws IOException {

		GridFSFile gridFSFile = fileService.getFileById(id);

		if (gridFSFile != null) {
			GridFsResource gridFsResource = new GridFsResource(gridFSFile,
					GridFSBuckets.create(mongoDbFactory.getDb()).openDownloadStream(gridFSFile.getId()));
			String fileName = gridFSFile.getFilename().replace(",", "");
			response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "fileName=\"" + fileName + "\"");
			response.addHeader(HttpHeaders.CONTENT_LENGTH, gridFSFile.getLength() + "");
			response.addHeader("Connection", "close");
			IOUtils.copy(gridFsResource.getInputStream(), response.getOutputStream());
		} else {
			return ResponseUtil.fail("File was not fount");
		}
		return null;
	}

	/**
	 * 上传接口
	 * 
	 * @param file
	 * @return
	 */
	@PostMapping("/upload")
	@ResponseBody
	public ResponseData handleFileUpload(@RequestParam("file") MultipartFile file) {
		try(InputStream inputStream = file.getInputStream()){
			String sufix = this.getFileSufix(file.getOriginalFilename());
			fileService.saveFile(inputStream,file.getOriginalFilename(),sufix);
		} catch (IOException e) {
			e.printStackTrace();
			return ResponseUtil.error("上传失败");
		}
		// 将文件存储到mongodb中,mongodb 将会返回这个文件的具体信息
		return ResponseUtil.success("上传成功");

	}

	/**
	 * 删除文件
	 * 
	 * @param id
	 * @return
	 */
	@DeleteMapping("/{id}")
	@ResponseBody
	public ResponseData deleteFile(@PathVariable String id) {

		try {
			fileService.removeFile(id);
			return ResponseUtil.success("文件删除成功");
		} catch (Exception e) {
			return ResponseUtil.fail("文件删除失败");
		}
	}

	public  String getFileSufix(String fileUrl) {
		int splitIndex = fileUrl.lastIndexOf('.');
		return fileUrl.substring(splitIndex + 1);
	}
}
