package com.sadid.myhometutor.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Helper class for converting images to/from Base64 strings
 * for storing in Firebase Database without using Firebase Storage
 */
public class Base64ImageHelper {

    /**
     * Convert image Uri to Base64 string
     * 
     * @param context Application context
     * @param imageUri Uri of the image to convert
     * @param maxSize Maximum dimension (width/height) for compression (e.g., 800)
     * @param quality JPEG compression quality (0-100, recommended: 60-85)
     * @return Base64 encoded string of the image, or null if conversion fails
     */
    public static String convertUriToBase64(Context context, Uri imageUri, int maxSize, int quality) {
        try {
            // Read the image from Uri
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                return null;
            }

            // Decode the image to Bitmap
            Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            if (originalBitmap == null) {
                return null;
            }

            // Compress/resize the bitmap to reduce size
            Bitmap compressedBitmap = compressBitmap(originalBitmap, maxSize);

            // Convert bitmap to Base64
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            compressedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.close();

            // Cleanup
            if (compressedBitmap != originalBitmap) {
                compressedBitmap.recycle();
            }
            originalBitmap.recycle();

            // Encode to Base64 string
            return Base64.encodeToString(byteArray, Base64.DEFAULT);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Convert image Uri to Base64 string with default settings
     * Default: maxSize=800px, quality=75%
     * 
     * @param context Application context
     * @param imageUri Uri of the image to convert
     * @return Base64 encoded string of the image, or null if conversion fails
     */
    public static String convertUriToBase64(Context context, Uri imageUri) {
        return convertUriToBase64(context, imageUri, 800, 75);
    }

    /**
     * Convert Base64 string to Bitmap
     * 
     * @param base64String Base64 encoded string of the image
     * @return Bitmap object, or null if decoding fails
     */
    public static Bitmap convertBase64ToBitmap(String base64String) {
        try {
            if (base64String == null || base64String.isEmpty()) {
                return null;
            }

            // Decode Base64 string to byte array
            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);

            // Convert byte array to Bitmap
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Load Base64 image into ImageView
     * 
     * @param imageView ImageView to display the image
     * @param base64String Base64 encoded string of the image
     * @param placeholderResId Placeholder resource ID while loading
     */
    public static void loadBase64IntoImageView(ImageView imageView, String base64String, int placeholderResId) {
        if (imageView == null || base64String == null || base64String.isEmpty()) {
            if (imageView != null && placeholderResId != 0) {
                imageView.setImageResource(placeholderResId);
            }
            return;
        }

        try {
            Bitmap bitmap = convertBase64ToBitmap(base64String);
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            } else if (placeholderResId != 0) {
                imageView.setImageResource(placeholderResId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (placeholderResId != 0) {
                imageView.setImageResource(placeholderResId);
            }
        }
    }

    /**
     * Load Base64 image into ImageView using Glide
     * This provides better memory management and smoother transitions
     * 
     * @param context Application context
     * @param imageView ImageView to display the image
     * @param base64String Base64 encoded string of the image
     * @param placeholderResId Placeholder resource ID while loading
     */
    public static void loadBase64IntoImageViewWithGlide(Context context, ImageView imageView, 
                                                         String base64String, int placeholderResId) {
        if (imageView == null || base64String == null || base64String.isEmpty()) {
            if (imageView != null && placeholderResId != 0) {
                Glide.with(context)
                    .load(placeholderResId)
                    .into(imageView);
            }
            return;
        }

        try {
            Bitmap bitmap = convertBase64ToBitmap(base64String);
            if (bitmap != null) {
                Glide.with(context)
                    .load(bitmap)
                    .placeholder(placeholderResId)
                    .into(imageView);
            } else if (placeholderResId != 0) {
                Glide.with(context)
                    .load(placeholderResId)
                    .into(imageView);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (placeholderResId != 0) {
                Glide.with(context)
                    .load(placeholderResId)
                    .into(imageView);
            }
        }
    }

    /**
     * Compress bitmap to fit within maxSize while maintaining aspect ratio
     * 
     * @param original Original bitmap to compress
     * @param maxSize Maximum dimension (width or height)
     * @return Compressed bitmap
     */
    private static Bitmap compressBitmap(Bitmap original, int maxSize) {
        int width = original.getWidth();
        int height = original.getHeight();

        // Check if resizing is needed
        if (width <= maxSize && height <= maxSize) {
            return original;
        }

        // Calculate new dimensions while maintaining aspect ratio
        float scaleFactor;
        if (width > height) {
            scaleFactor = (float) maxSize / width;
        } else {
            scaleFactor = (float) maxSize / height;
        }

        int newWidth = Math.round(width * scaleFactor);
        int newHeight = Math.round(height * scaleFactor);

        // Create scaled bitmap
        return Bitmap.createScaledBitmap(original, newWidth, newHeight, true);
    }

    /**
     * Get the approximate size of Base64 string in KB
     * 
     * @param base64String Base64 encoded string
     * @return Size in kilobytes
     */
    public static double getBase64SizeInKB(String base64String) {
        if (base64String == null || base64String.isEmpty()) {
            return 0;
        }
        return (base64String.length() * 3.0 / 4.0) / 1024.0; // Convert bytes to KB
    }

    /**
     * Validate Base64 string size (Firebase Realtime Database has ~10MB document limit)
     * Recommended to keep images under 1-2MB for performance
     * 
     * @param base64String Base64 encoded string
     * @param maxSizeKB Maximum allowed size in KB (recommended: 1000-2000)
     * @return true if size is within limit, false otherwise
     */
    public static boolean isBase64SizeValid(String base64String, int maxSizeKB) {
        double sizeKB = getBase64SizeInKB(base64String);
        return sizeKB <= maxSizeKB;
    }
}
