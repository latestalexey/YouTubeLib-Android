package com.t2.youtube;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.t2.youtube.lazylist.ImageLoader;

public class YouTubeSearchFragment extends ListFragment {
	private static final String API_KEY = "AI39si7MTKRN8WhD-PjPShszFM0plR95CWG-Tg24qdYrp49vqAJzGTVmLQzgNjtUThGQygW_ARBqScgQeIgpGW6af5e16ZEBRw";
	private static final String ROOT_URL = "http://gdata.youtube.com";
	private static final SimpleDateFormat DATE_PARSER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

	private static AsyncHttpClient sClient = new AsyncHttpClient();

	private YouTubeSearchListener mYouTubeSearchListener;

	private String mQuery;

	public void setYouTubeSearchListener(YouTubeSearchListener youTubeSearchListener) {
		mYouTubeSearchListener = youTubeSearchListener;
	}

	public static interface YouTubeSearchListener {
		public void onSearchStart();

		public void onSearchFailure();

		public void onSearchSuccess();
	}

	public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		sClient.get(getAbsoluteUrl(url), params, responseHandler);
	}

	private static String getAbsoluteUrl(String relativeUrl) {
		return ROOT_URL + relativeUrl;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setListShownNoAnimation(true);
		getListView().setDivider(new ColorDrawable(0x00000000));
	}

	public void search(String query) {
		search(query, 0, new AsyncHttpResponseHandler() {
			@Override
			public void onStart() {
				super.onStart();
				if (mYouTubeSearchListener != null) {
					mYouTubeSearchListener.onSearchStart();
				}
			}

			@Override
			public void onSuccess(String arg0) {
				try {
					JSONObject json = new JSONObject(arg0).getJSONObject("feed");
					JSONArray jsonVideos = json.getJSONArray("entry");
					List<YouTubeVideo> videos = new ArrayList<YouTubeVideo>();
					for (int i = 0; i < jsonVideos.length(); i++) {
						JSONObject video = jsonVideos.getJSONObject(i);
						videos.add(jsonToVideo(video));
					}
					if (mYouTubeSearchListener != null) {
						mYouTubeSearchListener.onSearchSuccess();
					}

					int total = json.getJSONObject("openSearch$totalResults").getInt("$t");
					int itemsPer = json.getJSONObject("openSearch$itemsPerPage").getInt("$t");

					setListAdapter(new YouTubeAdapter(getActivity(), videos, total, itemsPer));
				} catch (JSONException e) {
					e.printStackTrace();
				}

			}

			@Override
			public void onFailure(Throwable arg0) {
				super.onFailure(arg0);
				if (mYouTubeSearchListener != null) {
					mYouTubeSearchListener.onSearchFailure();
				}
			}

		});
	}

	public void search(String query, int startIndex, AsyncHttpResponseHandler handler) {
		mQuery = query;
		RequestParams params = new RequestParams();
		params.put("q", URLEncoder.encode(mQuery));
		params.put("max-results", 20 + "");
		params.put("alt", "json");
		if (startIndex > 0) {
			params.put("start-index", startIndex + "");
		}
		params.put("key", API_KEY);
		params.put(
				"fields",
				"openSearch:totalResults," +
						"	openSearch:startIndex," +
						"	openSearch:itemsPerPage," +
						"	entry(" +
						"		title[@type='text']," +
						"		author(" +
						"			name" +
						"		)," +
						"		published," +
						"		yt:statistics(" +
						"			@viewCount" +
						"		)," +
						"		media:group(" +
						"			yt:duration(" +
						"				@seconds" +
						"			)," +
						"			media:player," +
						"			media:thumbnail[@height=360](@url)" +
						"		)" +
						"	)");
		get("/feeds/mobile/videos", params, handler);
	}

	private static final Pattern ID_PATTERN = Pattern.compile("v=(.*?)&");

	public static YouTubeVideo jsonToVideo(JSONObject json) {
		YouTubeVideo video = new YouTubeVideo();

		try {
			video.setTitle(json.getJSONObject("title").getString("$t"));
			video.setAuthor(json.getJSONArray("author").getJSONObject(0).getJSONObject("name").getString("$t"));
			final JSONObject mediaGroup = json.getJSONObject("media$group");
			JSONArray thumbnails = mediaGroup.getJSONArray("media$thumbnail");
			video.setThumbnailUrl(thumbnails.getJSONObject(thumbnails.length() > 1 ? 1 : 0).getString("url"));
			StringBuilder views = new StringBuilder();
			Formatter format = new Formatter(views);
			format.format("%,d", json.getJSONObject("yt$statistics").getLong("viewCount"));
			video.setViewCount(views.toString());
			final String url = mediaGroup.getJSONArray("media$player").getJSONObject(0).getString("url");
			Matcher matcher = ID_PATTERN.matcher(url);
			if (matcher.find()) {
				video.setId(matcher.group(1));
			}
			video.setUrl(url);
			Date date = DATE_PARSER.parse(json.getJSONObject("published").getString("$t"));
			video.setPublished(DateUtils.getRelativeTimeSpanString(date.getTime(), System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
					DateUtils.FORMAT_ABBREV_ALL).toString());
			video.setLength(DateUtils.formatElapsedTime(mediaGroup.getJSONObject("yt$duration").getLong("seconds")));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return video;
	}

	private final class YouTubeAdapter extends ArrayAdapter<YouTubeVideo> {

		private ImageLoader mImageLoader;
		private int mLoadedIndex, mTotalVideos, mItemsPerPage;
		private List<YouTubeVideo> mVideos;
		private boolean mLoading;

		public YouTubeAdapter(Context context, List<YouTubeVideo> objects, int totalVideos, int itemsPerPage) {
			super(context, android.R.layout.simple_list_item_1, objects);
			mVideos = objects;
			mLoadedIndex = 1;
			mTotalVideos = totalVideos;
			mItemsPerPage = itemsPerPage;
			mImageLoader = new ImageLoader(context);
		}

		public void addVideos(List<YouTubeVideo> videos) {
			mVideos.addAll(videos);
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			if (!hasMoreVideos()) {
				return super.getCount();
			} else {
				return super.getCount() + 1;
			}
		}

		private boolean hasMoreVideos() {
			return mLoadedIndex + mItemsPerPage < mTotalVideos;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				v = LayoutInflater.from(getContext()).inflate(R.layout.video_row, null);
			}

			if (hasMoreVideos() && position == (getCount() - 1)) {
				RelativeLayout layout = new RelativeLayout(getContext());
				ProgressBar bar = new ProgressBar(getContext());
				bar.setIndeterminate(true);
				bar.setPadding(10, 10, 10, 10);
				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
						RelativeLayout.LayoutParams.WRAP_CONTENT);
				params.addRule(RelativeLayout.CENTER_IN_PARENT);
				bar.setLayoutParams(params);

				if (!mLoading) {
					mLoading = true;
					search(mQuery, mLoadedIndex + mItemsPerPage, new AsyncHttpResponseHandler() {
						@Override
						public void onSuccess(String arg0) {
							try {
								JSONObject json = new JSONObject(arg0).getJSONObject("feed");
								JSONArray jsonVideos = json.getJSONArray("entry");
								List<YouTubeVideo> videos = new ArrayList<YouTubeVideo>();
								for (int i = 0; i < jsonVideos.length(); i++) {
									JSONObject video = jsonVideos.getJSONObject(i);
									final YouTubeVideo ytVideo = jsonToVideo(video);
									if (!mVideos.contains(ytVideo)) {
										videos.add(ytVideo);
									}
								}
								if (mYouTubeSearchListener != null) {
									mYouTubeSearchListener.onSearchSuccess();
								}

								mLoadedIndex += mItemsPerPage;
								addVideos(videos);
							} catch (JSONException e) {
								e.printStackTrace();
							}

						}

						@Override
						public void onFinish() {
							super.onFinish();
							mLoading = false;
						}
					});
				}
				layout.addView(bar);

				return layout;
			} else if (v.findViewById(R.id.lbl_title) == null) {
				v = LayoutInflater.from(getContext()).inflate(R.layout.video_row, null);
			}

			YouTubeVideo video = getItem(position);
			if (video.getThumbnailUrl() != null) {
				mImageLoader.displayImage(video.getThumbnailUrl(), ((ImageView) v.findViewById(R.id.img_thumb)));
			}
			((TextView) v.findViewById(R.id.lbl_title)).setText(video.getTitle());
			((TextView) v.findViewById(R.id.lbl_author)).setText(video.getAuthor());
			((TextView) v.findViewById(R.id.lbl_published)).setText(video.getPublished() + " | " + video.getViewCount() + " views");
			((TextView) v.findViewById(R.id.lbl_duration)).setText(video.getLength());
			return v;
		}
	}
}
