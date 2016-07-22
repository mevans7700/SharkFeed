package com.evansappwriter.sharkfeed.core;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;

import com.evansappwriter.sharkfeed.R;
import com.evansappwriter.sharkfeed.util.Keys;
import com.evansappwriter.sharkfeed.util.Utils;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Set;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.protocol.HTTP;

/**
 * Created by markevans on 6/20/16.
 */
public class PhotoListService {
    private static final String TAG = "TELMATE.SERVICE";

    private static PhotoListService mInstance = null;

    private static final int TIMEOUT_READ = 60000; // ms
    private static final int TIMEOUT_CONNECT = 15000; // ms

    // Standard and Demo ROMS have different api_key
    private static final String API_KEY = "949e98778755d1982f537d56236bbb42";

    @SuppressWarnings("ConstantConditions")
    private static final String REST_API = " https://api.flickr.com";

    public static final String ENDPOINT_SHARK_SEARCH = "/services/rest/?method=flickr.photos.search&format=json&nojsoncallback=1&text=shark&extras=url_t,url_c,url_l,url_o";
    public static final String ENDPOINT_PHOTO_INFO = "/services/rest/?method=flickr.photos.getInfo&format=json&nojsoncallback=1";


    private static Resources mRes;

    public interface OnUIResponseHandler {
        void onSuccess(String payload);
        void onFailure(String errorTitle, String errorText, int dialogId);
    }

    // private constructor prevents instantiation from other classes
    private PhotoListService() {

    }

    /**
     * Creates a new instance of MovieListService.
     */
    public static PhotoListService getInstance(Context context) {

        if (mInstance == null) {
            mInstance = new PhotoListService();
        }

        mRes = context.getResources();

        return mInstance;
    }

    /**
     * *******************************************************************************************************
     */

    public void getMockSharks(Context context, OnUIResponseHandler handler) {
        InputStream in_s = context.getResources().openRawResource(R.raw.sharks);

        StringBuffer fileContent = new StringBuffer("");

        byte[] buffer = new byte[1024];

        try {
            while (in_s.read(buffer) != -1) {
                fileContent.append(new String(buffer));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        handler.onSuccess(fileContent.toString());
    }

    public void get(final String endpoints, Bundle params, final OnUIResponseHandler handler) {
        Bundle urlParams = getAuthBundle();
        if (params != null) {
            urlParams.putAll(params);
        }

        String uri = REST_API + endpoints;
        uri += "&" + encodeUrl(urlParams);

        Utils.printLogInfo(TAG, "API URL: " + uri);
        AsyncHttpClient aClient = new AsyncHttpClient();
        aClient.setTimeout(TIMEOUT_READ);
        aClient.get(uri, null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Utils.printLogInfo(TAG, "- Successful !: " + statusCode);

                processSuccessRepsonse(handler, new String(responseBody));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable e) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                Utils.printLogInfo(TAG, "- Failed !: " + statusCode);

                processFailureRepsonse(handler, new String(responseBody), e.toString());
            }
        });
    }

    public void post(String endpoints, Bundle params, final OnUIResponseHandler handler) {
        Bundle urlParams = getAuthBundle();

        String uri = REST_API + endpoints;
        uri += "?" + encodeUrl(urlParams);

        RequestParams requestparams = new RequestParams();
        for (String key : params.keySet()) {
            requestparams.put(key, params.get(key).toString());
        }

        Utils.printLogInfo(TAG, "API URL: " + uri);
        AsyncHttpClient aClient = new AsyncHttpClient();
        aClient.setTimeout(TIMEOUT_READ);
        aClient.post(uri, requestparams, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Utils.printLogInfo(TAG, "- Successful !: " + statusCode);

                processSuccessRepsonse(handler, new String(responseBody));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable e) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                Utils.printLogInfo(TAG, "- Failed !: " + statusCode);

                processFailureRepsonse(handler, new String(responseBody), e.toString());
            }
        });
    }

    private Bundle getAuthBundle() {
        Bundle params = new Bundle();

        params.putString(PARAM_API_KEY, API_KEY);

        return params;
    }

    public static String encodeUrl(Bundle parameters) {
        if (parameters == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder(200);
        boolean first = true;
        Set<String> keySet = parameters.keySet();

        for (String key : keySet) {
            Object parameter = parameters.get(key);

            if (!(parameter instanceof String)) {
                continue;
            }

            if (first) {
                first = false;
            } else {
                sb.append('&');
            }
            try {
                sb.append(URLEncoder.encode(key, HTTP.UTF_8));
            } catch (UnsupportedEncodingException e) {
                Utils.printStackTrace(e);
            }
            sb.append('=');
            try {
                sb.append(URLEncoder.encode(parameters.getString(key), HTTP.UTF_8));
            } catch (UnsupportedEncodingException e) {
                Utils.printStackTrace(e);
            }
        }
        return sb.toString();
    }

    private void processSuccessRepsonse(OnUIResponseHandler handler, String payload) {
        handler.onSuccess(payload);
    }

    private void processFailureRepsonse(OnUIResponseHandler handler, String payload, String exception) {
        String status = "";
        String status_msg = "";
        int dialogId = Keys.DIALOG_GENERAL_ERROR;
        if (payload != null) {
            BundledData data = new BundledData(PhotoListParser.TYPE_PARSER_ERROR);
            data.setHttpData(payload);
            PhotoListParser.parseResponse(data);
            status = (String) data.getAuxData()[0];
            status_msg = (String) data.getAuxData()[1];
            Utils.printLogInfo(TAG, "API error: ", status, status_msg);
        } else {
            //status = mRes.getString(R.string.error_title);
            //status_msg = mRes.getString(R.string.error_text);

            Utils.printLogInfo(TAG, "API error: ", exception);
        }

        handler.onFailure(status, status_msg, dialogId);
    }

    // PARAMS >>>>>>>>>

    public static final String PARAM_API_KEY = "api_key";
    public static final String PARAM_PAGE = "page";
    public static final String PARAM_PHOTO_ID = "photo_id";
}
