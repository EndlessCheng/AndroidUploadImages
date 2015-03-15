package com.endless.android.androiduploadimages;

import java.io.FileNotFoundException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;

public class UploadImageActivity extends Activity implements
        OnClickListener {
    private static final String TAG = UploadImageActivity.class
            .getSimpleName();

    private static final int MAX_FILE_SIZE = 1024 * 1024; // 1 MB

    // http://developer.android.com/guide/appendix/media-formats.html
    private static final String[] FILE_EXTENSIONS = { "bmp", "gif", "jpeg",
            "jpg", "png" };

    private static final String REQUEST_URL = "http://endlesscheng.sinaapp.com/books_management/upload-file";
    private static final String FIELD_NAME = "docfile";

    private Button mSelectImageButton;
    private Button mUploadImageButton;
    private ImageView mImageView;
    private String mPicturePath;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_image);

        mSelectImageButton = (Button) this.findViewById(R.id.select_image);
        mUploadImageButton = (Button) this.findViewById(R.id.upload_image);
        mSelectImageButton.setOnClickListener(this);
        mUploadImageButton.setOnClickListener(this);
        mImageView = (ImageView) this.findViewById(R.id.show_image_view);
        mPicturePath = null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.select_image:
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, RESULT_CANCELED);
                break;
            case R.id.upload_image:
                if (mPicturePath == null) {
                    alert("请选择文件！");
                    return;
                }
                if(!isNetworkAvailable()){
                    alert("网络未连接！");
                    return;
                }
                String mimeType = MimeTypeMap.getSingleton()
                        .getMimeTypeFromExtension(
                                MimeTypeMap.getFileExtensionFromUrl(mPicturePath));
                new UploadFileTask(this).execute(mPicturePath, mimeType,
                        REQUEST_URL, FIELD_NAME);
                break;
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            String path = convertUriToPath(uri);
            if (!isImageFileExtension(MimeTypeMap.getFileExtensionFromUrl(path))) {
                alert("不是有效的图片文件！");
                return;
            }
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(this
                        .getContentResolver().openInputStream(uri));
                if (bitmap.getByteCount() > MAX_FILE_SIZE) {
                    alert("图片文件过大！");
                    return;
                }
                mImageView.setImageBitmap(bitmap);
                mPicturePath = path;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private String convertUriToPath(Uri uri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, proj, null, null, null);
        if (cursor == null) {
            alert("不是有效的图片文件！");
            return null;
        }
        int colunm_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(colunm_index);
        Log.i(TAG, "path: " + path);
        return path;
    }

    private boolean isImageFileExtension(String fileExtension) {
        for (String extension : FILE_EXTENSIONS)
            if (fileExtension.equals(extension))
                return true;
        return false;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void alert(String msg) {
        Dialog dialog = new AlertDialog.Builder(this).setTitle("提示")
                .setMessage(msg)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // mPicturePath = null;
                    }
                }).create();
        dialog.show();
    }
}