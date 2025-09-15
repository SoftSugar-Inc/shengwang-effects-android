# Android 声网实时音视频(Agora RTC)接入 SenseAR Effects 特效引擎 SDK 集成 Demo

本项目是**商汤科技**提供的[**特效引擎 SDK**](https://sensear.softsugar.com/) 在声网 Agora 实时音视频基础上集成 `SenseAR Effects` 特效引擎（下称「Effects」）SDK 的 Android 演示工程。



关于SenseAR Effects特效引擎Android SDK详细介绍见[*仓库*](https://github.com/SoftSugar-Inc/effects-android)。

## 环境要求

- Android Studio 4.0+
- Android SDK API Level 21+
- NDK 27.0.11902837
- JDK 11

---

## 运行Demo

- clone工程到本地
- 使用Android Studio打开项目
- 调整local.properties文件中的ndk.dir和sdk.dir路径
- 将从商汤商务渠道获取的license文件放入工程（需要将名字改为"SenseME.lic"）
- 将工程的applicationId修改为与上述license绑定的包名
- 完成工程编译及App在测试机的安装，运行Demo

> 请[**提交免费试用申请**](https://sensear.softsugar.com/)，或**联系商务**（Tel: 181-1640-5190）获取测试license。

---


## 快速集成Effects SDK

> [详细接入文档](https://github.com/SoftSugar-Inc/effects-android/blob/main/SenseMeEffects/docs/md%E6%96%87%E4%BB%B6/androidDevManual.md)

本文档介绍如何在声网 Agora 实时音视频项目中集成 SenseAR Effects SDK，实现美颜、贴纸等特效功能。

### 项目结构

项目主要包含以下模块：
- STMobileJNI：SenseAR Effects SDK 的核心美颜功能模块，提供美颜特效能力
- app：声网实时音视频的示例应用模块（集成 Agora RTC 与 SenseAR Effects）
- 可选模块：`agora-simple-filter`、`agora-stream-encrypt` 等示例/扩展能力

### SenseAR Effects资源

#### SDK

SenseAR Effects SDK包含以下关键组件：
- STMobileJNI：美颜功能模块, 目前以module方式依赖, 也可以编译为aar依赖
- HardwareBuffer-release.aar：硬件缓冲区支持库

#### license

使用SDK需要有效的license文件：
- 请联系商务获取正式的license文件
- 将license文件放置在应用的assets目录下
- 确保license文件名与初始化时传入的名称一致

#### 测试素材

项目在External_Assets目录下提供了几个测试素材，主要包括：
- 1自然.zip：口红测试素材，用于口红特效效果展示
- bunny.zip：贴纸素材，用于展示动态贴纸效果
- filter_style_babypink.model：滤镜素材，用于实现图像滤镜效果
- oumei.zip：美妆测试素材，用于展示美妆特效效果
- whiten4.zip：美白素材包，用于实现美白效果

注：以上仅为部分示例素材

### 项目配置

#### Include配置

在项目的build.gradle中添加以下配置：
```gradle
android {
    defaultConfig {
        ndk {
            abiFilters "armeabi-v7a", "arm64-v8a"
        }
    }
}
```

#### Library配置

添加依赖：

```gradle
dependencies {
    implementation project(':STMobileJNI')
    implementation files('libs/HardwareBuffer-release.aar')
}
```



## Effects API接入

### GL Context 相关

在使用 Agora 自定义视频处理时，通常通过「视频帧回调/渲染管线」拿到可处理的纹理或数据。在 Effects 侧内部已完成 GL 环境初始化及输入/输出纹理创建，通常无需额外设置 GL Context，只需在回调中调用处理接口即可。

```java
@Override
public void onGLContextCreated() {
    // SDK已完成GL环境初始化，可以在这里进行其他纹理初始化操作
    // 由于SDK内部已创建好输入输出纹理，此处通常不需要额外处理
}
```

### SDK初始化

SDK初始化包含以下步骤：
1. 创建STEffectsEngine实例
2. 复制资源文件到应用目录
3. 进行license鉴权
4. 初始化引擎

```java
// 创建STEffectsEngine实例
STEffectsEngine stEffectsEngine = new STEffectsEngine();

// 复制资源文件到应用目录
String sdPath = context.getExternalFilesDir(null) + File.separator;
FileUtils.copyFilesFromAssets(context, "models", sdPath + "models");
FileUtils.copyFilesFromAssets(context, "stickers", sdPath + "stickers");

// 进行license鉴权
stEffectsEngine.checkLicenseFromBuffer(context, readAssetFileToBytes(context, "SenseME.lic"));

// 初始化引擎
stEffectsEngine.init(context, STRenderMode.PREVIEW.getMode());
```

### 设置美颜参数/素材

SenseAR Effects SDK提供了丰富的美颜功能，可以通过`setBeautyStrength`接口设置不同类型美颜的强度。

```java
/**
 * 设置美颜的强度
 * @param type  美颜类型，定义在STEffectBeautyType.java中
 * @param value 强度值，范围[-1.0, 1.0]
 * @return 成功返回0，错误返回其它，参考STResultCode
 */
int setBeautyStrength(int type, float value);

// 示例代码
// 1. 设置基础美颜-美白
stEffectsEngine.setBeautyFromSDPath(STEffectBeautyType.EFFECT_BEAUTY_BASE_WHITEN, sdRootPath + "External_Assets/whiten4.zip");
stEffectsEngine.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_BASE_WHITEN, 0.5f);

// 2. 设置基础美颜-红润
stEffectsEngine.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_BASE_REDDEN, 0.5f);

// 3. 设置基础美颜-磨皮
stEffectsEngine.setBeautyMode(STEffectBeautyType.EFFECT_BEAUTY_BASE_FACE_SMOOTH, STSmoothMode.EFFECT_SMOOTH_FACE_EVEN);
stEffectsEngine.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_BASE_FACE_SMOOTH, 0.5f);

// 4. 设置基础美颜-美形-瘦脸
stEffectsEngine.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_RESHAPE_SHRINK_FACE, 0.5f);
```

注意事项：
- 美颜类型定义在`STEffectBeautyType.java`中
- 强度值范围为[-1-1.0]，0.0表示无效果，1.0表示最强效果
- 部分美颜效果需要先加载对应的素材文件

### 设置/移除贴纸素材

```java
// 添加贴纸并获取贴纸ID
int stickerId = stEffectsEngine.addPackage(sdRootPath + "External_Assets/bunny.zip");

// 使用贴纸ID移除指定贴纸
stEffectsEngine.removeEffectById(stickerId);
```

注意事项：
- 通过`addPackage`方法添加贴纸时会返回一个唯一的贴纸ID
- 使用`removeEffectById`方法时需要传入对应的贴纸ID来移除特定贴纸
- 请妥善保存贴纸ID以便后续管理和移除操作

### 帧处理回调

#### buffer 输入/输出

```java
// 处理视频帧buffer数据
public byte[] processBuffer(byte[] srcData, byte[] dstData, int rotation, int width, int height) {
    // 创建输入参数，设置输入图像格式为RGBA8888
    STEffectsInput stEffectsInput = new STEffectsInput(null, 
        new STImageBuffer(srcData, STCommonNative.ST_PIX_FMT_RGBA8888, width, height),
        rotation, rotation, false, false, 0);
    
    // 创建输出参数
    STEffectsOutput stEffectsOutput = new STEffectsOutput(null,
        new STImageBuffer(dstData, STCommonNative.ST_PIX_FMT_RGBA8888, width, height));
    
    // 调用引擎处理图像buffer
    stEffectsEngine.processBuffer(stEffectsInput, stEffectsOutput);
    
    // 获取处理后的图像数据
    STImageBuffer imageBuffer = stEffectsOutput.getEffectImage();
    if (imageBuffer != null) {
        return stEffectsOutput.getEffectImage().getImageBuffer();
    }
    return null;
}

// 在 Agora 场景下的伪代码示例（根据实际 SDK 回调替换）
public int onCaptureVideoFrame(AgoraVideoFrame srcFrame, AgoraVideoFrame dstFrame) {
    if (PROCESS_TEXTURE) {
        dstFrame.textureId = stEffectsHelper.processTexture(
            srcFrame.textureId,
            srcFrame.rotation,
            srcFrame.width,
            srcFrame.height
        );
    } else {
        dstFrame.data = stEffectsHelper.processBuffer(
            srcFrame.data,
            dstFrame.data,
            srcFrame.rotation,
            srcFrame.width,
            srcFrame.height
        );
    }
    return 0;
}
```

参数说明：
- srcData：输入的原始图像数据buffer
- dstData：输出的目标图像数据buffer
- rotation：图像旋转角度
- width：图像宽度
- height：图像高度

处理流程：
1. 创建STEffectsInput对象，设置输入图像格式为RGBA8888
2. 创建STEffectsOutput对象，准备接收处理后的图像数据
3. 调用stEffectsEngine.processBuffer进行图像处理
4. 从STEffectsOutput中获取处理后的图像数据并返回

注意事项：
- 支持纹理处理和buffer处理两种模式，可通过PROCESS_TEXTURE标志控制
- Buffer处理模式下，输入输出的图像格式均为RGBA8888
- 确保输入输出buffer的大小足够容纳图像数据

#### texture 输入/输出

纹理处理方法的实现如下：

```java
// 处理纹理的方法实现
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
```

在 Agora 场景下的伪代码示例（根据实际 SDK 回调替换）：

```java
// 注册视频帧回调/观察者后，在帧回调中调用处理接口
public int onPreEncodeVideoFrame(AgoraVideoFrame srcFrame, AgoraVideoFrame dstFrame) {
    if (PROCESS_TEXTURE) {
        dstFrame.textureId = stEffectsHelper.processTexture(
            srcFrame.textureId,
            srcFrame.rotation,
            srcFrame.width,
            srcFrame.height
        );
    } else {
        dstFrame.data = stEffectsHelper.processBuffer(
            srcFrame.data,
            dstFrame.data,
            srcFrame.rotation,
            srcFrame.width,
            srcFrame.height
        );
    }
    return 0;
}
```

注意事项：
- 支持纹理处理和buffer处理两种模式，通过PROCESS_TEXTURE标志控制
- 纹理处理模式下，需要在 GL 环境销毁回调中释放资源
- 处理后的纹理 ID 通过目标帧返回给 Agora SDK

### 资源释放

资源释放是一个重要的环节，必须在 GL 环境下进行，以确保所有 GL 相关资源能够被正确释放。请在 Agora SDK 所在渲染/采集线程的销毁回调中进行释放操作：

```java
@Override
public void onGLContextDestroyed() {
    // 在GL环境销毁时释放资源
    stEffectsHelper.release();
}
```

注意事项：
- 必须在GL线程中调用release方法，在其他线程或场景下调用release方法，可能会导致GL资源泄露

## 其他注意事项

### 运行sample的配置环境

以下是运行sample的配置环境以供参考

Android Studio版本：Meerkat Feature Drop | 2024.3.2

JDK版本：corretto-11.0.17，参考设置Preferences->Build->Gradle->Gradle JDK；

NDK版本：27.0.11902837，工程根路径下创建local.properties，添加ndk.dir和sdk.dir，如下示例：

```
sdk.dir=/Users/xxx/Library/Android/sdk
ndk.dir=/Users/xxx/Library/Android/sdk/ndk/27.0.11902837
```
