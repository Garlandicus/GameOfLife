package github.garlandicus.gameoflife.app;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * Ryan Darge
 * Game of Life, and then some.
 *
 * LOG:
 * 6/3/2014
 * -Added "history" in the form of dead cells and thriving cells
 * -Fixed lock issues with resetting the board while running the simulation
 * -Compartmentalized a couple functions for faster/cleaner access
 * -Added Hashtable for colors
 * -Added new initialize function that doesn't change the board size
 * -Created strange new set of rules where dead cells can't be reborn until they're done "mourning" (see mode 2)
 *
 * 6/4/2014
 * - Fixed crashing on exit
 * - Added SharedPreferences to maintain state between sessions, now we can exit/return to the same session
 *
 *
 * TODO: Add options in expanded menu with "more" button
 * TODO: See if keeping a list of all the active cells (or cells to be checked) would be better than iterating through the array (size/processing issue)
 * TODO: ADD a "Zoom" function
 * TODO: ADD an "EDIT" function
 * TODO: Add "step backwards" and efficient backtracking
 * TODO: Add "About" page
 */
public class MainActivity
        extends Activity {

    public final String TAG = "Darge SurfaceView";
    public final String PREFS_NAME = "GOLPrefs";
    public SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button buttonStep = (Button)findViewById(R.id.button_step);
        buttonStep.setOnClickListener(stepListener);

        Button buttonRun = (Button)findViewById(R.id.button_run);
        buttonRun.setOnClickListener(runListener);

        Button buttonReset = (Button)findViewById(R.id.button_reset);
        buttonReset.setOnClickListener(resetListener);

        //Restore Preferences
        settings = getSharedPreferences(PREFS_NAME, 0);
    }

    private OnClickListener stepListener = new OnClickListener(){
        public void onClick(View v){
            GameOfLifeView gol = (GameOfLifeView)findViewById(R.id.surfaceView);
            gol.step();
        }
    };

    private OnClickListener runListener = new OnClickListener(){
        public void onClick(View v){
            GameOfLifeView gol = (GameOfLifeView)findViewById(R.id.surfaceView);
            gol.setRunning(!gol.getRunning());
            if(gol.getRunning() == true)
                ((Button)findViewById(R.id.button_step)).setEnabled(false);
            else
                ((Button)findViewById(R.id.button_step)).setEnabled(true);
        }
    };

    private OnClickListener resetListener = new OnClickListener(){
        public void onClick(View v){
            GameOfLifeView gol = (GameOfLifeView)findViewById(R.id.surfaceView);
            gol.reset();
        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*
    private void drawStuff(SurfaceHolder holder) {
        Log.i(TAG, "Trying to draw...");

        Canvas canvas = holder.lockCanvas();
        if(canvas == null){
            Log.e(TAG, "Can't draw, canvas is null!");
        }
        else{
            Log.i(TAG, "Drawing...");
            canvas.drawRGB(255,128,128);
            holder.unlockCanvasAndPost(canvas);
        }
    }
    */
}
