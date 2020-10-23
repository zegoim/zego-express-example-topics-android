package im.zego.common.widgets;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import im.zego.common.R;

public class SnapshotDialog extends Dialog {
    private  Context context;
    private ImageView snapshotImg;
    public SnapshotDialog(@NonNull Context context) {
        super(context,0);
    }

    public SnapshotDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        this.context=context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = View.inflate(context, R.layout.snapshot_dialog, null);
        setContentView(view);

        setCanceledOnTouchOutside(false);

        Window win = getWindow();
        WindowManager.LayoutParams lp = win.getAttributes();
        lp.height = dip2px(context, 250);
        lp.width = dip2px(context, 200);
        win.setAttributes(lp);

        view.findViewById(R.id.img_close).setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        snapshotImg = view.findViewById(R.id.snapshot_img);
    }
    /**屏幕密度比例*/
    public static float getScreenDendity(Context context){
        return context.getResources().getDisplayMetrics().density;//3
    }
    /**
     * dp转px
     * 16dp - 48px
     * 17dp - 51px*/
    public static int dip2px(Context context, float dpValue) {
        float scale = getScreenDendity(context);
        return (int)((dpValue * scale) + 0.5f);
    }
    public void setSnapshotBitmap(Bitmap bitmap){
        if(snapshotImg!=null){
            snapshotImg.setImageBitmap(bitmap);
        }
    }

}
