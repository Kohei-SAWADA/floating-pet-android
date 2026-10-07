package com.dot.floatingpet;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import com.dot.floatingpet.core.SpriteAtlas;
import java.io.*;

/** Local-only, bounded PNG import. Old valid image survives all failed imports. */
final class PetStore {
    static final long MAX_BYTES = 12L * 1024 * 1024;
    private static final String SAMPLE_ASSET = "pets/mofu/spritesheet.png";
    private final Context context;
    PetStore(Context context) { this.context = context.getApplicationContext(); }
    SharedPreferences prefs() { return context.getSharedPreferences("pet", Context.MODE_PRIVATE); }
    File imageFile() { return new File(context.getFilesDir(), "spritesheet.png"); }
    boolean hasImage() { return imageFile().isFile(); }
    Bitmap load() throws IOException { return decode(imageFile()); }
    void importImage(Uri uri) throws IOException {
        importImage(() -> context.getContentResolver().openInputStream(uri));
    }
    void importSampleImage() throws IOException {
        importImage(() -> context.getAssets().open(SAMPLE_ASSET));
    }
    // File-picker and bundled sample inputs use exactly the same validation and atomic replacement.
    private interface ImageSource { InputStream open() throws IOException; }
    private void importImage(ImageSource source) throws IOException {
        File temp = File.createTempFile("pet-import-", ".png", context.getFilesDir());
        try {
            try (InputStream in = source.open(); OutputStream out = new FileOutputStream(temp)) {
                if (in == null) throw new ImageException(R.string.error_image_read);
                byte[] buffer = new byte[16384]; long total = 0; int count;
                while ((count = in.read(buffer)) != -1) {
                    total += count;
                    if (total > MAX_BYTES) throw new ImageException(R.string.error_image_too_large);
                    out.write(buffer, 0, count);
                }
            }
            Bitmap checked = decode(temp); checked.recycle();
            // Both paths are private, same-filesystem files; rename atomically replaces the old atlas.
            if (!temp.renameTo(imageFile())) throw new ImageException(R.string.error_image_save);
        } finally { if (temp.exists()) temp.delete(); }
    }
    private Bitmap decode(File file) throws IOException {
        if (!file.isFile()) throw new ImageException(R.string.error_image_missing);
        if (file.length() > MAX_BYTES) throw new ImageException(R.string.error_image_too_large);
        byte[] signature = new byte[8];
        try (DataInputStream in = new DataInputStream(new FileInputStream(file))) { in.readFully(signature); }
        catch (EOFException e) { throw new ImageException(R.string.error_image_corrupt); }
        byte[] expected = {(byte)137,80,78,71,13,10,26,10};
        if (!java.util.Arrays.equals(signature, expected)) throw new ImageException(R.string.error_image_not_png);
        BitmapFactory.Options bounds = new BitmapFactory.Options(); bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), bounds);
        try { SpriteAtlas.validateDimensions(bounds.outWidth, bounds.outHeight); }
        catch (IllegalArgumentException e) { throw new ImageException(R.string.error_image_dimensions); }
        BitmapFactory.Options options = new BitmapFactory.Options(); options.inScaled = false; options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
            if (bitmap == null) throw new ImageException(R.string.error_image_corrupt);
            try { SpriteAtlas.validateDimensions(bitmap.getWidth(), bitmap.getHeight()); }
            catch (IllegalArgumentException e) { bitmap.recycle(); throw new ImageException(R.string.error_image_decode_dimensions); }
            if (!bitmap.hasAlpha()) { bitmap.recycle(); throw new ImageException(R.string.error_image_alpha); }
            return bitmap;
        } catch (OutOfMemoryError e) { throw new ImageException(R.string.error_image_memory); }
    }
    /** Only known, app-owned error IDs may reach the UI. Never display provider exception text. */
    static int errorMessageResource(Exception error) {
        return error instanceof ImageException ? ((ImageException)error).messageId : R.string.error_image_read;
    }
    private static final class ImageException extends IOException {
        final int messageId;
        ImageException(int messageId) { this.messageId = messageId; }
    }
}
