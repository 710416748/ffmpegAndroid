package roman10.tutorial.android_ffmpeg_tutorial02;

import java.io.File;
//import java.util.jar.Manifest;
import android.Manifest;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Point;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

public class MainActivity extends Activity implements SurfaceHolder.Callback {
	private static final String TAG = "android-ffmpeg-tutorial02";
	private static final String FRAME_DUMP_FOLDER_PATH =
			//Environment.getExternalStorageDirectory()
			"/mnt/sdcard"
			+ File.separator + "android-ffmpeg-tutorial02";
    private static final String SOURCE_BASE_PATH = "/sdcard/Movies";
	// video used to fill the width of the screen 
	private static final String videoFileName = "1.mp4";  	//640x360
	// video used to fill the height of the screen
//	private static final String videoFileName = "12.mp4";   //200x640
	
	private SurfaceView mSurfaceView;

	private  static final int PERSISSION_CODE_WRITE_EXTERNAL_STORAGE = 1;
	private  static final int PERSISSION_CODE_MOUNT_UNMOUNT_FILESYSTEMS = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main);
		//create directory for the tutorial

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
				//if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                //                                 PackageManager.PERMISSION_DENIED)
				//    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                //                           PERSISSION_CODE_WRITE_EXTERNAL_STORAGE);

			if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_DENIED) {
				Log.i(TAG,"do not have permission");
				requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERSISSION_CODE_WRITE_EXTERNAL_STORAGE);
			}
		}

		//copy input video file from assets folder to directory
		
		mSurfaceView = (SurfaceView)findViewById(R.id.surfaceview);
		mSurfaceView.getHolder().addCallback(this);

		Button btnCommand = (Button) this.findViewById(R.id.buttonCommand);
		btnCommand.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
                //Log.i(TAG,"java get click");
				int ret = naSetCommand("ffmpeg -i " + SOURCE_BASE_PATH + File.separator + "1.jpg -i " +
                        SOURCE_BASE_PATH + File.separator + "2.amr -vcodec mpeg4 -y "+
                        SOURCE_BASE_PATH + File.separator + "demo.mp4 ");
				Toast.makeText(getApplicationContext(),"compose done",Toast.LENGTH_SHORT).show();
				Log.i(TAG,"ret is " + ret);
			}
		});

		Button btnStart = (Button) this.findViewById(R.id.buttonStart);
		btnStart.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				naInit(FRAME_DUMP_FOLDER_PATH + File.separator + videoFileName);

				int[] res = naGetVideoRes();
				Log.d(TAG, "res width " + res[0] + ": height " + res[1]);
				int[] screenRes = getScreenRes();
				int width, height;
				float widthScaledRatio = screenRes[0]*1.0f/res[0];
				float heightScaledRatio = screenRes[1]*1.0f/res[1];

				if (widthScaledRatio > heightScaledRatio) {
					//use heightScaledRatio
					width = (int) (res[0]*heightScaledRatio);
					height = screenRes[1];
				} else {
					//use widthScaledRatio
					width = screenRes[0];
					height = (int) (res[1]*widthScaledRatio);
				}
				Log.d(TAG, "width " + width + ",height:" + height);
				updateSurfaceView(width, height);
				naSetup(width, height);
				naPlay();
			}
		});
	}
	
	private void updateSurfaceView(int pWidth, int pHeight) {
		//update surfaceview dimension, this will cause the native window to change
		RelativeLayout.LayoutParams params = (LayoutParams) mSurfaceView.getLayoutParams();
		params.width = pWidth;
		params.height = pHeight;
		mSurfaceView.setLayoutParams(params);
	}
	
	@SuppressLint("NewApi")
	private int[] getScreenRes() {
		int[] res = new int[2];
		Display display = getWindowManager().getDefaultDisplay();
		if (Build.VERSION.SDK_INT >= 13) {
			Point size = new Point();
			display.getSize(size);
			res[0] = size.x;
			res[1] = size.y;
		} else {
			res[0] = display.getWidth();  // deprecated
			res[1] = display.getHeight();  // deprecated
		}
		return res;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		naStop();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Log.d(TAG, "surfacechanged: " + width + ":" + height);
		naSetSurface(holder.getSurface());
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d(TAG, "surfaceDestroyed");
		naSetSurface(null);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		int result = grantResults[0];

		Log.i(TAG,"before switch");
		switch (requestCode){
			case PERSISSION_CODE_WRITE_EXTERNAL_STORAGE:

		    	//	break;
				// case PERSISSION_CODE_MOUNT_UNMOUNT_FILESYSTEMS:

				Log.i(TAG,"match PERSISSION_CODE_WRITE_EXTERNAL_STORAGE");
				File dumpFolder = new File(FRAME_DUMP_FOLDER_PATH);
				if(result == PackageManager.PERMISSION_GRANTED) {
					Log.i(TAG,"get permission");
					if (!dumpFolder.exists()) {
						dumpFolder.mkdirs();
					}
					if (!dumpFolder.exists()) {
						Log.i(TAG, "mkdir failed");
					}

					Utils.copyAssets(this, videoFileName, FRAME_DUMP_FOLDER_PATH);
				}
				break;
		}
	}

	private static native int naInit(String pFileName);
	private static native int[] naGetVideoRes();
	private static native void naSetSurface(Surface pSurface);
	private static native int naSetup(int pWidth, int pHeight);
	private static native void naPlay();
	private static native void naStop();

	private static native int naSetCommand(String command);

	
    static {
    	//System.loadLibrary("avutil-52");
        //System.loadLibrary("avcodec-55");
        //System.loadLibrary("avformat-55");
        //System.loadLibrary("swscale-2");
    	//System.loadLibrary("tutorial02");
		//System.loadLibrary("avutil-55");

		//System.loadLibrary("avcodec-57");
		//System.loadLibrary("avfilter-6");
		//System.loadLibrary("avformat-57");
		//System.loadLibrary("swresample-2");
		//System.loadLibrary("swscale-4");
		System.loadLibrary("tutorial02");

    }
}
