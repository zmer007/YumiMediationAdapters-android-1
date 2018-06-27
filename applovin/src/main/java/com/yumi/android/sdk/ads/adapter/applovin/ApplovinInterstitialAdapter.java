package com.yumi.android.sdk.ads.adapter.applovin;


import android.app.Activity;

import com.applovin.adview.AppLovinInterstitialAd;
import com.applovin.adview.AppLovinInterstitialAdDialog;
import com.applovin.sdk.AppLovinAd;
import com.applovin.sdk.AppLovinAdClickListener;
import com.applovin.sdk.AppLovinAdDisplayListener;
import com.applovin.sdk.AppLovinAdLoadListener;
import com.applovin.sdk.AppLovinAdVideoPlaybackListener;
import com.applovin.sdk.AppLovinErrorCodes;
import com.applovin.sdk.AppLovinSdk;
import com.yumi.android.sdk.ads.beans.YumiProviderBean;
import com.yumi.android.sdk.ads.publish.adapter.YumiCustomerInterstitialAdapter;
import com.yumi.android.sdk.ads.publish.enumbean.LayerErrorCode;
import com.yumi.android.sdk.ads.utils.ZplayDebug;

public class ApplovinInterstitialAdapter extends YumiCustomerInterstitialAdapter {

    private static final String TAG = "ApplovinInterstitialAdapter";
    private AppLovinSdk appLovinSDK;
    private AppLovinInterstitialAdDialog interstitialAd;
    private AppLovinAd currentAd = null;

    private AppLovinAdClickListener adClickListener;
    private AppLovinAdDisplayListener adDisplayListener;
    private AppLovinAdVideoPlaybackListener adVideoPlaybackListener;

    private boolean isFirstClick = false;
    private boolean isPrepared = false;

    protected ApplovinInterstitialAdapter(Activity activity, YumiProviderBean provider) {
        super(activity, provider);
    }

    @Override
    public void onActivityPause() {

    }

    @Override
    public void onActivityResume() {

    }

    @Override
    public boolean onActivityBackPressed() {
        return false;
    }

    @Override
    protected void onPrepareInterstitial() {
        ZplayDebug.d(TAG, "AppLovin request new interstitial ", onoff);
        isFirstClick = false;
        if (appLovinSDK != null) {
            if(isPrepared && currentAd!=null)
            {
                layerPrepared();
            }
            ZplayDebug.d(TAG, "AppLovin loadNextAdForZoneId ZoneId : " + getProvider().getKey2(), onoff);
            appLovinSDK.getAdService().loadNextAdForZoneId(getProvider().getKey2(), new AppLovinAdLoadListener() {
                @Override
                public void adReceived(AppLovinAd ad) {
                    ZplayDebug.i(TAG, "AppLovin Interstitial adReceived :" + getProvider().getKey2(), onoff);
                    currentAd = ad;
                    if (!isPrepared) {
                        layerPrepared();
                        isPrepared = true;
                    }
                }

                @Override
                public void failedToReceiveAd(int errorCode) {
                    ZplayDebug.i(TAG, "AppLovin Interstitial failedToReceiveAd ZoneID : " + getProvider().getKey2() + "  || errorCode : " + errorCode, onoff);
                    if (errorCode == AppLovinErrorCodes.NO_FILL) {
                        layerPreparedFailed(LayerErrorCode.ERROR_NO_FILL);
                    } else {
                        layerPreparedFailed(LayerErrorCode.ERROR_INTERNAL);
                    }
                }
            });
        }
    }

    @Override
    protected void onShowInterstitialLayer(Activity activity) {
        ZplayDebug.d(TAG, "onShowInterstitialLayer ", onoff);
        if (interstitialAd != null) {
            if (currentAd != null) {
                interstitialAd.showAndRender(currentAd);
                ZplayDebug.i(TAG, "AppLovin Interstitial Show ZoneId : " + getProvider().getKey2(), onoff);
            } else {
                ZplayDebug.i(TAG, "AppLovin Interstitial Show The currentAd  is null", onoff);
            }
        }
        isPrepared = false;
    }

    @Override
    protected boolean isInterstitialLayerReady() {
        if (currentAd != null) {
            ZplayDebug.i(TAG, "AppLovin Interstitial  isInterstitialLayerReady true", onoff);
            return true;
        }
        return false;
    }

    @Override
    protected void init() {
        ZplayDebug.i(TAG, "AppLovin Interstitial init  sdkKey : " + getProvider().getKey1() + "  ZoneId : " + getProvider().getKey2(), onoff);
        appLovinSDK = ApplovinExtraHolder.getAppLovinSDK(getActivity(), getProvider().getKey1());
        createAppLovinListener();
        interstitialAd = AppLovinInterstitialAd.create(appLovinSDK, getActivity());
        interstitialAd.setAdClickListener(adClickListener);
        interstitialAd.setAdDisplayListener(adDisplayListener);
        interstitialAd.setAdVideoPlaybackListener(adVideoPlaybackListener);
    }

    private void createAppLovinListener() {
        adClickListener = new AppLovinAdClickListener() {
            @Override
            public void adClicked(AppLovinAd appLovinAd) {
                if (!isFirstClick) {
                    ZplayDebug.d(TAG, "AppLovin ad clicked", onoff);
                    layerClicked(-99f, -99f);
                    isFirstClick = true;
                }
            }
        };
        adDisplayListener = new AppLovinAdDisplayListener() {
            @Override
            public void adDisplayed(AppLovinAd appLovinAd) {
                ZplayDebug.d(TAG, "AppLovin ad adDisplayed", onoff);
                layerExposure();
            }

            @Override
            public void adHidden(AppLovinAd appLovinAd) {
                ZplayDebug.d(TAG, "AppLovin ad adHidden", onoff);
                layerClosed();

            }
        };
        adVideoPlaybackListener = new AppLovinAdVideoPlaybackListener() {
            @Override
            public void videoPlaybackBegan(AppLovinAd appLovinAd) {
                ZplayDebug.d(TAG, "AppLovin ad videoPlaybackBegan", onoff);
            }

            @Override
            public void videoPlaybackEnded(AppLovinAd appLovinAd, double v, boolean b) {
                ZplayDebug.d(TAG, "AppLovin ad videoPlaybackEnded", onoff);
            }
        };
    }

    @Override
    protected void callOnActivityDestroy() {
        ApplovinExtraHolder.destroyHolder();
        appLovinSDK = null;
        interstitialAd = null;
        currentAd = null;
    }


}
