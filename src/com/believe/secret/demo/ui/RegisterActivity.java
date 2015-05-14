package com.believe.secret.demo.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import cn.bmob.im.bean.BmobChatUser;
import cn.bmob.im.util.BmobLog;
import cn.bmob.v3.BmobInstallation;
import cn.bmob.v3.listener.SaveListener;

import com.believe.secret.demo.R;
import com.believe.secret.demo.bean.User;
import com.believe.secret.demo.config.BmobConstants;
import com.believe.secret.demo.util.CommonUtils;

public class RegisterActivity extends BaseActivity {

	Button btn_register;
	EditText et_username, et_password, et_email;
	BmobChatUser currentUser;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);

		initTopBarForLeft("ע��");

		et_username = (EditText) findViewById(R.id.et_username);
		et_password = (EditText) findViewById(R.id.et_password);
		et_email = (EditText) findViewById(R.id.et_email);

		btn_register = (Button) findViewById(R.id.btn_register);
		btn_register.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				register();
			}
		});
	}
	
	private void register(){
		String name = et_username.getText().toString();
		String password = et_password.getText().toString();
		String pwd_again = et_email.getText().toString();
		
		if (TextUtils.isEmpty(name)) {
			ShowToast(R.string.toast_error_username_null);
			return;
		}

		if (TextUtils.isEmpty(password)) {
			ShowToast(R.string.toast_error_password_null);
			return;
		}
		if (!pwd_again.equals(password)) {
			ShowToast(R.string.toast_error_comfirm_password);
			return;
		}
		
		boolean isNetConnected = CommonUtils.isNetworkAvailable(this);
		if(!isNetConnected){
			ShowToast(R.string.network_tips);
			return;
		}
		
		final ProgressDialog progress = new ProgressDialog(RegisterActivity.this);
		progress.setMessage("����ע��...");
		progress.setCanceledOnTouchOutside(false);
		progress.show();
		//����ÿ��Ӧ�õ�ע����������϶���һ������IM sdkδ�ṩע�᷽�����û��ɰ���bmod SDK��ע�᷽ʽ����ע�ᡣ
		//ע���ʱ����Ҫע�����㣺1��User���а��豸id��type��2���豸���а�username�ֶ�
		final User bu = new User();
		bu.setUsername(name);
		bu.setPassword(password);
		//��user���豸id���а�
		bu.setDeviceType("android");
		bu.setInstallId(BmobInstallation.getInstallationId(this));
		bu.signUp(RegisterActivity.this, new SaveListener() {

			@Override
			public void onSuccess() {
				// TODO Auto-generated method stub
				progress.dismiss();
				ShowToast("ע��ɹ�");
				// ���豸��username���а�
				userManager.bindInstallationForRegister(bu.getUsername());
				//���µ���λ����Ϣ
				updateUserLocation();
				//���㲥֪ͨ��½ҳ���˳�
				sendBroadcast(new Intent(BmobConstants.ACTION_REGISTER_SUCCESS_FINISH));
				// ������ҳ
				Intent intent = new Intent(RegisterActivity.this,MainActivity.class);
				startActivity(intent);
				finish();
				
			}

			@Override
			public void onFailure(int arg0, String arg1) {
				// TODO Auto-generated method stub
				BmobLog.i(arg1);
				ShowToast("ע��ʧ��:" + arg1);
				progress.dismiss();
			}
		});
	}

}
