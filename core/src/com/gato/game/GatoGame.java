package com.gato.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.gato.game.screen.GameScreen;
import com.gato.game.screen.StartScreen;
import de.eskalon.commons.core.ManagedGame;
import de.eskalon.commons.screen.ManagedScreen;
import de.eskalon.commons.screen.transition.ScreenTransition;
import de.eskalon.commons.screen.transition.impl.*;


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

        screenManager.addScreenTransition("simple_zoom", createGlTransition());

        pushScreen(StartScreen.class, BlendingTransition.class);
    }

    public static <T, R> void pushScreen(Class<T> screen, Class<R> transition) {
        ((GatoGame) Gdx.app.getApplicationListener()).getScreenManager().pushScreen(screen.getName(), transition != null ? transition.getName() : null);
    }

    public static <T, R> void pushSimpleZoom(Class<T> screen) {
        ((GatoGame) Gdx.app.getApplicationListener()).getScreenManager().pushScreen(screen.getName(), "simple_zoom");
    }

    private GLTransitionsShaderTransition createGlTransition(){
        GLTransitionsShaderTransition simpleZoomShader = new GLTransitionsShaderTransition(0.9F, Interpolation.smooth);
        simpleZoomShader.compileGLTransition("uniform float zoom_quickness; // = 0.8\n" +
                "float nQuick = clamp(zoom_quickness,0.2,1.0);\n" +
                "\n" +
                "vec2 zoom(vec2 uv, float amount) {\n" +
                "  return 0.5 + ((uv - 0.5) * (1.0-amount));\t\n" +
                "}\n" +
                "\n" +
                "vec4 transition (vec2 uv) {\n" +
                "  return mix(\n" +
                "    getFromColor(zoom(uv, smoothstep(0.0, nQuick, progress))),\n" +
                "    getToColor(uv),\n" +
                "   smoothstep(nQuick-0.2, 1.0, progress)\n" +
                "  );\n" +
                "}");
        return simpleZoomShader;
    }

}
