package com.prolific.vidmediaplayer.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.net.Uri;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;
import com.prolific.vidmediaplayer.Activities.BaseActivity;
import com.prolific.vidmediaplayer.Activities.VideoPlayerActivity;
import com.prolific.vidmediaplayer.Models.ModelClassForVideo;
import com.prolific.vidmediaplayer.R;

import java.util.ArrayList;

public class VideoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    /* access modifiers changed from: private */
    private ArrayList<Object> arrayListVideosSearch;
    private Activity context;
    private OnItemClickListener mListener;
    /* access modifiers changed from: private */
    private ArrayList<Object> videosArrayList;
    private ArrayList<Uri> arrUriList;
    private String from;
    private View.OnClickListener onClick;
    private int viewTypeVal;
    private int position;
    private View view;
    public final int LAST_PLAY = 0;
    public final int LIST_ITEM = 1;
    public final int LOAD_AD = 2;

    public interface OnItemClickListener {
        void onItemClick(int i);
    }

    public void setOnItemCLickListener(OnItemClickListener onItemClickListener) {
        this.mListener = onItemClickListener;
    }

    public VideoAdapter(Activity context2, ArrayList<Object> arrayList, String from) {
        this.context = context2;
        this.videosArrayList = arrayList;
        this.from = from;
        this.arrayListVideosSearch = new ArrayList<>(this.videosArrayList);
    }

    public VideoAdapter(Activity context2, ArrayList<Object> arrayList, String from, View.OnClickListener listener) {
        this.context = context2;
        this.videosArrayList = arrayList;
        this.from = from;
        this.onClick = listener;
        this.arrayListVideosSearch = new ArrayList<>(this.videosArrayList);
    }

    public void setData(ArrayList<Object> arr) {
        this.videosArrayList = arr;
        notifyItemInserted(videosArrayList.size() - 1);
        notifyItemRangeChanged(videosArrayList.size() - 1, videosArrayList.size());
    }

    int n;

    @Override
    public int getItemViewType(int position) {

        Object data = videosArrayList.get(position);
        if (data instanceof UnifiedNativeAd) {
            n = LOAD_AD;
        } else if (data instanceof ModelClassForVideo) {
            if (((ModelClassForVideo) data).getTypeVal() == 0) {
                n = LAST_PLAY;
            } else {
                n = LIST_ITEM;
            }
        }
        return n;

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == LOAD_AD){
            View unifiedNativeLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_native_ad, parent, false);
            return new UnifiedNativeAdViewHolder(unifiedNativeLayoutView);
        }else {
            if (from.equalsIgnoreCase("player")) {
                view = LayoutInflater.from(context).inflate(R.layout.custom_video_linear_player, parent, false);
            } else {
                if (viewType == LAST_PLAY) {
                    view = LayoutInflater.from(context).inflate(R.layout.layout_recent_play, parent, false);
                } else {
                    viewTypeVal = ((BaseActivity) context).getIntPrefs(context, ((BaseActivity) context).PREF_VIEW_AS);
                    if (viewTypeVal == 0) {
                        view = LayoutInflater.from(context).inflate(R.layout.custom_video_linear, parent, false);
                    } else if (viewTypeVal == 1) {
                        view = LayoutInflater.from(context).inflate(R.layout.custom_video, parent, false);
                    } else {
                        view = LayoutInflater.from(context).inflate(R.layout.custom_video_full_title, parent, false);
                    }
                }
            }
            return new VideosViewHolder(view, viewType);
        }
    }

    @SuppressLint("SetTextI18n")
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int i) {

        Object data = videosArrayList.get(i);
        if(data instanceof UnifiedNativeAd){
            UnifiedNativeAd nativeAd = (UnifiedNativeAd) data;
            populateNativeAdView(nativeAd, ((UnifiedNativeAdViewHolder) vh).getAdView());

        }else {
            VideosViewHolder viewHolder = (VideosViewHolder) vh;
            ModelClassForVideo modelData = ((ModelClassForVideo) data);
            if (from.equalsIgnoreCase("player")) {
                if (i == VideoPlayerActivity.mPosition) {
                    viewHolder.mSongName.setTextColor(context.getResources().getColor(R.color.colorAccent));
                    viewHolder.tvDuration.setTextColor(context.getResources().getColor(R.color.colorAccent));
                } else {
                    viewHolder.mSongName.setTextColor(context.getResources().getColor(R.color.colorWhite));
                    viewHolder.tvDuration.setTextColor(context.getResources().getColor(R.color.colorWhite));
                }

                if (modelData.getSongThumb() != null && !modelData.getSongThumb().equalsIgnoreCase("")) {
                    Glide.with(context).load(modelData.getSongThumb())
                            .placeholder(R.drawable.splash_logo)
                            .error(R.color.colorBlackTransparent)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(viewHolder.mThumbnail);
                }

                viewHolder.mSongName.setText(modelData.getSongName());
                viewHolder.tvDuration.setText(BaseActivity.convertDuration(modelData.getSongDuration()));

                viewHolder.llMain.setTag(i);
                viewHolder.llMain.setOnClickListener(onClick);

            } else {
                if (getItemViewType(i) == LAST_PLAY) {

//                Log.e("TAG",  ""+data.getTotalSize());
                    viewHolder.tvVidCountSize.setText((getItemCount() - 1) + " VIDEOS total " + ((BaseActivity) context).readableFileSize(((BaseActivity) context).totalVidSize));

                    if (from.equalsIgnoreCase("main_list")) {
                        boolean isLastPlayVisible = ((BaseActivity) context).getBooleanPrefs(context, ((BaseActivity) context).PREF_IS_LAST_PLAY_VISIBLE);
                        if (isLastPlayVisible) {
                            ModelClassForVideo latestVideoData = ((BaseActivity) context).getLatestVideo(context, BaseActivity.PREF_LATEST_PLAY);
                            if (latestVideoData != null) {
                                viewHolder.tvLatestSongName.setText(latestVideoData.getSongName());
                                viewHolder.tvPlayTiming.setText(BaseActivity.convertDuration(latestVideoData.getCurrDuration()) + " / " + BaseActivity.convertDuration(latestVideoData.getSongDuration()));
                                Glide.with(context).load(latestVideoData.getSongThumb())
                                        .placeholder(R.drawable.splash_logo)
                                        .error(R.drawable.splash_logo)
                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                                        .into(viewHolder.ivThumb);
                                viewHolder.seekLatestPlay.setMax(latestVideoData.getSongDuration());
                                viewHolder.seekLatestPlay.setProgress(latestVideoData.getCurrDuration());
                                viewHolder.cvMain.setVisibility(View.VISIBLE);
                            } else {
                                viewHolder.cvMain.setVisibility(View.GONE);
                            }
                        } else {
                            viewHolder.cvMain.setVisibility(View.GONE);
                        }
                    } else {
                        viewHolder.cvMain.setVisibility(View.GONE);
                    }
                } else {
                    if (viewTypeVal == 2) {
                        viewHolder.mSongName.setText(modelData.getSongName());
                        viewHolder.tvDuration.setText(BaseActivity.convertDuration(modelData.getSongDuration()));
                    } else {

                        if (modelData.getSongThumb() != null && !modelData.getSongThumb().equalsIgnoreCase("")) {
                            Glide.with(context).load(modelData.getSongThumb())
                                    .placeholder(R.drawable.splash_logo)
                                    .error(R.drawable.splash_logo)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .into(viewHolder.mThumbnail);
                        }

                        viewHolder.mSongName.setText(modelData.getSongName());
                        viewHolder.tvDuration.setText(BaseActivity.convertDuration(modelData.getSongDuration()));
                        viewHolder.mSongSize.setText(((BaseActivity) context).readableFileSize(modelData.getSongSize()));
                        if (viewTypeVal == 0) {
                            viewHolder.mSongDate.setText(((BaseActivity) context).changeMillisToDate(modelData.getDateModified(), "yyyy-MM-dd"));//dd-MMM-yyyy  hh:mm a
                        }
                    }
                }
                viewHolder.cvMain.setTag(i);
                viewHolder.cvMain.setOnClickListener(onClick);

                viewHolder.iv_more.setTag(R.string.view, viewHolder.iv_more);
                viewHolder.iv_more.setTag(R.string.pos, i);
                viewHolder.iv_more.setOnClickListener(onClick);
            }
        }
    }


    public int getItemCount() {
        return videosArrayList.size();
    }


    public class VideosViewHolder extends RecyclerView.ViewHolder {// implements View.OnCreateContextMenuListener
        CardView mParentView;
        TextView mSongAlbum;
        TextView mSongName, tvDuration, mSongDate, mSongSize, tvVidCountSize;
        ImageView mThumbnail, iv_more;

        TextView tvLatestSongName, tvPlayTiming;
        ImageView ivThumb;
        LinearLayout llMain;
        SeekBar seekLatestPlay;
        CardView cvMain;

        public VideosViewHolder(View view, int viewType) {
            super(view);

            tvVidCountSize = (TextView) view.findViewById(R.id.tvVidCountSize);
            iv_more = (ImageView) view.findViewById(R.id.iv_more);
            if (from.equalsIgnoreCase("player")) {
                mThumbnail = (ImageView) view.findViewById(R.id.linearThumbnail);
                mSongName = (TextView) view.findViewById(R.id.linearSongName);
                tvDuration = (TextView) view.findViewById(R.id.tvDuration);
                llMain = (LinearLayout) view.findViewById(R.id.llMain);
            } else {
                if (viewType == LAST_PLAY) {
                    tvLatestSongName = view.findViewById(R.id.tvLatestSongName);
                    ivThumb = view.findViewById(R.id.ivThumb);
                    tvPlayTiming = view.findViewById(R.id.tvPlayTiming);
                    seekLatestPlay = view.findViewById(R.id.seekLatestPlay);
                    llMain = view.findViewById(R.id.llMain);
                    cvMain = view.findViewById(R.id.cvMain);

                } else {
                    llMain = view.findViewById(R.id.llMain);
                    cvMain = view.findViewById(R.id.cvMain);
                    mThumbnail = (ImageView) view.findViewById(R.id.linearThumbnail);
                    mSongName = (TextView) view.findViewById(R.id.linearSongName);
                    tvDuration = (TextView) view.findViewById(R.id.tvDuration);
                    tvPlayTiming = (TextView) view.findViewById(R.id.tvPlayTiming);
                    mSongDate = (TextView) view.findViewById(R.id.linearSongDate);
                    mSongSize = (TextView) view.findViewById(R.id.linearSongSize);
                    mSongAlbum = (TextView) view.findViewById(R.id.linearSongAlbum);
                    mParentView = (CardView) view.findViewById(R.id.parent);
                }
            }
        }
    }




    public class UnifiedNativeAdViewHolder extends RecyclerView.ViewHolder {

        private UnifiedNativeAdView adView;

        public UnifiedNativeAdView getAdView() {
            return adView;
        }

        public UnifiedNativeAdViewHolder(View view) {
            super(view);
            adView = (UnifiedNativeAdView) view.findViewById(R.id.ad_view);

            // The MediaView will display a video asset if one is present in the ad, and the
            // first image asset otherwise.
//            adView.setMediaView((MediaView) adView.findViewById(R.id.ad_media));

            // Register the view used for each individual asset.
            adView.setHeadlineView(adView.findViewById(R.id.appinstall_headline));
            adView.setBodyView(adView.findViewById(R.id.appinstall_body));
//            adView.setCallToActionView(adView.findViewById(R.id.appinstall_call_to_action));
            adView.setIconView(adView.findViewById(R.id.appinstall_app_icon));
//            adView.setPriceView(adView.findViewById(R.id.ad_price));
//            adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
//            adView.setStoreView(adView.findViewById(R.id.ad_store));
//            adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));
        }

    }


    private void populateNativeAdView(UnifiedNativeAd nativeAd, UnifiedNativeAdView adView) {
        // Some assets are guaranteed to be in every UnifiedNativeAd.
        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
//        ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        NativeAd.Image icon = nativeAd.getIcon();

        if (icon == null) {
            adView.getIconView().setVisibility(View.INVISIBLE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(icon.getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

//        if (nativeAd.getPrice() == null) {
//            adView.getPriceView().setVisibility(View.INVISIBLE);
//        } else {
//            adView.getPriceView().setVisibility(View.VISIBLE);
//            ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
//        }

//        if (nativeAd.getStore() == null) {
//            adView.getStoreView().setVisibility(View.INVISIBLE);
//        } else {
//            adView.getStoreView().setVisibility(View.VISIBLE);
//            ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
//        }

//        if (nativeAd.getStarRating() == null) {
//            adView.getStarRatingView().setVisibility(View.INVISIBLE);
//        } else {
//            ((RatingBar) adView.getStarRatingView())
//                    .setRating(nativeAd.getStarRating().floatValue());
//            adView.getStarRatingView().setVisibility(View.VISIBLE);
//        }

//        if (nativeAd.getAdvertiser() == null) {
//            adView.getAdvertiserView().setVisibility(View.INVISIBLE);
//        } else {
//            ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
//            adView.getAdvertiserView().setVisibility(View.VISIBLE);
//        }

        // Assign native ad object to the native view.
        adView.setNativeAd(nativeAd);
    }
}
