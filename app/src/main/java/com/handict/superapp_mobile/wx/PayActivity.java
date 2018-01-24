package com.handict.superapp_mobile.wx;


import android.app.Activity;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.handict.superapp_mobile.R;
import com.tencent.mm.opensdk.constants.Build;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class PayActivity extends Activity {
	
	private IWXAPI api;
    private final String TAG = getClass().getSimpleName();
	private static final String INNER_URL = "http://api.yolkworld.net/Payment/AsgWxPay";

	public String custOrderId="dingdanweishengcheng";
	String sign;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pay);

		api = WXAPIFactory.createWXAPI(this, "wx52d31ccb73e0222f");
		custOrderId = getCustOrderId("ASG");

		Button appayBtn = (Button) findViewById(R.id.appay_btn);
		appayBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String stringA="appId=wx52d31ccb73e0222f&orderNo="+GetCustId()+"&key=#YIE9HNqIMvRaU8u";
				sign= md5(stringA);//注：MD5签名方式




//				String url = "http://wxpay.wxutil.com/pub_v2/app/app_pay.php";
				Button payBtn = (Button) findViewById(R.id.appay_btn);
				payBtn.setEnabled(false);
				Toast.makeText(PayActivity.this, "获取订单中...", Toast.LENGTH_SHORT).show();
				postKeyValue();
//		        try{
//					byte[] buf = Util.httpGet(url);
//					if (buf != null && buf.length > 0) {
//						String content = new String(buf);
//						Log.e("get server pay params:",content);
//			        	JSONObject json = new JSONObject(content);
//						if(null != json && !json.has("retcode") ){
//							PayReq req = new PayReq();
//							req.appId = "wxf8b4f85f3a794e77";  // 测试用appId
//							req.appId			= json.getString("appid");
//							req.partnerId		= json.getString("partnerid");
//							req.prepayId		= json.getString("prepayid");
//							req.nonceStr		= json.getString("noncestr");
//							req.timeStamp		= json.getString("timestamp");
//							req.packageValue	= json.getString("package");
//							req.sign			= json.getString("sign");
//							req.extData			= "app data"; // optional
							Toast.makeText(PayActivity.this, "正常调起支付", Toast.LENGTH_SHORT).show();
							// 在支付之前，如果应用没有注册到微信，应该先调用IWXMsg.registerApp将应用注册到微信
//							api.sendReq(req);
//						}else{
//				        	Log.d("PAY_GET", "返回错误"+json.getString("retmsg"));
//				        	Toast.makeText(PayActivity.this, "返回错误"+json.getString("retmsg"), Toast.LENGTH_SHORT).show();
//						}
//					}else{
//			        	Log.d("PAY_GET", "服务器请求错误");
//			        	Toast.makeText(PayActivity.this, "服务器请求错误", Toast.LENGTH_SHORT).show();
//			        }
//		        }catch(Exception e){
//		        	Log.e("PAY_GET", "异常："+e.getMessage());
//		        	Toast.makeText(PayActivity.this, "异常："+e.getMessage(), Toast.LENGTH_SHORT).show();
//		        }
		        payBtn.setEnabled(true);
			}
		});		
		Button checkPayBtn = (Button) findViewById(R.id.check_pay_btn);
		checkPayBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				boolean isPaySupported = api.getWXAppSupportAPI() >= Build.PAY_SUPPORTED_SDK_INT;
				Toast.makeText(PayActivity.this, String.valueOf(isPaySupported), Toast.LENGTH_SHORT).show();
			}
		});
	}


	/**
	 *		"OrderNo": "2315646488945132134",
	 *			"Price": 1.15,
	 *		"ClientIp": "183.159.96.121",
	 *			"ProductName": "爱手工-1111",
	 *		"Sign": "984dcec8f8b7709f6ba29918366f1e8e"
	 * Post方式提交键值对
	 */
	private void postKeyValue() {
		//http://zhushou.72g.com/app/gift/gift_list/
		//请求参数`platform=2&gifttype=1&compare=60841c5b7c69a1bbb3f06536ed685a48
		OkHttpClient okHttpClient = new OkHttpClient();
		//TODO 和Get方式不一样的地方是， Post方式的请求参数 必须放入到请求体中才可以。
		//如果提交键值对，我们需要通过new FormBody.Builder()添加键值对。
		RequestBody requestBody = new FormBody.Builder()
				.add("OrderNo",custOrderId)
				.add("Price","0.01")
				.add("ClientIp","192.168.3.170")
				.add("ProductName","爱手工VIP会员·季卡")
				.add("Sign",sign)
				.build();
		Log.i(TAG, "custOrderId: "+custOrderId);
		Log.i(TAG, "Sign: "+sign);
		Request request = new Request.Builder()
				.url(INNER_URL)
				.post(requestBody)
				.build();
		Call call = okHttpClient.newCall(request);
		call.enqueue(new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {

			}

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				ResponseBody body = response.body();
				final String result = body.string();
//				Toast.makeText(PayActivity.this, result, Toast.LENGTH_SHORT).show();
				/*try {
					JSONObject json = new JSONObject(result);
					String data = json.getString("data");
					Log.i(TAG, "data: "+data);
					JSONObject json2 = new JSONObject(data);
					Log.i(TAG, "appid: "+json2.getString("appid"));
					PayReq req = new PayReq();
					req.appId = "wxf8b4f85f3a794e77";  // 测试用appId
					req.appId			= json2.getString("appid");
					req.partnerId		= json2.getString("partnerid");
					req.prepayId		= json2.getString("prepayid");
					req.nonceStr		= json2.getString("noncestr");
					req.timeStamp		= json2.getString("timestamp");
					req.packageValue	= json2.getString("package");
					req.sign			= json2.getString("sign");
					req.extData			= "app data"; // optional
					Toast.makeText(PayActivity.this, "正常调起支付", Toast.LENGTH_SHORT).show();
					// 在支付之前，如果应用没有注册到微信，应该先调用IWXMsg.registerApp将应用注册到微信
					api.sendReq(req);
				} catch (JSONException e) {
					e.printStackTrace();
				}*/
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(PayActivity.this, result, Toast.LENGTH_SHORT).show();
						try {
							JSONObject json = new JSONObject(result);
							String data = json.getString("data");
							Log.i(TAG, "data: "+data);
							JSONObject json2 = new JSONObject(data);
							Log.i(TAG, "appid: "+json2.getString("appid"));
							PayReq req = new PayReq();
							req.appId = "wxf8b4f85f3a794e77";  // 测试用appId
							req.appId			= json2.getString("appid");
							req.partnerId		= json2.getString("partnerid");
							req.prepayId		= json2.getString("prepayid");
							req.nonceStr		= json2.getString("noncestr");
							req.timeStamp		= json2.getString("timestamp");
							req.packageValue	= json2.getString("package");
							req.sign			= json2.getString("sign");
							req.extData			= "app data"; // optional
							Toast.makeText(PayActivity.this, "正常调起支付", Toast.LENGTH_SHORT).show();
							// 在支付之前，如果应用没有注册到微信，应该先调用IWXMsg.registerApp将应用注册到微信
							api.sendReq(req);
						} catch (JSONException e) {
		Log.e("PAY_GET", "异常："+e.getMessage());
		Toast.makeText(PayActivity.this, "异常："+e.getMessage(), Toast.LENGTH_SHORT).show();
						}
					}
				});
			}
		});
	}
	//获得订单号
	public String GetCustId(){
		return custOrderId;
	}
	private String getCustOrderId(String name){
		return System.currentTimeMillis()+name+"_"+"wxapp";
	}
	//获取渠道号
/*	public String getChannelId(){
		ApplicationInfo appInfo = null;
		try {
			appInfo = this.getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
			String msg=appInfo.metaData.getString("UMENG_CHANNEL");
			return msg;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}*/
	//ID
	public String getAndroidId() {
		String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
		return androidId;
	}


	public static String md5(String string) {
		if (TextUtils.isEmpty(string)) {
			return "";
		}
		MessageDigest md5 = null;
		try {
			md5 = MessageDigest.getInstance("MD5");
			byte[] bytes = md5.digest(string.getBytes());
			String result = "";
			for (byte b : bytes) {
				String temp = Integer.toHexString(b & 0xff);
				if (temp.length() == 1) {
					temp = "0" + temp;
				}
				result += temp;
			}
			return result;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
	}
}
	

