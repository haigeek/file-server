package com.haigeek.fileserver.service;

import com.haigeek.fileserver.model.MyFile;
import com.haigeek.fileserver.repository.FileRepository;
import com.mongodb.client.gridfs.GridFSFindIterable;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * MyFile 服务.
 * 
 * @since 1.0.0 2017年7月30日
 * @author <a href="https://waylau.com">Way Lau</a> 
 */
@Service
public class FileServiceImpl implements FileService {
	
	@Autowired
	public FileRepository fileRepository;
	@Resource
	private GridFsTemplate gridFsTemplate;

	private static final String METADATA_TYPE = "fileType";


	@Override
	public MyFile saveFile(InputStream inputStream, String name, String type) {
		ObjectId objectId = gridFsTemplate.store(inputStream,name);
		Document document = new Document();
		document.append(METADATA_TYPE,type);
		GridFSFile gridFSFile=gridFsTemplate.findOne(new Query(Criteria.where("_id").is(objectId)));
		MyFile file=new MyFile();
		file.setName(name);
		if (gridFSFile != null) {
			file.setContentType(gridFSFile.getMetadata() != null ? gridFSFile.getMetadata().getString("METADATA_TYPE") : null);
		}
		file.setMd5(gridFSFile.getMD5());
		file.setId(objectId.toString());
		file.setUploadDate(gridFSFile.getUploadDate());
		file.setSize(gridFSFile.getLength());
		return file;
	}

	@Override
	public void removeFile(String id) {
		gridFsTemplate.delete(new Query(Criteria.where("_id").is(id)));
	}

	@Override
	public GridFSFile getFileById(String id) {
		return gridFsTemplate.findOne(new Query(Criteria.where("_id").is(id)));
	}

	@Override
	public List<MyFile> listFilesByPage(int pageIndex, int pageSize) {
		List<MyFile> list = new ArrayList<>();
		Query query = new Query();
		query.skip(pageIndex * pageSize).limit(pageSize);
		GridFSFindIterable gridFSFiles = gridFsTemplate.find(query);
		for (GridFSFile file:gridFSFiles
			 ) {
			MyFile myFile=new MyFile();
			myFile.setName(file.getFilename());

			myFile.setContentType(file.getMetadata() != null ? file.getMetadata().getString("METADATA_TYPE") : null);

			myFile.setMd5(file.getMD5());
			myFile.setId(file.getObjectId().toString());
			myFile.setUploadDate(file.getUploadDate());
			myFile.setSize(file.getLength());
			list.add(myFile);
		}
		return list;
	}

	@Override
	public List<MyFile> getAllFile() {
		List<MyFile> list = new ArrayList<>();
		GridFSFindIterable gridFSFiles = gridFsTemplate.find(new Query());
		for (GridFSFile file:gridFSFiles
		) {
			MyFile myFile=new MyFile();
			myFile.setName(file.getFilename());
			myFile.setContentType(file.getMetadata() != null ? file.getMetadata().getString("METADATA_TYPE") : null);
			myFile.setMd5(file.getMD5());
			myFile.setId(file.getObjectId().toString());
			myFile.setUploadDate(file.getUploadDate());
			myFile.setSize(file.getLength());
			list.add(myFile);
		}
		return list;
	}
}
