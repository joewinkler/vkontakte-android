//package org.googlecode.vkontakte_android;
//
//import android.widget.TabHost;
//import android.content.Context;
//import android.util.AttributeSet;
//import android.view.View;
//
//public class MyTabHost extends TabHost{
//    public MyTabHost(Context context, AttributeSet attributeSet) {
//        super(context, attributeSet);
//    }
//
//    @Override
//    public TabSpec newTabSpec(String s) {
//        TabHost.TabSpec spec = super.newTabSpec(s);
//        return spec;
//
//    }
//
//    public class MyTabSpec extends TabSpec{
//
//        MyTabSpec() {
////            super();
//        }
//    }
//
//    /**
//     * How to create a tab indicator by specifying a view.
//     */
//    private class ViewIndicatorStrategy implements IndicatorStrategy {
//
//        private final View mView;
//
//        private ViewIndicatorStrategy(View view) {
//            mView = view;
//        }
//
//        public View createIndicatorView() {
//            return mView;
//        }
//    }
//}
