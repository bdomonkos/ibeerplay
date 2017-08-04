package atom.partners.ibeerplay;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.util.AttributeSet;
import android.widget.VideoView;


public class SizedVideoView extends VideoView implements OnVideoSizeChangedListener {
    private int videoWidth, videoHeight;

    public SizedVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SizedVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(videoWidth, widthMeasureSpec);
        int height = getDefaultSize(videoHeight, heightMeasureSpec);
        // Do not change w & h for screen fill
        setMeasuredDimension(width, height);
        // Experiment by changing width & height compared to the video size for different results
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        videoWidth = width;
        videoHeight = height;
    }
}
