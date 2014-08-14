package github.garlandicus.gameoflife.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.graphics.Paint;
import android.util.Log;
import java.util.Random;
import java.util.Hashtable;

/**
 * Created by Ryan on 6/1/2014.
 * Designed to run with GOLThread.java
 */
public class GameOfLifeView
        extends SurfaceView
        implements SurfaceHolder.Callback{

    Context context;
    GOLThread golThread;
    Hashtable<String, Paint> colors;
    SharedPreferences settings;

    boolean ready;
    boolean running;
    boolean reset;
    int size;
    int mode;
    int[][] states;
    int[][] buffer;

    int cellHeight;
    int cellWidth;
    int topOffset;
    int leftOffset;

    public final String PREFS_NAME = "GOLPrefs";
    public boolean DEBUG = false;



    public GameOfLifeView(Context newContext, AttributeSet attrs) {
        super(newContext, attrs);

        context=newContext;
        settings = context.getSharedPreferences(PREFS_NAME, 0);
        mode = settings.getInt("mode",1);
        size = settings.getInt("size",100);

        ready = false;
        running = false;
        reset = false;
        initialize(size, mode);

        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
    }

    public void initialize(int newSize, int newMode)
    {
        size = newSize;
        states = new int[size][size];
        buffer = new int[size][size];

        initialize(newMode);
    }

    public void initialize(int newMode)
    {
        mode = newMode;

        if(settings.getBoolean("saved",false)){
            Log.i("GOL","Loading from saved state");
            printSettings();
            setState(settings.getString("state", ""));
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("saved", false);
            editor.commit();
        }
        else if(mode > 0) {
            Random random = new Random();
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    if(random.nextInt(10) < 2)
                        states[x][y] = 1;
                    else
                        states[x][y] = 0;
                }
            }
        }

        initializeColors();
        ready = true;

    }

    public void initializeColors(){
        Paint alive = new Paint();
        alive.setStyle(Paint.Style.FILL);
        alive.setColor(Color.GREEN);
        alive.setStrokeWidth(2);

        Paint border = new Paint();
        border.setStyle(Paint.Style.FILL);
        border.setColor(Color.BLACK);
        border.setStrokeWidth(2);

        Paint dead = new Paint();
        dead.setStyle(Paint.Style.FILL);
        dead.setColor(Color.RED);
        dead.setStrokeWidth(2);

        Paint thriving = new Paint();
        thriving.setStyle(Paint.Style.FILL);
        thriving.setColor(Color.BLUE);
        thriving.setStrokeWidth(2);

        Paint background = new Paint();
        background.setStyle(Paint.Style.FILL);
        background.setColor(Color.LTGRAY);
        background.setStrokeWidth(2);

        colors = new Hashtable<String, Paint>();
        colors.put("alive", alive);
        colors.put("border", border);
        colors.put("dead", dead);
        colors.put("thriving", thriving);
        colors.put("background", background);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i("GOL","Surface Created!");
        golThread = new GOLThread(holder, context, this);
        golThread.setRunning(true);
        golThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i("GOL","Surface Destroyed!");
        golThread.setRunning(false);
        boolean retry = true;
        while(retry)
        {
            try{
                golThread.join();
                retry = false;
            }
            catch(Exception e)
            {
                Log.v("Exception occurred", e.getMessage());
            }
        }

        //Export state to SharedPreferences
        Log.i("GOL","Saving state to SharedPreferences");
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("mode",mode);
        editor.putInt("size", size);
        editor.putString("state", exportState());
        editor.putBoolean("saved", true);
        editor.commit();
        printSettings();

    }

    public void drawToCanvas(Canvas canvas) {
        if(cellHeight == 0 || cellWidth == 0) {
            cellHeight = canvas.getHeight() / (size);
            cellWidth = canvas.getWidth() / (size);
            topOffset = (canvas.getHeight() - (size * (canvas.getHeight() / size))) / 2;
            leftOffset = (canvas.getWidth() - (size * (canvas.getWidth() / size))) / 2;
        }

        if(reset) {
            initialize(mode);
            reset = false;
        }
        //Log.i("GOL","Drawing Ground");
        canvas.drawRGB(0, 0, 0);
        canvas.drawRect(leftOffset, topOffset, leftOffset+size*cellWidth,topOffset+size*cellHeight,colors.get("background"));
        if(ready){

            //Log.i("GOL","Drawing Cells");
            if(mode == 0){
                canvas.drawRect(0,0,cellWidth * size, cellHeight*size, colors.get("border"));
            }
            if(mode > 0) {
                for (int x = 0; x < size; x++) {
                    for (int y = 0; y < size; y++) {

                        //Living
                        if (states[x][y] > 0 && states[x][y] < 20) {
                            drawCell(canvas, x, y, "alive");
                        }
                        //Thriving
                        else if (states[x][y] >= 20) {
                            drawCell(canvas, x, y, "thriving");
                        }
                        //Dead
                        else if (states[x][y] < 0) {
                            drawCell(canvas, x, y, "dead");
                        }

                    }
                }
            }
        }

    }

    public void drawCell(Canvas c, int x, int y, String color)
    {
        c.drawRect(leftOffset + x * cellWidth,topOffset + y * cellHeight,leftOffset + (x + 1) * cellWidth,topOffset + (y + 1) * cellHeight, colors.get("border"));
        c.drawRect(leftOffset + x * cellWidth + 1,topOffset + y * cellHeight + 1,leftOffset + (x + 1) * cellWidth - 1, topOffset + (y + 1) * cellHeight - 1, colors.get(color));
    }

    public void step() {
        //Log.i("GOL","Growing Cells");
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                buffer[x][y] = growCell(x,y);
            }
        }
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                states[x][y] = buffer[x][y];
                buffer[x][y] = 0;
            }
        }
    }

    public int growCell(int x,int y){
        int neighbors = 0;
        int result = 0;
        for(int a = x-1; a <= x+1; a++) {
            if(a >= 0 && a < size) {
                for (int b = y - 1; b <= y + 1; b++) {
                    if(mode == 1) {
                        if (b >= 0 && b < size && states[a][b] > 0 && !(a == x && b == y))
                            neighbors++;
                    }
                    if(mode == 2) {
                        if (b >= 0 && b < size && states[a][b] > 0 && !(a == x && b == y))
                            neighbors++;
                    }
                }
            }
        }

        if(mode == 1) {
            if ((states[x][y] > 0 && neighbors > 1 && neighbors < 4) || (states[x][y] < 0)) {
                result = states[x][y] + 1;
            }
            if (states[x][y] <= 0 && neighbors == 3) {
                result = 1;
            }
            else if(states[x][y] > 0 && (neighbors < 2 || neighbors > 3)) {
                result = -10;
            }
        }
        if(mode == 2) {
            if ((states[x][y] > 0 && neighbors > 1 && neighbors < 4) || (states[x][y] < 0)) {
                result = states[x][y] + 1;
            }
            else if (states[x][y] == 0 && neighbors == 3) {
                result = 1;
            }
            else if(states[x][y] > 0 && (neighbors < 2 || neighbors > 3)) {
                result = -10;
            }
        }
        return result;
    }

    public String exportState(){
        String output = "";
        for(int y = 0; y < size; y++){
            for(int x = 0; x < size; x++){
                output += states[x][y] + " ";
            }
            output += "\n";
        }
        return output;
    }

    public void setState(String newState){
        states = new int[size][size];
        String[] rows = newState.split("\n");
        for(int y = 0; y < size; y++)
        {
            String[] row = rows[y].split(" ");
            for(int x = 0; x < size; x++)
            {
                states[x][y] = Integer.parseInt((row[x]));
            }
        }
    }

    public void setRunning(boolean state){
        running = state;
    }
    public boolean getRunning(){
        return running;
    }
    public void reset(){
        reset = true;
    }

    public void printSettings(){
        if(DEBUG) {
            Log.i("GOL", "==============Settings==================");
            Log.i("GOL", "Mode: " + settings.getInt("mode", -1));
            Log.i("GOL", "Size: " + settings.getInt("size", -1));
            Log.i("GOL", "Saved: " + settings.getBoolean("saved", false));
            Log.i("GOL", "State: " + settings.getString("state", "null"));
            Log.i("GOL", "========================================");
        }
    }
}
