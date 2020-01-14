/**
 * 
 */
package com.haigeek.fileserver.service;


import com.haigeek.fileserver.model.MyFile;
import com.mongodb.client.gridfs.model.GridFSFile;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

/**
 * MyFile 服务接口.
 * 
 * @since 1.0.0 2017年3月28日
 * @author <a href="https://waylau.com">Way Lau</a> 
 */
public interface FileService {
	/**
	 * 保存文件
	 * @param inputStream
	 * @param name
	 * @param type
	 * @return
	 */
	MyFile saveFile(InputStream inputStream, String name, String type);
	
	/**
	 * 删除文件
	 * @param id
	 * @return
	 */
	void removeFile(String id);
	
	/**
	 * 根据id获取文件
	 * @param id
	 * @return
	 */
	GridFSFile getFileById(String id);

	/**
	 * 分页查询，按上传时间降序
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 */
	List<MyFile> listFilesByPage(int pageIndex, int pageSize);

	/**
	 * 获取所有文件
	 * @return
	 */
	List<MyFile> getAllFile();
}
