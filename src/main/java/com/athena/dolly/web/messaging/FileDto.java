package com.athena.dolly.web.messaging;

import java.util.List;

import com.athena.dolly.web.common.dto.BaseDto;

public class FileDto extends BaseDto {
	private static final long serialVersionUID = 1L;
	private String dir;

	private List<FileInfoDto> fileList;
	
	public String getDir() {
		return dir;
	}
	public void setDir(String dir) {
		this.dir = dir;
	}
	public List<FileInfoDto> getFileList() {
		return fileList;
	}
	public void setFileList(List<FileInfoDto> fileList) {
		this.fileList = fileList;
	}	
}
