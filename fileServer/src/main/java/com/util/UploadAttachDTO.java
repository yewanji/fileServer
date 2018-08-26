package com.util;

public class UploadAttachDTO {

		private String fileName;
		private String fileId;
		private String fileIndex;
		private String chunkCount;
		public String getFileName() {
			return fileName;
		}
		public void setFileName(String fileName) {
			this.fileName = fileName;
		}
		public String getFileId() {
			return fileId;
		}
		public void setFileId(String fileId) {
			this.fileId = fileId;
		}
		public String getFileIndex() {
			return fileIndex;
		}
		public void setFileIndex(String fileIndex) {
			this.fileIndex = fileIndex;
		}
		public String getChunkCount() {
			return chunkCount;
		}
		public void setChunkCount(String chunkCount) {
			this.chunkCount = chunkCount;
		}
}
