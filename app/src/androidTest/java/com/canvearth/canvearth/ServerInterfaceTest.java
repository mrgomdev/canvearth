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

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Random;


@RunWith(AndroidJUnit4.class)
public class ServerInterfaceTest {

    @BeforeClass
    public static void setup() {
        Configs.TESTING = true;
    }

    @AfterClass
    public static void tearDown() {
        DatabaseUtils.clearDev();
    }

    @Test
    public void leafPixelReadTest() {
        FBPixelManager fBPixelManager = FBPixelManager.getInstance();
        ArrayList<PixelData> samePixelData = PixelUtils.makeBatchPixelData(
                new PixelData(0, 0, Constants.LEAF_PIXEL_ZOOM_LEVEL),
                20,
                20);
        // You have to watch pixel first..
        fBPixelManager.watchPixels(samePixelData);
        // Get a random pixel info
        Random random = new Random();
        PixelData randomPixelData = samePixelData.get(random.nextInt(20 * 20));
        FBPixel pixelInfo = fBPixelManager.readPixel(randomPixelData);
        // You have to unwatch pixel
        fBPixelManager.unwatchPixels(samePixelData);
    }

    @Test
    public void leafPixelWriteTest() {
        FBPixelManager fBPixelManager = FBPixelManager.getInstance();
        ArrayList<PixelData> samePixelData = PixelUtils.makeBatchPixelData(new PixelData(0, 0, Constants.LEAF_PIXEL_ZOOM_LEVEL),
                20,
                20);

        // You have to watch pixel first..
        fBPixelManager.watchPixels(samePixelData);
        // Write black color to the random pixel
        Random random = new Random();
        PixelData randomPixelData = samePixelData.get(random.nextInt(20 * 20));
        fBPixelManager.writePixelSync(randomPixelData, new Color(0L, 0L, 0L));
        // You have to unwatch pixel
        fBPixelManager.unwatchPixels(samePixelData);
    }

    @Test
    public void leafPixelWriteReadTest() {
        FBPixelManager fBPixelManager = FBPixelManager.getInstance();
        ArrayList<PixelData> samePixelData = PixelUtils.makeBatchPixelData(new PixelData(0, 0, Constants.LEAF_PIXEL_ZOOM_LEVEL),
                20,
                20);
        // You have to watch pixel first..
        fBPixelManager.watchPixels(samePixelData);
        // Write black color to the random pixel
        Random random = new Random();
        PixelData randomPixelData = samePixelData.get(random.nextInt(20 * 20));
        Color red = new Color(255L, 0L, 0L);
        fBPixelManager.writePixelSync(randomPixelData, red);
        // Read same pixel
        FBPixel pixelInfo = fBPixelManager.readPixel(randomPixelData);
        Assert.assertTrue(pixelInfo.color.equals(red));
        // You have to unwatch pixel
        fBPixelManager.unwatchPixels(samePixelData);
    }

    @Test
    public void bitmapReadTest() {
        FBPixelManager fBPixelManager = FBPixelManager.getInstance();
        ArrayList<PixelData> samePixelData
                = PixelUtils.makeBatchPixelData(new PixelData(0, 0, Constants.LEAF_PIXEL_ZOOM_LEVEL), 8, 8);
        // You have to watch pixel first..
        fBPixelManager.watchPixels(samePixelData);
        Color green = new Color(0L, 255L, 0L);
        for (PixelData pixelData : samePixelData) {
            fBPixelManager.writePixelSync(pixelData, green);
        }
        PixelData zoomedOutPixelData = new PixelData(0, 0, Constants.LEAF_PIXEL_ZOOM_LEVEL - 3);
        Bitmap bitmap = fBPixelManager.getBitmapSync(zoomedOutPixelData, 5);
        int expectedSide = MathUtils.intPow(2, 5);
        Assert.assertEquals(bitmap.getWidth(), expectedSide);
        Assert.assertEquals(bitmap.getHeight(), expectedSide);
        for (int y = 0; y < bitmap.getHeight(); y++) {
            for (int x = 0; x < bitmap.getWidth(); x++) {
                Assert.assertEquals(bitmap.getPixel(x, y), BitmapUtils.intColor(green));
            }
        }
        // You have to unwatch pixel
        fBPixelManager.unwatchPixels(samePixelData);
    }
}
