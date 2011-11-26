/*
 * Kontalk Android client
 * Copyright (C) 2011 Kontalk Devteam <devteam@kontalk.org>

 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.kontalk.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;


/**
 * Media storage utilities.
 * @author Daniele Ricci
 */
public abstract class MediaStorage {
    public static final File MEDIA_ROOT = new File(Environment.getExternalStorageDirectory(), "Kontalk");

    private static final int THUMBNAIL_WIDTH = 80;
    private static final int THUMBNAIL_HEIGHT = 80;

    /** Writes a media to the internal cache. */
    public static File writeInternalMedia(Context context, String filename, byte[] contents) throws IOException {
        File file = new File(context.getCacheDir(), filename);
        FileOutputStream fout = new FileOutputStream(file);
        fout.write(contents);
        fout.close();
        return file;
    }

    private static BitmapFactory.Options processOptions(BitmapFactory.Options options) {
        int w = options.outWidth;
        int h = options.outHeight;
        // error :(
        if (w < 0 || h < 0) return null;

        if (w > THUMBNAIL_WIDTH)
            options.inSampleSize = (w / THUMBNAIL_WIDTH);
        else if (h > THUMBNAIL_HEIGHT)
            options.inSampleSize = (h / THUMBNAIL_HEIGHT);

        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        return options;
    }

    /** Generates {@link BitmapFactory.Options} for the given {@link InputStream}. */
    private static BitmapFactory.Options preloadBitmap(InputStream in) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(in, null, options);

        return processOptions(options);
    }

    /** Writes a thumbnail of a media to the internal cache. */
    public static File cacheThumbnail(Context context, Uri media, String filename) throws IOException {
        File file = new File(context.getCacheDir(), filename);
        cacheThumbnail(context, media, file);
        return file;
    }

    /** Writes a thumbnail of a media to a {@link File}. */
    public static void cacheThumbnail(Context context, Uri media, File destination) throws IOException {
        FileOutputStream fout = new FileOutputStream(destination);
        cacheThumbnail(context, media, fout);
        fout.close();
    }

    private static void cacheThumbnail(Context context, Uri media, FileOutputStream fout) throws IOException {
        ContentResolver cr = context.getContentResolver();
        InputStream in = cr.openInputStream(media);
        BitmapFactory.Options options = preloadBitmap(in);
        in.close();

        // open again
        in = cr.openInputStream(media);
        Bitmap bitmap = BitmapFactory.decodeStream(in, null, options);
        in.close();
        Bitmap thumbnail = ThumbnailUtils
            .extractThumbnail(bitmap, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT);
        bitmap.recycle();

        // write down to file
        thumbnail.compress(Bitmap.CompressFormat.PNG, 90, fout);
        thumbnail.recycle();
    }

    public static File writeMedia(String filename, InputStream source) throws IOException {
        MEDIA_ROOT.mkdirs();
        File f = new File(MEDIA_ROOT, filename);
        FileOutputStream fout = new FileOutputStream(f);
        byte[] buffer = new byte[1024];
        int len;
        while ((len = source.read(buffer)) != -1)
            fout.write(buffer, 0, len);
        fout.close();
        return f;
    }

}
