package com.dalingge.gankio.module.home.gank;


import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.dalingge.gankio.R;
import com.dalingge.gankio.common.Constants;
import com.dalingge.gankio.common.base.BaseLazyFragment;
import com.dalingge.gankio.common.base.factory.RequiresPresenter;
import com.dalingge.gankio.common.bean.GankBean;
import com.dalingge.gankio.common.widgets.recyclerview.anim.adapter.AlphaAnimatorAdapter;
import com.dalingge.gankio.common.widgets.recyclerview.anim.itemanimator.SlideInOutBottomItemAnimator;
import com.dalingge.gankio.module.web.WebActivity;
import com.dalingge.gankio.network.HttpExceptionHandle;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;


/**
 * A simple {@link Fragment} subclass.
 */
@RequiresPresenter(GankPresenter.class)
public class GankFragment extends BaseLazyFragment<GankPresenter> implements SwipeRefreshLayout.OnRefreshListener,GankAdapter.OnItemClickListener {

    @BindView(R.id.recycle_view)
    RecyclerView recycleView;
    @BindView(R.id.swipe_refresh_widget)
    SwipeRefreshLayout swipeRefreshWidget;

    private ArrayList<GankBean> mData = new ArrayList<>();
    private GankAdapter mGankAdapter;
    private String mType;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mType = args.getString(Constants.BUNDLE_KEY_TYPE);
        }
    }

    @Override
    protected void lazyLoad() {
        if ((!isPrepared || !isVisible)) {
            return;
        }

        if (mData.isEmpty()) {
            showRefresh();
            onRefresh();
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_gank;
    }

    @Override
    protected void initView(View view) {
        swipeRefreshWidget.setColorSchemeResources(
                R.color.primary, R.color.accent,
                R.color.primary_dark, R.color.primary_light);
        swipeRefreshWidget.setOnRefreshListener(this);
        mGankAdapter = new GankAdapter(getActivity(), mData);
        mGankAdapter.setOnItemClickListener(this);
        AlphaAnimatorAdapter animatorAdapter = new AlphaAnimatorAdapter(mGankAdapter, recycleView);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getActivity());
        recycleView.setLayoutManager(mLinearLayoutManager);
        recycleView.setHasFixedSize(true);
        recycleView.setItemAnimator(new SlideInOutBottomItemAnimator(recycleView));
        recycleView.setAdapter(animatorAdapter);
    }

    @Override
    public void onRefresh() {
        mData.clear();
        getPresenter().request(mType);
    }

    public void onAddData(List<GankBean> gankBeanList) {
        hideRefresh();
        mData.addAll(gankBeanList);
        mGankAdapter.notifyDataSetChanged();
    }

    public void onNetworkError(HttpExceptionHandle.ResponeThrowable responeThrowable) {
        hideRefresh();
        Snackbar.make(recycleView.getRootView(), responeThrowable.message, Snackbar.LENGTH_SHORT).show();
    }

    private void showRefresh(){
        swipeRefreshWidget.setRefreshing(true);
    }

    private void hideRefresh(){
        // 防止刷新消失太快，让子弹飞一会儿. do not use lambda!!
        swipeRefreshWidget.postDelayed(()-> {
                if(swipeRefreshWidget != null){
                    swipeRefreshWidget.setRefreshing(false);
            }
        },1000);
    }

    @Override
    public void onItemClick(View view, int position) {
         ActivityOptionsCompat options;
        if (Build.VERSION.SDK_INT >= 21) {
            options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    getActivity(), view,  mGankAdapter.getItem(position)._id);
        } else {
            options = ActivityOptionsCompat.makeScaleUpAnimation(
                    view,
                    view.getWidth()/2, view.getHeight()/2,//拉伸开始的坐标
                    0, 0);//拉伸开始的区域大小，这里用（0，0）表示从无到全屏
        }
        GankBean resultsBean = mGankAdapter.getItem(position);
        getActivity().startActivity(WebActivity.newIntent(getActivity(),resultsBean.url,resultsBean.desc));
      //  ActivityCompat.startActivity(getActivity(), WebActivity.newIntent(getActivity(),resultsBean.url,resultsBean.desc),options.toBundle());
    }
}
