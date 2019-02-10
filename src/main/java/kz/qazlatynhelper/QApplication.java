package kz.qazlatynhelper;

import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;

import com.orhanobut.hawk.Hawk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class QApplication extends Application {
    private static Context qContext;
    @Override
    public void onCreate() {
        super.onCreate();
        qContext = this;
    }

    public static String readAssetsFile(String fileName){
        StringBuilder stringBuffer = new StringBuilder();
        AssetManager assetManager = qContext.getAssets();
        try {
            InputStream is = assetManager.open(fileName);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String str = null;
            while ((str = br.readLine()) != null) {
                stringBuffer.append(str);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuffer.toString();
    }
}
