package com.t2.youtube;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.t2.youtube.YouTubeSearchFragment.YouTubeSearchListener;

public class YouTubeSearchActivity extends FragmentActivity implements YouTubeSearchListener, OnItemClickListener {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.youtube_search);
		((YouTubeSearchFragment) getSupportFragmentManager().findFragmentById(R.id.frg_youtube)).setYouTubeSearchListener(this);
		((YouTubeSearchFragment) getSupportFragmentManager().findFragmentById(R.id.frg_youtube)).getListView().setOnItemClickListener(this);
		findViewById(R.id.btn_search).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onSearchRequested();
			}
		});
		handleSearchIntent(getIntent());
	}

	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		YouTubeVideo video = (YouTubeVideo) ((YouTubeSearchFragment) getSupportFragmentManager().findFragmentById(R.id.frg_youtube))
				.getListAdapter().getItem(arg2);

		Intent result = new Intent();
		result.setData(Uri.parse(video.getUrl()));
		result.putExtra("thumbnail", video.getThumbnailUrl());
		result.putExtra("title", video.getTitle());
		result.putExtra("id", video.getId());
		setResult(Activity.RESULT_OK, result);
		finish();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		handleSearchIntent(intent);
	}

	private void handleSearchIntent(Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			((TextView) findViewById(R.id.lbl_query)).setText("Results for \"" + query + "\"");
			((YouTubeSearchFragment) getSupportFragmentManager().findFragmentById(R.id.frg_youtube)).search(query);
		} else {
			onSearchRequested();
		}
	}

	public void onSearchStart() {
		findViewById(R.id.lbl_query).setVisibility(View.VISIBLE);
		findViewById(R.id.img_progress).setVisibility(View.VISIBLE);
	}

	public void onSearchFailure() {
		findViewById(R.id.img_progress).setVisibility(View.GONE);
	}

	public void onSearchSuccess() {
		findViewById(R.id.img_progress).setVisibility(View.GONE);
	}

}