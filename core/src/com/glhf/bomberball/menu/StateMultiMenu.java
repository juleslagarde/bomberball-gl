package com.glhf.bomberball.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.glhf.bomberball.Constants;
import com.glhf.bomberball.Game;
import com.glhf.bomberball.Graphics;
import com.glhf.bomberball.menu.StateMenu.SetStateListener;

public class StateMultiMenu extends StateMenu {

    //Constructor
    public StateMultiMenu()
    {
        super();
        initializeButtons();
    }

    public void initializeButtons(){
        TextButton textButton = new TextButton("Jouer", Graphics.GUI.getSkin());
        textButton.addListener(new SetStateListener(new StateGameMulti("maze_0.json")));
        centerButtons.addActor(textButton);
    }
}
