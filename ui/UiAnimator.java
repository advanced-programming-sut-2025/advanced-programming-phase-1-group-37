package com.Client.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.math.Interpolation;

public final class UiAnimator {
    private UiAnimator() {}

    public static void popIn(Actor a) {
        a.clearActions();
        a.getColor().a = 0f;
        a.setScale(0.92f);
        a.addAction(Actions.sequence(
            Actions.parallel(
                Actions.fadeIn(0.18f, Interpolation.fade),
                Actions.scaleTo(1f, 1f, 0.22f, Interpolation.pow2Out)
            )
        ));
    }

    public static void popOutAndHide(Actor a) {
        a.clearActions();
        a.addAction(Actions.sequence(
            Actions.parallel(
                Actions.fadeOut(0.15f, Interpolation.fade),
                Actions.scaleTo(0.96f, 0.96f, 0.15f, Interpolation.pow2In)
            ),
            Actions.visible(false)
        ));
    }

    public static void slideToggle(Actor a, boolean show, float fromY) {
        a.clearActions();
        if (show) {
            a.setVisible(true);
            float toY = a.getY();
            a.moveBy(0, fromY);
            a.getColor().a = 0f;
            a.addAction(Actions.parallel(
                Actions.moveTo(a.getX(), toY, 0.22f, Interpolation.sine),
                Actions.fadeIn(0.18f)
            ));
        } else {
            a.addAction(Actions.sequence(
                Actions.parallel(
                    Actions.moveBy(0, fromY, 0.18f, Interpolation.sine),
                    Actions.fadeOut(0.18f)
                ),
                Actions.visible(false)
            ));
        }
    }
}
