package com.t2.youtube.lazylist;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

public class ImageLoader {

	private MemoryCache mMemoryCache = new MemoryCache();
	private FileCache mFileCache;
	private Map<ImageView, String> mImageViews = Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
	private ExecutorService mExecutorService;

	public ImageLoader(Context context) {
		mFileCache = new FileCache(context);
		mExecutorService = Executors.newFixedThreadPool(5);
	}

	public void displayImage(String url, ImageView imageView)
	{
		mImageViews.put(imageView, url);
		Bitmap bitmap = mMemoryCache.get(url);
		if (bitmap != null)
			imageView.setImageBitmap(bitmap);
		else
		{
			queuePhoto(url, imageView);
			imageView.setImageBitmap(null);
		}
	}

	private void queuePhoto(String url, ImageView imageView)
	{
		PhotoToLoad p = new PhotoToLoad(url, imageView);
		mExecutorService.submit(new PhotosLoader(p));
	}

	private Bitmap getBitmap(String url)
	{
		File f = mFileCache.getFile(url);

		// from SD cache
		Bitmap b = decodeFile(f);
		if (b != null)
			return b;

		// from web
		try {
			Bitmap bitmap = null;
			URL imageUrl = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
			conn.setConnectTimeout(30000);
			conn.setReadTimeout(30000);
			conn.setInstanceFollowRedirects(true);
			InputStream is = conn.getInputStream();
			OutputStream os = new FileOutputStream(f);
			Utils.CopyStream(is, os);
			os.close();
			bitmap = decodeFile(f);
			return bitmap;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	// decodes image and scales it to reduce memory consumption
	private Bitmap decodeFile(File f) {
		try {
			// decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(new FileInputStream(f), null, o);

			// Find the correct scale value. It should be the power of 2.
			final int REQUIRED_SIZE = 70;
			int width_tmp = o.outWidth, height_tmp = o.outHeight;
			int scale = 1;
			while (true) {
				if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE)
					break;
				width_tmp /= 2;
				height_tmp /= 2;
				scale *= 2;
			}

			// decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
		} catch (FileNotFoundException e) {
		}
		return null;
	}

	// Task for the queue
	private class PhotoToLoad
	{
		public String mUrl;
		public ImageView mImageView;

		public PhotoToLoad(String u, ImageView i) {
			mUrl = u;
			mImageView = i;
		}
	}

	class PhotosLoader implements Runnable {
		PhotoToLoad mPhotosToLoad;

		PhotosLoader(PhotoToLoad photoToLoad) {
			this.mPhotosToLoad = photoToLoad;
		}

		public void run() {
			if (imageViewReused(mPhotosToLoad))
				return;
			Bitmap bmp = getBitmap(mPhotosToLoad.mUrl);
			mMemoryCache.put(mPhotosToLoad.mUrl, bmp);
			if (imageViewReused(mPhotosToLoad))
				return;
			BitmapDisplayer bd = new BitmapDisplayer(bmp, mPhotosToLoad);
			Activity a = (Activity) mPhotosToLoad.mImageView.getContext();
			a.runOnUiThread(bd);
		}
	}

	boolean imageViewReused(PhotoToLoad photoToLoad) {
		String tag = mImageViews.get(photoToLoad.mImageView);
		if (tag == null || !tag.equals(photoToLoad.mUrl))
			return true;
		return false;
	}

	// Used to display bitmap in the UI thread
	class BitmapDisplayer implements Runnable
	{
		Bitmap mBitmap;
		PhotoToLoad mPhotosToLoad;

		public BitmapDisplayer(Bitmap b, PhotoToLoad p) {
			mBitmap = b;
			mPhotosToLoad = p;
		}

		public void run()
		{
			if (imageViewReused(mPhotosToLoad))
				return;
			if (mBitmap != null)
				mPhotosToLoad.mImageView.setImageBitmap(mBitmap);
			else
				mPhotosToLoad.mImageView.setImageBitmap(null);
		}
	}

	public void clearCache() {
		mMemoryCache.clear();
		mFileCache.clear();
	}

}
