package com.yumi.android.sdk.ads.adapter.mobvista;

import android.app.Activity;

import com.mintegral.msdk.MIntegralConstans;
import com.mintegral.msdk.MIntegralSDK;
import com.mintegral.msdk.out.MIntegralSDKFactory;
import com.mintegral.msdk.out.MTGRewardVideoHandler;
import com.mintegral.msdk.out.RewardVideoListener;
import com.yumi.android.sdk.ads.beans.YumiProviderBean;
import com.yumi.android.sdk.ads.publish.AdError;
import com.yumi.android.sdk.ads.publish.YumiSettings;
import com.yumi.android.sdk.ads.publish.adapter.YumiCustomerMediaAdapter;
import com.yumi.android.sdk.ads.publish.enumbean.LayerErrorCode;
import com.yumi.android.sdk.ads.publish.enumbean.YumiGDPRStatus;
import com.yumi.android.sdk.ads.utils.ZplayDebug;

import java.util.Map;

import static com.yumi.android.sdk.ads.adapter.mobvista.Util.sdkVersion;
import static com.yumi.android.sdk.ads.publish.enumbean.LayerErrorCode.ERROR_FAILED_TO_SHOW;

/**
 * Created by hjl on 2017/11/28.
 */
public class MobvistaMediaAdapter extends YumiCustomerMediaAdapter {

    private static final String TAG = "MobvistaMediaAdapter";
    private static final int REQUEST_NEXT_MEDIA = 0x001;
    private MTGRewardVideoHandler mMvRewardVideoHandler;

    protected MobvistaMediaAdapter(Activity activity, YumiProviderBean yumiProviderBean) {
        super(activity, yumiProviderBean);
    }

    @Override
    protected void onPrepareMedia() {
        try {
            ZplayDebug.d(TAG, "Mobvista request new media", onoff);
            if (mMvRewardVideoHandler != null) {
                ZplayDebug.d(TAG, "Mobvista media prapared", onoff);
                mMvRewardVideoHandler.load();
            } else {
                initHandler();
                mMvRewardVideoHandler.load();
            }
        } catch (Exception e) {
            ZplayDebug.e(TAG, "Mobvista media onPrepareMedia error:", e, onoff);
        }
    }

    @Override
    protected void onShowMedia() {
        try {
            if (mMvRewardVideoHandler != null) {
                if (mMvRewardVideoHandler.isReady()) {
                    mMvRewardVideoHandler.show(getProvider().getKey4());//"rewardid"
                    ZplayDebug.d(TAG, "Mobvista media onShowMedia true : " + getProvider().getKey4(), onoff);
                } else {
                    mMvRewardVideoHandler.load();
                    ZplayDebug.d(TAG, "Mobvista media onShowMedia false", onoff);
                }
            }
        } catch (Exception e) {
            ZplayDebug.e(TAG, "Mobvista media onShowMedia error:", e, onoff);
        }
    }

    @Override
    protected boolean isMediaReady() {
        if (mMvRewardVideoHandler != null) {
            boolean isReady = mMvRewardVideoHandler.isReady();
            ZplayDebug.d(TAG, "Mobvista media isMediaReady " + isReady, onoff);
            return isReady;
        }
        ZplayDebug.d(TAG, "Mobvista media isMediaReady mMvRewardVideoHandler is null", onoff);
        return false;
    }

    @Override
    protected void init() {
        try {
            ZplayDebug.d(TAG, "Mobvista media init appId : " + getProvider().getKey1() + "   || appKey : " + getProvider().getKey2(), onoff);
            MIntegralSDK sdk = MIntegralSDKFactory.getMIntegralSDK();
            Map<String, String> map = sdk.getMTGConfigurationMap(getProvider().getKey1(), getProvider().getKey2()); //appId, appKey
            if (YumiSettings.getGDPRStatus() != YumiGDPRStatus.UNKNOWN) {
                int isConsent = YumiSettings.getGDPRStatus() == YumiGDPRStatus.PERSONALIZED ? MIntegralConstans.IS_SWITCH_ON : MIntegralConstans.IS_SWITCH_OFF;
                sdk.setUserPrivateInfoType(getActivity(), MIntegralConstans.AUTHORITY_ALL_INFO, isConsent);
            }
            sdk.init(map, getContext());
            initHandler();
        } catch (Exception e) {
            ZplayDebug.e(TAG, "Mobvista media init error:", e, onoff);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMvRewardVideoHandler = null;
    }

    @Override
    public void onActivityPause() {

    }

    @Override
    public void onActivityResume() {

    }

    private void initHandler() {
        try {
            mMvRewardVideoHandler = new MTGRewardVideoHandler(getActivity(), getProvider().getKey3()); //UnitId
            mMvRewardVideoHandler.setRewardVideoListener(new RewardVideoListener() {

                @Override
                public void onVideoLoadSuccess(String unitId) {
                    ZplayDebug.d(TAG, "Mobvista media onVideoLoadSuccess unitId:" + unitId, onoff);
                    layerPrepared();
                }

                @Override
                public void onLoadSuccess(String s) {

                }

                @Override
                public void onVideoLoadFail(String errorMsg) {
                    ZplayDebug.d(TAG, "Mobvista media onVideoLoadFail errorMsg:" + errorMsg, onoff);
                    AdError error = new AdError(LayerErrorCode.ERROR_NO_FILL);
                    error.setErrorMessage("minteral-China errorMsg: " + errorMsg);
                    layerPreparedFailed(error);
                }

                @Override
                public void onShowFail(String errorMsg) {
                    ZplayDebug.d(TAG, "Mobvista media onShowFail errorMsg:" + errorMsg, onoff);
                    AdError adError = new AdError(ERROR_FAILED_TO_SHOW);
                    adError.setErrorMessage("Mobvista errorMsg: " + errorMsg);
                    layerExposureFailed(adError);
                }

                @Override
                public void onAdShow() {
                    ZplayDebug.d(TAG, "Mobvista media onAdShow", onoff);
                    layerExposure();
                    layerStartPlaying();
                }

                @Override
                public void onAdClose(boolean isCompleteView, String RewardName, float RewardAmout) {
                    //三个参数为：是否播放完，奖励名，奖励积分
                    ZplayDebug.d(TAG, "Mobvista media onAdClose isCompleteView :" + isCompleteView + "   RewardName:" + RewardName + "   RewardAmout:" + RewardAmout, onoff);
                    if (isCompleteView) {
                        layerIncentived();
                    }
                    layerClosed(isCompleteView);
                }

                @Override
                public void onVideoAdClicked(String unitId) {
                    ZplayDebug.d(TAG, "Mobvista media onVideoAdClicked unitId:" + unitId, onoff);
                    layerClicked();
                }

                @Override
                public void onVideoComplete(String s) {

                }

                @Override
                public void onEndcardShow(String s) {

                }

            });
        } catch (Exception e) {
            ZplayDebug.e(TAG, "Mobvista media initHandler error:", e, onoff);
        }
    }

    @Override
    public String getProviderVersion() {
        return sdkVersion();
    }
}