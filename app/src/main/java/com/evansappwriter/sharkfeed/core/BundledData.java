package com.evansappwriter.sharkfeed.core;

public class BundledData {
    private int mParserType = 0;
    private String mHttpData = null;
    private Object[] mAuxData = null;

    public BundledData(int parserType) {
        mParserType = parserType;
    }

    /**
     * @param parserType the parserType to set
     */
    public final void setParserType(int parserType) {
        mParserType = parserType;
    }

    /**
     * @return the parserType
     */
    public final int getParserType() {
        return mParserType;
    }

    /**
     * @param httpData the httpData to set
     */
    public final void setHttpData(String httpData) {
        mHttpData = httpData;
    }

    /**
     * @return the httpData
     */
    public final String getHttpData() {
        return mHttpData;
    }

    /**
     * @param auxData the auxData to set
     */
    public final void setAuxData(Object... auxData) {
        if (auxData == null || auxData.length == 0) {
            mAuxData = null;
            return;
        }

        mAuxData = auxData;
    }

    /**
     * @return the auxData
     */
    public final Object[] getAuxData() {
        return mAuxData;
    }
}
