package com.dot.floatingpet;

import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.*;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.View;
import com.dot.floatingpet.core.IdleMotion;
import com.dot.floatingpet.core.SpriteAtlas;
import com.dot.floatingpet.core.SystemAnimationPolicy;

/** Draws atlas crops without allocating a bitmap for each frame. */
// This programmatic-only view requires a validated bitmap; it is never inflated from XML.
@android.annotation.SuppressLint("ViewConstructor")
final class PetView extends View {
    private final Bitmap atlas;
    private final SpriteAtlas.Layout layout;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Rect source = new Rect();
    private final RectF target = new RectF();
    private final IdleMotion idleMotion = new IdleMotion();
    private final IdleMotion.Transform idleTransform = new IdleMotion.Transform();
    private boolean idleFramePending;
    private boolean idleAttached;
    private boolean idleObserversReady;
    private boolean observingAnimatorSettings;
    private boolean observingPowerSave;
    private float observedDurationScale;
    private boolean observedPowerSave = true;
    private ValueAnimator.DurationScaleChangeListener durationScaleListener;
    private final BroadcastReceiver powerSaveReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            refreshSystemAnimationState();
        }
    };
    private final Runnable idleFrame = new Runnable() {
        @Override public void run() {
            idleFramePending = false;
            refreshIdleAnimation();
        }
    };
    private final ContentObserver animatorSettingsObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
        @Override public void onChange(boolean selfChange) {
            // Read-only observation also lets a stopped loop resume after animations are enabled.
            refreshSystemAnimationState();
        }
    };

    PetView(Context context, Bitmap atlas) {
        super(context);
        this.atlas = atlas;
        this.layout = SpriteAtlas.forDimensions(atlas.getWidth(), atlas.getHeight());
        pose(0, 0);
        setContentDescription(getContext().getString(R.string.pet_accessibility_description));
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
    }

    @Override protected void onConfigurationChanged(android.content.res.Configuration configuration) {
        super.onConfigurationChanged(configuration);
        setContentDescription(getContext().getString(R.string.pet_accessibility_description));
    }

    void pose(int row, int frame) {
        int[] r = layout.frameRect(row, frame);
        source.set(r[0], r[1], r[2], r[3]);
        invalidate();
    }

    /** App preference; disabling removes callbacks and restores identity immediately. */
    void setIdleMotionEnabled(boolean enabled) {
        idleMotion.setEnabled(enabled, SystemClock.uptimeMillis());
        refreshIdleAnimation();
    }

    /** Set false for the entire held-touch gesture; interaction entry/exit fades over 300 ms. */
    void setIdleAllowed(boolean allowed) {
        idleMotion.setAllowed(allowed, SystemClock.uptimeMillis());
        refreshIdleAnimation();
    }

    /** Shared by the idle renderer and bounded interaction-pose controller. */
    boolean systemAnimationsAllowed() {
        return idleObserversReady && SystemAnimationPolicy.allows(Build.VERSION.SDK_INT >= 33,
                observedDurationScale, observedPowerSave, ValueAnimator.areAnimatorsEnabled());
    }

    private void refreshSystemAnimationState() {
        try {
            observedDurationScale = Settings.Global.getFloat(getContext().getContentResolver(),
                    Settings.Global.ANIMATOR_DURATION_SCALE, 1f);
            PowerManager power = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
            observedPowerSave = power == null || power.isPowerSaveMode();
        } catch (RuntimeException unavailable) {
            observedDurationScale = 0f;
            observedPowerSave = true;
        }
        refreshIdleAnimation();
    }

    private void refreshIdleAnimation() {
        long now = SystemClock.uptimeMillis();
        idleMotion.setAvailable(idleAttached && idleObserversReady && isAttachedToWindow() && isShown()
                && getWindowVisibility() == VISIBLE && systemAnimationsAllowed(), now);
        // This final invalidation is needed even when a fade has just finished.
        invalidate();
        if (idleMotion.needsFrame(now)) {
            if (!idleFramePending) idleFramePending = postDelayed(idleFrame, IdleMotion.FRAME_INTERVAL_MS);
        } else {
            removeCallbacks(idleFrame);
            idleFramePending = false;
        }
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        idleAttached = true;
        idleObserversReady = startObservingIdleSettings();
        refreshSystemAnimationState();
    }

    private boolean startObservingIdleSettings() {
        try {
            if (!observingAnimatorSettings) {
                getContext().getContentResolver().registerContentObserver(
                        Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE),
                        false, animatorSettingsObserver);
                observingAnimatorSettings = true;
            }
            if (!observingPowerSave) {
                IntentFilter filter = new IntentFilter(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED);
                if (Build.VERSION.SDK_INT >= 33) {
                    getContext().registerReceiver(powerSaveReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
                } else {
                    getContext().registerReceiver(powerSaveReceiver, filter);
                }
                observingPowerSave = true;
            }
            if (Build.VERSION.SDK_INT >= 33 && durationScaleListener == null) {
                durationScaleListener = scale -> refreshIdleAnimation();
                if (!ValueAnimator.registerDurationScaleChangeListener(durationScaleListener)) {
                    stopObservingIdleSettings();
                    return false;
                }
            }
            return true;
        } catch (RuntimeException unavailable) {
            // An OEM/settings-provider failure must leave a still pet, never crash attach.
            stopObservingIdleSettings();
            return false;
        }
    }

    private void stopObservingIdleSettings() {
        if (observingAnimatorSettings) {
            try {
                getContext().getContentResolver().unregisterContentObserver(animatorSettingsObserver);
            } catch (RuntimeException ignored) { /* Best-effort teardown; animation is already gated. */ }
            observingAnimatorSettings = false;
        }
        if (observingPowerSave) {
            try {
                getContext().unregisterReceiver(powerSaveReceiver);
            } catch (RuntimeException ignored) { /* Continue cleaning up the remaining listeners. */ }
            observingPowerSave = false;
        }
        if (Build.VERSION.SDK_INT >= 33 && durationScaleListener != null) {
            try {
                ValueAnimator.unregisterDurationScaleChangeListener(durationScaleListener);
            } catch (RuntimeException ignored) { /* Detached callbacks cannot restart the loop. */ }
            durationScaleListener = null;
        }
    }

    @Override protected void onDetachedFromWindow() {
        // isAttachedToWindow() can remain true until super returns; close our gate first.
        idleAttached = false;
        idleObserversReady = false;
        idleMotion.setAvailable(false, SystemClock.uptimeMillis());
        removeCallbacks(idleFrame);
        idleFramePending = false;
        stopObservingIdleSettings();
        removeCallbacks(idleFrame);
        super.onDetachedFromWindow();
    }

    @Override protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        // View's superclass can call this before this subclass's fields are initialized.
        if (idleMotion != null) refreshIdleAnimation();
    }

    @Override protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (idleMotion != null) refreshIdleAnimation();
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float scale = Math.min((float)getWidth()/SpriteAtlas.CELL_WIDTH, (float)getHeight()/SpriteAtlas.CELL_HEIGHT);
        float w = SpriteAtlas.CELL_WIDTH * scale, h = SpriteAtlas.CELL_HEIGHT * scale;
        target.set((getWidth()-w)/2, (getHeight()-h)/2, (getWidth()+w)/2, (getHeight()+h)/2);
        idleMotion.sample(SystemClock.uptimeMillis(), idleTransform);
        int checkpoint = canvas.save();
        if (w > 0 && h > 0 && idleTransform.amplitude > 0f) {
            // Bottom center is fixed. A 0.5% horizontal margin contains the <=0.4% sway,
            // and breathing only shrinks upward, so no crop/hitbox/window changes occur.
            canvas.translate(target.centerX(), target.bottom);
            canvas.skew(-idleTransform.swayWidthFraction * w / h, 0f);
            canvas.scale(idleTransform.scaleX, idleTransform.scaleY);
            canvas.translate(-target.centerX(), -target.bottom);
        }
        canvas.drawBitmap(atlas, source, target, paint);
        canvas.restoreToCount(checkpoint);
    }

    @Override public boolean performClick() { super.performClick(); return true; }
}
