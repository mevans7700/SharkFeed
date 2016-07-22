package com.evansappwriter.sharkfeed.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.evansappwriter.sharkfeed.R;
import com.evansappwriter.sharkfeed.core.BundledData;
import com.evansappwriter.sharkfeed.core.PhotoListParser;
import com.evansappwriter.sharkfeed.core.PhotoListService;
import com.evansappwriter.sharkfeed.model.Photo;
import com.evansappwriter.sharkfeed.util.Keys;
import com.evansappwriter.sharkfeed.util.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class SharkActivity extends BaseActivity {
    private static final String TAG = "SharkActivity";
    private static final String STATE_FIRST_API = "state_first_api";
    private static final String STATE_PHOTO_INFO_BUNDLE = "state_photo_info_bundle";

    // folder name on the sdcard where the images will be saved
    private static final String FOLDER_NAME = "SharkFeed";

    private File mGalleryFolder;
    private String mPhotoPage;
    private Bundle mPhotoInfo;
    private boolean mFirstAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shark);

        verifyStoragePermissions(this);

        getSupportActionBar().hide();

        mGalleryFolder = createFolders();

        if (savedInstanceState != null) {
            mFirstAPI = savedInstanceState.getBoolean(STATE_FIRST_API, false);
            mPhotoInfo = savedInstanceState.getBundle(STATE_PHOTO_INFO_BUNDLE);
            mPhotoPage = mPhotoInfo.getString(Keys.KEY_PHOTOPAGE,"");
        } else {
            mPhotoPage = "";
            mFirstAPI = false;
        }

        final Bundle b = getIntent().getExtras();
        final Photo photo = b.getParcelable(Keys.KEY_PHOTO);

        ImageView photoIV = (ImageView) findViewById(R.id.img_shark_large);

        String url = "";
        if (!TextUtils.isEmpty(photo.getUrlLarge())) {
            url = photo.getUrlLarge();
        } else if (!TextUtils.isEmpty(photo.getUrlOriginal())) {
            url = photo.getUrlOriginal();
        } else if (!TextUtils.isEmpty(photo.getUrlMedium())) {
            url = photo.getUrlMedium();
        } else if ((!TextUtils.isEmpty(photo.getUrlThumb()))) {
            url = photo.getUrlThumb();
        }
        Glide.with(this).load(url)
                .thumbnail(0.5f)
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(photoIV);

        TextView descTV = (TextView) findViewById(R.id.desc_tv);
        descTV.setText(photo.getTitle());

        Button downBtn = (Button) findViewById(R.id.download_btn);
        downBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableProgress();
                Glide.with(getApplicationContext())
                .load(photo.getUrlOriginal())
                .asBitmap()
                .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {

                                // first check the external storage availability
                                if (!isExternalStorageAvailable()) {
                                    showMessage(getString(R.string.nostorage_title), getString(R.string.nostorage_text), null);
                                    return;
                                }

                                String cameraImagePath =  getNewImagePath();

                                try {
                                    FileOutputStream out = new FileOutputStream(cameraImagePath);
                                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                                    out.flush();
                                    out.close();

                                    // send a notification to the media scanner
                                    updateMedia(cameraImagePath);
                                } catch(Exception e) {
                                    Utils.printLogInfo(TAG,e.toString());
                                }
                                disableProgress();
                                showMessage(getString(R.string.download_completed_title),getString(R.string.download_completed_text),null);
                            }
                });
            }
        });

        // initialize photo webpage variable


        Button openBtn = (Button) findViewById(R.id.app_btn);
        openBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mPhotoPage));
                startActivity(browserIntent);
            }
        });

        if (!mFirstAPI) {
            // call API to get photo info
            getPhotoInfo(photo.getId());
        }
        mFirstAPI = true;
    }

    private void enableProgress() {
        View vLoading = findViewById(R.id.progress_send);
        vLoading.setVisibility(View.VISIBLE);
        View btnDown = findViewById(R.id.download_btn);
        btnDown.setVisibility(View.GONE);
        View btnApp = findViewById(R.id.app_btn);
        btnApp.setEnabled(false);
    }

    private void disableProgress() {
        View vLoading = findViewById(R.id.progress_send);
        vLoading.setVisibility(View.GONE);
        View btnDown = findViewById(R.id.download_btn);
        btnDown.setVisibility(View.VISIBLE);
        btnDown.setEnabled(false);
        View btnApp = findViewById(R.id.app_btn);
        btnApp.setEnabled(true);
    }

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * Checks if the app has permission to write to device storage
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    private File createFolders() {
        File baseDir;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
            baseDir = Environment.getExternalStorageDirectory();
        } else {
            baseDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        }

        if (baseDir == null) {
            return Environment.getExternalStorageDirectory();
        }

        Utils.printLogInfo(TAG, "Pictures folder: ", baseDir.getAbsolutePath());
        File SharkFolder = new File(baseDir, FOLDER_NAME);

        if (SharkFolder.exists()) {
            return SharkFolder;
        }

        if (SharkFolder.mkdirs()) {
            return SharkFolder;
        }

        return Environment.getExternalStorageDirectory();
    }

    // check the external storage status
    private boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public String getNewImagePath () {
        return mGalleryFolder.getPath() + String.format("/%d.jpg", System.currentTimeMillis());
    }

    // we need to notify the MediaScanner when a new file is created. In this way all the gallery applications will be notified too.
    private void updateMedia(String filepath) {
        Utils.printLogInfo(TAG, "updateMedia: ", filepath);
        MediaScannerConnection.scanFile(this, new String[]{filepath}, new String[]{"image/*"}, new MediaScannerConnection.OnScanCompletedListener() {
            @Override
            public void onScanCompleted(String path, Uri uri) {
                Utils.printLogInfo(TAG, "path: ", path);
                Utils.printLogInfo(TAG, "uri: ", uri);
            }
        });
    }

    private void getPhotoInfo(String id) {
        showProgress(getString(R.string.progress_photo_info));
        final Bundle params = new Bundle();
        params.putString(PhotoListService.PARAM_PHOTO_ID, id);
        PhotoListService.getInstance(this).get(PhotoListService.ENDPOINT_PHOTO_INFO, params, new PhotoListService.OnUIResponseHandler() {

            @Override
            public void onSuccess(String payload) {
                if (this == null || isFinishing()) {
                    return;
                }

                if (payload != null) {
                    BundledData data = new BundledData(PhotoListParser.TYPE_PARSER_PHOTO_INFO);
                    data.setHttpData(payload);
                    PhotoListParser.parseResponse(data);
                    if (data.getAuxData() == null) {
                        Utils.printLogInfo(TAG, "Parsing error: ", data.toString());
                    } else {
                        if (data.getAuxData().length == 1) {
                            mPhotoInfo = (Bundle) data.getAuxData()[0];
                            mPhotoPage = mPhotoInfo.getString(Keys.KEY_PHOTOPAGE,"");
                            // TODO: - any other fields

                        } else {
                            Utils.printLogInfo(TAG, "Parsing error: ", data.getAuxData()[1]);
                        }
                    }
                } else {
                    Utils.printLogInfo(TAG, "Payload error: ", "No Payload but a status code of 200");
                }
                dismissProgress();
            }

            @Override
            public void onFailure(String errorTitle, String errorText, int dialogId) {
                if (this == null || isFinishing()) {
                    return;
                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_FIRST_API, mFirstAPI);
        outState.putBundle(STATE_PHOTO_INFO_BUNDLE,mPhotoInfo);
    }
}
