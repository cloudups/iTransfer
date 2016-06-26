package cn.edu.sdust.silence.itransfer.web;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;

public interface ITransferClient {

	/**
	 * 发送普通表单
	 * 
	 * @param
	 * @param
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public abstract ResponseParser sendForm(String operation,
											Map<String, String> params) throws ClientProtocolException,
			IOException;

	/**
	 * 发送文件上传表单
	 * 
	 * @param
	 * @param
	 * @param
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public abstract ResponseParser sendFileForm(String operation, List<File> files,
												Map<String, String> params) throws ClientProtocolException,
			IOException;

	/**
	 * 下载文件
	 * 
	 * @param
	 * @param
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public abstract ResponseParser downloadFile(String fid,String filename, String storeName, String savePath)
			throws ClientProtocolException, IOException;

}