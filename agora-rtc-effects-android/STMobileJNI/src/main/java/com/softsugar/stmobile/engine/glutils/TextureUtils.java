package com.softsugar.stmobile.engine.glutils;

import android.graphics.Bitmap;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * api
 * 1 Bitmap textureOESToBitmap(int textureId, int width, int height);
 * 2 ByteBuffer texture2dToByteBuffer(int textureId, int width, int height);
 * void byteBufferToTexture2D(ByteBuffer byteBuffer, int textureId, int width, int height);
 * byte[] getBytesArrayFromTexture(int textureId, int width, int height);
 */

public class TextureUtils {
    private static final String TAG = "TextureUtils";

    public static void saveTexture2DToBitmapFile(int textureId, int width, int height, String filePath) {
        Bitmap bitmap = texture2DToBitmap(textureId, width, height);
        saveBitmapToSDCard(bitmap, filePath);
    }

    public static int loadTexture(final Bitmap bitmap) {
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        return textures[0];
    }

    public static boolean saveBitmapToSDCard(Bitmap bitmap, String filePath) {
        if (bitmap == null || filePath == null) return false;

        FileOutputStream out = null;
        try {
            File file = new File(filePath);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();  // 创建父目录
            }

            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);  // 可改为 JPEG
            out.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static Bitmap texture2DToBitmap(int textureId, int width, int height) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(width * height * 4);
        buffer.order(ByteOrder.nativeOrder());

        int[] frameBuffers = new int[1];
        int[] renderBuffers = new int[1];

        // 创建 Framebuffer
        GLES20.glGenFramebuffers(1, frameBuffers, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffers[0]);

        // 创建并绑定一个 Renderbuffer（可选）
        GLES20.glGenRenderbuffers(1, renderBuffers, 0);
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, renderBuffers[0]);
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, width, height);
        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, renderBuffers[0]);

        // 绑定 textureId 到 framebuffer 的 color attachment 上
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, textureId, 0);

        // 读取像素数据
        GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer);

        // 解绑
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glDeleteFramebuffers(1, frameBuffers, 0);
        GLES20.glDeleteRenderbuffers(1, renderBuffers, 0);

        // 创建 Bitmap 并填充像素
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        buffer.rewind();
        bitmap.copyPixelsFromBuffer(buffer);
        return bitmap;
        // OpenGL 是从左下角开始的，而 Bitmap 是从左上角，需要翻转
//        Matrix flip = new Matrix();
//        flip.postScale(1, -1);
//        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), flip, true);
    }

    // 1.将转换成bitmap
    public static Bitmap textureOESToBitmap(int textureId, int width, int height) {
        ByteBuffer tmpBuffer = ByteBuffer.allocate(width * height * 4);
        int[] frameBuffers = new int[1];
        GLES20.glGenFramebuffers(1, frameBuffers, 0);
        GLES20.glActiveTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffers[0]);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId, 0);
        GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, tmpBuffer);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        Bitmap srcBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        tmpBuffer.position(0);
        srcBitmap.copyPixelsFromBuffer(tmpBuffer);

        GLES20.glDeleteFramebuffers(1, frameBuffers, 0);
        return srcBitmap;
    }

    // 将纹理转为字节数组
    public static byte[] getBytesArrayFromTexture(int textureId, int width, int height) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(width * height * 4);
        buffer.order(ByteOrder.nativeOrder());
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);
        return data;
    }

    public static ByteBuffer texture2dToByteBuffer(int textureId, int width, int height) {
        ByteBuffer tmpBuffer = ByteBuffer.allocate(width * height * 4);
        int[] frameBuffers = new int[1];
        GLES20.glGenFramebuffers(1, frameBuffers, 0);
        GLES20.glActiveTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffers[0]);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId, 0);
        GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, tmpBuffer);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glDeleteFramebuffers(1, frameBuffers, 0);
        return tmpBuffer;
    }

    public static void byteBufferToTexture2D(ByteBuffer byteBuffer, int textureId, int width, int height) {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        // 将字节数组的数据传递给纹理对象
        byteBuffer.position(0);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, byteBuffer);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    public static Bitmap Texture2DToBitmap(int textureId, int width, int height) {
        ByteBuffer tmpBuffer = ByteBuffer.allocate(width * height * 4);
        int[] frameBuffers = new int[1];
        GLES20.glGenFramebuffers(1, frameBuffers, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE_2D);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffers[0]);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, textureId, 0);
        GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, tmpBuffer);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        Bitmap srcBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        tmpBuffer.position(0);
        srcBitmap.copyPixelsFromBuffer(tmpBuffer);

        GLES20.glDeleteFramebuffers(1, frameBuffers, 0);
        return srcBitmap;
    }

    // 将位图转换为纹理
    public static int bitmapToTexture(Bitmap bitmap) {
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        int textureId = textures[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        return textureId;
    }

    // 将字节数组转换为纹理
    public static void byteArrayToTexture(byte[] data, int width, int height, int textureId) {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        // 将字节数组的数据传递给纹理对象
        ByteBuffer buffer = ByteBuffer.allocateDirect(data.length);
        buffer.order(ByteOrder.nativeOrder());
        buffer.put(data);
        buffer.position(0);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    // 将位图转换为纹理
    public static int bitmapToTexture(Bitmap bitmap, int textureId, boolean recycle) {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        if (recycle) {
            bitmap.recycle();
        }
        return textureId;
    }

}
