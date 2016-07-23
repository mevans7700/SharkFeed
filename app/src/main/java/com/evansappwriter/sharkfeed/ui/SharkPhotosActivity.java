package com.evansappwriter.sharkfeed.ui;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Parcelable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.evansappwriter.sharkfeed.util.EndlessRecyclerViewScrollListener;
import com.evansappwriter.sharkfeed.R;
import com.evansappwriter.sharkfeed.util.SharksAdapter;
import com.evansappwriter.sharkfeed.core.BundledData;
import com.evansappwriter.sharkfeed.core.PhotoListParser;
import com.evansappwriter.sharkfeed.core.PhotoListService;
import com.evansappwriter.sharkfeed.model.Photo;
import com.evansappwriter.sharkfeed.util.Keys;
import com.evansappwriter.sharkfeed.util.Utils;

import java.util.ArrayList;

public class SharkPhotosActivity extends BaseActivity {
    private static final String TAG = "SharkPhotosActivity";
    private static final String STATE_FIRST_API = "state_first_api";
    private static final String STATE_SHARKS = "state_sharks";
    private Context mContext;

    private SwipeRefreshLayout mSwipeContainer;

    private RecyclerView mRecyclerView;

    private SharksAdapter mAdapter;
    private GridLayoutManager mLayoutManager;

    private ArrayList<Photo> mSharks;

    private boolean mFirstAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shark_photos);

        if (savedInstanceState != null) {
            mFirstAPI = savedInstanceState.getBoolean(STATE_FIRST_API, false);
            mSharks = savedInstanceState.getParcelableArrayList(STATE_SHARKS);
        } else {
            mFirstAPI = false;
            mSharks = new ArrayList<>();
        }

        mContext = getApplicationContext();

        // Get the widgets reference from XML layout
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mSwipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);

        // Define a layout for RecyclerView
        mLayoutManager = new GridLayoutManager(mContext,3);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // Initialize a new instance of RecyclerView Adapter instance
        mAdapter = new SharksAdapter(mContext, mSharks);

        // Set the adapter for RecyclerView
        mRecyclerView.setAdapter(mAdapter);

        // Setup refresh listener which triggers new data loading
        mSwipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!isNetworkAvailable()) {
                    Toast.makeText(SharkPhotosActivity.this, getString(R.string.error_no_connection), Toast.LENGTH_SHORT).show();
                    mSwipeContainer.setRefreshing(false);
                    return;
                }
                getSharkImages(0);
            }
        });

        // item click listener
        mRecyclerView.addOnItemTouchListener(new SharksAdapter.RecyclerTouchListener(mContext, mRecyclerView, new SharksAdapter.ClickListener() {

            @Override
            public void onClick(View view, int position) {
                if (!isNetworkAvailable()) {
                    Toast.makeText(SharkPhotosActivity.this, getString(R.string.error_no_connection), Toast.LENGTH_SHORT).show();
                    return;
                }
                Photo p = mAdapter.getItem(position);
                Intent i = new Intent(SharkPhotosActivity.this,SharkActivity.class);
                i.putExtra(Keys.KEY_PHOTO, p);
                startActivity(i);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        // scroll listener
        mRecyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(mLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                if (!isNetworkAvailable()) {
                    return;
                }
                getSharkImages(page);
            }
        });

        if (!mFirstAPI) {
            showProgress(getString(R.string.progress_sharks));
            getSharkImages(0);
        }
        mFirstAPI = true;

    }

    private void getSharkImages(int page) {
        final Bundle params = new Bundle();
        params.putString(PhotoListService.PARAM_PAGE, String.valueOf(page));

        PhotoListService.getInstance(mContext).get(PhotoListService.ENDPOINT_SHARK_SEARCH, params, new PhotoListService.OnUIResponseHandler() {

            @Override
            public void onSuccess(String payload) {
                if (mContext == null || isFinishing()) {
                    return;
                }

                if (payload != null) {
                    BundledData data = new BundledData(PhotoListParser.TYPE_PARSER_PHOTOS);
                    data.setHttpData(payload);
                    PhotoListParser.parseResponse(data);
                    if (data.getAuxData() == null) {
                        Utils.printLogInfo(TAG, "Parsing error: ", data.toString());
                        showError(getString(R.string.error_title), getString(R.string.photo_get_error), null);
                    } else {
                        if (data.getAuxData().length == 1) {
                            mSharks = (ArrayList<Photo>) data.getAuxData()[0];
                            if (mSwipeContainer.isRefreshing()) {
                                mSwipeContainer.setRefreshing(false);
                            }
                            if (params.getString(PhotoListService.PARAM_PAGE, "0").equals("0")) {
                                mAdapter.showNew(mSharks);
                            } else {
                                mAdapter.showOlder(mSharks);
                            }
                        } else {
                            Utils.printLogInfo(TAG, "Parsing error: ", data.getAuxData()[1]);
                            showError(getString(R.string.error_title), getString(R.string.photo_get_error), null);
                        }
                    }
                } else {
                    Utils.printLogInfo(TAG, "Payload error: ", "No Payload but a status code of 200");
                    //mActivity.showError(getString(R.string.error_title), getString(R.string.photo_get_error), null);
                }
                dismissProgress();
            }

            @Override
            public void onFailure(String errorTitle, String errorText, int dialogId) {
                if (mContext == null || isFinishing()) {
                    return;
                }

                dismissProgress();
                showError(getString(R.string.error_title), getString(R.string.photo_get_error), null);
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_FIRST_API, mFirstAPI);
        outState.putParcelableArrayList(STATE_SHARKS, mSharks);
    }
}
