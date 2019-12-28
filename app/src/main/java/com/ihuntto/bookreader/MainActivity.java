/*
 *    Copyright 2019 Huntto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.ihuntto.bookreader;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.ihuntto.bookreader.flip.FlipOver;
import com.ihuntto.bookreader.flip.FlipOverPage;
import com.ihuntto.bookreader.ui.PageEditView;
import com.ihuntto.bookreader.ui.SimpleFlipOver;
import com.ihuntto.bookreader.ui.SimulateFlipOver;
import com.ihuntto.bookreader.ui.ViewPagerFlipOver;
import com.ihuntto.bookreader.ui.gl.SimpleGLFlipOver;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private FlipOver mFlipOver;
    private PageEditView mPageEditView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        useFlipOver(SimpleGLFlipOver.class, R.id.simple_gl_flip_over);
        mPageEditView = findViewById(R.id.page_edit_view);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mFlipOver.setOnPageFlipListener(null);
        mFlipOver.setPageProvider(null);
    }

    private FlipOver.OnPageFlipListener mOnPageFlipListener = new FlipOver.OnPageFlipListener() {

        @Override
        public void onFlipStart() {
            mPageEditView.dismiss();
        }

        @Override
        public void onPageClick() {
            mPageEditView.switchVisibility();
        }
    };

    private FlipOver.PageProvider mPageProvider = new FlipOver.PageProvider() {
        private int[] mBitmapIds = new int[]{
                R.mipmap.one,
                R.mipmap.two,
                R.mipmap.three
        };

        private Bitmap[] mBitmaps = new Bitmap[mBitmapIds.length];

        @Override
        public int getPageCount() {
            return mBitmaps.length * 2;
        }

        @Override
        public FlipOverPage updatePage(int index, int width, int height) {
            Bitmap leftPageBitmap = loadBitmap(index - 1, width, height);
            Bitmap currentPageBitmap = loadBitmap(index, width, height);
            Bitmap rightPageBitmap = loadBitmap(index + 1, width, height);
            return new FlipOverPage(leftPageBitmap, currentPageBitmap, rightPageBitmap);
        }

        private Bitmap loadBitmap(final int index, final int width, final int height) {
            if (index < 0 || index > getPageCount() - 1 || width <= 0 || height <= 0) {
                Log.e(TAG, "loadBitmap wrong params: index=" + index
                        + " width=" + width
                        + " height=" + height);
                return null;
            }
            int bitmapIndex = index % mBitmaps.length;

            Bitmap cachedBitmap = mBitmaps[bitmapIndex];
            if (cachedBitmap != null
                    && !cachedBitmap.isRecycled()
                    && cachedBitmap.getWidth() == width
                    && cachedBitmap.getHeight() == height) {
                Log.i(TAG, "use cached bitmap");
                return cachedBitmap;
            }

            cachedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            cachedBitmap.eraseColor(Color.WHITE);
            Canvas canvas = new Canvas(cachedBitmap);
            Drawable drawable = getResources().getDrawable(mBitmapIds[bitmapIndex]);

            int margin = 7;
            int border = 3;
            Rect dirtyRect = new Rect(margin, margin, width - margin, height - margin);

            int imageWidth = dirtyRect.width() - (border * 2);
            int imageHeight = imageWidth * drawable.getIntrinsicHeight()
                    / drawable.getIntrinsicWidth();
            if (imageHeight > dirtyRect.height() - (border * 2)) {
                imageHeight = dirtyRect.height() - (border * 2);
                imageWidth = imageHeight * drawable.getIntrinsicWidth()
                        / drawable.getIntrinsicHeight();
            }

            dirtyRect.left += ((dirtyRect.width() - imageWidth) / 2) - border;
            dirtyRect.right = dirtyRect.left + imageWidth + border + border;
            dirtyRect.top += ((dirtyRect.height() - imageHeight) / 2) - border;
            dirtyRect.bottom = dirtyRect.top + imageHeight + border + border;

            Paint p = new Paint();
            p.setColor(0xFFC0C0C0);
            canvas.drawRect(dirtyRect, p);
            dirtyRect.left += border;
            dirtyRect.right -= border;
            dirtyRect.top += border;
            dirtyRect.bottom -= border;

            drawable.setBounds(dirtyRect);
            drawable.draw(canvas);

            mBitmaps[bitmapIndex] = cachedBitmap;

            return cachedBitmap;
        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.use_view_pager) {
            useFlipOver(ViewPagerFlipOver.class, R.id.view_pager_flip_over);
            return true;
        } else if (id == R.id.use_third_party) {
            useFlipOver(SimulateFlipOver.class, R.id.simulate_flip_over);
            return true;
        } else if (id == R.id.use_simple) {
            useFlipOver(SimpleFlipOver.class, R.id.simple_flip_over);
            return true;
        } else if (id == R.id.use_simple_gl) {
            useFlipOver(SimpleGLFlipOver.class, R.id.simple_gl_flip_over);
        }

        return super.onOptionsItemSelected(item);
    }

    private void useFlipOver(Class flipOverClazz, @IdRes int viewId) {
        if (hideFlipOver(flipOverClazz)) return;
        mFlipOver = findViewById(viewId);
        showAndInitFlipOver();
    }

    private void showAndInitFlipOver() {
        ((View) mFlipOver).setVisibility(View.VISIBLE);
        mFlipOver.setOnPageFlipListener(mOnPageFlipListener);
        mFlipOver.setPageProvider(mPageProvider);
    }

    private boolean hideFlipOver(Class clazz) {
        if (mFlipOver != null) {
            if (mFlipOver.getClass() == clazz) {
                return true;
            }
            ((View) mFlipOver).setVisibility(View.GONE);
        }
        return false;
    }
}
