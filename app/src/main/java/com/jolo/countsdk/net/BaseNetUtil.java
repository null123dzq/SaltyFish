package com.jolo.countsdk.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.jolo.countsdk.config.SPConstants;
import com.jolo.countsdk.net.bean.ClientInfo;
import com.jolo.countsdk.net.bean.UserAgent;
import com.jolo.countsdk.net.request.BaseReq;
import com.jolo.countsdk.net.response.BaseResp;
import com.jolo.countsdk.util.JsonParser;
import com.jolo.countsdk.util.LocationUtil;
import com.jolo.countsdk.util.SLog;
import com.jolo.countsdk.util.SharedPreferencesUtil;
import com.jolo.countsdk.util.VersionUtil;

/**
 * Description: 网络请求
 * Created by dzq on 2016/10/11.
 */

public abstract class BaseNetUtil<T extends BaseNetData, Q extends BaseReq, P extends BaseResp> {

    private static final String TAG = BaseNetUtil.class.getSimpleName();

    private static ConnectivityManager cm;
    private UserAgent ua;

    public static void init(Context ctx) {
        BaseNetUtil.cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    private static class NetBean {
        Callbacks callbacks;
        BaseReq req;
        Class<? extends BaseResp> respClass;

        Exception e;
        BaseResp resp;

        NetBean(BaseReq req, Class<? extends BaseResp> respClass, Callbacks callbacks) {
            this.callbacks = callbacks;
            this.req = req;
            this.respClass = respClass;
        }
    }

    public final void postRequest(Callbacks callbacks) {
        boolean isNetConnected = checkNetwork();
        if (!isNetConnected) {
            callbacks.onNetError();
            return;
        }
        Q request = getRequest();
        request.setUserAgent(getUA());
        SLog.i(TAG, "start post request");
        new NetTask().execute(new NetBean(request, getRespClass(), callbacks));
    }

    private class NetTask extends AsyncTask<NetBean, Void, NetBean> {

        @Override
        protected NetBean doInBackground(NetBean... params) {
            SLog.i(TAG, "doInBackground");
            NetBean netBean = params[0];
            OkHttpWrapper<Q, P> okHttpWrapper = new OkHttpWrapper<>((Q) netBean.req, getUrl());
            try {
                String result = okHttpWrapper.postMethod();
                SLog.d(TAG,"There is result json: " + result);
                netBean.resp = JsonParser.fromJson(result, getRespClass());
                if (netBean.resp != null) {
                    System.out.print("原始response响应码的：" + netBean.resp.toString());
                }
                return netBean;
            } catch (Exception e) {
                netBean.e = e;
                e.printStackTrace();
                System.err.print("原始response响应码Exception：" + e.toString());
                return netBean;
            }
        }

        @Override
        protected void onPostExecute(NetBean netBean) {
//            SLog.i(TAG, "netBean:code->" +", msg->" + netBean.resp.getResponseMsg());

            if (netBean.e != null) {
                netBean.callbacks.onError(netBean.e);

            } else {
                P resp = (P) netBean.resp;
                if (resp == null){
                    netBean.callbacks.onFailed();
                    return;
                }
                Integer responseCode = resp.getResponseCode();
                T data = parseResponse(resp);

                if (responseCode != null && responseCode == 200) {
                    netBean.callbacks.onSuccess(data);
                }/*else if (responseCode == null) {
                    netBean.callbacks.onOther();
                }*/
                //TODO
            }
        }
    }

    private static boolean checkNetwork() {

        NetworkInfo[] allNetworkInfo = cm.getAllNetworkInfo();
        for (NetworkInfo networkInfo : allNetworkInfo) {
            if (networkInfo.isConnected()) {
                return true;
            }
        }
        return false;
    }

    protected abstract Context getContext();

    protected abstract Q getRequest();

    protected abstract String getUrl();

    protected abstract T parseResponse(P response);

    protected abstract Class<? extends BaseResp> getRespClass();

    public interface Callbacks<T extends BaseNetData, P extends BaseResp> {

        void onFailed();

        void onError(Exception e);

        void onNetError();

        void onSuccess(@NonNull T result);

//        void onOther();
    }

    private UserAgent getUA() {
        if (ua == null) {
            ua = new UserAgent();
            ClientInfo clientInfo = ClientInfo.initClientInfo(getContext());
            if (null != clientInfo) {
                ua.setAndroidSystemVer(clientInfo.androidVer);
                ua.setApkVer(clientInfo.apkVerName);
                ua.setApkverInt(clientInfo.apkVerCode);
                ua.setCpu(clientInfo.cpu);
                ua.setHsman(clientInfo.hsman);
                ua.setHstype(clientInfo.hstype);
                ua.setImei(clientInfo.imei);
                ua.setImsi(clientInfo.imsi);
                ua.setNetworkType(ClientInfo.networkType);
                ua.setPackegeName(clientInfo.packageName);
                ua.setProvider(clientInfo.provider);
                ua.setChannelCode(clientInfo.channelCode);
                ua.setRamSize(clientInfo.ramSize);
                ua.setRomSize(clientInfo.romSize);
                ua.setScreenSize(clientInfo.screenSize);
                ua.setDpi(clientInfo.dpi);
                ua.setMac(clientInfo.mac);
                ua.setTerminalId(SharedPreferencesUtil.getString(getContext(), SPConstants.KEY_TERMINAL_ID, ""));
                ua.setLat(LocationUtil.getLatitude(getContext()));
                ua.setLng(LocationUtil.getLongitude(getContext()));
                ua.setInstallTime(VersionUtil.getFirstInstallAppTime(getContext()));
            }
        } else {
            ua.setNetworkType(ClientInfo.networkType); // 网络类型经常发生变化,每次要重新设置
        }

        return ua;
    }
}
