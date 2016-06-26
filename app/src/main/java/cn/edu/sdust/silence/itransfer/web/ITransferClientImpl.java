package cn.edu.sdust.silence.itransfer.web;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import cn.edu.sdust.silence.itransfer.util.ConfigUtil;


//每次的请求操作，处理结束前都要进行状态保存，若收到的jsessionid不为空，保存jsessionid，最新请求时间必须保存
//除了登录的请求操作前，都要进行当前时间和最新请求时间的比较，若间隔时间超过28分钟，要把账户密码添加到发送表单中

/**
 * Ushare的http连接客户端
 * 
 * @author Silence团队 2015-11-15
 *
 */
public class ITransferClientImpl implements ITransferClient {

	private HttpClient httpClient;
	private HttpPost post;
	private HttpResponse response;

	public ITransferClientImpl() {
		this.httpClient = new DefaultHttpClient();
	}

	public ITransferClientImpl(HttpClient client) throws IOException {
		this.httpClient = client;
	}

	/**
	 * 合成普通表单HttpPost
	 * 
	 * @param operation
	 * @param
	 * @return
	 */
	private HttpPost buildHttpPost(String operation, Map<String, String> params) {
		String url = ConfigUtil.getProperty("address") + operation;
		post = new HttpPost(url);
		params.put("client", "app");
		// 封装请求参数
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		for (Map.Entry<String, String> entry : params.entrySet()) {
			// 单个请求参数
			NameValuePair pair = new BasicNameValuePair(entry.getKey(),
					entry.getValue());
			list.add(pair);
		}
		try {
			// 对请求参数进行编码,得到实体数据
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list,
					"UTF-8");
			post.setEntity(entity);
			return post;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 合成文件表单HttpPost
	 * 
	 * @param operation
	 * @param
	 * @return
	 */

	private HttpPost buildFileHttpPost(String operation, List<File> files,
			Map<String, String> params) {
		params.put("client", "app");
		String url = ConfigUtil.getProperty("address") + operation;
		post = new HttpPost(url);

		MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create()
				.setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
				.setCharset(Charset.forName("UTF-8"));

		// 填充文件体
		for (File f : files) {
			entityBuilder.addPart("file", new FileBody(f));
		}

		// 普通表单体
		for (Map.Entry<String, String> entry : params.entrySet()) {
			String name = entry.getKey();
			String value = entry.getValue();
			if (value != null)
				try {
					entityBuilder.addPart(name,
							new StringBody(value, Charset.forName("UTF-8")));
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException(e);
				}
		}
		post.setEntity(entityBuilder.build());
		return post;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cn.edu.sdust.cise.ushare.UShareClient#sendForm(java.lang.String,
	 * java.util.Map)
	 */
	@Override
	public ResponseParser sendForm(String operation, Map<String, String> params)
			throws ClientProtocolException, IOException {
		post = buildHttpPost(operation, params);

		response = httpClient.execute(post);
		if (response.getStatusLine().getStatusCode() == 200) {
			String result = EntityUtils.toString(response.getEntity(), "UTF-8");
			return new ResponseParserImpl(result);
		} else
			return new ResponseParserImpl(response.getStatusLine()
					.getStatusCode());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cn.edu.sdust.cise.ushare.UShareClient#sendFileForm(java.lang.String,
	 * java.io.File, java.util.Map)
	 */
	@Override
	public ResponseParser sendFileForm(String operation, List<File> files,
			Map<String, String> params) throws ClientProtocolException,
			IOException {

		post = buildFileHttpPost(operation, files, params);

		response = httpClient.execute(post);
		if (response.getStatusLine().getStatusCode() == 200) {
			String result = EntityUtils.toString(response.getEntity(), "GBK");
			System.out.println("result = " + result);
			return new ResponseParserImpl(result);
		} else
			return new ResponseParserImpl(response.getStatusLine()
					.getStatusCode());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cn.edu.sdust.cise.ushare.UShareClient#downloadFile(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public ResponseParser downloadFile(String fid,String filename, String storeName,
			String savePath) throws ClientProtocolException, IOException {

		Map<String, String> map = new HashMap<String, String>();
		map.put("type", "query");
		map.put("fid", fid);
		map.put("filename", filename);
		map.put("storeName", storeName);
		post = buildHttpPost("download", map);

		response = httpClient.execute(post);
		if (response.getStatusLine().getStatusCode() == 200) {
			// 文件表单，进行下载
			Header header = response.getFirstHeader("content-disposition");
			if (header != null) {
				// 文件传输，请求成功
				FileOutputStream os = new FileOutputStream(savePath);
				IOUtils.write(EntityUtils.toByteArray(response.getEntity()), os);
				IOUtils.closeQuietly(os);
				return new ResponseParserImpl(ResponseParser.ResultType.Download);
			} else {
				// 请求失败，此处非文件表单，查看错误信息
				return new ResponseParserImpl(EntityUtils.toString(
						response.getEntity(), "UTF-8"));
			}
		} else
			// 服务器异常
			return new ResponseParserImpl(response.getStatusLine()
					.getStatusCode());
	}
}
