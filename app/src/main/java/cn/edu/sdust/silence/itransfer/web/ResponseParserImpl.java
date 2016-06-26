package cn.edu.sdust.silence.itransfer.web;


import com.google.gson.Gson;

import cn.edu.sdust.silence.itransfer.web.domain.Result;

/*
 * http响应类型解析
 * 1.为空 Null
 * 2.http状态码不为200，解析出具体的状态码
 * 以下为正常通信情况下的自定义类型
 * 3.error：返回提交产生的错误信息，包括String和HashMap两种格式的错误信息
 * 4.completed：上传文件已存在，返回已存在的文件实体
 * 5.success：返回当次请求的所需要的实体或者成功信息或为空
 */

/**
 * 解析http的Response解析类
 * 
 * @author Silence团队 2015-11-15
 *
 */
public class ResponseParserImpl implements ResponseParser {

	/**
	 * 传入的Response结果
	 */
	private Object obj;
	/**
	 * 服务器http响应码
	 */
	private int responseCode;
	/**
	 * Response响应结果体
	 */
	private Result result;
	/**
	 * 该Response解析之后的类型
	 */
	private ResultType resultType;
	/**
	 * 标记该Response是否解析过
	 */
	private boolean parsed = false;

	public ResponseParserImpl(ResultType resultType) {
		this.resultType = resultType;
		responseCode = 200;
		parsed = true;
		// 保存最新请求时间
	}

	public ResponseParserImpl(Object result) {
		this.obj = result;
		responseCode = 200;
		// 保存最新请求时间

		System.out.println("结果：" + result);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cn.edu.sdust.cise.ushare.ResponseParser#parse()
	 */
	@Override
	public ResultType parse() {
		if (parsed)
			return resultType;
		if (obj instanceof String) {
			String str = (String) obj;
			result = new Gson().fromJson(str, Result.class);
			System.out.println("gson:"+result);
			if(result.getType().equals("message")){
				resultType=ResultType.Error;
			}else if(result.getType().equals("file")){
				resultType=ResultType.FileLog;
			}
			parsed = true;
			return resultType;
		}
		return ResultType.Error;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cn.edu.sdust.cise.ushare.ResponseParser#getResponseCode()
	 */
	@Override
	public int getResponseCode() {
		return responseCode;
	}

	/* (non-Javadoc)
	 * @see cn.edu.sdust.cise.itransfer.ResponseParser#getResult()
	 */
	@Override
	public Result getResult() {
		return result;
	}

	public Object getObj() {
		return obj;
	}

	public void setObj(Object obj) {
		this.obj = obj;
	}
}
