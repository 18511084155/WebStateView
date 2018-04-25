package com.woodys.stateview;
import android.view.LayoutInflater;
import android.view.View;

import com.woodys.stateview.R;
import com.woodys.statelayout.OverlapVaryViewHelper;
import com.woodys.statelayout.ReplaceVaryViewHelper;
import com.woodys.statelayout.interfaces.IVaryViewHelper;


/**
 * 有数据、加载中、加载失败、加载成功页面切换控制器
 *
 * @author woodys
 * @date 2016/11/17 17:02
 */
public class ViewHelperController{

    public IVaryViewHelper helper; // 切换不同视图的帮助类
    public View mLoadingView; // 加载中View
    public View mErrorView; // 加载失败View
    public View mSuccessView; // 加载成功View
    private View.OnClickListener mRefreshListener; // 刷新监听

    public ViewHelperController(View contentView) {
        this(new ReplaceVaryViewHelper(contentView));
    }

    public ViewHelperController(IVaryViewHelper helper) {
        this.helper = helper;
    }

    /**
     * 创建CaseViewHelperController(方法二)
     */
    public static ViewHelperController createCaseViewHelperController(View view) {
        return new ViewHelperController(new OverlapVaryViewHelper(view))
                .setUpSuccessView(LayoutInflater.from(view.getContext()).inflate(R.layout.success_view_layout, null))
                .setUpLoadingView(LayoutInflater.from(view.getContext()).inflate(R.layout.load_view_layout, null))
                .setUpErrorView(LayoutInflater.from(view.getContext()).inflate(R.layout.error_view_layout, null));
    }

    
    public View getCurrentView() {
        return helper.getCurrentView();
    }

    
    public View getContentView() {
        return helper.getContentView();
    }

    
    public View getSuccessView() {
        return mSuccessView;
    }

    
    public View getLoadingView() {
        return mLoadingView;
    }

    
    public View getErrorView() {
        return mErrorView;
    }

    
    public ViewHelperController setUpSuccessView(View view) {
        this.mSuccessView = view;
        return this;
    }

    
    public ViewHelperController setUpLoadingView(View view) {
        this.mLoadingView = view;
        return this;
    }

    
    public ViewHelperController setUpErrorView(View view) {
        this.mErrorView = view;
        return this;
    }


    
    public ViewHelperController setUpRefreshViews(View... views) {
        if (views != null && views.length > 0 && mRefreshListener != null) {
            for (View view : views) {
                if (view != null) {
                    view.setEnabled(true);
                    view.setClickable(true);
                    view.setOnClickListener(mRefreshListener);
                }
            }
        }
        return this;
    }

    
    public ViewHelperController setUpRefreshListener(View.OnClickListener refreshListener) {
        this.mRefreshListener = refreshListener;
        return this;
    }

    
    public void showSuccessView() {
        helper.showLayout(mSuccessView);
    }

    
    public void showLoadingView() {
        helper.showLayout(mLoadingView);
    }

    
    public void showErrorView() {
        helper.showLayout(mErrorView);
    }


    public void restore() {
        helper.restoreLayout();
    }

    
    public void releaseCaseViews() {
        mSuccessView = null;
        mLoadingView = null;
        mErrorView = null;
    }

    public static class Builder {

        private View mContentView;
        private View mSuccessView;
        private View mLoadingView;
        private View mErrorView;
        private View[] views;
        private View.OnClickListener mRefreshListener;

        /**
         * 返回显示数据的View
         */
        public View getContentView() {
            return mContentView;
        }

        /**
         * 返回无数据的View
         */
        public View getSuccessView() {
            return mSuccessView;
        }

        /**
         * 返回加载中的View
         */
        public View getLoadingView() {
            return mLoadingView;
        }

        /**
         * 返回加载失败的View
         */
        public View getErrorView() {
            return mErrorView;
        }

        /**
         * 设置显示数据的View
         */
        public Builder setContentView(View contentView) {
            this.mContentView = contentView;
            return this;
        }

        /**
         * 设置无数据的View
         */
        public Builder setSuccessView(View emptyView) {
            this.mSuccessView = emptyView;
            return this;
        }

        /**
         * 设置加载中的View
         */
        public Builder setLoadingView(View loadingView) {
            this.mLoadingView = loadingView;
            return this;
        }

        /**
         * 设置加载失败的View
         */
        public Builder setErrorView(View errorView) {
            this.mErrorView = errorView;
            return this;
        }

        /**
         * 设置刷新监听
         */
        public Builder setRefreshListener(View.OnClickListener refreshListener) {
            this.mRefreshListener = refreshListener;
            return this;
        }

        /**
         * 设置可以刷新的View
         */
        public Builder setRefreshViews(View... views) {
            this.views = views;
            return this;
        }

        /**
         * 构建CaseViewHelperController对象
         */
        public ViewHelperController build() {
            if (mContentView == null) {
                throw new IllegalArgumentException("The contentView can not be null!");
            }
            ViewHelperController helper = new ViewHelperController(mContentView);
            if (mSuccessView != null) {
                helper.setUpSuccessView(mSuccessView);
            }
            if (mLoadingView != null) {
                helper.setUpLoadingView(mLoadingView);
            }
            if (mErrorView != null) {
                helper.setUpErrorView(mErrorView);
            }
            if (mRefreshListener != null) {
                helper.setUpRefreshListener(mRefreshListener);
            }
            if (views != null && views.length > 0) {
                helper.setUpRefreshViews(views);
            }
            return helper;
        }
    }
}
