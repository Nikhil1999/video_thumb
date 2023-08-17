package in.lazymanstudios.video_thumb.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.flutter.plugin.common.MethodChannel;
import wseemann.media.FFmpegMediaMetadataRetriever;

public class VideoThumb {
    private static final String TAG = "VideoThumb";
    private static final ExecutorService executorService = Executors.newFixedThreadPool(3);
    private final Context context;

    public VideoThumb(Context context) {
        this.context = context;
    }

    public void getFileThumbnail(MethodChannel.Result result, String path) {
        try {
            Log.e(TAG, "Called");
            if (path != null && !path.isEmpty()) {
                executorService.submit(new GetFileThumbnailCallable(context, result, path));
            } else {
                sendFileCorruptedMessage(result);
            }
        } catch (Exception ex) {
            sendErrorMessage(result, ex.getMessage());
        }
    }


    public void clearTemporaryFiles(MethodChannel.Result result) {
        try {
            executorService.submit(new ClearCacheCallable(context, result));
        } catch (Exception ex) {
            sendErrorMessage(result, ex.getMessage());
        }
    }

    private void sendFileCorruptedMessage(MethodChannel.Result result) {
        result.error("IO_EXCEPTION", "File corrupted", null);
    }

    private void sendErrorMessage(MethodChannel.Result result, String message) {
        result.error("IO_EXCEPTION", message, null);
    }

    private static class GetFileThumbnailCallable implements Callable<Boolean> {
        private final Context context;
        private final MethodChannel.Result result;
        private final String path;

        public GetFileThumbnailCallable(Context context, MethodChannel.Result result, String path) {
            this.context = context;
            this.result = result;
            this.path = path;
        }

        private static synchronized File getCacheDirectory(Context context) throws IOException {
            File cacheDirectory = context.getCacheDir();

            String fileDirectory = UUID.randomUUID().toString();
            File outputDirectory = new File(cacheDirectory + File.separator + "video_thumb" + File.separator + fileDirectory);

            while (outputDirectory.exists()) {
                fileDirectory = UUID.randomUUID().toString();
                outputDirectory = new File(cacheDirectory+ File.separator + "video_thumb" + File.separator + fileDirectory);
            }

            boolean isOutputDirectoryCreated = outputDirectory.mkdirs();

            if (isOutputDirectoryCreated) {
                return outputDirectory;
            } else {
                throw new IOException("Failed to create output directory");
            }
        }

        private Bitmap getBitmap(String path) {
            Bitmap bitmap;
            FFmpegMediaMetadataRetriever retriever = new FFmpegMediaMetadataRetriever();
//            MediaMetadataRetriever retriever = new MediaMetadataRetriever();

            try {
                retriever.setDataSource(path);
                Log.e(TAG, path);
//                bitmap = retriever.getFrameAtTime(-1, MediaMetadataRetriever.OPTION_CLOSEST);
                bitmap = retriever.getFrameAtTime(-1, FFmpegMediaMetadataRetriever.OPTION_CLOSEST);

                Log.e(TAG, String.valueOf((bitmap == null)));
                if (bitmap != null) {
                    int width = bitmap.getWidth();
                    int height = bitmap.getHeight();
                    int max = Math.max(width, height);
                    if (max > 512) {
                        float scale = 512f / max;
                        int w = Math.round(scale * width);
                        int h = Math.round(scale * height);
                        bitmap = Bitmap.createScaledBitmap(bitmap, w, h, true);
                    }
                }

                return bitmap;
            } catch (Exception ex) {
                Log.e(TAG, "Assuming this file is corrupted: " + ex);
            } finally {
                retriever.release();
            }

            return null;
        }

        private String getOutputFileName(String path) {
            int startIndex = path.lastIndexOf('/');
            int endIndex = path.lastIndexOf('.');
            if (endIndex != -1) {
                return path.substring(startIndex, endIndex) + ".jpg";
            } else {
                return path.substring(startIndex) + ".jpg";
            }
        }

        private void copyThumbnailToFile(String path, File outputFile) throws IOException {
            Bitmap bitmap = getBitmap(path);

            if (bitmap == null) {
                sendErrorResult("File corrupted");
                return;
            }

            outputFile.createNewFile();
            FileOutputStream outputStream = null;
            try {
                outputStream = new FileOutputStream(outputFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

                sendSuccessResult(outputFile.getAbsolutePath());
            } finally {
                if (outputStream != null) {
                    outputStream.close();
                }
            }

//            FileOutputStream outputStream = null;
//            try {
//                outputStream = new FileOutputStream(path);
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
//                sendSuccessResult(outputFile.getAbsolutePath());
//            } finally {
//                if (outputStream != null) {
//                    outputStream.close();
//                }
//            }
        }

        @Override
        public Boolean call() {
            try {
                File cacheDirectory = getCacheDirectory(context);
                File outputFile = new File(cacheDirectory + File.separator + getOutputFileName(path));

                copyThumbnailToFile(path, outputFile);
            } catch (Exception ex) {
                sendErrorResult(ex.getMessage());
            }
            return true;
        }

        private void sendSuccessResult(final String filepath) {
            result.success(filepath);
        }

        private void sendErrorResult(final String errorMessage) {
            result.error("IO_EXCEPTION", errorMessage, null);
        }
    }

    private static class ClearCacheCallable implements Callable<Boolean> {
        private final Context context;
        private final MethodChannel.Result result;

        public ClearCacheCallable(Context context, MethodChannel.Result result) {
            this.context = context;
            this.result = result;
        }

        private void delete(File file) {
            File[] subfiles = file.listFiles();

            if (subfiles != null) {
                for (File i : subfiles) {
                    if (i.isDirectory()) {
                        delete(i);
                    }
                    i.delete();
                }
            }
        }

        @Override
        public Boolean call() {
            try {
                File cacheDirectory = context.getCacheDir();
                File appCacheDirectory = new File(cacheDirectory + File.separator + "video_thumb");
                delete(appCacheDirectory);

                sendSuccessResult();
            } catch (Exception ex) {
                sendErrorResult(ex.getMessage());
            }
            return true;
        }

        private void sendSuccessResult() {
            result.success(true);
        }

        private void sendErrorResult(final String errorMessage) {
            result.error("IO_EXCEPTION", errorMessage, null);
        }
    }
}
