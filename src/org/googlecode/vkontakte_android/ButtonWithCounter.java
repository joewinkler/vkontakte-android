package org.googlecode.vkontakte_android;

import android.widget.FrameLayout;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageButton;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.graphics.drawable.Drawable;

public class ButtonWithCounter extends FrameLayout {
    private ImageButton imageButton;
    private TextView textView;

    public ButtonWithCounter(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.button_with_counter, this, true);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ButtonWithCounter, 0, 0);
        Drawable buttonBackground = array.getDrawable(R.styleable.ButtonWithCounter_buttonBackground);
        imageButton = (ImageButton) findViewById(R.id.button);
        imageButton.setImageDrawable(buttonBackground);
        textView = (TextView) findViewById(R.id.counter);
        String text = array.getString(R.styleable.ButtonWithCounter_counterText);
        Drawable counterBackground = array.getDrawable(R.styleable.ButtonWithCounter_counterBackground);
        textView.setText(text);
        textView.setBackgroundDrawable(counterBackground);
        array.recycle();
    }

    @Override
    public void setOnClickListener(OnClickListener onClickListener) {
        imageButton.setOnClickListener(onClickListener);
    }

    @Override
    public void setSelected(boolean b) {
        imageButton.setSelected(b);
    }

    public void setCounter(String text) {
        textView.setText(text);
    }
}
