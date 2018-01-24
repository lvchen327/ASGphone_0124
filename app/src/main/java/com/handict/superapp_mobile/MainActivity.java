package com.handict.superapp_mobile;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.umeng.analytics.MobclickAgent;
import com.umeng.analytics.game.UMGameAgent;
import com.unity3d.player.UnityPlayerActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity extends UnityPlayerActivity {
    Context mContext = null;
    private String mUnityObjStr;
    public String custOrderId = "dingdanweishengcheng";
    private IWXAPI api;
    private final String TAG = getClass().getSimpleName();
    private static final String INNER_URL = "http://api.yolkworld.net/Payment/AsgWxPay";
    String sign;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        api = WXAPIFactory.createWXAPI(this, "wx52d31ccb73e0222f");
//        custOrderId = getCustOrderId("ASG");
        mContext=this;
        UMGameAgent.setDebugMode(true);//设置输出运行时日志
        UMGameAgent.init(this);
    }
    public void Pay(String cust_order_id, String name, String num, final String objStr, final String ffm) {
        custOrderId = getCustOrderId("ASG");
        String stringA="appId=wx52d31ccb73e0222f&orderNo="+GetCustId()+"&key=#YIE9HNqIMvRaU8u";
        sign= md5(stringA);//注：MD5签名方式

        Toast.makeText(this, "获取订单中...", Toast.LENGTH_SHORT).show();

        //http://zhushou.72g.com/app/gift/gift_list/
        //请求参数`platform=2&gifttype=1&compare=60841c5b7c69a1bbb3f06536ed685a48
        OkHttpClient okHttpClient = new OkHttpClient();
        //TODO 和Get方式不一样的地方是， Post方式的请求参数 必须放入到请求体中才可以。
        //如果提交键值对，我们需要通过new FormBody.Builder()添加键值对。
        RequestBody requestBody = new FormBody.Builder()
                .add("OrderNo",custOrderId)
                .add("Price",num)
                .add("ClientIp",getIPAddress(this))
                .add("ProductName",name)
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
                        Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
                        try {
                            JSONObject json = new JSONObject(result);
                            String data = json.getString("data");
                            Log.i(TAG, "data: "+data);
                            JSONObject json2 = new JSONObject(data);
                            Log.i(TAG, "appid: "+json2.getString("appid"));
                            PayReq req = new PayReq();
                            req.appId			= json2.getString("appid");
                            req.partnerId		= json2.getString("partnerid");
                            req.prepayId		= json2.getString("prepayid");
                            req.nonceStr		= json2.getString("noncestr");
                            req.timeStamp		= json2.getString("timestamp");
                            req.packageValue	= json2.getString("package");
                            req.sign			= json2.getString("sign");
                            req.extData			= "app data"; // optional
                            Toast.makeText(MainActivity.this, "正常调起支付", Toast.LENGTH_SHORT).show();
                            // 在支付之前，如果应用没有注册到微信，应该先调用IWXMsg.registerApp将应用注册到微信
                            api.sendReq(req);
                        } catch (JSONException e) {
                            Log.e("PAY_GET", "异常："+e.getMessage());
                            Toast.makeText(MainActivity.this, "异常："+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

    }

//    public void StartActivity1(String url)
//    {
//        Log.d("1111", "StartActivity1: ----->");
//        Intent intent = new Intent();
//        intent.setClass(this, SkinActivity.class);
//
//        intent.putExtra("url", url);
//        this.startActivity(intent);
//    }
    private void StartActivity1(String url) {

//        String url = urlEdit.getText().toString();
//        String url="http://handict-supperapp-course.oss-cn-hangzhou.aliyuncs.com/ASGVIDEO/SG_dazuiyu.mp4";
        Intent intent = new Intent();
        intent.setClass(this, SkinActivity.class);
        intent.putExtra("type","localSource");
        intent.putExtra("url", url);
        startActivity(intent);
    }
    //友盟
    public void onResume() {
        super.onResume();
        UMGameAgent.onResume(this);
    }

    public void onPause() {
        super.onPause();
        UMGameAgent.onPause(this);
    }

    //ID
    public String getAndroidId(){
        String androidId = Settings.Secure.getString(getContentResolver(),Settings.Secure.ANDROID_ID);
        return androidId;
    }
    //获取渠道号
    public String getChannelId(){
        ApplicationInfo appInfo = null;
        try {
            appInfo = this.getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            String msg=appInfo.metaData.getString("UMENG_CHANNEL");
            return msg;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
    //自定义事件
    public void customEvent(String eventId){
        MobclickAgent.onEvent(mContext,eventId);
    }
    //获得订单号
    public String GetCustId(){
        return custOrderId;
    }
    private String getCustOrderId(String name){
        return System.currentTimeMillis()+name+"_"+"wxapp";
    }
    //获得实体名称
    public void SetObjString(String objStr){
        mUnityObjStr=objStr;
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

    //获得IP
    public static String getIPAddress(Context context) {
        NetworkInfo info = ((ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            if (info.getType() == ConnectivityManager.TYPE_MOBILE) {//当前使用2G/3G/4G网络
                try {
                    //Enumeration<NetworkInterface> en=NetworkInterface.getNetworkInterfaces();
                    for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                        NetworkInterface intf = en.nextElement();
                        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                            InetAddress inetAddress = enumIpAddr.nextElement();
                            if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                                return inetAddress.getHostAddress();
                            }
                        }
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                }

            } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {//当前使用无线网络
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String ipAddress = intIP2StringIP(wifiInfo.getIpAddress());//得到IPV4地址
                return ipAddress;
            }
        } else {
            //当前无网络连接,请在设置中打开网络
        }
        return null;
    }

    /**
     * 将得到的int类型的IP转换为String类型
     *
     * @param ip
     * @return
     */
    public static String intIP2StringIP(int ip) {
        return (ip & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                (ip >> 24 & 0xFF);
    }
}