package com.github.katelee.recyclerviewlayout;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

/**
 * Created by Kate on 2015/5/7
 */
public class RecyclerViewLayout extends SwipeRefreshLayout {
    private FrameLayout mFrameLayout;
    private RecyclerView mRecyclerView;
    private boolean mHeaderAdjust = false;
    private View mAdjustHeader;
    private RelativeLayout mHeader;
    private RelativeLayout mFooter;
    private RelativeLayout mLoaderMore;
    private Adapter<? extends RecyclerView.ViewHolder> mAdapter;
    private RecyclerViewWithHeaderListener mHeaderListener;
    private RecyclerView.OnScrollListener mScrollListener;

    public RecyclerViewLayout(Context context) {
        this(context, null);
    }

    public RecyclerViewLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        mFrameLayout = new FrameLayout(context);
        mFrameLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        addView(mFrameLayout);

        mRecyclerView = new RecyclerView(context);
        mRecyclerView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        mFrameLayout.addView(mRecyclerView);

        mHeader = new RelativeLayout(context);
        mHeader.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        mFooter = new RelativeLayout(context);
        mFooter.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        mLoaderMore = new RelativeLayout(context);
        mLoaderMore.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        initialize();
    }

    private void initialize() {
        mHeaderListener = new RecyclerViewWithHeaderListener(mRecyclerView) {
            @Override
            View getHeaderView() {
                return mAdjustHeader;
            }
        };
        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (mHeaderAdjust) {
                    mHeaderListener.onScrollStateChanged(recyclerView, newState);
                }
                if (mScrollListener != null) {
                    mScrollListener.onScrollStateChanged(recyclerView, newState);
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (mHeaderAdjust) {
                    mHeaderListener.onScrolled(recyclerView, dx, dy);
                }
                if (mScrollListener != null) {
                    mScrollListener.onScrolled(recyclerView, dx, dy);
                }
            }
        });


    }

    @Override
    public boolean canChildScrollUp() {

        if (android.os.Build.VERSION.SDK_INT < 14) {
            return mRecyclerView.getScrollY() > 0;
        } else {
            return ViewCompat.canScrollVertically(mRecyclerView, -1);
        }
    }

    public void setOnScrollListener(RecyclerView.OnScrollListener listener) {
        mScrollListener = listener;
    }

    public void setAdapter(Adapter<? extends RecyclerView.ViewHolder> adapter) {
        mAdapter = adapter;
        mRecyclerView.setAdapter(mAdapter);
    }

    public void setLayoutManager(RecyclerView.LayoutManager layout) {
        mRecyclerView.setLayoutManager(layout);

        if (layout instanceof GridLayoutManager) {
            final GridLayoutManager gridLayoutManager = ((GridLayoutManager) layout);
            ((GridLayoutManager) layout).setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    return mAdapter.isFullSpan(position) ? gridLayoutManager.getSpanCount() : 1;
                }
            });
        }
    }

    public RecyclerView.LayoutManager getLayoutManager() {
        return mRecyclerView.getLayoutManager();
    }

    public void setAdjustHeaderView(View view) {
        mHeaderAdjust = true;
        if (mAdjustHeader != null) {
            mFrameLayout.removeView(mAdjustHeader);
        }
        mFrameLayout.addView(view, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        mAdjustHeader = view;
        mAdapter.setAdjustHeaderView(view);
    }

    public void removeAdjustHeaderView() {
        mHeaderAdjust = false;
        if (mAdjustHeader != null) {
            mFrameLayout.removeView(mAdjustHeader);
        }

        mAdjustHeader = null;
        mAdapter.setAdjustHeaderView(null);
        mAdapter.notifyHeaderViewChanged();
    }

//    public void setHeaderView(View view) {
//        mAdapter.setHeaderView(view);
//    }
//
//    /**
//     * enable header animate with scroll
//     */
//    public void enableHeaderAdjustWithScroll() {
//        mHeaderAdjust = true;
//    }
//
//    /**
//     * disable header animate with scroll
//     */
//    public void disableHeaderAdjustWithScroll() {
//        mHeaderAdjust = false;
//    }

//    public void setFooterView(View view) {
//        mFooter = view;
//        mAdapter.notifyFooterViewChanged();
//    }
//
//    public void removeFooterView() {
//        mFooter = null;
//        mAdapter.notifyFooterViewChanged();
//    }
//
//    public void setLoaderMoreView(View view) {
//        mLoaderMore = view;
//        mAdapter.notifyLoadMoreViewChanged();
//    }
//
//    public void enableLoadMore() {
//        mAdapter.enableLoadMore();
//    }
//
//    public void disableLoadMore() {
//        mAdapter.disableLoadMore();
//    }

    public static abstract class Adapter<VH extends RecyclerView.ViewHolder> extends AdvanceAdapter<VH> {

        private View adjustHeaderView;

        @Override
        protected void onHeaderBindViewHolder(RecyclerView.ViewHolder viewHolder) {
            super.onHeaderBindViewHolder(viewHolder);

            if (adjustHeaderView != null) {
                ViewGroup.LayoutParams vlp = viewHolder.itemView.getLayoutParams();
                vlp.height = adjustHeaderView.getHeight();
                viewHolder.itemView.setLayoutParams(vlp);
            }
            else {
                ViewGroup.LayoutParams vlp = viewHolder.itemView.getLayoutParams();
                vlp.height = 0;
                viewHolder.itemView.setLayoutParams(vlp);
            }
        }

        private void setAdjustHeaderView(View view) {
            this.adjustHeaderView = view;

            adjustHeaderView.getViewTreeObserver().addOnGlobalLayoutListener(new OnItemGlobalLayoutListener(adjustHeaderView) {
                @Override
                public void onGlobalLayout(View view) {
                    // make sure it is not called anymore
                    view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    notifyHeaderViewChanged();
                }
            });
        }

        private abstract class OnItemGlobalLayoutListener implements ViewTreeObserver.OnGlobalLayoutListener {
            private View view;

            protected OnItemGlobalLayoutListener(View view) {
                this.view = view;
            }

            abstract public void onGlobalLayout(View view);

            @Override
            public void onGlobalLayout() {
                onGlobalLayout(view);
            }

        }
    }
}