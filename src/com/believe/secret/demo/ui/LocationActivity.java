package com.believe.secret.demo.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import cn.bmob.im.util.BmobLog;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfigeration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.believe.secret.demo.R;
import com.believe.secret.demo.view.HeaderLayout.onRightImageButtonClickListener;

/**
 * ���ڷ���λ�õĽ���
 * 
 * @ClassName: LocationActivity
 * @Description: TODO
 * @author smile
 * @date 2014-6-23 ����3:17:05
 */
public class LocationActivity extends BaseActivity implements
		OnGetGeoCoderResultListener {

	// ��λ���
	LocationClient mLocClient;
	public MyLocationListenner myListener = new MyLocationListenner();
	BitmapDescriptor mCurrentMarker;

	MapView mMapView;
	BaiduMap mBaiduMap;

	private BaiduReceiver mReceiver;// ע��㲥�����������ڼ��������Լ���֤key

	GeoCoder mSearch = null; // ����ģ�飬��Ϊ�ٶȶ�λsdk�ܹ��õ���γ�ȣ�����ȴ�޷��õ��������ϸ��ַ�������Ҫ��ȡ�����뷽ʽȥ�����˾�γ�ȴ���ĵ�ַ

	static BDLocation lastLocation = null;

	BitmapDescriptor bdgeo = BitmapDescriptorFactory.fromResource(R.drawable.icon_geo); 
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_location);
		initBaiduMap();
	}

	private void initBaiduMap() {
		// ��ͼ��ʼ��
		mMapView = (MapView) findViewById(R.id.bmapView);
		mBaiduMap = mMapView.getMap();
		//�������ż���
		mBaiduMap.setMaxAndMinZoomLevel(18, 13);
		// ע�� SDK �㲥������
		IntentFilter iFilter = new IntentFilter();
		iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
		iFilter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);
		mReceiver = new BaiduReceiver();
		registerReceiver(mReceiver, iFilter);

		Intent intent = getIntent();
		String type = intent.getStringExtra("type");
		if (type.equals("select")) {// ѡ����λ��
			initTopBarForBoth("λ��", R.drawable.btn_login_selector, "����",
					new onRightImageButtonClickListener() {

						@Override
						public void onClick() {
							// TODO Auto-generated method stub
							gotoChatPage();
						}
					});
			mHeaderLayout.getRightImageButton().setEnabled(false);
			initLocClient();
		} else {// �鿴��ǰλ��
			initTopBarForLeft("λ��");
			Bundle b = intent.getExtras();
			LatLng latlng = new LatLng(b.getDouble("latitude"), b.getDouble("longtitude"));//ά����ǰ�������ں�
			mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(latlng));
			//��ʾ��ǰλ��ͼ��
			OverlayOptions ooA = new MarkerOptions().position(latlng).icon(bdgeo).zIndex(9);
			mBaiduMap.addOverlay(ooA);
		}

		mSearch = GeoCoder.newInstance();
		mSearch.setOnGetGeoCodeResultListener(this);

	}

	/**
	 * �ص��������
	 * @Title: gotoChatPage
	 * @Description: TODO
	 * @param
	 * @return void
	 * @throws
	 */
	private void gotoChatPage() {
		if(lastLocation!=null){
			Intent intent = new Intent();
			intent.putExtra("y", lastLocation.getLongitude());// ����
			intent.putExtra("x", lastLocation.getLatitude());// ά��
			intent.putExtra("address", lastLocation.getAddrStr());
			setResult(RESULT_OK, intent);
			this.finish();
		}else{
			ShowToast("��ȡ����λ����Ϣʧ��!");
		}
	}

	private void initLocClient() {
//		 ������λͼ��
		mBaiduMap.setMyLocationEnabled(true);
		mBaiduMap.setMyLocationConfigeration(new MyLocationConfigeration(
				com.baidu.mapapi.map.MyLocationConfigeration.LocationMode.NORMAL, true, null));
		// ��λ��ʼ��
		mLocClient = new LocationClient(this);
		mLocClient.registerLocationListener(myListener);
		LocationClientOption option = new LocationClientOption();
		option.setProdName("bmobim");// ���ò�Ʒ��
		option.setOpenGps(true);// ��gps
		option.setCoorType("bd09ll"); // ������������
		option.setScanSpan(1000);
		option.setOpenGps(true);
		option.setIsNeedAddress(true);
		option.setIgnoreKillProcess(true);
		mLocClient.setLocOption(option);
		mLocClient.start();
		if (mLocClient != null && mLocClient.isStarted())
		    mLocClient.requestLocation();

		if (lastLocation != null) {
			// ��ʾ�ڵ�ͼ��
			LatLng ll = new LatLng(lastLocation.getLatitude(),
					lastLocation.getLongitude());
			MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
			mBaiduMap.animateMapStatus(u);
		}
	}

	/**
	 * ��λSDK��������
	 */
	public class MyLocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			// map view ���ٺ��ڴ����½��յ�λ��
			if (location == null || mMapView == null)
				return;

			if (lastLocation != null) {
				if (lastLocation.getLatitude() == location.getLatitude()
						&& lastLocation.getLongitude() == location
						.getLongitude()) {
					BmobLog.i("��ȡ������ͬ");// �����������ȡ���ĵ���λ����������ͬ�ģ����ٶ�λ
					mLocClient.stop();
					return;
				}
			}
			lastLocation = location;
			
			BmobLog.i("lontitude = " + location.getLongitude() + ",latitude = "
					+ location.getLatitude() + ",��ַ = "
					+ lastLocation.getAddrStr());

			MyLocationData locData = new MyLocationData.Builder()
					.accuracy(location.getRadius())
					// �˴����ÿ����߻�ȡ���ķ�����Ϣ��˳ʱ��0-360
					.direction(100).latitude(location.getLatitude())
					.longitude(location.getLongitude()).build();
			mBaiduMap.setMyLocationData(locData);
			LatLng ll = new LatLng(location.getLatitude(),
					location.getLongitude());
			String address = location.getAddrStr();
			if (address != null && !address.equals("")) {
				lastLocation.setAddrStr(address);
			} else {
				// ��Geo����
				mSearch.reverseGeoCode(new ReverseGeoCodeOption().location(ll));
			}
			// ��ʾ�ڵ�ͼ��
			MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
			mBaiduMap.animateMapStatus(u);
			//���ð�ť�ɵ��
			mHeaderLayout.getRightImageButton().setEnabled(true);
		}

	}

	/**
	 * ����㲥�����࣬���� SDK key ��֤�Լ������쳣�㲥
	 */
	public class BaiduReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			String s = intent.getAction();
			if (s.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR)) {
				ShowToast("key ��֤����! ���� AndroidManifest.xml �ļ��м�� key ����");
			} else if (s
					.equals(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR)) {
				ShowToast("�������");
			}
		}
	}

	@Override
	public void onGetGeoCodeResult(GeoCodeResult arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
		// TODO Auto-generated method stub
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			ShowToast("��Ǹ��δ���ҵ����");
			return;
		}
		BmobLog.i("������õ��ĵ�ַ��" + result.getAddress());
		lastLocation.setAddrStr(result.getAddress());
	}

	@Override
	protected void onPause() {
		mMapView.onPause();
		super.onPause();
		lastLocation = null;
	}

	@Override
	protected void onResume() {
		mMapView.onResume();
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		if(mLocClient!=null && mLocClient.isStarted()){
			// �˳�ʱ���ٶ�λ
			mLocClient.stop();
		}
		// �رն�λͼ��
		mBaiduMap.setMyLocationEnabled(false);
		mMapView.onDestroy();
		mMapView = null;
		// ȡ������ SDK �㲥
		unregisterReceiver(mReceiver);
		super.onDestroy();
		// ���� bitmap ��Դ
		bdgeo.recycle();
	}

}
