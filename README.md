# GPS-Demo
Android原生GPS定位以及位置解析

### 优点：

 1、GPS方式准确度是最高的；

2、GPS走的是卫星通信的通道，即使在没有网络连接的情况下也能用。

## 缺点：

1、比较耗电；

2、绝大部分用户默认不开启GPS模块；（如果需要适配android6.0以上版本需要做权限申请）

3、从GPS模块启动到获取第一次定位数据，可能需要比较长的时间；

4、室内几乎无法使用。（这其中，缺点2,3都是比较致命的）



总结的可能不全面，后面如果有深入了解再行修改吧。

# 具体实现：

## 1、GPS设置
        
        // 判断GPS是否正常启动
        if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(context, "请开启GPS导航...", Toast.LENGTH_SHORT).show();
            // 返回开启GPS导航设置界面
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            context.startActivityForResult(intent, 0);
            return;
        }


## 2、设置查询条件

        // 为获取地理位置信息时设置查询条件
        String bestProvider = mLocationManager.getBestProvider(getCriteria(), true);

        private static Criteria getCriteria() {
           Criteria criteria = new Criteria();
           // 设置定位精确度 Criteria.ACCURACY_COARSE比较粗略，Criteria.ACCURACY_FINE则比较精细
           criteria.setAccuracy(Criteria.ACCURACY_FINE);
           // 设置是否要求速度
           criteria.setSpeedRequired(false);
           // 设置是否允许运营商收费
           criteria.setCostAllowed(false);
           // 设置是否需要方位信息
           criteria.setBearingRequired(false);
           // 设置是否需要海拔信息
           criteria.setAltitudeRequired(false);
           // 设置对电源的需求
           criteria.setPowerRequirement(Criteria.POWER_LOW);
           return criteria;
       }

## 3、获取地理位置
        // 获取位置信息
        // 如果不设置查询要求，getLastKnownLocation方法传人的参数为LocationManager.GPS_PROVIDER
        Location location = mLocationManager.getLastKnownLocation(bestProvider);

## 4、监听状态

        // 监听状态
        mLocationManager.addGpsStatusListener(listener);
        // 状态监听
        GpsStatus.Listener listener = new GpsStatus.Listener() {
            public void onGpsStatusChanged(int event) {
                switch (event) {
                    // 第一次定位
                    case GpsStatus.GPS_EVENT_FIRST_FIX:
                        Log.i(TAG, "第一次定位");
                        break;
                    // 卫星状态改变
                    case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                        Log.i(TAG, "卫星状态改变");
                        GpsStatus gpsStatus = mLocationManager.getGpsStatus(null);
                        // 获取卫星颗数的默认最大值
                        int maxSatellites = gpsStatus.getMaxSatellites();
                        // 创建一个迭代器保存所有卫星
                        Iterator<GpsSatellite> iters = gpsStatus.getSatellites()
                                .iterator();
                        int count = 0;
                        while (iters.hasNext() && count <= maxSatellites) {
                            GpsSatellite s = iters.next();
                            count++;
                        }
                        System.out.println("搜索到：" + count + "颗卫星");
                        break;
                    // 定位启动
                    case GpsStatus.GPS_EVENT_STARTED:
                        Log.i(TAG, "定位启动");
                        break;
                    // 定位结束
                    case GpsStatus.GPS_EVENT_STOPPED:
                        Log.i(TAG, "定位结束");
                        break;
                }
            }
        };


## 5、绑定监听
        // 绑定监听，有4个参数
        // 参数1，设备：有GPS_PROVIDER和NETWORK_PROVIDER两种
        // 参数2，位置信息更新周期，单位毫秒
        // 参数3，位置变化最小距离：当位置距离变化超过此值时，将更新位置信息
        // 参数4，监听
        // 备注：参数2和3，如果参数3不为0，则以参数3为准；参数3为0，则通过时间来定时更新；两者为0，则随时刷新

        // 1秒更新一次，或最小位移变化超过1米更新一次；
        // 注意：此处更新准确度非常低，推荐在service里面启动一个Thread，在run中sleep(10000);然后执行handler.sendMessage(),更新位置
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener)

        // 位置监听
        private static LocationListener locationListener = new LocationListener() {

            //位置信息变化时触发
            public void onLocationChanged(Location location) {
                mLocation = location;
                Log.i(TAG, "时间：" + location.getTime());
                Log.i(TAG, "经度：" + location.getLongitude());
                Log.i(TAG, "纬度：" + location.getLatitude());
                Log.i(TAG, "海拔：" + location.getAltitude());
            }

            //GPS状态变化时触发
            public void onStatusChanged(String provider, int status, Bundle extras) {
                switch (status) {
                    // GPS状态为可见时
                    case LocationProvider.AVAILABLE:
                        Log.i(TAG, "当前GPS状态为可见状态");
                        break;
                    // GPS状态为服务区外时
                    case LocationProvider.OUT_OF_SERVICE:
                        Log.i(TAG, "当前GPS状态为服务区外状态");
                        break;
                    // GPS状态为暂停服务时
                    case LocationProvider.TEMPORARILY_UNAVAILABLE:
                        Log.i(TAG, "当前GPS状态为暂停服务状态");
                        break;
                }
            }

            //GPS开启时触发
            public void onProviderEnabled(String provider) {
                Location location = mLocationManager.getLastKnownLocation(provider);
                mLocation = location;
            }

            //GPS禁用时触发
            public void onProviderDisabled(String provider) {
                mLocation = null;
            }
        };
## 6、AndroidManifest权限添加
    <!-- 粗略定位授权 -->
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
    <!-- 精细定位授权 -->
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>


到此就结束了。
