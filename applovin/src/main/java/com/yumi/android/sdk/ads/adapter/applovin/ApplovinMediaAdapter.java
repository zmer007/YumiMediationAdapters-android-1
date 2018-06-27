package com.yumi.android.sdk.ads.adapter.applovin;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;

import com.applovin.adview.AppLovinIncentivizedInterstitial;
import com.applovin.sdk.AppLovinAd;
import com.applovin.sdk.AppLovinAdClickListener;
import com.applovin.sdk.AppLovinAdDisplayListener;
import com.applovin.sdk.AppLovinAdLoadListener;
import com.applovin.sdk.AppLovinAdRewardListener;
import com.applovin.sdk.AppLovinAdVideoPlaybackListener;
import com.applovin.sdk.AppLovinErrorCodes;
import com.applovin.sdk.AppLovinSdk;
import com.yumi.android.sdk.ads.beans.YumiProviderBean;
import com.yumi.android.sdk.ads.publish.adapter.YumiCustomerMediaAdapter;
import com.yumi.android.sdk.ads.publish.enumbean.LayerErrorCode;
import com.yumi.android.sdk.ads.utils.ZplayDebug;

import java.util.Map;

public class ApplovinMediaAdapter extends YumiCustomerMediaAdapter {

    private static final String TAG = "ApplovinMediaAdapter";
    private static final int REQUEST_NEXT_MEDIA = 0x001;
    private AppLovinSdk appLovinSDK;
    private AppLovinIncentivizedInterstitial mediaAd;
    private AppLovinAdLoadListener adLoadListener;

    private AppLovinAdRewardListener adRewardListener;
    private AppLovinAdVideoPlaybackListener adVideoPlaybackListener;
    private AppLovinAdDisplayListener adDisplayListener;
    private AppLovinAdClickListener adClickListener;

    private boolean isFirstClick = false;

    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REQUEST_NEXT_MEDIA:
                    preloadAd();
                    break;
                default:
                    break;
            }
        }

        ;
    };

    protected ApplovinMediaAdapter(Activity activity, YumiProviderBean provider) {
        super(activity, provider);
    }

    @Override
    public void onActivityPause() {

    }

    @Override
    public void onActivityResume() {

    }

    @Override
    protected void onPrepareMedia() {
        ZplayDebug.d(TAG, "AppLovin request new media", onoff);
        isFirstClick = false;
        preloadAd();
    }

    @Override
    protected void onShowMedia() {
        if (mediaAd != null) {
            ZplayDebug.i(TAG, "AppLovin Media onShowMedia : " + getProvider().getKey2(), onoff);
            mediaAd.show(getActivity(), adRewardListener, adVideoPlaybackListener, adDisplayListener, adClickListener);
        }
    }

    @Override
    protected boolean isMediaReady() {
        if (mediaAd != null) {
            boolean isReady = mediaAd.isAdReadyToDisplay();
            ZplayDebug.i(TAG, "AppLovin Media isMediaReady : " + isReady, onoff);
            return isReady;
        }
        return false;
    }

    @Override
    protected void init() {
        ZplayDebug.i(TAG, "AppLovin Media init sdkKey : " + getProvider().getKey1() + "  ZoneId : " + getProvider().getKey2(), onoff);
        appLovinSDK = ApplovinExtraHolder.getAppLovinSDK(getActivity(), getProvider().getKey1());
        createMediaListener();
        mediaAd = AppLovinIncentivizedInterstitial.create(getProvider().getKey2(), appLovinSDK);
        preloadAd();
    }

    private void preloadAd() {
        if (mediaAd != null) {
            ZplayDebug.i(TAG, "AppLovin Media preloadAd : " + getProvider().getKey2(), onoff);
            mediaAd.preload(adLoadListener);
        }
    }

    private void createMediaListener() {
        adRewardListener = new AppLovinAdRewardListener() {
            @Override
            public void userRewardVerified(AppLovinAd appLovinAd, Map<String, String> map) {
                ZplayDebug.i(TAG, "AppLovin Media userRewardVerified ", onoff);
                layerIncentived();
            }

            @Override
            public void userOverQuota(AppLovinAd appLovinAd, Map<String, String> map) {
                ZplayDebug.i(TAG, "AppLovin Media userOverQuota ", onoff);
            }

            @Override
            public void userRewardRejected(AppLovinAd appLovinAd, Map<String, String> map) {
                ZplayDebug.i(TAG, "AppLovin Media userRewardRejected ", onoff);
            }

            @Override
            public void validationRequestFailed(AppLovinAd appLovinAd, int i) {
                ZplayDebug.i(TAG, "AppLovin Media validationRequestFailed ", onoff);
            }

            @Override
            public void userDeclinedToViewAd(AppLovinAd appLovinAd) {
                ZplayDebug.i(TAG, "AppLovin Media userDeclinedToViewAd ", onoff);
            }
        };

        adVideoPlaybackListener = new AppLovinAdVideoPlaybackListener() {
            @Override
            public void videoPlaybackBegan(AppLovinAd appLovinAd) {
                ZplayDebug.i(TAG, "AppLovin Media videoPlaybackBegan ", onoff);
                layerMediaStart();
            }

            @Override
            public void videoPlaybackEnded(AppLovinAd appLovinAd, double v, boolean b) {
                ZplayDebug.i(TAG, "AppLovin Media videoPlaybackEnded ", onoff);
                layerMediaEnd();
            }
        };

        adClickListener = new AppLovinAdClickListener() {
            @Override
            public void adClicked(AppLovinAd appLovinAd) {
                ZplayDebug.i(TAG, "AppLovin Media adClicked ", onoff);
                if (!isFirstClick) {
                    ZplayDebug.d(TAG, "clicked" + appLovinAd.getAdIdNumber(), onoff);
                    layerClicked();
                    isFirstClick = true;
                }
            }
        };

        adDisplayListener = new AppLovinAdDisplayListener() {
            @Override
            public void adDisplayed(AppLovinAd appLovinAd) {
                ZplayDebug.i(TAG, "AppLovin Media adDisplayed ", onoff);
                layerExposure();
            }

            @Override
            public void adHidden(AppLovinAd appLovinAd) {
                ZplayDebug.i(TAG, "AppLovin Media adHidden ", onoff);
                layerClosed();
                preloadAd();
            }
        };

        adLoadListener = new AppLovinAdLoadListener() {
            @Override
            public void adReceived(AppLovinAd appLovinAd) {
                ZplayDebug.i(TAG, "AppLovin Media adReceived ZoneID : " + getProvider().getKey2(), onoff);
                layerPrepared();
            }

            @Override
            public void failedToReceiveAd(int errorCode) {
                ZplayDebug.i(TAG, "AppLovin Media failedToReceiveAd ZoneID : " + getProvider().getKey2() + "  ||  errorCode:" + errorCode, onoff);
                if (errorCode == AppLovinErrorCodes.NO_FILL) {
                    layerPreparedFailed(LayerErrorCode.ERROR_NO_FILL);
                } else {
                    layerPreparedFailed(LayerErrorCode.ERROR_INTERNAL);
                }
                requestAD(15);
            }
        };
    }

    private void requestAD(int delaySecond) {
        try {
            ZplayDebug.d(TAG, "AppLovin media requestAD delaySecond : " + delaySecond, onoff);
            if(!mHandler.hasMessages(REQUEST_NEXT_MEDIA))
            mHandler.sendEmptyMessageDelayed(REQUEST_NEXT_MEDIA, delaySecond * 1000);
        } catch (Exception e) {
            ZplayDebug.e(TAG, "AppLovin media requestAD error ", e, onoff);
        }
    }

    @Override
    protected void callOnActivityDestroy() {
        ApplovinExtraHolder.destroyHolder();
        appLovinSDK = null;
        mediaAd = null;
        mHandler.removeMessages(REQUEST_NEXT_MEDIA);
    }

}
