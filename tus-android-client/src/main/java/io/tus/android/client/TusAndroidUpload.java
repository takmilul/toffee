package io.tus.android.client;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.tus.java.client.TusUpload;

public class TusAndroidUpload extends TusUpload {
    public TusAndroidUpload(Uri uri, Context context,String filename,String absPath) throws FileNotFoundException {
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(uri, new String[]{OpenableColumns.SIZE, OpenableColumns.DISPLAY_NAME}, null, null, null);
        if(cursor == null) {
            cursor= context.getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    , new String[] { MediaStore.Video.Media._ID }
                    , MediaStore.Video.Media.DATA + "=? "
                    , new String[] { absPath }, null);
        }
        cursor.moveToFirst();
        ParcelFileDescriptor fd = resolver.openFileDescriptor(uri, "r");
        if(fd == null) {
            throw new FileNotFoundException();
        }
        long size = fd.getStatSize();
        try {
            fd.close();
        } catch (IOException e) {
            Log.e("TusAndroidUpload", "unable to close ParcelFileDescriptor", e);
        }
        setSize(size);
        setInputStream(resolver.openInputStream(uri));
        setFingerprint(String.format("%s-%d", uri.toString(), size));
        Map<String, String> metadata = new HashMap<>();
        metadata.put("filename", filename);
        setMetadata(metadata);
        cursor.close();
    }
}
