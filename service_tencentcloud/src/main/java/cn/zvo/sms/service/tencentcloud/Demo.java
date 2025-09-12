package cn.zvo.sms.service.tencentcloud;


import cn.zvo.sms.ServiceInterface;
import cn.zvo.sms.Sms;
import com.xnx3.BaseVO;
import net.sf.json.JSONArray;
import org.apache.commons.collections.map.HashedMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 示例
 * @author 陈学斌
 *
 */
public class Demo {
	public static void main(String[] args) {
		// 资源地域，表示操作的资源所属的地域
		String region = "ap-beijing";
		// secretId
		String secretId = "AKIDmdNvKXYN0TltqeHk19Azc2IPR*******";
		// secretKey
		String secretKey = "lKj5II5g4d6OTN5e3PgmgNsAF7******";
		//手机号
		String phone = "158********";

		Map<String, String> map = new HashedMap();
		map.put("region",region);
		map.put("secretId",secretId);
		map.put("secretKey",secretKey);
		ServiceInterface service = new TencentcloudService(map); //创建短信服务通道
		Sms sms = new Sms(service);
		Map<String, String> params = new HashMap<String, String>();
		params.put("signName", "腾讯云短信测试");
		params.put("templateId", "SMS_154950909");
		params.put("countryPhoneCode","+86");
		List<String> templateParams = new ArrayList<>();
		templateParams.add("验证码");
		templateParams.add("1234");
		templateParams.add("5分钟");
		// 转换为JSON数组格式：["value1","value2","value3"]
		JSONArray jsonArray = JSONArray.fromObject(templateParams);
		params.put("templateParam", jsonArray.toString());
		params.put("smsSdkAppId", "1400006666");
		BaseVO send = sms.send(phone, params);//发送短信
		System.out.println(send);
	}
}
