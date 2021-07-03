package com.gato.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.gato.game.GatoGame;
import com.rafaskoberg.gdx.typinglabel.TypingLabel;
import de.eskalon.commons.screen.ManagedScreen;
import de.eskalon.commons.screen.transition.impl.BlendingTransition;

public class StartScreen extends ManagedScreen {

    private final Stage background = new Stage(new FillViewport(640, 480));
    private final Stage text = new Stage(new ExtendViewport(640, 480));
    private final Skin skin = new Skin(Gdx.files.internal("uiskin.json"));

    Texture bg;
    private final SpriteBatch batch = new SpriteBatch();

    @Override
    protected void create() {
        addInputProcessor(text);

        bg = new Texture(Gdx.files.internal("bg.png"));

        Table gameName = new Table();
        gameName.setFillParent(true);

        TypingLabel typingLabel = new TypingLabel("{EASE}{SPEED=SLOWER}Hello{WAIT=1}, traveler{SPEED}.\n" +
                "Your journey begins here {SPEED=0.4}{WAIT=1} with ... \n{SPEED=0.3}{SHAKE}Gato{ENDSHAKE}", skin);
        typingLabel.setFontScale(2);
        gameName.add(typingLabel).padTop(40).padLeft(90).left().top().expand();
        text.addActor(gameName);
        gameName.addAction(Actions.sequence(Actions.moveBy(-2000, 0, 0),
                Actions.delay(0.2f), Actions.moveBy(2000, 0, 0.75f, Interpolation.exp5Out)));

        Table buttons = new Table();
        buttons.setFillParent(true);

        TextButton newGame = new TextButton("New game", skin);
        newGame.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GatoGame.pushSimpleZoom(GameScreen.class);
            }
        });
        newGame.addAction(actionButton(1f));

        TextButton exit = new TextButton("Exit", skin);
        exit.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.exit();
            }
        });
        exit.addAction(actionButton(1.5f));

        buttons.add(newGame).right().row();
        buttons.add(exit).right().row();
        buttons.padTop(200).padRight(100).right().top();
        text.addActor(buttons);
    }

    public Action actionButton(float delay) {
        return Actions.sequence(
                Actions.moveBy(300, 0, 0.01f),
                Actions.delay(delay), Actions.moveBy(-300, 0, 0.5f, Interpolation.exp5Out));
    }

    @Override
    public void hide() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.448f, 0.449f, 0.247f, 1);
        Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT);

        batch.disableBlending();
        batch.begin();
        batch.draw(bg, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.end();

        text.getViewport().apply();
        text.act(delta);
        text.draw();
    }

    @Override
    public void resize(int width, int height) {
        background.getViewport().update(width, height, true);
        text.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {

    }

}
