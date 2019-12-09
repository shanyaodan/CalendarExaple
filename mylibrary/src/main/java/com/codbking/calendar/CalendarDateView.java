package com.codbking.calendar;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static com.codbking.calendar.CalendarFactory.getMonthOfDayList;

/**
 * Created by codbking on 2016/12/18.
 * email:codbking@gmail.com
 * github:https://github.com/codbking
 * blog:http://www.jianshu.com/users/49d47538a2dd/latest_articles
 */

public class CalendarDateView extends ViewPager implements CalendarTopView {

    HashMap<Integer, CalendarView> views = new HashMap<>();
    private CaledarTopViewChangeListener mCaledarLayoutChangeListener;
    private CalendarView.OnItemClickListener onItemClickListener;

    private LinkedList<CalendarView> cache = new LinkedList();

    private int MAXCOUNT=6;


    private int row = 6;

    private CaledarAdapter mAdapter;
    private int calendarItemHeight = 0;

    public void setAdapter(CaledarAdapter adapter) {
        mAdapter = adapter;
        initData();
    }

    public void setOnItemClickListener(CalendarView.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public CalendarDateView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CalendarDateView);
        row = a.getInteger(R.styleable.CalendarDateView_cbd_calendar_row, 6);
        a.recycle();
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int calendarHeight = 0;
        if (getAdapter() != null) {
            CalendarView view = (CalendarView) getChildAt(0);
            if (view != null) {
                calendarHeight = view.getMeasuredHeight();
                calendarItemHeight = view.getItemHeight();
            }
        }
        setMeasuredDimension(widthMeasureSpec, MeasureSpec.makeMeasureSpec(calendarHeight, MeasureSpec.EXACTLY));
    }

    private int prePosition=-1;

    private int checkContantMonth(CalendarBean compare){
        for(int i=0;i<cache.size();i++){
            List<CalendarBean> data= cache.get(i).getData();
           if(data.get(0).year==compare.year&&data.get(0).moth==compare.moth&&data.get(0).day==compare.day)
            return i;
        }
        return -1;
    }

    private void init() {
       final int[] dateArr= CalendarUtil.getYMD(new Date());

        setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return Integer.MAX_VALUE;
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public Object instantiateItem(ViewGroup container, final int position) {
                CalendarView view;
                boolean iscache=false;
                List<CalendarBean> monthOfday=getMonthOfDayList(dateArr[0], dateArr[1] + position - Integer.MAX_VALUE / 2);
                int index=checkContantMonth(monthOfday.get(0));
                if(index==-1) {
                    view = new CalendarView(container.getContext(), row);
                }else {
                    view=  cache.removeFirst();
                }
                view.setOnItemClickListener(new CalendarView.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view1, int postion1, CalendarBean bean) {

                        for(int i=0;i<cache.size();i++){
                            cache.get(i).setData(getMonthOfDayList(dateArr[0],dateArr[1]+position-Integer.MAX_VALUE/2),false);
                        }
                             Iterator iterator = views.keySet().iterator();
                           while (iterator.hasNext()){
                             try {
                                views.get(iterator.next()).resetSelection();
                            }catch (Throwable t){
                                t.printStackTrace();
                            }
                        }
                        if(null!=onItemClickListener){
                            onItemClickListener.onItemClick(view1,postion1,bean);
                        }
                        prePosition = position;
                    }
                });
                view.setAdapter(mAdapter);
                boolean istoday = position==Integer.MAX_VALUE/2;
                if(prePosition>=0){
                    if(index<0) {
                        view.setData(monthOfday, false);
                    }else {
                        view.selectChild();
                    }
                }else {
                    view.setData(monthOfday,istoday);
                }
                if(istoday){
                    prePosition = position;
                }
                container.addView(view);
                views.put(position, view);
                return view;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView((View) object);
                if(!cache.contains(object)) {
                    cache.addLast((CalendarView) object);
                }
                views.remove(position);
            }
        });

        addOnPageChangeListener(new SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

//                if (onItemClickListener != null) {
//                    CalendarView view = views.get(position);
//                    Object[] obs = view.getSelect();
//                    if(null==obs){
//
//                    }else {
//                        onItemClickListener.onItemClick((View) obs[0], (int) obs[1], (CalendarBean) obs[2]);
//                    }
//                }

                mCaledarLayoutChangeListener.onLayoutChange(CalendarDateView.this);
            }
        });
    }


    private void initData() {
        setCurrentItem(Integer.MAX_VALUE/2, false);
        getAdapter().notifyDataSetChanged();

    }

    @Override
    public int[] getCurrentSelectPositon() {
        CalendarView view = views.get(getCurrentItem());
        if (view == null) {
            view = (CalendarView) getChildAt(0);
        }
        if (view != null) {
            return view.getSelectPostion();
        }
        return new int[4];
    }

    @Override
    public int getItemHeight() {
        return calendarItemHeight;
    }

    @Override
    public void setCaledarTopViewChangeListener(CaledarTopViewChangeListener listener) {
        mCaledarLayoutChangeListener = listener;
    }


}
