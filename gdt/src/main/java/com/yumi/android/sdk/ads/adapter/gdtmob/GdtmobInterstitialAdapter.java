package com.yumi.android.sdk.ads.adapter.gdtmob;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;

import com.qq.e.ads.interstitial2.UnifiedInterstitialAD;
import com.qq.e.ads.interstitial2.UnifiedInterstitialADListener;
import com.qq.e.comm.util.AdError;
import com.yumi.android.sdk.ads.beans.YumiProviderBean;
import com.yumi.android.sdk.ads.publish.adapter.YumiCustomerInterstitialAdapter;
import com.yumi.android.sdk.ads.utils.ZplayDebug;

import static com.yumi.android.sdk.ads.adapter.GdtUtil.recodeError;
import static com.yumi.android.sdk.ads.adapter.GdtUtil.sdkVersion;

public class GdtmobInterstitialAdapter extends YumiCustomerInterstitialAdapter {

    private static final String TAG = "GdtInterstitialAdapter";
    private static final int REQ_INTERSTITIAL = 0x321;
    protected boolean interstitialReady;
    private UnifiedInterstitialADListener unifiedInterstitialListener;
    private UnifiedInterstitialAD unifiedInterstitial;
    private final Handler gdtInterstitialHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == REQ_INTERSTITIAL) {
                if (unifiedInterstitial != null) {
                    unifiedInterstitial.loadAD();
                }
            }
        }

        ;
    };

    protected GdtmobInterstitialAdapter(Activity activity,
                                        YumiProviderBean provider) {
        super(activity, provider);
    }

    @Override
    public void onActivityPause() {

    }

    @Override
    public void onActivityResume() {
    }

    @Override
    protected final void onDestroy() {
        if (unifiedInterstitial != null) {
            unifiedInterstitial.destroy();
        }
    }

    @Override
    public boolean onActivityBackPressed() {
        return false;
    }

    @Override
    protected void onPrepareInterstitial() {
        ZplayDebug.d(TAG, "load new interstitial");
        interstitialReady = false;
        if (unifiedInterstitial == null) {
            unifiedInterstitial = new UnifiedInterstitialAD(getActivity(), getProvider().getKey1(), getProvider().getKey2(), unifiedInterstitialListener);
        }
        gdtInterstitialHandler.sendEmptyMessageDelayed(REQ_INTERSTITIAL, 1000);
    }

    @Override
    protected void onShowInterstitialLayer(Activity activity) {
        if (unifiedInterstitial != null) {
            unifiedInterstitial.show(activity);
        }
    }

    @Override
    protected boolean isInterstitialLayerReady() {
        if (unifiedInterstitial != null && interstitialReady) {
            return true;
        }
        return false;
    }

    @Override
    protected void init() {
        ZplayDebug.i(TAG, "init appId : " + getProvider().getKey1() + ",pId : " + getProvider().getKey2());
        unifiedInterstitialListener = new UnifiedInterstitialADListener() {

            @Override
            public void onNoAD(AdError adError) {
                interstitialReady = false;
                if (adError == null) {
                    ZplayDebug.d(TAG, "onNoAD adError = null");
                    layerPreparedFailed(recodeError(null));
                    return;
                }
                ZplayDebug.d(TAG, " failed ErrorCode:" + adError.getErrorCode() + " msg:" + adError.getErrorMsg());
                layerPreparedFailed(recodeError(adError));
            }

            @Override
            public void onADReceive() {
                interstitialReady = true;
                ZplayDebug.d(TAG, "onADReceive");
                layerPrepared();
            }

            @Override
            public void onADOpened() {

            }

            @Override
            public void onADLeftApplication() {

            }

            @Override
            public void onADExposure() {
                ZplayDebug.d(TAG, "onADExposure");
                interstitialReady = false;
                layerExposure();
                layerStartPlaying();
            }

            @Override
            public void onADClosed() {
                if (unifiedInterstitial != null) {
                    unifiedInterstitial.destroy();
                }
                ZplayDebug.d(TAG, "onADClosed");
                layerClosed();
            }

            @Override
            public void onADClicked() {
                ZplayDebug.d(TAG, "onADClicked");
                interstitialReady = false;
                layerClicked(-99f, -99f);
            }
        };
    }

    @Override
    public String getProviderVersion() {
        return sdkVersion();
    }
}
