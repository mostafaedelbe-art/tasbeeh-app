
package com.tasbeeh.app;

import android.os.Bundle;
import android.os.Vibrator;
import android.os.VibrationEffect;
import android.os.Build;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class MainActivity extends AppCompatActivity {
    WebView webView;
    Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Fullscreen immersive - يغطي الساعة والاشعارات
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        getWindow().setNavigationBarColor(android.graphics.Color.TRANSPARENT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        webView = new WebView(this);
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowFileAccessFromFileURLs(true);
        s.setAllowUniversalAccessFromFileURLs(true);
        s.setMediaPlaybackRequiresUserGesture(false);

        webView.setWebViewClient(new WebViewClient());
        webView.addJavascriptInterface(new AndroidBridge(), "Android");
        webView.addJavascriptInterface(new AndroidBridge(), "App");
        webView.addJavascriptInterface(new AndroidBridge(), "JSInterface");
        
        webView.loadUrl("file:///android_asset/index.html");
        setContentView(webView);
        
        enterImmersive();
    }

    public void enterImmersive(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
            controller.hide(WindowInsetsCompat.Type.statusBars() | WindowInsetsCompat.Type.navigationBars());
            controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
            );
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) enterImmersive();
    }

    @Override
    protected void onResume() {
        super.onResume();
        enterImmersive();
    }

    public class AndroidBridge {
        @JavascriptInterface
        public void vibrate(int duration, int repeat){
            if(vibrator==null) return;
            try{
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    if(repeat>1){
                        long[] pattern = new long[repeat*2-1];
                        for(int i=0;i<repeat;i++){
                            pattern[i*2]=duration;
                            if(i<repeat-1) pattern[i*2+1]=60;
                        }
                        vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));
                    } else {
                        vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE));
                    }
                } else {
                    if(repeat>1){
                        long[] pattern = new long[repeat*2-1];
                        for(int i=0;i<repeat;i++){
                            pattern[i*2]=duration;
                            if(i<repeat-1) pattern[i*2+1]=60;
                        }
                        vibrator.vibrate(pattern, -1);
                    } else {
                        vibrator.vibrate(duration);
                    }
                }
            }catch(Exception e){}
        }

        @JavascriptInterface
        public void Vibrate(int duration){ vibrate(duration,1); }

        @JavascriptInterface
        public void performHaptic(){
            try{
                getWindow().getDecorView().performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
            }catch(Exception e){}
        }

        @JavascriptInterface
        public void enterImmersive(){
            runOnUiThread(()-> MainActivity.this.enterImmersive());
        }
    }
}
