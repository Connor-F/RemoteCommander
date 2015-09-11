package com.github.connorf.remotecommander;

import java.util.ArrayList;
import java.util.Random;

/**
 * holds valid key codes for the chaos command
 */
public class KeyCode
{
    private ArrayList<Integer> keys;

    public KeyCode()
    {
        keys = populateKeyCodes();
    }

    /**
     * uses valid keycodes to populate our keycode arraylist
     * @return an arraylist of integers that contain valid keycodes
     */
    private ArrayList<Integer> populateKeyCodes()
    {
        ArrayList<Integer> keycodes = new ArrayList<>();
        for(int i = 32; i <= 126; i++) // all type-able chars (ASCII codes)
            keycodes.add(i);
        return keycodes;
    }

    /**
     * gets a random valid keycode to be used with the chaos command
     * @return a random keycode
     */
    public int randomKeyCode()
    {
        return keys.get(new Random().nextInt(keys.size()));
    }
}
