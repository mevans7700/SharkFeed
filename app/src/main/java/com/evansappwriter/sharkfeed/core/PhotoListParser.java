package com.evansappwriter.sharkfeed.core;


import android.os.Bundle;

import com.evansappwriter.sharkfeed.model.Photo;
import com.evansappwriter.sharkfeed.util.Keys;
import com.evansappwriter.sharkfeed.util.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by markevans on 6/20/16.
 */
public class PhotoListParser {
    private static final String TAG = "TELMATE.PARSER";

    public static final String PHOTO_PAGE_TYPE = "photopage";

    public static final int TYPE_PARSER_ERROR = -1;
    public static final int TYPE_PARSER_NONE = 0;
    public static final int TYPE_PARSER_PHOTOS = 1;
    public static final int TYPE_PARSER_PHOTO_INFO = 2;


    // this class cannot be instantiated
    private PhotoListParser() {

    }

    public static void parseResponse(BundledData data) {
        int parserType = data.getParserType();

        Utils.printLogInfo(TAG, data.getHttpData());

        switch (parserType) {
            case TYPE_PARSER_PHOTOS:
                parsePhotoList(data);
                break;
            case TYPE_PARSER_PHOTO_INFO:
                parsePhotoInfo(data);
                break;
            case TYPE_PARSER_ERROR:
                parseError(data);
                break;
            case TYPE_PARSER_NONE:
            default:
                // no parse needed
                break;
        }
    }

    private static void parsePhotoList(BundledData data) {
        if (getStringObject(data.getHttpData()) == null) {
            data.setAuxData();
            return;
        }

        try {
            // starting to parse...
            JSONObject jObject = new JSONObject(data.getHttpData());
            if (jObject.isNull(Keys.KEY_PHOTOS)) {
                data.setAuxData();
                data.setAuxData(jObject.getString(Keys.KEY_STAT), jObject.getString(Keys.KEY_MESSAGE));
                return;
            }

            JSONObject jObt = jObject.getJSONObject(Keys.KEY_PHOTOS);
            JSONArray jsonArray = jObt.getJSONArray(Keys.KEY_PHOTO);
            // ensure resources get cleaned up timely and properly
            data.setHttpData(null);

            ArrayList<Photo> photos = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jo = jsonArray.getJSONObject(i);
                Photo photo = new Photo();
                photo.setId(jo.getString(Keys.KEY_ID));
                photo.setTitle(jo.getString(Keys.KEY_TITLE));
                photo.setUrlThumb(jo.isNull(Keys.KEY_URL_T) ? "" : jo.getString(Keys.KEY_URL_T));
                photo.setUrlMedium(jo.isNull(Keys.KEY_URL_C) ? "" : jo.getString(Keys.KEY_URL_C));
                photo.setUrlLarge(jo.isNull(Keys.KEY_URL_L) ? "" : jo.getString(Keys.KEY_URL_L));
                photo.setUrlOriginal(jo.isNull(Keys.KEY_URL_O) ? "" : jo.getString(Keys.KEY_URL_O));
                photos.add(photo);
            }
            data.setAuxData(photos);
        } catch (Exception e) {
            Utils.printStackTrace(e);
            data.setHttpData(null);
            data.setAuxData();
        }
    }

    private static void parsePhotoInfo(BundledData data) {
        if (getStringObject(data.getHttpData()) == null) {
            data.setAuxData();
            return;
        }

        try {
            JSONObject jObject = new JSONObject(data.getHttpData());
            if (jObject.isNull(Keys.KEY_PHOTO)) {
                data.setAuxData();
                data.setAuxData(jObject.getString(Keys.KEY_STAT), jObject.getString(Keys.KEY_MESSAGE));
                return;
            }

            Bundle b = new Bundle();

            JSONObject jObt = jObject.getJSONObject(Keys.KEY_PHOTO);
            JSONObject jObtUrls = jObt.getJSONObject(Keys.KEY_URLS);
            JSONArray jsonArray = jObtUrls.getJSONArray(Keys.KEY_URL);


            // ensure resources get cleaned up timely and properly
            data.setHttpData(null);

            String url = "";
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jo = jsonArray.getJSONObject(i);

                if (jo.getString(Keys.KEY_TYPE).equals(PHOTO_PAGE_TYPE)) {
                    url = jo.getString(Keys.KEY_CONTENT);
                }
            }
            b.putString(Keys.KEY_PHOTOPAGE, url);
            data.setAuxData(b);
        } catch (Exception e) {
            Utils.printStackTrace(e);
            data.setHttpData(null);
            data.setAuxData();
        }
    }

    private static void parseError(BundledData data) {
        if (getStringObject(data.getHttpData()) == null) {
            data.setAuxData("Bad Payload", data.getHttpData());
            return;
        }

        try {
            JSONObject json = new JSONObject(data.getHttpData());

            // ensure resources get cleaned up timely and properly
            data.setHttpData(null);


            String status = "" ;
            String status_msg = "";

            data.setAuxData(status, status_msg);
        } catch (Exception e) {
            Utils.printStackTrace(e);
            data.setAuxData("Server Error", data.getHttpData());
            data.setHttpData(null);
        }
    }

    private static String getStringObject(String txt) {
        return txt == null ? null : txt.equalsIgnoreCase("null") ? null : txt;
    }


}
