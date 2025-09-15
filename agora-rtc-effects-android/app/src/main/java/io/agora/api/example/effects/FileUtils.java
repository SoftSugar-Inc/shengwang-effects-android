package io.agora.api.example.effects;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class FileUtils {
    private final static String TAG = FileUtils.class.getSimpleName();

    /**
     * 创建文件夹
     */
    public static void createFolder(String dir) {
        File destDir = new File(dir);
        if (!destDir.exists()) {
            boolean b = destDir.mkdirs();
            // 创建文件夹失败则日志提示
            if (!b)
                Log.e(TAG, "create dir fail!");
        }
    }

    /**
     * 删除文件及目录
     */
    public static void delete(String dir) {
        File deleteFile = new File(dir);
        recursionDeleteFile(deleteFile);
    }

    /**
     * 递归删除文件和文件夹
     *
     * @param file 要删除的根目录
     */
    private static void recursionDeleteFile(File file) {
        if (file.isFile()) {
            boolean b = file.delete();
            if (!b) {
                Log.e(TAG, "delete failed!!");
            }
            return;
        }
        if (file.isDirectory()) {
            File[] childFile = file.listFiles();
            if (childFile == null || childFile.length == 0) {
                boolean b = file.delete();
                if (!b) {
                    Log.e(TAG, "delete failed!!");
                }
                return;
            }
            for (File f : childFile) {
                recursionDeleteFile(f);
            }
            boolean b = file.delete();
            if (!b) {
                Log.e(TAG, "delete failed!!");
            }
        }
    }

    /**
     * 获取指定目录下所有文件夹的名字（zip文件）
     *
     * @param path 目标路径
     */
    public static ArrayList<String> getZipFiles(String path) {
        ArrayList<String> filePaths = new ArrayList<>();
        File file = new File(path);
        // 如果目标目录不存在则返回
        if (!file.exists())
            return filePaths;
        File[] tempList = file.listFiles();
        if (tempList == null)
            return filePaths;
        // 这段代码仅打印作用，项目中应用要去掉
        for (File f : tempList) {
            String absolutePath = f.getAbsolutePath();
            if (absolutePath.endsWith(".zip")) {
                filePaths.add(absolutePath);
            }
            if (f.isFile() ) {
                Log.i(TAG, "文件：" + f.toString());
            }
        }
        return filePaths;
    }

    /**
     * 获取指定目录下所有文件夹的名字
     *
     * @param path 目标路径
     */
    public static ArrayList<String> getFiles(String path) {
        ArrayList<String> fileNames = new ArrayList<>();
        File file = new File(path);
        // 如果目标目录不存在则返回
        if (!file.exists())
            return fileNames;
        File[] tempList = file.listFiles();
        if (tempList == null)
            return fileNames;
        // 这段代码仅打印作用，项目中应用要去掉
        for (File f : tempList) {
            if (f.isDirectory()) {
                Log.i(TAG, "文件夹名字：" + f.getName());
                fileNames.add(f.getName());
            }
            if (f.isFile()) {
                Log.i(TAG, "文件：" + f.toString());
            }
        }
        return fileNames;
    }

    /**
     * 获取指定目录下所有文件的名字(文件)
     *
     * @param path 目标路径
     */
    public static ArrayList<String> getFiles2(String path) {
        ArrayList<String> fileNames = new ArrayList<>();
        File file = new File(path);
        // 如果目标目录不存在则返回
        if (!file.exists())
            return fileNames;
        File[] tempList = file.listFiles();
        if (tempList == null)
            return fileNames;
        // 这段代码仅打印作用，项目中应用要去掉
        for (File f : tempList) {
            if (f.isDirectory()) {
                Log.i(TAG, "文件夹名字：" + f.getName());
            }
            if (f.isFile()) {
                fileNames.add(f.getName());
                Log.i(TAG, "文件：" + f.toString());
            }
        }
        return fileNames;
    }

    private static final String SEPARATOR = File.separator;//路径分隔符

    /**
     * 复制assets中的文件到指定目录
     *
     * @param context     上下文
     * @param assetsPath  assets资源路径
     * @param storagePath 目标文件夹的路径
     */
    public static void copyFilesFromAssets(Context context, String assetsPath, String storagePath) {
        long startTime = System.currentTimeMillis();
        Log.d(TAG, "copyFilesFromAssets() called with: context = [" + context + "], assetsPath = [" + assetsPath + "], storagePath = [" + storagePath + "]");
        String temp = "";

        if (TextUtils.isEmpty(storagePath)) {
            return;
        } else if (storagePath.endsWith(SEPARATOR)) {
            storagePath = storagePath.substring(0, storagePath.length() - 1);
        }

        if (TextUtils.isEmpty(assetsPath) || assetsPath.equals(SEPARATOR)) {
            assetsPath = "";
        } else if (assetsPath.endsWith(SEPARATOR)) {
            assetsPath = assetsPath.substring(0, assetsPath.length() - 1);
        }

        AssetManager assetManager = context.getAssets();
        try {
            File file = new File(storagePath);
            if (!file.exists()) {//如果文件夹不存在，则创建新的文件夹
                file.mkdirs();
            }

            // 获取assets目录下的所有文件及目录名
            String[] fileNames = assetManager.list(assetsPath);
            if (fileNames.length > 0) {//如果是目录 apk
                for (String fileName : fileNames) {
                    if (!TextUtils.isEmpty(assetsPath)) {
                        temp = assetsPath + SEPARATOR + fileName;//补全assets资源路径
                    }

                    String[] childFileNames = assetManager.list(temp);
                    if (!TextUtils.isEmpty(temp) && childFileNames.length > 0) {//判断是文件还是文件夹：如果是文件夹
                        copyFilesFromAssets(context, temp, storagePath + SEPARATOR + fileName);
                    } else {//如果是文件
                        InputStream inputStream = assetManager.open(temp);
                        readInputStream(storagePath + SEPARATOR + fileName, inputStream);
                    }
                }
            } else {//如果是文件 doc_test.txt或者apk/app_test.apk
                InputStream inputStream = assetManager.open(assetsPath);
                if (assetsPath.contains(SEPARATOR)) {//apk/app_test.apk
                    assetsPath = assetsPath.substring(assetsPath.lastIndexOf(SEPARATOR), assetsPath.length());
                }
                readInputStream(storagePath + SEPARATOR + assetsPath, inputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "copyFilesFromAssets: " + "耗时" + assetsPath + "," + (System.currentTimeMillis() - startTime));
    }

    /**
     * 读取输入流中的数据写入输出流
     *
     * @param storagePath 目标文件路径
     * @param inputStream 输入流
     */
    public static void readInputStream(String storagePath, InputStream inputStream) {
        File file = new File(storagePath);
        try {
            if (!file.exists()) {
                // 1.建立通道对象
                FileOutputStream fos = new FileOutputStream(file);
                // 2.定义存储空间
                byte[] buffer = new byte[inputStream.available()];
                // 3.开始读文件
                int lenght = 0;
                while ((lenght = inputStream.read(buffer)) != -1) {// 循环从输入流读取buffer字节
                    // 将Buffer中的数据写到outputStream对象中
                    fos.write(buffer, 0, lenght);
                }
                fos.flush();// 刷新缓冲区
                // 4.关闭流
                fos.close();
                inputStream.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取指定文件夹下1级目录文件数量
     * 使用场景：判断文件夹下文件容量是否达到上限，例如A文件夹下图片小于100张，则继续保存
     */
    public static int getFilesNum(String path) {
        ArrayList<String> fileNames = new ArrayList<>();
        File file = new File(path);
        // 如果目标目录不存在则返回
        if (!file.exists())
            return 0;
        File[] tempList = file.listFiles();
        if (tempList == null)
            return 0;
        return tempList.length;
    }

}
