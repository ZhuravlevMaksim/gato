package com.gato.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.gato.game.screen.GameScreen;
import com.gato.game.screen.StartScreen;
import de.eskalon.commons.core.ManagedGame;
import de.eskalon.commons.screen.ManagedScreen;
import de.eskalon.commons.screen.transition.ScreenTransition;
import de.eskalon.commons.screen.transition.impl.BlendingTransition;
import de.eskalon.commons.screen.transition.impl.HorizontalSlicingTransition;
import de.eskalon.commons.screen.transition.impl.SlidingDirection;
import de.eskalon.commons.screen.transition.impl.SlidingOutTransition;


public class GatoGame extends ManagedGame<ManagedScreen, ScreenTransition> {

    private SpriteBatch batch;

    @Override
    public void create() {
        super.create();

        batch = new SpriteBatch();

        screenManager.addScreen(StartScreen.class.getName(), new StartScreen());
        screenManager.addScreen(GameScreen.class.getName(), new GameScreen());

        screenManager.addScreenTransition(BlendingTransition.class.getName(), new BlendingTransition(batch, 2F, Interpolation.pow2In));
        screenManager.addScreenTransition(SlidingOutTransition.class.getName(), new SlidingOutTransition(batch, SlidingDirection.DOWN, 0.35F));
        screenManager.addScreenTransition(HorizontalSlicingTransition.class.getName(), new HorizontalSlicingTransition(batch, 5, 1F, Interpolation.exp5In));

        pushScreen(StartScreen.class, BlendingTransition.class);
    }

    public static <T, R> void pushScreen(Class<T> screen, Class<R> transition) {
        ((GatoGame) Gdx.app.getApplicationListener()).getScreenManager().pushScreen(screen.getName(), transition != null ? transition.getName() : null);
    }

}
