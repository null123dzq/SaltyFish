# 统计SDK接入文档(CountSDK)

### 1.在工程的lib目录下加入jar包：**count-sdk.jar**

### 2.在后台常驻进程的service的onCreate()方法中加入如下代码：

```
CountSDK.initCountSDKConfig(context);
```

### 3.在后台常驻进程的service的onDestory()方法中加入如下代码：

```
CountSDK.releaseCountSDKConfig(context);
```

### 4.在android manifest.xml 文件中加入一下权限：

```
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.READ_PHONE_STATE"/>
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
```

### 5.在android manifest.xml 文件的application节点下加入以下声明：

```
<meta-data android:name="count_sdk_channel" android:value="cola"></meta-data>

<service android:name="com.jolo.countsdk.CountDataService"></service>
```

- 注意：**在application节点下加入签名<meta-data android:name="count_sdk_channel" android:value="cola"></meta-data> 请一定不要忘记**
- 注意：**在application节点下加入签名<meta-data android:name="count_sdk_channel" android:value="cola"></meta-data> 请一定不要忘记**
- 注意：**在application节点下加入签名<meta-data android:name="count_sdk_channel" android:value="cola"></meta-data> 请一定不要忘记**

**以上组件的声明中，meta-data的value的值表示上传至服务器的渠道号。**

### Debug 调试统计SDk是否接入成功。在CountSDK.initCountSDKConfig(context)前面加入一句代码如下：

```
CountSDK.setDebugTrue();
CountSDK.initCountSDKConfig(context);
```

**混淆规则** ，接入方可以随意混淆。

