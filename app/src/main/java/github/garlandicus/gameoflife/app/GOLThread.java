package github.garlandicus.gameoflife.app;

import android.graphics.Canvas;
import android.view.SurfaceHolder;
import android.content.Context;
import android.util.Log;

/**
 * Created by Ryan on 6/1/2014.
 */
public class GOLThread extends Thread {

    boolean mRun;
    Canvas mCanvas;
    SurfaceHolder surfaceHolder;
    Context context;
    GameOfLifeView GOLView;

    public GOLThread(SurfaceHolder sHolder, Context ctx, GameOfLifeView golView)
    {

        mRun = false;

        surfaceHolder = sHolder;
        context = ctx;
        GOLView = golView;
    }

    void setRunning(boolean bRun)
    {
        mRun = bRun;
    }

    @Override
    public void run() {
        super.run();
        while (mRun) {
            mCanvas = surfaceHolder.lockCanvas();
            if (mCanvas != null)
            {
                if(GOLView.getRunning()) {
                    GOLView.step();
                }
                GOLView.drawToCanvas(mCanvas);
            }
            try{
                surfaceHolder.unlockCanvasAndPost(mCanvas);
            }
            catch(Exception e)
            {
                Log.e("GOL","Can't update the surfaceHolder!");
            }
        }
    }

}
