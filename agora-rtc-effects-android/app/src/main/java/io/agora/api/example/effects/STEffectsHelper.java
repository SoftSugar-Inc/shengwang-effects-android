package io.agora.api.example.effects;

import android.content.Context;
import android.graphics.Bitmap;

import com.softsugar.stmobile.STCommonNative;
import com.softsugar.stmobile.engine.STEffectsEngine;
import com.softsugar.stmobile.engine.STEffectsEngineI;
import com.softsugar.stmobile.engine.STRenderMode;
import com.softsugar.stmobile.engine.STTextureFormat;
import com.softsugar.stmobile.engine.glutils.STEffectsInput;
import com.softsugar.stmobile.engine.glutils.STEffectsOutput;
import com.softsugar.stmobile.engine.glutils.STImageBuffer;
import com.softsugar.stmobile.engine.glutils.TextureUtils;
import com.softsugar.stmobile.model.STEffectTexture;
import com.softsugar.stmobile.params.STEffectBeautyGroup;
import com.softsugar.stmobile.params.STEffectBeautyType;
import com.softsugar.stmobile.params.STSmoothMode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class STEffectsHelper {

    private final Context context;
    private final STEffectsEngineI stEffectsEngine;

    private final String sdRootPath;

    public STEffectsHelper(Context context) {
        this.context = context;
        this.stEffectsEngine = new STEffectsEngine();
        this.sdRootPath = context.getExternalFilesDir(null) + File.separator;
    }

    /**
     * 鉴权 -> 初始化SDK -> 加载模型
     */
    public void init() {
        String sdPath = context.getExternalFilesDir(null) + File.separator;
        FileUtils.copyFilesFromAssets(context, "models", sdPath + "models");
        FileUtils.copyFilesFromAssets(context, "External_Assets", sdPath + "External_Assets");

        stEffectsEngine.checkLicenseFromBuffer(context, readAssetFileToBytes(context, "SenseME.lic"));
        stEffectsEngine.init(context, STRenderMode.PREVIEW.getMode());
        String model = sdPath + "models/M_SenseME_Face_Video_Template_p_4.0.0.model";
        //noinspection Convert2Lambda
        new Thread(new Runnable() {
            @Override
            public void run() {
                stEffectsEngine.loadSubModel(model);
            }
        }).start();
    }
    /**
     * 添加特效
     */
    public void addBeauty() {
        // 添加基础美颜-美白4
        stEffectsEngine.setBeautyFromSDPath(STEffectBeautyType.EFFECT_BEAUTY_BASE_WHITEN, sdRootPath + "External_Assets/whiten4.zip");
        stEffectsEngine.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_BASE_WHITEN, 0.5f);

        // 添加基础美颜-红润
        stEffectsEngine.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_BASE_REDDEN, 0.5f);

        // 添加基础美颜-磨皮4
        stEffectsEngine.setBeautyMode(STEffectBeautyType.EFFECT_BEAUTY_BASE_FACE_SMOOTH, STSmoothMode.EFFECT_SMOOTH_FACE_EVEN);
        stEffectsEngine.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_BASE_FACE_SMOOTH, 0.5f);

        // 添加基础美颜-美形-瘦脸
        stEffectsEngine.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_RESHAPE_SHRINK_FACE, 0.5f);

        // 添加基础美颜-美形-小脸
        stEffectsEngine.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_RESHAPE_SHRINK_JAW, 0.5f);

        // 添加基础美颜-美形-窄脸
        stEffectsEngine.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_RESHAPE_NARROW_FACE, 0.5f);

        // 添加基础美颜-美形-圆眼
        stEffectsEngine.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_RESHAPE_ROUND_EYE, 0.5f);

        // 添加基础美颜-美形-大眼
        stEffectsEngine.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_RESHAPE_ENLARGE_EYE, 0.5f);

        // 添加基础美颜-微整形-小头
        stEffectsEngine.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_THINNER_HEAD, 0.5f);

        // 添加基础美颜-微整形-下巴
        stEffectsEngine.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_CHIN_LENGTH, 0.5f);

        // 添加滤镜
        stEffectsEngine.setBeauty(STEffectBeautyType.EFFECT_BEAUTY_FILTER, sdRootPath + "External_Assets/filter_style_babypink.model");
        stEffectsEngine.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_FILTER, 0.5f);

        // 添加风格妆
        int styleMakeUpId = stEffectsEngine.addPackage(sdRootPath + "External_Assets/oumei.zip");
        stEffectsEngine.setPackageBeautyGroupStrength(styleMakeUpId, STEffectBeautyGroup.EFFECT_BEAUTY_GROUP_FILTER, 0.8f);
        stEffectsEngine.setPackageBeautyGroupStrength(styleMakeUpId, STEffectBeautyGroup.EFFECT_BEAUTY_GROUP_MAKEUP, 0.8f);

        // 添加单妆口红
        stEffectsEngine.setBeauty(STEffectBeautyType.EFFECT_BEAUTY_MAKEUP_LIP, sdRootPath + "External_Assets/1自然.zip");
        stEffectsEngine.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_MAKEUP_LIP, 0.8f);

        // 添加2D脸部贴纸
        stEffectsEngine.addPackage(sdRootPath + "External_Assets/bunny.zip");

        // 移除指定贴纸示例
        // 添加贴纸获取到stickerId, 用stickerId移除贴纸
        //int stickerId = stEffectsEngine.addPackage(sdRootPath + "External_Assets/bunny.zip");
        //stEffectsEngine.removeEffectById(stickerId);
    }

    public int processTexture(int textureId, int rotation, int width, int height) {
        // 输入参数
        STEffectTexture stEffectTexture = new STEffectTexture(textureId, width, height, STTextureFormat.FORMAT_TEXTURE_2D.getFormat());
        STEffectsInput stEffectsInput = new STEffectsInput(stEffectTexture, null, rotation, rotation, false, false, 0);

        // 输出参数
        STEffectsOutput stEffectsOutput = new STEffectsOutput(null, null);

        // 纹理处理
        stEffectsEngine.processTexture(stEffectsInput, stEffectsOutput);
        return stEffectsOutput.getTextureId();
    }

    public byte[] processBuffer(byte[] srcData, byte[] dstData, int rotation, int width, int height) {
        STEffectsInput stEffectsInput = new STEffectsInput(null, new STImageBuffer(srcData, STCommonNative.ST_PIX_FMT_RGBA8888, width, height), rotation, rotation, true, false, 0);
        STEffectsOutput stEffectsOutput = new STEffectsOutput( null, new STImageBuffer(dstData, STCommonNative.ST_PIX_FMT_RGBA8888, width, height));
        stEffectsEngine.processBuffer(stEffectsInput, stEffectsOutput);
        STImageBuffer imageBuffer = stEffectsOutput.getEffectImage();
        if (imageBuffer != null) {
            return stEffectsOutput.getEffectImage().getImageBuffer();
        }
        return null;
    }

    public void release() {
        stEffectsEngine.release();
    }

    /**
     * @noinspection SameParameterValue
     */
    private byte[] readAssetFileToBytes(Context context, String fileName) {
        try {
            InputStream inputStream = context.getAssets().open(fileName);
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                byteStream.write(buffer, 0, len);
            }

            inputStream.close();
            return byteStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
