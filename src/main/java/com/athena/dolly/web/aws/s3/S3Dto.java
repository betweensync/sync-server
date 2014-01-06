package com.athena.dolly.web.aws.s3;

public class S3Dto {
	private String bucketName;
	private String key;
	private String size;
	private String lastModified;
	private String dataType;
	private String parent;
	private String url;
	
	
	public String getBucketName() {
		return bucketName;
	}
	public void setBucketName(String bucketName) {
		this.bucketName = bucketName;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getSize() {
		return size;
	}
	public void setSize(String size) {
		this.size = size;
	}
	public String getLastModified() {
		return lastModified;
	}
	public void setLastModified(String lastModified) {
		this.lastModified = lastModified;
	}
	public String getDataType() {
		return dataType;
	}
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	
	public String getParent() {
		return parent;
	}
	public void setParent(String parent) {
		this.parent = parent;
	}
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	@Override
	public String toString() {
		return "S3Dto [bucketName=" + bucketName + ", key=" + key + ", size="
				+ size + ", lastModified=" + lastModified + ", dataType="
				+ dataType + ", parent=" + parent + ", url=" + url + "]/n";
	}
	
	
	
}
