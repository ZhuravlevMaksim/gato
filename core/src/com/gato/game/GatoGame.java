package com.gato.game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import de.eskalon.commons.core.ManagedGame;
import de.eskalon.commons.screen.ManagedScreen;
import de.eskalon.commons.screen.transition.ScreenTransition;
import de.eskalon.commons.screen.transition.impl.BlendingTransition;
import de.eskalon.commons.screen.transition.impl.HorizontalSlicingTransition;
import de.eskalon.commons.screen.transition.impl.SlidingDirection;
import de.eskalon.commons.screen.transition.impl.SlidingOutTransition;


public class GatoGame extends ManagedGame<ManagedScreen, ScreenTransition> {

    @Override
    public void create() {
        super.create();

        SpriteBatch batch = new SpriteBatch();

        screenManager.addScreen(StartScreen.class.getName(), new StartScreen());
        screenManager.addScreen(GameScreen.class.getName(), new GameScreen());

        screenManager.addScreenTransition(BlendingTransition.class.getName(), new BlendingTransition(batch, 1F, Interpolation.pow2In));
        screenManager.addScreenTransition(SlidingOutTransition.class.getName(), new SlidingOutTransition(batch, SlidingDirection.DOWN, 0.35F));
        screenManager.addScreenTransition(HorizontalSlicingTransition.class.getName(), new HorizontalSlicingTransition(batch, 5, 1F, Interpolation.exp5In));


        this.screenManager.pushScreen(GameScreen.class.getName(), null);

    }

}
