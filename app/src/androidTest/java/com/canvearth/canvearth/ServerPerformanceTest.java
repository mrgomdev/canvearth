package com.canvearth.canvearth;

import android.graphics.Bitmap;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.canvearth.canvearth.pixel.Color;
import com.canvearth.canvearth.pixel.PixelData;
import com.canvearth.canvearth.server.FBPixel;
import com.canvearth.canvearth.server.FBPixelManager;
import com.canvearth.canvearth.utils.BitmapUtils;
import com.canvearth.canvearth.utils.Configs;
import com.canvearth.canvearth.utils.Constants;
import com.canvearth.canvearth.utils.DatabaseUtils;
import com.canvearth.canvearth.utils.MathUtils;
import com.canvearth.canvearth.utils.PixelUtils;
import com.canvearth.canvearth.utils.TimeUtils;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;


@RunWith(AndroidJUnit4.class)
public class ServerPerformanceTest {

    @BeforeClass
    public static void setup() {
        Configs.TESTING = true;
    }

    @AfterClass
    public static void tearDown() {
        DatabaseUtils.clearDev();
    }

    @Test
    public void pixelWritePerformanceTest() {
        final String TAG = "ServerPerformanceTest/pixelWritePerformanceTest";
        FBPixelManager fBPixelManager = FBPixelManager.getInstance();
        ArrayList<PixelData> samePixelData = PixelUtils.makeBatchPixelData(
                new PixelData(0, 0, Constants.LEAF_PIXEL_ZOOM_LEVEL), 10, 10);

        // You have to watch pixel first..
        fBPixelManager.watchPixels(samePixelData);
        // Write black color to the random pixel
        long startTime = System.nanoTime();
        int totalUpdated = 0;
        for (PixelData pixelData : samePixelData) {
            PixelData lastUpdatedPixelData
                    = fBPixelManager.writePixelSync(pixelData, new Color(0L, 0L, 0L));
            int numUpdated = pixelData.zoom - lastUpdatedPixelData.zoom + 1;
            totalUpdated += numUpdated;
        }
        long endTime = System.nanoTime();
        long elapsedTime =TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
        float averageUpdated = ((float) totalUpdated) / samePixelData.size();
        float averageElapsedTime = elapsedTime / samePixelData.size();
        Log.i(TAG, "Average Elapsed Time: " + averageElapsedTime
                + "ms, Average Updated Number: " + averageUpdated);
        // You have to unwatch pixel
        fBPixelManager.unwatchPixels(samePixelData);
    }

    @Test
    public void bitmapReadPerformanceTest() {
        final String TAG = "ServerPerformanceTest/bitmapReadPerformanceTest";
        FBPixelManager fBPixelManager = FBPixelManager.getInstance();
        ArrayList<PixelData> samePixelData = PixelUtils.makeBatchPixelData(
                new PixelData(0, 0, Constants.LEAF_PIXEL_ZOOM_LEVEL), 16, 16);

        // You have to watch pixel first..
        fBPixelManager.watchPixels(samePixelData);
        Color green = new Color(0L, 255L, 0L);
        for (PixelData pixelData : samePixelData) {
            fBPixelManager.writePixelSync(pixelData, green);
        }
        PixelData zoomedOutPixelData8x8 = new PixelData(0, 0, Constants.LEAF_PIXEL_ZOOM_LEVEL - 3);
        long elapsedTime8x8 = TimeUtils.measureTimeMillis((Object object) -> {
            fBPixelManager.getBitmapSync(zoomedOutPixelData8x8, 3);
        });
        Log.i(TAG, "Elapsed Time For getting 8x8 bitmap: " + elapsedTime8x8 + "ms");
        PixelData zoomedOutPixelData16x16 = new PixelData(0, 0, Constants.LEAF_PIXEL_ZOOM_LEVEL - 4);
        long elapsedTime16x16 = TimeUtils.measureTimeMillis((Object object) -> {
            fBPixelManager.getBitmapSync(zoomedOutPixelData16x16, 4);
        });
        Log.i(TAG, "Elapsed Time For getting 16x16 bitmap: " + elapsedTime16x16 + "ms");

        // You have to unwatch pixel
        fBPixelManager.unwatchPixels(samePixelData);
    }
}
