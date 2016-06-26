package cn.edu.sdust.silence.itransfer.web;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


/**
 * OKHttp处理htto请求
 * 
 * @author 林宇强
 *
 */
public class OKHttp {
	/**
	 * Cookie管理器
	 */
	private CookieManager cookieManager;
	/**
	 * OKHttp客户端
	 */
	private OkHttpClient mHTTP;

	/**
	 * 构造方法
	 */
	public OKHttp() {
		// StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
		// .permitAll().build());
		this.mHTTP = new OkHttpClient();
		// 初始化Cookie管理器
		newCookie();
	}

	/**
	 * 获取不安全的http客户端，不进行安全证书的校验
	 */
	public OkHttpClient getUnsafeOkHttpClient() {
		try {
			// 设置接收所有安全证书，不进行拒绝
			TrustManager[] arrayOfTrustManager = new TrustManager[1];
			arrayOfTrustManager[0] = new X509TrustManager() {
				public void checkClientTrusted(
						X509Certificate[] paramAnonymousArrayOfX509Certificate,
						String paramAnonymousString)
						throws CertificateException {
				}

				public void checkServerTrusted(
						X509Certificate[] paramAnonymousArrayOfX509Certificate,
						String paramAnonymousString)
						throws CertificateException {
				}

				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			};
			// 采用SSL，是https所需
			SSLContext localSSLContext = SSLContext.getInstance("SSL");
			// 初始化SSLContext
			localSSLContext.init(null, arrayOfTrustManager, new SecureRandom());
			SSLSocketFactory localSSLSocketFactory = localSSLContext
					.getSocketFactory();
			// 设置OKHttpClient的安全模式工厂
			this.mHTTP.setSslSocketFactory(localSSLSocketFactory);
			this.mHTTP.setHostnameVerifier(new HostnameVerifier() {
				public boolean verify(String paramAnonymousString,
						SSLSession paramAnonymousSSLSession) {
					return true;
				}
			});
			return mHTTP;
		} catch (Exception localException) {
			throw new RuntimeException(localException);
		}
	}

	/**
	 * 通过get请求图片
	 * 
	 * @param
	 * @param
	 * @throws IOException
	 */
	public void getImage(String url, String imagePath) throws IOException {
		Request localRequest = new Request.Builder().url(url).build();
		FileOutputStream fos = new FileOutputStream(imagePath);
		InputStream is = this.mHTTP.newCall(localRequest).execute().body()
				.byteStream();
		int len = 0;
		byte[] buffer = new byte[1024];
		while ((len = is.read(buffer)) > 0) {
			fos.write(buffer, 0, len);
		}
		is.close();
		fos.close();
	}

	/**
	 * 通过get请求字符串，例如一个网页
	 * 
	 * @param
	 * @return 返回http响应体的内容
	 * @throws IOException
	 */
	public String getString(String url) throws IOException {
		Request localRequest = new Request.Builder().url(url).build();
		return this.mHTTP.newCall(localRequest).execute().body().string();
	}

	/**
	 * 通过get请求字符串，例如一个网页
	 * 
	 * @param
	 * @param
	 * @return 返回http响应体的内容
	 * @throws IOException
	 */
	public String getString(String url, String charset) throws IOException {
		Request localRequest = new Request.Builder().url(url).build();
		if (charset != null)
			return new String(this.mHTTP.newCall(localRequest).execute().body()
					.bytes(), charset);
		return null;
	}

	/**
	 * 初始化Cookie管理器
	 */
	public void newCookie() {
		this.cookieManager = new CookieManager();
		// 接收所有cookie
		this.cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
		this.mHTTP.setCookieHandler(this.cookieManager);
	}

	/**
	 * 通过post请求字符串，例如一个网页
	 * 
	 * @param
	 * @param
	 * @return 返回http响应体
	 * @throws IOException
	 */
	public String postString(String url, Map<String, String> params)
			throws IOException {
		FormEncodingBuilder localFormEncodingBuilder = new FormEncodingBuilder();
		new Request.Builder();
		for (Map.Entry<String, String> entry : params.entrySet())
			localFormEncodingBuilder.add(entry.getKey(), entry.getValue());

		RequestBody localRequestBody = localFormEncodingBuilder.build();
		System.out.println("params -->>" + localRequestBody.toString());
		Request localRequest = new Request.Builder()
				.url(url)
				.post(localRequestBody)
				.header("User-Agent",
						"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.71 Safari/537.36")
				.build();
		System.out.println("-----");
		for (HttpCookie cookie : cookieManager.getCookieStore().getCookies()) {
			System.out.println(cookie.getName() + " -->> " + cookie.getValue());
		}
		return this.mHTTP.newCall(localRequest).execute().body().string();
	}

	/**
	 * 通过post请求字符串，例如一个网页
	 * 
	 * @param
	 * @param
	 * @param
	 * @return 返回http响应体
	 * @throws IOException
	 */
	public String postString(String url, Map<String, String> params,
			String charset) throws IOException {
		FormEncodingBuilder localFormEncodingBuilder = new FormEncodingBuilder();
		new Request.Builder();
		for (Map.Entry<String, String> entry : params.entrySet()) {
			localFormEncodingBuilder.add(entry.getKey(), entry.getValue());
		}
		RequestBody localRequestBody = localFormEncodingBuilder.build();
		System.out.println("params -->>" + localRequestBody.toString());
		Request localRequest = new Request.Builder()
				.url(url)
				.post(localRequestBody)
				.header("User-Agent",
						"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.71 Safari/537.36")
				.build();
		return new String(this.mHTTP.newCall(localRequest).execute().body()
				.bytes(), charset);
	}
	/**
	 * 通过post请求字符串，例如一个网页
	 * 
	 * @param
	 * @param
	 * @param
	 * @return 返回http响应体
	 * @throws IOException
	 */
	public String postFiles(String url, Map<String, String> params,
			String charset) throws IOException {		
		FormEncodingBuilder localFormEncodingBuilder = new FormEncodingBuilder();
		new Request.Builder();
		for (Map.Entry<String, String> entry : params.entrySet()) {
			localFormEncodingBuilder.add(entry.getKey(), entry.getValue());
		}
		RequestBody localRequestBody = localFormEncodingBuilder.build();
		System.out.println("params -->>" + localRequestBody.toString());
		Request localRequest = new Request.Builder()
		.url(url)
		.post(localRequestBody)
		.header("User-Agent",
				"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.71 Safari/537.36")
				.build();
		return new String(this.mHTTP.newCall(localRequest).execute().body()
				.bytes(), charset);
	}

	/**
	 * 通过post请求字符串，请求参数不进行编码，例如一个网页
	 * 
	 * @param
	 * @param
	 * @return 返回http响应体
	 * @throws IOException
	 */
	public String postStringNoUrlEncode(String url, Map<String, String> params)
			throws IOException {
		FormEncodingBuilder localFormEncodingBuilder = new FormEncodingBuilder();
		new Request.Builder();
		for (Map.Entry<String, String> entry : params.entrySet())
			// 插入的是encoded，就是url编码完成的，已经不需要OKHttp后台进行编码
			localFormEncodingBuilder.addEncoded(entry.getKey(),
					entry.getValue());
		RequestBody localRequestBody = localFormEncodingBuilder.build();
		System.out.println("params -->>" + localRequestBody.toString());
		Request localRequest = new Request.Builder()
				.url(url)
				.post(localRequestBody)
				.header("User-Agent",
						"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.71 Safari/537.36")
				.build();

		return this.mHTTP.newCall(localRequest).execute().body().string();
	}

	/**
	 * 设置http代理
	 * 
	 * @param
	 * @param
	 */
	public void setProxy(String ip, int port) {
		Proxy localProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip,
				port));
		this.mHTTP.setProxy(localProxy);
	}

	/**
	 * 设置连接超时时间
	 * 
	 * @param timeOut
	 *            ，秒为单位
	 */
	public void setTimeout(int timeOut) {
		this.mHTTP.setConnectTimeout(timeOut, TimeUnit.SECONDS);
		this.mHTTP.setReadTimeout(timeOut, TimeUnit.SECONDS);
	}
}
