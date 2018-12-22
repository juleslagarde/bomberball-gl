package com.glhf.bomberball.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.glhf.bomberball.Bomberball;
import com.glhf.bomberball.Graphics;
import com.glhf.bomberball.Graphics.GUI;
import com.glhf.bomberball.InputHandler;
import com.glhf.bomberball.InputHandler.Action;
import com.glhf.bomberball.config.AppConfig;
import com.glhf.bomberball.config.InputsConfig;
import com.glhf.bomberball.config.InputsConfig.InputProfile;
import com.glhf.bomberball.utils.Resolutions;
import com.glhf.bomberball.utils.WaitNextInput;

import java.lang.reflect.Field;
import java.util.*;

import static com.badlogic.gdx.scenes.scene2d.ui.Table.Debug.table;

public class SettingsMenuScreen extends AbstractScreen {

    private final EventListener button_listener;
    private final Table[] contents;
    private final TextButton[] labels;
    private final ButtonGroup<InputButton> inputsButtonGroup;
    private final ClickListener labels_listener;
    private InputProcessor tmp;

    //Constructor
    public SettingsMenuScreen() {
        super();
        AppConfig appConfig = AppConfig.get();
        InputsConfig inputsConfig = InputsConfig.get();
        Table table = new Table();
        table.setFillParent(true);
        addUI(table);

//        input_handler.setSettingsMenuScreen(this);

        final int NB_TABS = 2;
        labels = new TextButton[NB_TABS];
        Stack stack = new Stack();
        contents = new Table[NB_TABS];

        labels_listener = new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                for(int i=0; i<NB_TABS; i++)
                    contents[i].setVisible(labels[i].isChecked());
                stack.swapActor(0,1);
            }
        };

        SettingsMenuScreen self = this;
        button_listener = new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                final TextButton textButton = (TextButton) event.getListenerActor();
                textButton.setText("?");
                textButton.setChecked(true);
                tmp = Gdx.input.getInputProcessor();
                Gdx.input.setInputProcessor(new WaitNextInput(self));
                return true;
            }
        };

        labels[0] = new TextButton("general", Graphics.GUI.getSkin());
        labels[0].addListener(labels_listener);

        labels[1] = new TextButton("inputs", Graphics.GUI.getSkin());
        labels[1].addListener(labels_listener);

        contents[0] = new Table();
        contents[0].add(new ParameterScreenSize()).growX().row();
        CheckBox fullscreen = new CheckBox("fullscreen", GUI.getSkin());
        fullscreen.setChecked(appConfig.fullscreen);
        fullscreen.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                DisplayMode displayMode = Gdx.graphics.getDisplayMode();
                AppConfig config = AppConfig.get();
                if(((CheckBox)actor).isChecked())
                    Gdx.graphics.setFullscreenMode(displayMode);
                else
                    Bomberball.resizeWindow(config.resolution);
                config.fullscreen = ((CheckBox)actor).isChecked();
                config.exportConfig();
            }
        });
        contents[0].add(fullscreen).growX().row();

        //ajout de chaque paramètre pour inputs
        HashMap<Action,String[]> map = inputsConfig.getReversedInputMap();
        contents[1] = new Table();
        inputsButtonGroup = new ButtonGroup<>();
        inputsButtonGroup.setMaxCheckCount(1);
        inputsButtonGroup.setMinCheckCount(1);
        for(Action a : map.keySet()){
            Label label = new Label(a.toString(), Graphics.GUI.getSkin(), "very_small");
            contents[1].add(label).growX();
            for(int i=0; i<map.get(a).length; i++) {
                String id = map.get(a)[i];
                InputButton textButton = new InputButton(id, a, i);
                textButton.addListener(button_listener);
                contents[1].add(textButton).growX();
                inputsButtonGroup.add(textButton);
            }
            contents[1].row();
        }

        ButtonGroup<TextButton> labelsButtonGroup = new ButtonGroup<>();
        for(int i=0; i<NB_TABS; i++) {
            table.add(labels[i]).growX();
            labelsButtonGroup.add(labels[i]);
        }
        table.row();
        for(int i=0; i<NB_TABS; i++)
            stack.add(new ScrollPane(contents[i]));
        table.add(stack).colspan(NB_TABS).grow().row();

        TextButton cancelButton = new TextButton("Retour", Graphics.GUI.getSkin());
        cancelButton.addListener(new ScreenChangeListener(MainMenuScreen.class));
        table.add(cancelButton).colspan(NB_TABS).growX();

        stack.swapActor(0,1);
        labels[0].setChecked(true);
        contents[1].setVisible(false);
        labelsButtonGroup.setMaxCheckCount(1);
        labelsButtonGroup.setMinCheckCount(1);
    }

    public void setIdReceived(String code) {
        Gdx.input.setInputProcessor(tmp);
        final InputButton button = inputsButtonGroup.getChecked();
        inputsButtonGroup.uncheckAll();
        String esc = InputsConfig.getIDForKeyCode(Keys.ESCAPE);
        InputsConfig inputsConfig = InputsConfig.get();
        //if(code.equals(esc)) code = inputsConfig.getReversedInputMap().get(button.action)[button.numProfile];
        if(code.equals(esc)){
            code = inputsConfig.getReversedInputMap().get(button.action)[button.numProfile];
            button.setText("");
            inputsConfig.delAction(code, InputProfile.values()[button.numProfile]);
        }else {
            button.setText(code);
            inputsConfig.addAction(code, button.action, InputProfile.values()[button.numProfile]);
        }
        inputsConfig.exportConfig();
    }

    public abstract class Parameter extends HorizontalGroup {
        protected Label label;
        public Parameter(String name) {
            super();
            left();

            label = new Label(name, Graphics.GUI.getSkin());
            addActor(label);
        }
    }

    private class ParameterScreenSize extends Parameter {
        AppConfig config = AppConfig.get();
        public ParameterScreenSize() {
            super("screen size");
            SelectBox<Resolutions> value = new SelectBox<Resolutions>(Graphics.GUI.getSkin());
            value.setItems(Resolutions.values());
            value.setSelected(config.resolution);
            value.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    Bomberball.resizeWindow(value.getSelected());
                    config.resolution = value.getSelected();
                    config.exportConfig();
                }
            });
            this.addActor(value);
        }
    }
    public class ParameterString extends Parameter {
        private SelectBox<String> value;
        public ParameterString(String name) {
            super(name);
            value = new SelectBox<>(Graphics.GUI.getSkin());
            value.setItems("test1","test2","test3","test4");
            addActor(value);
        }
    }
    public class ParameterInt extends Parameter {
        private HorizontalGroup value;
        public ParameterInt(String name, float min, float max, float step) {
            super(name);
            value = new HorizontalGroup();
            Slider slider = new Slider(min, max, step, false, GUI.getSkin());
            Label label = new Label("0", GUI.getSkin());
            slider.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    label.setText(""+(int)((Slider)actor).getValue());
                }
            });
            value.addActor(label);
            value.addActor(slider);
            addActor(value);
        }
    }

    private class InputButton extends TextButton {
        public Action action;
        public int numProfile;

        public InputButton(String id, Action action, int numProfile) {
            super(id, Graphics.GUI.getSkin(), "input_select");
            this.action = action;
            this.numProfile = numProfile;
        }
    }
}