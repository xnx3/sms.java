package cn.zvo.sms.service.tencentcloud;


import cn.zvo.sms.ServiceInterface;
import com.tencentcloudapi.common.AbstractModel;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.sms.v20190711.SmsClient;
import com.tencentcloudapi.sms.v20190711.models.SendSmsRequest;
import com.tencentcloudapi.sms.v20190711.models.SendSmsResponse;
import com.xnx3.BaseVO;
import net.sf.json.JSONObject;

import java.util.Map;

/**
 * 短信发送服务
 * @author 陈学斌
 *
 */
public class TencentcloudService implements ServiceInterface {

	private String region; //资源地域，表示操作的资源所属的地域

	private String secretId; //secretId

	private String secretKey; //secretKey

	/**
	 * 腾讯云短信发送配置参数初始化
	 * @param region 机房信息，如
	 * 			<ul>
	 * 				<li>ap-beijing</li>
	 * 				<li>ap-guangzhou</li>
	 * 				<li>ap-nanjing</li>
	 * 			    <li>ap-tianjin</li>
	 * 			</ul>
	 * @param secretId secretId ， 参见 https://console.cloud.tencent.com/cam/capi
	 * @param secretKey secretKey， 参见 https://console.cloud.tencent.com/cam/capi
	 */
	public TencentcloudService(Map<String, String> map) {
		this.region = map.get("region");
		this.secretId = map.get("secretId");
		this.secretKey = map.get("secretKey");
	}

	public TencentcloudService(){

	}

	/**
	 * 发送短信接口
	 * @param signName 签名名称
	 * @param templateCode 模版ID
	 * @param templateParams 模板参数，若无模板参数，则设置为空。 模板参数的个数需要与 TemplateId 对应模板的变量个数保持一致。String数组类型
	 * @param smsSdkAppId 短信 SdkAppId，在 短信控制台 添加应用后生成的实际 SdkAppId，示例如1400006666。
	 * @param phone 手机号
	 * @param countryPhoneCode 国际电话区号，例如，拨打中国的电话时，可能需要拨打 +86 作为国际区号。
	 * @return 结果
	 */
	@Override
	public BaseVO send(String phone, Map<String, String> params) {

		String signName = params.get("signName");
		String templateId = params.get("templateId");
		String templateParam = params.get("templateParam");
		String smsSdkAppId = params.get("smsSdkAppId");
		String countryPhoneCode = params.get("countryPhoneCode");
		if (countryPhoneCode == null || countryPhoneCode.isEmpty()){
			countryPhoneCode = "+86";
		}

		try {
			String[] parsePhone = new String[1];
			// 手机号
			parsePhone[0] = countryPhoneCode + phone;
			Credential credential = new Credential(secretId, secretKey);
			SmsClient smsClient = new SmsClient(credential, region);
			SendSmsRequest req = new SendSmsRequest();
			req.setSmsSdkAppid(smsSdkAppId);
			req.setPhoneNumberSet(parsePhone);
			req.setSign(signName);
			req.setTemplateID(templateId);

			// 将templateParam转换为String数组
			if (templateParam != null && !templateParam.trim().isEmpty()) {
				try {
					// 尝试解析为JSON数组格式: ["value1","value2"]
					net.sf.json.JSONArray jsonArray = net.sf.json.JSONArray.fromObject(templateParam);
					String[] templateParamArray = new String[jsonArray.size()];
					for (int i = 0; i < jsonArray.size(); i++) {
						templateParamArray[i] = jsonArray.getString(i);
					}
					req.setTemplateParamSet(templateParamArray);
				} catch (Exception e) {
					e.printStackTrace();
					return BaseVO.failure("短信发送失败，请检查模板参数格式是否正确。");
				}
			} else {
				req.setTemplateParamSet(null);
			}

			SendSmsResponse sendSmsResponse = smsClient.SendSms(req);

			// 输出json格式的字符串回包
			String responseJson = AbstractModel.toJsonString(sendSmsResponse);
			System.out.println(responseJson);
			
			// 解析返回的JSON数据，获取Code参数
			try {
				JSONObject jsonObject = JSONObject.fromObject(responseJson);
				if (jsonObject.has("SendStatusSet") && jsonObject.getJSONArray("SendStatusSet").size() > 0) {
					JSONObject sendStatus = jsonObject.getJSONArray("SendStatusSet").getJSONObject(0);
					String code = sendStatus.getString("Code");
					String message = sendStatus.getString("Message");
					
					if ("Ok".equals(code)) {
						return BaseVO.success("短信发送成功");
					} else {
						// 根据错误码返回对应的错误信息
						String errorMessage = getErrorMessage(code);
						return BaseVO.failure(errorMessage != null ? errorMessage : "短信发送失败，错误代码：" + code + "，错误信息：" + message);
					}
				} else {
					return BaseVO.failure("短信发送失败，返回数据格式异常");
				}
			} catch (Exception e) {
				e.printStackTrace();
				return BaseVO.failure("短信发送失败，解析返回数据异常：" + e.getMessage());
			}

		} catch (TencentCloudSDKException e) {
			e.printStackTrace();
			return BaseVO.failure("短信发送失败，请检查参数是否正确：" + e.getMessage());
		}
	}

	@Override
	public BaseVO getBalance() {
		return null;
	}
	
	/**
	 * 根据错误码获取对应的错误信息
	 * @param code 错误码
	 * @return 错误信息
	 */
	private String getErrorMessage(String code) {
		switch (code) {
			case "FailedOperation.ContainSensitiveWord":
				return "短信内容中含有敏感词，请联系腾讯云短信小助手";
			case "FailedOperation.FailResolvePacket":
				return "请求包解析失败，通常情况下是由于没有遵守API接口说明规范导致的";
			case "FailedOperation.InsufficientBalanceInSmsPackage":
				return "套餐包余量不足，请购买套餐包";
			case "FailedOperation.JsonParseFail":
				return "解析请求包体时候失败";
			case "FailedOperation.MarketingSendTimeConstraint":
				return "营销短信发送时间限制，为避免骚扰用户，营销短信只允许在8点到22点发送";
			case "FailedOperation.PhoneNumberInBlacklist":
				return "手机号在免打扰名单库中，通常是用户退订或者命中运营商免打扰名单导致的";
			case "FailedOperation.SignatureIncorrectOrUnapproved":
				return "签名未审批或格式错误，请检查签名是否已审批并且审批通过";
			case "FailedOperation.TemplateIncorrectOrUnapproved":
				return "模板未审批或内容不匹配，请检查模板是否已审批并审批通过";
			case "FailedOperation.TemplateParamSetNotMatchApprovedTemplate":
				return "请求内容与审核通过的模板内容不匹配，请检查请求中模板参数的个数是否与申请的模板一致";
			case "FailedOperation.TemplateUnapprovedOrNotExist":
				return "模板未审批或不存在，请检查模板是否已审批并审批通过";
			case "InternalError.OtherError":
				return "其他错误，请联系腾讯云短信小助手";
			case "InternalError.RequestTimeException":
				return "请求发起时间不正常，通常是由于您的服务器时间与腾讯云服务器时间差异超过10分钟导致的";
			case "InternalError.RestApiInterfaceNotExist":
				return "不存在该RESTAPI接口，请核查REST API接口说明";
			case "InternalError.SendAndRecvFail":
				return "接口超时或短信收发包超时，请检查您的网络是否有波动";
			case "InternalError.SigFieldMissing":
				return "后端包体中请求包体没有Sig字段或Sig为空";
			case "InternalError.SigVerificationFail":
				return "后端校验Sig失败";
			case "InternalError.Timeout":
				return "请求下发短信超时";
			case "InternalError.UnknownError":
				return "未知错误类型";
			case "InvalidParameterValue.ContentLengthLimit":
				return "请求的短信内容太长";
			case "InvalidParameterValue.IncorrectPhoneNumber":
				return "手机号格式错误";
			case "InvalidParameterValue.ProhibitedUseUrlInTemplateParameter":
				return "禁止在模板变量中使用URL";
			case "InvalidParameterValue.SdkAppIdNotExist":
				return "SdkAppId不存在";
			case "InvalidParameterValue.TemplateParameterFormatError":
				return "验证码模板参数格式错误，验证码类模板，模板变量只能传入0-6位纯数字";
			case "InvalidParameterValue.TemplateParameterLengthLimit":
				return "单个模板变量字符数超过规定的限制数";
			case "LimitExceeded.AppCountryOrRegionDailyLimit":
				return "业务短信国家/地区日下发条数超过设定的上限";
			case "LimitExceeded.AppCountryOrRegionInBlacklist":
				return "业务短信国家/地区不在国际港澳台短信发送限制设置的列表中而禁发";
			case "LimitExceeded.AppDailyLimit":
				return "业务短信日下发条数超过设定的上限";
			case "LimitExceeded.AppGlobalDailyLimit":
				return "业务短信国际/港澳台日下发条数超过设定的上限";
			case "LimitExceeded.AppMainlandChinaDailyLimit":
				return "业务短信中国大陆日下发条数超过设定的上限";
			case "LimitExceeded.DailyLimit":
				return "短信日下发条数超过设定的上限(国际/港澳台)";
			case "LimitExceeded.DeliveryFrequencyLimit":
				return "下发短信命中了频率限制策略";
			case "LimitExceeded.PhoneNumberCountLimit":
				return "调用接口单次提交的手机号个数超过200个";
			case "LimitExceeded.PhoneNumberDailyLimit":
				return "单个手机号日下发短信条数超过设定的上限";
			case "LimitExceeded.PhoneNumberOneHourLimit":
				return "单个手机号1小时内下发短信条数超过设定的上限";
			case "LimitExceeded.PhoneNumberSameContentDailyLimit":
				return "单个手机号下发相同内容超过设定的上限";
			case "LimitExceeded.PhoneNumberThirtySecondLimit":
				return "单个手机号30秒内下发短信条数超过设定的上限";
			case "MissingParameter.EmptyPhoneNumberSet":
				return "传入的号码列表为空，请确认您的参数中是否传入号码";
			case "UnauthorizedOperation.IndividualUserMarketingSmsPermissionDeny":
				return "个人用户没有发营销短信的权限";
			case "UnauthorizedOperation.RequestIpNotInWhitelist":
				return "请求IP不在白名单中";
			case "UnauthorizedOperation.RequestPermissionDeny":
				return "请求没有权限，请联系腾讯云短信小助手";
			case "UnauthorizedOperation.SdkAppIdIsDisabled":
				return "此SdkAppId禁止提供服务，如有需要请联系腾讯云短信小助手";
			case "UnauthorizedOperation.ServiceSuspendDueToArrears":
				return "欠费被停止服务，可自行登录腾讯云充值来缴清欠款";
			case "UnauthorizedOperation.SmsSdkAppIdVerifyFail":
				return "SmsSdkAppId校验失败，请检查SmsSdkAppId是否属于云API密钥的关联账户";
			case "UnsupportedOperation":
				return "操作不支持";
			case "UnsupportedOperation.ChineseMainlandTemplateToGlobalPhone":
				return "国内短信模板不支持发送国际/港澳台手机号";
			case "UnsupportedOperation.ContainDomesticAndInternationalPhoneNumber":
				return "群发请求里既有国内手机号也有国际手机号";
			case "UnsupportedOperation.GlobalTemplateToChineseMainlandPhone":
				return "国际/港澳台短信模板不支持发送国内手机号";
			case "UnsupportedOperation.UnsupportedRegion":
				return "不支持该地区短信下发";
			default:
				return null;
		}
	}
}
