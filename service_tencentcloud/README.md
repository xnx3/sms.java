java 发送短信，一行代码接入，可任意切换多家短信通道

## 使用
#### 1. pom.xml 中
````
<!-- 短信的核心实现 https://github.com/xnx3/sms.java -->
<dependency> 
	<groupId>cn.zvo.sms</groupId>
	<artifactId>sms-core</artifactId>
	<version>1.0</version>
</dependency>
<!-- 使用 阿里云的短信服务通道 -->
<dependency>
	<groupId>cn.zvo.sms</groupId>
	<artifactId>sms-service-tencentcloud</artifactId>
	<version>1.0</version>
</dependency>
````

#### 2. Java代码

````
Map<String, String> params = new HashMap<String, String>();
params.put("signName", "腾讯云短信测试");
params.put("templateId", "SMS_154950909");
List<String> templateParams = new ArrayList<>();
templateParams.add("验证码");
templateParams.add("1234");
templateParams.add("5分钟");

net.sf.json.JSONArray jsonArray = net.sf.json.JSONArray.fromObject(templateParams);
params.put("templateParam", jsonArray.toString());
params.put("smsSdkAppId", "1400006666");
BaseVO send = sms.send(phone, params);//发送短信
````

##### params 参数说明
params 其map的传参有： 

* ```` params.put("signName", "腾讯云短信测试"); ```` 短信签名，必填
* ```` params.put("templateId", "SMS_154950909"); ```` 短信模板ID，必填
* ```` params.put("templateParam", "[\"验证码\",\"1234\",\"5分钟\"]"); ```` 模板参数，必填
* ```` params.put("smsSdkAppId", "1400006666"); ```` 短信应用ID，必填

**注意事项：**
* 模板中的每个变量都必须赋值，且取值不能为空
* 参数顺序必须与模板中的变量顺序一致
* 腾讯云短信模板变量格式为 {1}、{2}、{3} 等
