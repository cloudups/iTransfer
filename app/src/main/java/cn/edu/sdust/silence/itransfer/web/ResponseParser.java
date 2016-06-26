package cn.edu.sdust.silence.itransfer.web;

import java.util.List;

import cn.edu.sdust.silence.itransfer.web.domain.Result;


public interface ResponseParser {

	/**
	 * 标记该Response的类型
	 * 
	 * @author Silence团队 2015-11-15
	 *
	 */
	public enum ResultType {
		//出错状态码
		ResponseCode,
		// 成功查询文件
		FileLog,
		//成功下载
		Download,
		//出错
		Error
	}

	/**
	 * 进行结果解析
	 * 
	 * @return 返回解析完成的类型
	 */
	public abstract ResultType parse();


	/**
	 * 返回http响应码
	 * 
	 * @return
	 */
	public abstract int getResponseCode();


	public abstract Result getResult();

	public abstract Object getObj() ;

	public abstract void setObj(Object obj) ;
}