package com.athena.dolly.web.messaging;

import com.athena.dolly.web.common.dto.BaseDto;

public class FileInfoDto extends BaseDto {
	private static final long serialVersionUID = 1L;

	private String name;
	private long length;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public long getLength() {
		return length;
	}
	public void setLength(long length) {
		this.length = length;
	}
}
