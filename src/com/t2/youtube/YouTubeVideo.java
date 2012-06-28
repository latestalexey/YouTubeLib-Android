package com.t2.youtube;

public final class YouTubeVideo {
	private String mPublished;
	private String mTitle;
	private String mAuthor;
	private String mUrl;
	private String mThumbnailUrl;
	private String mId;
	private String mViewCount;
	private String mLength;

	/**
	 * @return the id
	 */
	public String getId() {
		return mId;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		mId = id;
	}

	/**
	 * @return the published
	 */
	public String getPublished() {
		return mPublished;
	}

	/**
	 * @return the length
	 */
	public String getLength() {
		return mLength;
	}

	/**
	 * @param length
	 *            the length to set
	 */
	public void setLength(String length) {
		mLength = length;
	}

	/**
	 * @param published
	 *            the published to set
	 */
	public void setPublished(String published) {
		mPublished = published;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return mTitle;
	}

	/**
	 * @param title
	 *            the title to set
	 */
	public void setTitle(String title) {
		mTitle = title;
	}

	/**
	 * @return the author
	 */
	public String getAuthor() {
		return mAuthor;
	}

	/**
	 * @param author
	 *            the author to set
	 */
	public void setAuthor(String author) {
		mAuthor = author;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return mUrl;
	}

	/**
	 * @param url
	 *            the url to set
	 */
	public void setUrl(String url) {
		mUrl = url;
	}

	/**
	 * @return the thumbnailUrl
	 */
	public String getThumbnailUrl() {
		return mThumbnailUrl;
	}

	/**
	 * @param thumbnailUrl
	 *            the thumbnailUrl to set
	 */
	public void setThumbnailUrl(String thumbnailUrl) {
		mThumbnailUrl = thumbnailUrl;
	}

	/**
	 * @return the viewCount
	 */
	public String getViewCount() {
		return mViewCount;
	}

	/**
	 * @param viewCount
	 *            the viewCount to set
	 */
	public void setViewCount(String viewCount) {
		mViewCount = viewCount;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mId == null) ? 0 : mId.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		YouTubeVideo other = (YouTubeVideo) obj;
		if (mId == null) {
			if (other.mId != null)
				return false;
		} else if (!mId.equals(other.mId))
			return false;
		return true;
	}

}