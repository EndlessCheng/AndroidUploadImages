package com.easyway.fileupload;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class UploadFileTask extends AsyncTask<String, Void, Integer> {
	private static final String[] MSG = { "上传成功！", "上传失败！", "文件不存在！" };

	private Activity mActivity;
	private ProgressDialog mProgressDialog;

	public UploadFileTask(Activity activity) {
		mActivity = activity;
		mProgressDialog = ProgressDialog.show(mActivity, "正在上传...",
				"系统正在处理您的请求");
	}

	@Override
	protected Integer doInBackground(String... params) {
		try {
			return UploadUtils.postFileToURL(new File(params[0]), params[1],
					new URL(params[2]), params[3]);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return UploadUtils.FAILURE;
	}

	@Override
	protected void onPostExecute(Integer result) {
		mProgressDialog.dismiss();
		Toast.makeText(mActivity, MSG[-result], Toast.LENGTH_LONG).show();
	}
}