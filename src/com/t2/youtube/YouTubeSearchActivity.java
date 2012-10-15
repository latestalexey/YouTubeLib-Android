/*
 * 
 * YouTubeLib
 * 
 * Copyright © 2009-2012 United States Government as represented by 
 * the Chief Information Officer of the National Center for Telehealth 
 * and Technology. All Rights Reserved.
 * 
 * Copyright © 2009-2012 Contributors. All Rights Reserved. 
 * 
 * THIS OPEN SOURCE AGREEMENT ("AGREEMENT") DEFINES THE RIGHTS OF USE, 
 * REPRODUCTION, DISTRIBUTION, MODIFICATION AND REDISTRIBUTION OF CERTAIN 
 * COMPUTER SOFTWARE ORIGINALLY RELEASED BY THE UNITED STATES GOVERNMENT 
 * AS REPRESENTED BY THE GOVERNMENT AGENCY LISTED BELOW ("GOVERNMENT AGENCY"). 
 * THE UNITED STATES GOVERNMENT, AS REPRESENTED BY GOVERNMENT AGENCY, IS AN 
 * INTENDED THIRD-PARTY BENEFICIARY OF ALL SUBSEQUENT DISTRIBUTIONS OR 
 * REDISTRIBUTIONS OF THE SUBJECT SOFTWARE. ANYONE WHO USES, REPRODUCES, 
 * DISTRIBUTES, MODIFIES OR REDISTRIBUTES THE SUBJECT SOFTWARE, AS DEFINED 
 * HEREIN, OR ANY PART THEREOF, IS, BY THAT ACTION, ACCEPTING IN FULL THE 
 * RESPONSIBILITIES AND OBLIGATIONS CONTAINED IN THIS AGREEMENT.
 * 
 * Government Agency: The National Center for Telehealth and Technology
 * Government Agency Original Software Designation: YouTubeLib001
 * Government Agency Original Software Title: YouTubeLib
 * User Registration Requested. Please send email 
 * with your contact information to: robert.kayl2@us.army.mil
 * Government Agency Point of Contact for Original Software: robert.kayl2@us.army.mil
 * 
 */
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