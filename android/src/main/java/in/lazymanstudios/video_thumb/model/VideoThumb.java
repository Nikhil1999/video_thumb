package in.lazymanstudios.video_thumb.model;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.Random;
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
            if (path != null && !path.isEmpty()) {
                executorService.submit(new GetFileThumbnailCallable(context, result, getFileName(path), path, null));
            } else {
                sendFileCorruptedMessage(result);
            }
        } catch (Exception ex) {
            sendErrorMessage(result, ex.getMessage());
        }
    }

    private String getFileName(String path) {
        int startIndex = path.lastIndexOf('/');
        return path.substring(startIndex);
    }

    public void getUriThumbnail(MethodChannel.Result result, String uri) {
        try {
            if (uri != null && !uri.isEmpty()) {
                Uri tUri = Uri.parse(uri);
                executorService.submit(new GetFileThumbnailCallable(context, result, getFileName(tUri), null, tUri));
            } else {
                sendFileCorruptedMessage(result);
            }
        } catch (Exception ex) {
            sendErrorMessage(result, ex.getMessage());
        }
    }

    public void getUriFileName(MethodChannel.Result result, String uri) {
        try {
            if (uri != null && !uri.isEmpty()) {
                Uri tUri = Uri.parse(uri);
                result.success(getFileName(tUri));
            } else {
                sendFileCorruptedMessage(result);
            }
        } catch (Exception ex) {
            sendErrorMessage(result, ex.getMessage());
        }
    }

    private String getFileName(Uri uri) {
        String filename = null;

        try {
            try (Cursor cursor = context.getContentResolver().query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index != -1) {
                        filename = cursor.getString(index);
                    }
                }
            }

            if(filename == null || filename.isEmpty()) {
                filename = uri.getLastPathSegment();
            }
        } catch (Exception ex) {
            Log.e(TAG, "Failed to get file name: " + ex);
        }

        if (filename == null || filename.isEmpty()) {
            filename = "" + new Random().nextInt(100000);
        }

        return filename;
    }

    private String getUriFileName(Uri uri) {
        String filename = null;

        try {
            try (Cursor cursor = context.getContentResolver().query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index != -1) {
                        filename = cursor.getString(index);
                    }
                }
            }

            if(filename == null || filename.isEmpty()) {
                filename = uri.getLastPathSegment();
            }
        } catch (Exception ex) {
            Log.e(TAG, "Failed to get file name: " + ex);
        }

        if (filename == null || filename.isEmpty()) {
            filename = "";
        }

        return filename;
    }

    public void getVideoMeta(MethodChannel.Result result, String path) {
        try {
            if (path != null && !path.isEmpty()) {
                executorService.submit(new GetVideoMetaCallable(context, result, path));
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
        private final String name;
        private final String path;
        private final Uri uri;

        public GetFileThumbnailCallable(Context context, MethodChannel.Result result, String name, String path, Uri uri) {
            this.context = context;
            this.result = result;
            this.name = name;
            this.path = path;
            this.uri = uri;
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

        private Bitmap getBitmap() {
            Bitmap bitmap;

            try {
                if (uri != null) {
                    bitmap = Glide.with(context)
                            .asBitmap()
                            .load(uri)
                            .submit(512, 512)
                            .get();
                } else {
                    bitmap = Glide.with(context)
                            .asBitmap()
                            .load(path)
                            .submit(512, 512)
                            .get();
                }

                return bitmap;
            } catch (Exception ex) {
                Log.e(TAG, "Assuming this file is corrupted: " + ex);
            }

            return null;
        }

        private String getOutputFileName() {
            int startIndex = 0;
            int endIndex = name.lastIndexOf('.');
            if (endIndex != -1) {
                return name.substring(startIndex, endIndex) + ".jpg";
            } else {
                return name.substring(startIndex) + ".jpg";
            }
        }

        private void copyThumbnailToFile(File outputFile) throws IOException {
            Bitmap bitmap = getBitmap();

            if (bitmap == null) {
                sendErrorResult("File corrupted");
                return;
            }

            outputFile.createNewFile();
            try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

                sendSuccessResult(outputFile.getAbsolutePath());
            }
        }

        @Override
        public Boolean call() {
            try {
                File cacheDirectory = getCacheDirectory(context);
                File outputFile = new File(cacheDirectory + File.separator + getOutputFileName());

                copyThumbnailToFile(outputFile);
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

    private static class GetVideoMetaCallable implements Callable<Boolean> {
        private final Context context;
        private final MethodChannel.Result result;
        private final String path;

        public GetVideoMetaCallable(Context context, MethodChannel.Result result, String path) {
            this.context = context;
            this.result = result;
            this.path = path;
        }

        private void getVideoMeta(String path) throws JSONException {
            FFmpegMediaMetadataRetriever retriever = new FFmpegMediaMetadataRetriever();

            try {
                retriever.setDataSource(path);
                retriever.getMetadata();

                String durationStr = retriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_DURATION);

                JSONObject json = new JSONObject();
                json.put("duration", durationStr);

                sendSuccessResult(json);
            } finally {
                retriever.release();
            }
        }

        @Override
        public Boolean call() {
            try {
                getVideoMeta(path);
            } catch (Exception ex) {
                sendErrorResult(ex.getMessage());
            }
            return true;
        }

        private void sendSuccessResult(final JSONObject json) {
            result.success(json.toString());
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
